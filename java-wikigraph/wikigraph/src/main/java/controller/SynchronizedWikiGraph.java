package controller;

import model.MutableGraphImpl;
import model.MutableWikiGraph;
import model.Pair;
import model.WikiGraphNode;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SynchronizedWikiGraph implements ConcurrentWikiGraph {
    private volatile boolean aborted = false;
    private final Map<String, Lock> locks = new HashMap<>();
    private final MutableWikiGraph graph;

    public static SynchronizedWikiGraph empty() {
        return new SynchronizedWikiGraph();
    }

    private SynchronizedWikiGraph(final MutableWikiGraph graph) {
        this.graph = graph;
    }

    private SynchronizedWikiGraph() {
        this(new MutableGraphImpl());
    }

    @Override
    public void setAborted() {
        this.aborted = true;
    }

    @Override
    public boolean isAborted() {
        return this.aborted;
    }

    @Override
    public Lock getLockOn(final String nodeTerm) {
        synchronized (this.locks) {
            if (this.locks.containsKey(nodeTerm)) {
                return this.locks.get(nodeTerm);
            }
            final Lock newLock = new ReentrantLock();
            this.locks.put(nodeTerm, newLock);
            return newLock;
        }
    }

    @Override
    public synchronized boolean add(final WikiGraphNode node) {
        return this.graph.add(node);
    }

    @Override
    public synchronized boolean remove(final String nodeTerm) {
        return this.graph.remove(nodeTerm);
    }

    @Override
    public synchronized boolean set(final WikiGraphNode node) {
        return this.graph.set(node);
    }

    @Override
    public synchronized Set<String> terms() {
        return this.graph.terms();
    }

    @Override
    public synchronized Collection<WikiGraphNode> nodes() {
        return this.graph.nodes();
    }

    @Override
    public synchronized Set<Pair<String, String>> termEdges() {
        return this.graph.termEdges();
    }

    @Override
    public synchronized boolean contains(String term) {
        return this.graph.contains(term);
    }
}
