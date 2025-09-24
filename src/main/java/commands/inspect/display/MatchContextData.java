package commands.inspect.display;

import core.Match;
import core.Token;
import java.util.List;

/**
 * Data container for match context display parameters.
 * Reduces parameter count in display methods by grouping related data.
 *
 * @author ujnaa
 */
public final class MatchContextData {
    private final Match match;
    private final String firstText;
    private final String secondText;
    private final List<Token> firstSequence;
    private final List<Token> secondSequence;
    private final int firstPosition;
    private final int secondPosition;
    private final int contextSize;

    /**
     * Creates a new MatchContextData.
     *
     * @param match the match to display
     * @param firstText first text content
     * @param secondText second text content
     * @param firstSequence first token sequence
     * @param secondSequence second token sequence
     * @param positions container with both positions
     * @param contextSize context size around match
     */
    public MatchContextData(Match match, String firstText, String secondText,
                            List<Token> firstSequence, List<Token> secondSequence,
                            PositionPair positions, int contextSize) {
        this.match = match;
        this.firstText = firstText;
        this.secondText = secondText;
        this.firstSequence = firstSequence;
        this.secondSequence = secondSequence;
        this.firstPosition = positions.firstPosition();
        this.secondPosition = positions.secondPosition();
        this.contextSize = contextSize;
    }

    /**
     * Gets the match being displayed.
     *
     * @return the match
     */
    public Match getMatch() {
        return match;
    }

    /**
     * Gets the first text content.
     *
     * @return first text content
     */
    public String getFirstText() {
        return firstText;
    }

    /**
     * Gets the second text content.
     *
     * @return second text content
     */
    public String getSecondText() {
        return secondText;
    }

    /**
     * Gets the first token sequence.
     *
     * @return first token sequence
     */
    public List<Token> getFirstSequence() {
        return firstSequence;
    }

    /**
     * Gets the second token sequence.
     *
     * @return second token sequence
     */
    public List<Token> getSecondSequence() {
        return secondSequence;
    }

    /**
     * Gets the position in first sequence.
     *
     * @return first position
     */
    public int getFirstPosition() {
        return firstPosition;
    }

    /**
     * Gets the position in second sequence.
     *
     * @return second position
     */
    public int getSecondPosition() {
        return secondPosition;
    }

    /**
     * Gets the context size around match.
     *
     * @return context size
     */
    public int getContextSize() {
        return contextSize;
    }

    /**
     * Simple record to hold position pair and reduce constructor parameters.
     *
     * @param firstPosition position in first sequence
     * @param secondPosition position in second sequence
     */
    public record PositionPair(int firstPosition, int secondPosition) {
    }
}