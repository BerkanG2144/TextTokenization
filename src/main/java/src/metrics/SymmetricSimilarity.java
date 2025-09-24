package src.metrics;

import core.Match;
import matching.MatchResult;
import metrics.SimilarityMetric;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Symmetric similarity metric (AVG): 2m/(a+b)
 * Returns percentage with 2 decimal places.
 * m = number of unique matching tokens (not sum of match lengths)
 *
 * @author ujnaa
 */
public class SymmetricSimilarity implements SimilarityMetric {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        FORMATTER = new DecimalFormat("0.00", symbols);
    }

    @Override
    public double calculate(MatchResult result) {
        int a = result.getSequence1().size();
        int b = result.getSequence2().size();

        // Calculate unique matching tokens instead of sum of match lengths
        int m = calculateUniqueMatchingTokens(result);

        if (a + b == 0) {
            return 0.0;
        }

        return (2.0 * m) / (a + b) * 100.0;
    }

    /**
     * Calculates the number of unique matching tokens.
     * For AVG metric: m represents the total number of unique token matches
     * across both sequences.
     */
    private int calculateUniqueMatchingTokens(MatchResult result) {
        Set<Integer> matchedPositionsSeq1 = new HashSet<>();
        Set<Integer> matchedPositionsSeq2 = new HashSet<>();

        // Collect all positions covered by matches in both sequences
        for (Match match : result.getMatches()) {
            int start1 = match.startPosSequence1();
            int start2 = match.startPosSequence2();
            int length = match.length();

            for (int i = 0; i < length; i++) {
                matchedPositionsSeq1.add(start1 + i);
                matchedPositionsSeq2.add(start2 + i);
            }
        }

        // For symmetric similarity, m should be the number of tokens that match
        // Since each match represents tokens that are identical in both sequences,
        // we should count the actual matching tokens (which is the length of matches)
        // But avoid double counting due to overlapping matches

        // The number of unique matching tokens is the minimum because
        // each matched position represents one token match
        return Math.min(matchedPositionsSeq1.size(), matchedPositionsSeq2.size());
    }

    @Override
    public String getName() {
        return "AVG";
    }

    @Override
    public boolean isPercentage() {
        return true;
    }

    @Override
    public String format(double value) {
        return FORMATTER.format(value) + "%";
    }
}