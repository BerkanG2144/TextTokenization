package metrics;

import matching.MatchResult;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/**
 * Symmetric similarity metric (AVG): 2m/(a+b)
 * Returns percentage with 2 decimal places.
 *
 * @author [Dein u-Kürzel]
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
        int m = result.getTotalMatchingTokens();

        if (a + b == 0) {
            return 0.0;
        }

        return (2.0 * m) / (a + b) * 100.0;
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
