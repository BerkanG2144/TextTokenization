package src.metrics;

import matching.MatchResult;
import metrics.SimilarityMetric;

/**
 * Longest match metric (LONG): length of longest matching subsequence in tokens.
 *
 * @author ujnaa
 */
public class LongestMatch implements SimilarityMetric {

    @Override
    public double calculate(MatchResult result) {
        return result.getLongestMatchLength();
    }

    @Override
    public String getName() {
        return "LONG";
    }

    @Override
    public boolean isPercentage() {
        return false;
    }

    @Override
    public String format(double value) {
        return String.valueOf((int) value);
    }
}