package core;

import matching.MatchResult;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * Container for all analysis results of a text comparison session.
 * Stores results for all text pairs and provides lookup functionality.
 *
 * @author [Dein u-Kürzel]
 */
public class AnalysisResult {
    private final List<MatchResult> results;
    private final Map<String, MatchResult> resultMap;
    private final String tokenizationStrategy;
    private final int minMatchLength;
    private final long analysisTimeMs;

    /**
     * Creates a new AnalysisResult.
     *
     * @param results list of all match results
     * @param tokenizationStrategy the tokenization strategy used
     * @param minMatchLength the minimum match length used
     * @param analysisTimeMs the time taken for analysis in milliseconds
     */
    public AnalysisResult(List<MatchResult> results, String tokenizationStrategy,
                          int minMatchLength, long analysisTimeMs) {
        this.results = new ArrayList<>(results);
        this.resultMap = new HashMap<>();
        this.tokenizationStrategy = tokenizationStrategy;
        this.minMatchLength = minMatchLength;
        this.analysisTimeMs = analysisTimeMs;

        // Build lookup map
        for (MatchResult result : results) {
            String key = createKey(result.getText1().getIdentifier(),
                    result.getText2().getIdentifier());
            resultMap.put(key, result);
        }
    }

    /**
     * Gets all match results.
     *
     * @return list of all match results
     */
    public List<MatchResult> getAllResults() {
        return new ArrayList<>(results);
    }

    /**
     * Gets the match result for two specific texts.
     *
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @return the match result, or null if not found
     */
    public MatchResult getResult(String id1, String id2) {
        String key = createKey(id1, id2);
        return resultMap.get(key);
    }

    /**
     * Gets the tokenization strategy used.
     *
     * @return the strategy name
     */
    public String getTokenizationStrategy() {
        return tokenizationStrategy;
    }

    /**
     * Gets the minimum match length used.
     *
     * @return the minimum match length
     */
    public int getMinMatchLength() {
        return minMatchLength;
    }

    /**
     * Gets the analysis time in milliseconds.
     *
     * @return the analysis time
     */
    public long getAnalysisTimeMs() {
        return analysisTimeMs;
    }

    /**
     * Creates a lookup key for two text identifiers.
     * Key is order-independent.
     *
     * @param id1 first identifier
     * @param id2 second identifier
     * @return the lookup key
     */
    private String createKey(String id1, String id2) {
        if (id1.compareTo(id2) <= 0) {
            return id1 + ":" + id2;
        } else {
            return id2 + ":" + id1;
        }
    }
}