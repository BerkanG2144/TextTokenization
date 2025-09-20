package core;

/**
 * Represents a match between two token sequences.
 * A match consists of a start position in each sequence and a length.
 *
 * @author [Dein u-Kürzel]
 */
public class Match {
    private final int startPosSequence1;
    private final int startPosSequence2;
    private final int length;

    /**
     * Creates a new Match.
     *
     * @param startPosSequence1 start position in the first sequence
     * @param startPosSequence2 start position in the second sequence
     * @param length the length of the match in tokens
     */
    public Match(int startPosSequence1, int startPosSequence2, int length) {
        if (startPosSequence1 < 0 || startPosSequence2 < 0 || length <= 0) {
            throw new IllegalArgumentException("Invalid match parameters");
        }

        this.startPosSequence1 = startPosSequence1;
        this.startPosSequence2 = startPosSequence2;
        this.length = length;
    }

    /**
     * Gets the start position in the first sequence.
     *
     * @return the start position in sequence 1
     */
    public int getStartPosSequence1() {
        return startPosSequence1;
    }

    /**
     * Gets the start position in the second sequence.
     *
     * @return the start position in sequence 2
     */
    public int getStartPosSequence2() {
        return startPosSequence2;
    }

    /**
     * Gets the length of this match in tokens.
     *
     * @return the match length
     */
    public int getLength() {
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
        return !(getEndPosSequence1() <= other.startPosSequence1 ||
                other.getEndPosSequence1() <= startPosSequence1);
    }

    /**
     * Checks if this match overlaps with another match in the second sequence.
     *
     * @param other the other match to check
     * @return true if there is an overlap in sequence 2
     */
    public boolean overlapsInSequence2(Match other) {
        return !(getEndPosSequence2() <= other.startPosSequence2 ||
                other.getEndPosSequence2() <= startPosSequence2);
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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Match match = (Match) obj;
        return startPosSequence1 == match.startPosSequence1 &&
                startPosSequence2 == match.startPosSequence2 &&
                length == match.length;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(startPosSequence1, startPosSequence2, length);
    }

    @Override
    public String toString() {
        return String.format("Match{length=%d, pos1=%d, pos2=%d}",
                length, startPosSequence1, startPosSequence2);
    }
}