package commands;

import core.*;
import matching.*;
import tokenization.*;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Command to analyze all loaded texts for similarities.
 * Usage: analyze <strategy> <minMatchLength>
 *
 * @author [Dein u-Kürzel]
 */
public class AnalyzeCommand implements Command {
    private final TextManager textManager;
    private AnalysisResult lastAnalysisResult;

    /**
     * Creates a new AnalyzeCommand.
     *
     * @param textManager the text manager to use
     */
    public AnalyzeCommand(TextManager textManager) {
        this.textManager = textManager;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("analyze command requires exactly two arguments: analyze <strategy> <minMatchLength>");
        }

        String strategyName = args[0].toUpperCase();
        int minMatchLength;

        try {
            minMatchLength = Integer.parseInt(args[1]);
            if (minMatchLength <= 0 || minMatchLength >= 100) {
                throw new CommandException("Minimum match length must be between 1 and 99");
            }
        } catch (NumberFormatException e) {
            throw new CommandException("Minimum match length must be a valid integer");
        }

        // Get tokenization strategy
        TokenizationStrategy strategy = getStrategy(strategyName);
        if (strategy == null) {
            throw new CommandException("Unknown tokenization strategy: " + strategyName +
                    ". Available strategies: CHAR, WORD, SMART");
        }

        // Check if we have texts to analyze
        if (textManager.getTextCount() < 2) {
            throw new CommandException("Need at least 2 texts to perform analysis");
        }

        long startTime = System.currentTimeMillis();

        // Perform analysis on all text pairs
        List<MatchResult> results = analyzeAllPairs(strategy, minMatchLength);

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Store results
        lastAnalysisResult = new AnalysisResult(results, strategyName, minMatchLength, duration);

        return "Analysis took " + duration + "ms";
    }

    /**
     * Analyzes all pairs of texts.
     *
     * @param strategy the tokenization strategy
     * @param minMatchLength the minimum match length
     * @return list of match results for all pairs
     */
    private List<MatchResult> analyzeAllPairs(TokenizationStrategy strategy, int minMatchLength) {
        List<MatchResult> results = new ArrayList<>();
        List<Text> texts = new ArrayList<>(textManager.getAllTexts());

        // Sort texts alphabetically by identifier to ensure consistent ordering
        texts.sort(Comparator.comparing(Text::getIdentifier));

        SequenceMatcher matcher = new SequenceMatcher();

        // Compare all pairs
        for (int i = 0; i < texts.size(); i++) {
            for (int j = i + 1; j < texts.size(); j++) {
                Text text1 = texts.get(i);
                Text text2 = texts.get(j);

                // Tokenize both texts
                List<Token> sequence1 = strategy.tokenize(text1.getContent());
                List<Token> sequence2 = strategy.tokenize(text2.getContent());

                // Find matches - SequenceMatcher handles the search/pattern logic internally
                List<Match> matches = matcher.findMatches(sequence1, sequence2, minMatchLength);

                // Create result - matches are already in correct positions relative to text1, text2
                MatchResult result = new MatchResult(
                        text1, text2,  // Always in original order
                        sequence1, sequence2,  // Always in original order
                        matches, strategy.getName(), minMatchLength
                );

                results.add(result);
            }
        }

        return results;
    }

    /**
     * Gets the tokenization strategy by name.
     *
     * @param strategyName the strategy name
     * @return the strategy instance, or null if not found
     */
    private TokenizationStrategy getStrategy(String strategyName) {
        switch (strategyName) {
            case "CHAR":
                return new CharTokenizer();
            case "WORD":
                return new WordTokenizer();
            case "SMART":
                return new SmartTokenizer();
            default:
                return null;
        }
    }

    /**
     * Gets the last analysis result.
     *
     * @return the last analysis result, or null if no analysis performed
     */
    public AnalysisResult getLastAnalysisResult() {
        return lastAnalysisResult;
    }

    /**
     * Updates a specific match result in the analysis.
     *
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @param newResult the new match result
     */
    public void updateMatchResult(String id1, String id2, MatchResult newResult) {
        if (lastAnalysisResult == null) {
            return;
        }

        // Find and replace the match result in the analysis
        List<MatchResult> allResults = new ArrayList<>(lastAnalysisResult.getAllResults());
        for (int i = 0; i < allResults.size(); i++) {
            MatchResult result = allResults.get(i);
            if (result.involves(id1, id2)) {
                allResults.set(i, newResult);
                break;
            }
        }

        // Create a new AnalysisResult with updated results
        lastAnalysisResult = new AnalysisResult(
                allResults,
                lastAnalysisResult.getTokenizationStrategy(),
                lastAnalysisResult.getMinMatchLength(),
                lastAnalysisResult.getAnalysisTimeMs()
        );
    }

    @Override
    public String getName() {
        return "analyze";
    }

    @Override
    public String getUsage() {
        return "analyze <strategy> <minMatchLength> - Analyze all loaded texts for similarities";
    }
}