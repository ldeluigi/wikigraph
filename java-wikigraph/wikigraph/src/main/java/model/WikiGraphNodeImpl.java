package model;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class WikiGraphNodeImpl implements WikiGraphNode {
    private final String name;
    private final Set<String> synonyms;
    private final Set<String> children;
    private final int depth;

    public WikiGraphNodeImpl(final String term, final int depth, final Set<String> children) {
        this(term, depth, Collections.emptySet(), children);
    }

    public WikiGraphNodeImpl(final String term, final int depth, final Set<String> synonyms, final Set<String> children) {
        this.name = term;
        this.synonyms = synonyms;
        this.children = children;
        this.depth = depth;
    }

    @Override
    public Set<String> childrenTerms() {
        return this.children;
    }

    @Override
    public String term() {
        return this.name;
    }

    @Override
    public int getDepth() {
        return this.depth;
    }

    @Override
    public String toString() {
        return "WikiGraphNode{" +
                "name='" + name + '\'' +
                ", synonyms=" + synonyms +
                ", children=" + children +
                '}';
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (!(o instanceof WikiGraphNode)) return false;
        final WikiGraphNode that = (WikiGraphNode) o;
        return Objects.equals(name, that.term()) &&
                Objects.equals(children, that.childrenTerms());
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, children);
    }
}
