package core;

/**
 * Represents a text with an identifier and content.
 * Texts can be loaded from files or input via console.
 *
 * @author [Dein u-Kürzel]
 */
public class Text {
    private final String identifier;
    private final String content;

    /**
     * Creates a new Text instance.
     *
     * @param identifier the unique identifier for this text
     * @param content the actual text content
     */
    public Text(String identifier, String content) {
        if (identifier == null || identifier.trim().isEmpty()) {
            throw new IllegalArgumentException("Identifier cannot be null or empty");
        }
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        this.identifier = identifier;
        this.content = content;
    }

    /**
     * Gets the identifier of this text.
     *
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Gets the content of this text.
     *
     * @return the text content
     */
    public String getContent() {
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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

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