package model;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WikiGraphs {

    /**
     * Creates a view of a {@link Map}<{@link String}, {@link WikiGraphNode}> that implements
     * the {@link WikiGraph} interface.
     * @param nodes the map of term -> node of that term
     * @return a view of the map as a {@link WikiGraph}
     */
    public static WikiGraph from(final Map<String, WikiGraphNode> nodes, final String root) {
        return new WikiGraph() {
            @Override
            public Set<String> terms() {
                final HashSet<String> all = new HashSet<>(nodes.keySet());
                nodes.values().stream().flatMap(v -> v.childrenTerms().stream()).collect(Collectors.toCollection(() -> all));
                return Collections.unmodifiableSet(all);
            }

            @Override
            public Collection<WikiGraphNode> nodes() {
                return Collections.unmodifiableCollection(nodes.values());
            }

            @Override
            public Set<Pair<String, String>> termEdges() {
                return nodes.entrySet().stream()
                        .flatMap(e -> e.getValue()
                                .childrenTerms().stream()
                                .map(c -> new PairImpl<>(e.getKey(), c)))
                        .collect(Collectors.toUnmodifiableSet());
            }

            @Override
            public boolean contains(final String term) {
                return nodes.containsKey(term);
            }

            @Override
            public String getRoot() {
                return root;
            }
        };
    }

    /**
     * Returns a list of operation that would make the older graph equal the newer.
     * @param older the older graph
     * @param newer the newer graph
     * @return a list of {@link NodeDifference}
     */
    public static List<NodeDifference> difference(final WikiGraph older, final WikiGraph newer) {
        final Set<WikiGraphNode> olderNodes = new HashSet<>(older.nodes());
        final Set<WikiGraphNode> newerNodes = new HashSet<>(newer.nodes());
        final Map<String, WikiGraphNode> olderMap = older.nodes().stream().collect(Collectors.toUnmodifiableMap(WikiGraphNode::term, n -> n));
        final Map<String, WikiGraphNode> newerMap = newer.nodes().stream().collect(Collectors.toUnmodifiableMap(WikiGraphNode::term, n -> n));
        final Set<WikiGraphNode> diff = new HashSet<>(olderNodes);
        diff.removeAll(newerNodes);
        Stream<NodeDifference> d1 = diff.stream()
                .map(t -> {
                    final boolean isReplace = newerMap.containsKey(t.term());
                    final WikiGraphNode n = newerMap.getOrDefault(t.term(), null);
                    return new NodeDifference() {
                        @Override
                        public boolean isAdd() {
                            return false;
                        }

                        @Override
                        public boolean isRemove() {
                            return !isReplace;
                        }

                        @Override
                        public boolean isReplace() {
                            return isReplace;
                        }

                        @Override
                        public WikiGraphNode oldNode() {
                            return t;
                        }

                        @Override
                        public WikiGraphNode newNode() {
                            return n;
                        }
                    };
                });
        final Set<WikiGraphNode> revDiff = new HashSet<>(newerNodes);
        revDiff.removeAll(olderNodes);
        Stream<NodeDifference> d2 = revDiff.stream()
                .filter(t -> !olderMap.containsKey(t.term()))
                .map(t ->
                        new NodeDifference() {
                        @Override
                        public boolean isAdd() {
                            return true;
                        }

                        @Override
                        public boolean isRemove() {
                            return false;
                        }

                        @Override
                        public boolean isReplace() {
                            return false;
                        }

                        @Override
                        public WikiGraphNode oldNode() {
                            return null;
                        }

                        @Override
                        public WikiGraphNode newNode() {
                            return t;
                        }
                    });
        return Stream.concat(d1, d2).collect(Collectors.toUnmodifiableList());
    }
}
