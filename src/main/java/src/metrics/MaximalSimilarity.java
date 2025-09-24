package src.metrics;

import matching.MatchResult;
import metrics.SimilarityMetric;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Maximal similarity metric (MAX): max(m/a, m/b)
 * Returns percentage with 2 decimal places.
 *
 * @author ujnaa
 */
public class MaximalSimilarity implements SimilarityMetric {
    private static final DecimalFormat FORMATTER;

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        FORMATTER = new DecimalFormat("0.00", symbols);
    }

    @Override
    public double calculate(MatchResult result) {
        int a = result.getSequence1().size();
        int b = result.getSequence2().size();
        int m = result.getTotalMatchingTokens();

        if (a == 0 && b == 0) {
            return 0.0;
        }

        double ratioA = (a == 0) ? 0.0 : (double) m / a;
        double ratioB = (b == 0) ? 0.0 : (double) m / b;

        return Math.max(ratioA, ratioB) * 100.0;
    }

    @Override
    public String getName() {
        return "MAX";
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