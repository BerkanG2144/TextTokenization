package core;

/**
 * Represents a text with an identifier and content.
 * Texts can be loaded from files or input via console.
 *
 * @param identifier the unique identifier for this text
 * @param content    the actual text content
 * @author ujnaa
 */
public record Text(String identifier, String content) {
    /**
     * Creates a new Text instance.
     *
     * @param identifier the unique identifier for this text
     * @param content    the actual text content
     */
    public Text {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }
    }

    /**
     * Gets the identifier of this text.
     *
     * @return the identifier
     */
    @Override
    public String identifier() {
        return identifier;
    }

    /**
     * Gets the content of this text.
     *
     * @return the text content
     */
    @Override
    public String content() {
        return content;
    }

    /**
     * Gets the length of the text in characters.
     *
     * @return the character count
     */
    public int length() {
        return content.length();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        Text text = (Text) obj;
        return identifier.equals(text.identifier) && content.equals(text.content);
    }

    @Override
    public int hashCode() {
        return identifier.hashCode();
    }

    @Override
    public String toString() {
        return "Text{id='" + identifier + "', length=" + content.length() + "}";
    }
}