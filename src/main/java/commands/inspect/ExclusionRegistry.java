package commands.inspect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Global registry for excluded text identifiers during inspection.
 *
 * @author ujnaa
 */
public final class ExclusionRegistry {

    /** Singleton instance. */
    private static final ExclusionRegistry INSTANCE = new ExclusionRegistry();

    /** Internal set of excluded identifiers. */
    private final Set<String> excluded = new HashSet<>();

    /** Private constructor for singleton. */
    private ExclusionRegistry() { }

    /**
     * Returns the singleton instance of the registry.
     *
     * @return the ExclusionRegistry instance
     */
    public static ExclusionRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Adds a text identifier to the exclusion set.
     *
     * @param textId the identifier of the text to exclude, must not be null
     */
    public void exclude(String textId) {
        if (textId != null) {
            excluded.add(textId);
        }
    }

    /**
     * Checks whether a text identifier is excluded.
     *
     * @param textId the identifier to check
     * @return {@code true} if the identifier is excluded, {@code false} otherwise
     */
    public boolean isExcluded(String textId) {
        return textId != null && excluded.contains(textId);
    }

    /**
     * Returns a read-only snapshot of the currently excluded identifiers.
     *
     * @return unmodifiable set of excluded identifiers
     */
    public Set<String> snapshot() {
        return Collections.unmodifiableSet(excluded);
    }
}