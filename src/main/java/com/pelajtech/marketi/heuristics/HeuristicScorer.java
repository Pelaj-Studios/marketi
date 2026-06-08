package com.pelajtech.marketi.heuristics;

import java.util.*;
import java.util.function.Predicate;

public class HeuristicScorer<T> {

    private final NavigableMap<Integer, List<Predicate<T>>> rules;

    private HeuristicScorer(NavigableMap<Integer, List<Predicate<T>>> rules) {
        this.rules = rules;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public int score(T value) {
        return rules.entrySet()
                .stream()
                .filter(entry -> entry.getValue()
                        .stream()
                        .anyMatch(predicate -> predicate.test(value)))
                .mapToInt(Map.Entry::getKey)
                .sum();
    }

    public Optional<T> highest(Collection<T> values) {
        return values.stream()
                .max(Comparator.comparingInt(this::score));
    }

    public static final class Builder<T> {

        private final NavigableMap<Integer, List<Predicate<T>>> rules = new TreeMap<>();

        public Builder<T> add(Predicate<T> predicate, int score) {
            rules.computeIfAbsent(score, _ -> new ArrayList<>())
                    .add(predicate);

            return this;
        }

        public HeuristicScorer<T> build() {
            var copy = new TreeMap<Integer, List<Predicate<T>>>();
            rules.forEach((score, predicates) -> copy.put(score, List.copyOf(predicates)));
            return new HeuristicScorer<>(Collections.unmodifiableNavigableMap(copy));
        }

    }

}
