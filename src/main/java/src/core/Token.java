package src.core;

import exceptions.TokenizationException;

/**
 * Represents a single token in a text sequence.
 * Tokens are the smallest unit for text comparison.
 *
 * @author ujnaa
 */
public class Token {
    private final String value;
    private final int startPosition;
    private final int endPosition;

    /**
     * Creates a new Token.
     *
     * @param value the string value of this token
     * @param startPosition the start position in the original text
     * @param endPosition the end position in the original text
     * @throws TokenizationException if the value is null or the positions are invalid
     */
    public Token(String value, int startPosition, int endPosition) throws TokenizationException {
        if (value == null) {
            throw new TokenizationException("ERROR: Token value cannot be null");
        }
        if (startPosition < 0 || endPosition < startPosition) {
            throw new TokenizationException("ERROR: Invalid token position values (start="
                    + startPosition + ", end=" + endPosition + ")");
        }

        this.value = value;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
    }

    /**
     * Creates a new Token with only the value (for testing purposes).
     *
     * @param value the string value of this token
     * @throws TokenizationException if the value is null
     */
    public Token(String value) throws TokenizationException {
        this(value, 0, value != null ? value.length() : 0);
    }

    /**
     * Gets the string value of this token.
     *
     * @return the token value
     */
    public String getValue() {
        return value;
    }

    /**
     * Gets the start position of this token in the original text.
     *
     * @return the start position
     */
    public int getStartPosition() {
        return startPosition;
    }

    /**
     * Gets the end position of this token in the original text.
     *
     * @return the end position
     */
    public int getEndPosition() {
        return endPosition;
    }

    /**
     * Gets the length of this token.
     *
     * @return the token length
     */
    public int length() {
        return value.length();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Token token = (Token) obj;
        return value.equals(token.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value;
    }
}
