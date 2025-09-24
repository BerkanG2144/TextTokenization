package core;

/**
 * Represents a match between two token sequences.
 * A match consists of a start position in each sequence and a length.
 *
 * @param startPosSequence1 start position in the first sequence
 * @param startPosSequence2 start position in the second sequence
 * @param length            the length of the match in tokens
 *
 * @author ujnaa
 */
public record Match(int startPosSequence1, int startPosSequence2, int length) {
    /**
     * Creates a new Match.
     *
     * @param startPosSequence1 start position in the first sequence
     * @param startPosSequence2 start position in the second sequence
     * @param length            the length of the match in tokens
     */
    public Match {
        if (startPosSequence1 < 0 || startPosSequence2 < 0 || length <= 0) {
            throw new IllegalArgumentException("Invalid match parameters");
        }

    }

    /**
     * Gets the start position in the first sequence.
     *
     * @return the start position in sequence 1
     */
    @Override
    public int startPosSequence1() {
        return startPosSequence1;
    }

    /**
     * Gets the start position in the second sequence.
     *
     * @return the start position in sequence 2
     */
    @Override
    public int startPosSequence2() {
        return startPosSequence2;
    }

    /**
     * Gets the length of this match in tokens.
     *
     * @return the match length
     */
    @Override
    public int length() {
        return length;
    }

    /**
     * Gets the end position in the first sequence (exclusive).
     *
     * @return the end position in sequence 1
     */
    public int getEndPosSequence1() {
        return startPosSequence1 + length;
    }

    /**
     * Gets the end position in the second sequence (exclusive).
     *
     * @return the end position in sequence 2
     */
    public int getEndPosSequence2() {
        return startPosSequence2 + length;
    }

    /**
     * Checks if this match overlaps with another match in the first sequence.
     *
     * @param other the other match to check
     * @return true if there is an overlap in sequence 1
     */
    public boolean overlapsInSequence1(Match other) {
        return !(getEndPosSequence1() <= other.startPosSequence1
                || other.getEndPosSequence1() <= startPosSequence1);
    }

    /**
     * Checks if this match overlaps with another match in the second sequence.
     *
     * @param other the other match to check
     * @return true if there is an overlap in sequence 2
     */
    public boolean overlapsInSequence2(Match other) {
        return !(getEndPosSequence2() <= other.startPosSequence2 || other.getEndPosSequence2() <= startPosSequence2);
    }

    /**
     * Checks if this match overlaps with another match in either sequence.
     *
     * @param other the other match to check
     * @return true if there is any overlap
     */
    public boolean overlapsWith(Match other) {
        return overlapsInSequence1(other) || overlapsInSequence2(other);
    }

    @Override
    public String toString() {
        return String.format("Match{length=%d, pos1=%d, pos2=%d}",
                length, startPosSequence1, startPosSequence2);
    }
}