package controller.api;

import model.Pair;
import model.WikiGraphNode;
import model.WikiGraphNodeFactory;
import model.WikiGraphNodeImpl;

import java.net.URL;
import java.util.*;
import java.util.stream.IntStream;

public class MockWikiGraph implements WikiGraphNodeFactory {
    private List<String> randomName = Arrays.asList(
            "pippo","pluto","paperino","Cerroni","Deluigi","Magnani","uomo", "poche", "parole", "qualcuno", "che", "si",
            "vende", "per","poco", "letteralmente", "proprio", "perche", "dire", "cerca", "venirne", "capo",
            "altrimenti", "indica", "qualcuno", "segreti", "possono", "cruciverba");
    private Random rand = new Random();

    @Override
    public List<Pair<String, String>> search(String term) {
        return null;
    }

    @Override
    public WikiGraphNode from(URL url, final int depth) {
        return this.random(depth);
    }

    @Override
    public WikiGraphNode from(String term, int depth) {
        Set<String> sameTerm = new HashSet<>();
        sameTerm.add(term);
        final Set<String> terms = new HashSet<>();
        IntStream.rangeClosed(0,this.rand.nextInt(2)).forEach(i -> terms.add(this.getRandomName()));
        return new WikiGraphNodeImpl(term, depth, sameTerm, terms);
    }

    private String getRandomName() {
        return this.randomName.get(this.rand.nextInt(this.randomName.size()));
    }

    @Override
    public WikiGraphNode random(final int depth) {
        return from(this.getRandomName(), depth);
    }

    @Override
    public String getLanguage() {
        return "en";
    }

    @Override
    public String getLanguage(URL url) {
        return "en";
    }

    @Override
    public boolean setLanguage(String langCode) {
        return false;
    }
}
