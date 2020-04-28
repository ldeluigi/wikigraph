package model;

import java.util.Set;

/**
 * A structural (non-leaf) node of a {@link WikiGraph}.
 */
public interface WikiGraphNode {
    /**
     * Returns the list of terms that are linked from this node Wiki page.
     *
     * @return every term as Set of String
     */
    Set<String> childrenTerms();

    /**
     * Returns the term to which this node refers to.
     *
     * @return the term as a string
     */
    String term();
}
