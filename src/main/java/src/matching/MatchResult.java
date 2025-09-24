package src.matching;

import core.Match;
import core.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Container for the results of matching two texts.
 * Stores the matched texts, their token sequences, and found matches.
 *
 * @author ujnaa
 */
public class MatchResult {
    private final Text text1;
    private final Text text2;
    private final List<core.Token> sequence1;
    private final List<core.Token> sequence2;
    private final List<Match> matches;
    private final String tokenizationStrategy;
    private final int minMatchLength;
    private final boolean text1IsSearch; // true if text1 was search sequence, false if text2 was search sequence

    /**
     * Creates a new MatchResult.
     *
     * @param text1 the first text
     * @param text2 the second text
     * @param sequence1 tokenized sequence of first text
     * @param sequence2 tokenized sequence of second text
     * @param matches list of found matches
     * @param tokenizationStrategy the tokenization strategy used
     * @param minMatchLength the minimum match length used
     */
    public MatchResult(Text text1, Text text2,
                       List<core.Token> sequence1, List<core.Token> sequence2,
                       List<Match> matches, String tokenizationStrategy, int minMatchLength) {
        this.text1 = text1;
        this.text2 = text2;
        this.sequence1 = new ArrayList<>(sequence1);
        this.sequence2 = new ArrayList<>(sequence2);
        this.matches = new ArrayList<>(matches);
        this.tokenizationStrategy = tokenizationStrategy;
        this.minMatchLength = minMatchLength;

        // Determine which text was the search sequence
        // Search sequence is the longer one, or text1 if same length
        if (sequence1.size() > sequence2.size()) {
            this.text1IsSearch = true;
        } else if (sequence1.size() < sequence2.size()) {
            this.text1IsSearch = false;
        } else {
            this.text1IsSearch = true; // Default to text1 for same length
        }
    }

    /**
     * Gets the first text.
     *
     * @return the first text
     */
    public Text getText1() {
        return text1;
    }

    /**
     * Gets the second text.
     *
     * @return the second text
     */
    public Text getText2() {
        return text2;
    }

    /**
     * Gets the search text (longer sequence or text1 if same length).
     *
     * @return the search text
     */
    public Text getSearchText() {
        return text1IsSearch ? text1 : text2;
    }

    /**
     * Gets the pattern text (shorter sequence or text2 if same length).
     *
     * @return the pattern text
     */
    public Text getPatternText() {
        return text1IsSearch ? text2 : text1;
    }

    /**
     * Gets the token sequence of the first text.
     *
     * @return the first token sequence
     */
    public List<core.Token> getSequence1() {
        return new ArrayList<>(sequence1);
    }

    /**
     * Gets the token sequence of the second text.
     *
     * @return the second token sequence
     */
    public List<core.Token> getSequence2() {
        return new ArrayList<>(sequence2);
    }

    /**
     * Gets the search sequence (longer one or sequence1 if same length).
     *
     * @return the search sequence
     */
    public List<core.Token> getSearchSequence() {
        return text1IsSearch ? sequence1 : sequence2;
    }

    /**
     * Gets the pattern sequence (shorter one or sequence2 if same length).
     *
     * @return the pattern sequence
     */
    public List<core.Token> getPatternSequence() {
        return text1IsSearch ? sequence2 : sequence1;
    }

    /**
     * Gets all found matches.
     *
     * @return list of matches
     */
    public List<Match> getMatches() {
        return new ArrayList<>(matches);
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
     * Gets the total number of matching tokens.
     *
     * @return sum of all match lengths
     */
    public int getTotalMatchingTokens() {
        return matches.stream().mapToInt(Match::length).sum();
    }

    /**
     * Gets the length of the longest match.
     *
     * @return the longest match length, or 0 if no matches
     */
    public int getLongestMatchLength() {
        return matches.stream().mapToInt(Match::length).max().orElse(0);
    }

    /**
     * Checks if this result involves the given text identifiers.
     *
     * @param id1 first identifier
     * @param id2 second identifier
     * @return true if this result matches the given identifiers (order independent)
     */
    public boolean involves(String id1, String id2) {
        return (text1.identifier().equals(id1) && text2.identifier().equals(id2))
                || (text1.identifier().equals(id2) && text2.identifier().equals(id1));
    }

    /**
     * Checks if text1 was the search sequence.
     *
     * @return true if text1 was search sequence, false if text2 was search sequence
     */
    public boolean isText1Search() {
        return text1IsSearch;
    }
}