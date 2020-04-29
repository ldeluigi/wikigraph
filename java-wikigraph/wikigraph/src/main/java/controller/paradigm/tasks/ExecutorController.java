package controller.paradigm.tasks;


import controller.ConcurrentWikiGraph;
import controller.Controller;
import controller.SynchronizedWikiGraph;
import controller.api.RESTWikiGraph;
import model.WikiGraphNodeFactory;
import view.View;
import view.ViewEvent;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ExecutorController implements Controller {

    private final View view;
    private final Lock scheduleLock = new ReentrantLock();
    private ForkJoinPool pool;
    private Optional<ViewEvent> event = Optional.empty();
    private ConcurrentWikiGraph last = null;

    public ExecutorController(View view) {
        this.view = view;
        this.view.addEventListener(this);
    }

    @Override
    public void start() {
        pool = ForkJoinPool.commonPool();
        view.start();
    }

    private void startComputing(String root, int depth) {
        final WikiGraphNodeFactory nodeFactory = new RESTWikiGraph();
        nodeFactory.setLanguage(Locale.ENGLISH.getLanguage());
        this.last = SynchronizedWikiGraph.empty();
        this.pool.execute(new ComputeChildrenTask(nodeFactory, this.last, this.view, depth, root) {
            @Override
            public void onCompletion(CountedCompleter<?> caller) {
                super.onCompletion(caller);
                endComputing();
            }
        });
    }

    private void endComputing() {
        this.scheduleLock.lock();
        try {
            if (this.event.isPresent()) {
                final ViewEvent e = this.event.get();
                if (e.getType().equals(ViewEvent.EventType.CLEAR)) {
                    this.view.clearGraph();
                } else {
                    startComputing(e.getType().equals(ViewEvent.EventType.RANDOM_SEARCH) ? null : e.getText(),
                            e.getDepth());
                }
            }
        } finally {
            scheduleLock.unlock();
        }
    }

    private void exit() {
        if (this.pool != null) {
            this.pool.shutdown();
        }
    }

    @Override
    public void notifyEvent(ViewEvent event) {
        if (event.getType().equals(ViewEvent.EventType.EXIT)) {
            this.exit();
        } else if (event.getType().equals(ViewEvent.EventType.SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
            }
            this.scheduleLock.lock();
            try {
                if (this.pool.isQuiescent()) {
                    startComputing(event.getText(), event.getDepth());
                } else {
                    this.event = Optional.of(event);
                }
            } finally {
                this.scheduleLock.unlock();
            }
        } else if (event.getType().equals(ViewEvent.EventType.RANDOM_SEARCH)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
            }
            this.scheduleLock.lock();
            try {
                if (this.pool.isQuiescent()) {
                    startComputing(null, event.getDepth());
                } else {
                    this.event = Optional.of(event);
                }
            } finally {
                this.scheduleLock.unlock();
            }
        } else if (event.getType().equals(ViewEvent.EventType.CLEAR)) {
            if (this.last != null) {
                this.last.setAborted();
                this.last = null;
            }
            this.scheduleLock.lock();
            try {
                if (this.pool.isQuiescent()) {
                    this.view.clearGraph();
                } else {
                    this.event = Optional.of(event);
                }
            } finally {
                this.scheduleLock.unlock();
            }
        }
    }

}
