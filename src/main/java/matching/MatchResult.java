package matching;

import core.Match;
import core.Text;
import java.util.List;
import java.util.ArrayList;

/**
 * Container for the results of matching two texts.
 * Stores the matched texts, their token sequences, and found matches.
 *
 * @author [Dein u-Kürzel]
 */
public class MatchResult {
    private final Text text1;
    private final Text text2;
    private final List<core.Token> sequence1;
    private final List<core.Token> sequence2;
    private final List<Match> matches;
    private final String tokenizationStrategy;
    private final int minMatchLength;

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
        return matches.stream().mapToInt(Match::getLength).sum();
    }

    /**
     * Gets the length of the longest match.
     *
     * @return the longest match length, or 0 if no matches
     */
    public int getLongestMatchLength() {
        return matches.stream().mapToInt(Match::getLength).max().orElse(0);
    }

    /**
     * Checks if this result involves the given text identifiers.
     *
     * @param id1 first identifier
     * @param id2 second identifier
     * @return true if this result matches the given identifiers (order independent)
     */
    public boolean involves(String id1, String id2) {
        return (text1.getIdentifier().equals(id1) && text2.getIdentifier().equals(id2)) ||
                (text1.getIdentifier().equals(id2) && text2.getIdentifier().equals(id1));
    }
}