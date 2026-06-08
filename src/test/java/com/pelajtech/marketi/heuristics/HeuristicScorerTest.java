package com.pelajtech.marketi.heuristics;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HeuristicScorerTest {

    @Test
    void emptyScorerReturnsZero() {
        var scorer = HeuristicScorer.<String>builder().build();

        assertEquals(0, scorer.score("milk"));
    }

    @Test
    void sumsScoresForEachMatchingScoreGroup() {
        var scorer = HeuristicScorer.<String>builder()
                .add(value -> value.startsWith("organic"), 10)
                .add(value -> value.contains("milk"), 5)
                .add(value -> value.endsWith("bread"), 2)
                .build();

        assertEquals(15, scorer.score("organic milk"));
    }

    @Test
    void countsScoreGroupOnlyOnceWhenMultiplePredicatesMatch() {
        var scorer = HeuristicScorer.<String>builder()
                .add(value -> value.contains("milk"), 7)
                .add(value -> value.length() > 3, 7)
                .build();

        assertEquals(7, scorer.score("milk"));
    }

    @Test
    void supportsNegativeScores() {
        var scorer = HeuristicScorer.<Integer>builder()
                .add(value -> value > 10, 5)
                .add(value -> value % 2 == 0, -2)
                .build();

        assertEquals(3, scorer.score(12));
    }

    @Test
    void buildSnapshotsRulesAlreadyAddedToBuilder() {
        var builder = HeuristicScorer.<String>builder()
                .add(value -> value.contains("milk"), 4);

        var scorer = builder.build();
        builder.add(value -> value.contains("milk"), 100);

        assertEquals(4, scorer.score("milk"));
    }

    @Test
    void highestReturnsValueWithHighestScore() {
        var scorer = HeuristicScorer.<String>builder()
                .add(value -> value.contains("organic"), 8)
                .add(value -> value.contains("milk"), 4)
                .add(value -> value.contains("bread"), 2)
                .build();

        assertEquals(
                Optional.of("organic milk"),
                scorer.highest(List.of("bread", "milk", "organic milk"))
        );
    }

    @Test
    void highestReturnsFirstValueWhenScoresAreTied() {
        var scorer = HeuristicScorer.<String>builder()
                .add(value -> value.contains("milk"), 4)
                .add(value -> value.contains("bread"), 4)
                .build();

        assertEquals(
                Optional.of("milk"),
                scorer.highest(List.of("milk", "bread"))
        );
    }

    @Test
    void highestReturnsEmptyForEmptyValues() {
        var scorer = HeuristicScorer.<String>builder()
                .add(value -> value.contains("milk"), 4)
                .build();

        assertEquals(Optional.empty(), scorer.highest(List.of()));
    }

}
