package core;

import exceptions.CommandException;
import java.util.HashMap;
import java.util.Map;
import java.util.Collection;

/**
 * Central manager for all loaded texts in the application.
 * Manages text storage, retrieval, and identifier handling.
 *
 * @author ujnaa
 */
public class TextManager {
    private final Map<String, Text> texts;

    /**
     * Creates a new TextManager.
     */
    public TextManager() {
        this.texts = new HashMap<>();
    }

    /**
     * Adds or updates a text in the manager.
     *
     * @param text the text to add
     * @return true if this was an update (text with same ID existed), false if new
     * @throws CommandException if the text is null
     */
    public boolean addText(Text text) throws CommandException {
        if (text == null) {
            throw new CommandException("ERROR: Text cannot be null");
        }

        boolean wasUpdate = texts.containsKey(text.identifier());
        texts.put(text.identifier(), text);
        return wasUpdate;
    }

    /**
     * Gets a text by its identifier.
     *
     * @param identifier the text identifier
     * @return the text, or null if not found
     */
    public Text getText(String identifier) {
        return texts.get(identifier);
    }

    /**
     * Checks if a text with the given identifier exists.
     *
     * @param identifier the text identifier
     * @return true if the text exists
     */
    public boolean hasText(String identifier) {
        return texts.containsKey(identifier);
    }

    /**
     * Gets all loaded texts.
     *
     * @return a collection of all texts
     */
    public Collection<Text> getAllTexts() {
        return texts.values();
    }

    /**
     * Gets the number of loaded texts.
     *
     * @return the text count
     */
    public int getTextCount() {
        return texts.size();
    }

    /**
     * Clears all texts from the manager.
     */
    public void clear() {
        texts.clear();
    }

    /**
     * Removes a specific text by identifier.
     *
     * @param identifier the text identifier
     * @return true if the text was removed, false if it didn't exist
     */
    public boolean removeText(String identifier) {
        return texts.remove(identifier) != null;
    }

    /**
     * Gets all text identifiers.
     *
     * @return a collection of all identifiers
     */
    public Collection<String> getIdentifiers() {
        return texts.keySet();
    }
}