package src.commands.inspect;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Global registry for excluded subsequence keys during inspection.
 * Keys are stable String representations of token subsequences.
 *
 * @author ujnaa
 */
public final class ExclusionRegistry {

    /** Singleton instance. */
    private static final ExclusionRegistry INSTANCE = new ExclusionRegistry();

    /** Internal set of excluded subsequence keys. */
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
     * Adds a subsequence key to the exclusion set.
     *
     * @param key the subsequence key to exclude, must not be null
     */
    public void exclude(String key) {
        if (key != null) {
            excluded.add(key);
        }
    }

    /**
     * Checks whether a subsequence key is excluded.
     *
     * @param key the subsequence key to check
     * @return {@code true} if the key is excluded, {@code false} otherwise
     */
    public boolean contains(String key) {
        return key != null && excluded.contains(key);
    }

    /**
     * Clears all excluded keys (for fresh analysis runs).
     */
    public void clear() {
        excluded.clear();
    }

    /**
     * Returns a read-only snapshot of the currently excluded keys.
     *
     * @return unmodifiable set of excluded keys
     */
    public Set<String> snapshot() {
        return Collections.unmodifiableSet(excluded);
    }
}
