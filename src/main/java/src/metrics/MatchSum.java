package src.metrics;

import matching.MatchResult;
import metrics.SimilarityMetric;

/**
 * Match sum metric (LEN): total number of matching tokens.
 *
 * @author ujnaa
 */
public class MatchSum implements SimilarityMetric {

    @Override
    public double calculate(MatchResult result) {
        return result.getTotalMatchingTokens();
    }

    @Override
    public String getName() {
        return "LEN";
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