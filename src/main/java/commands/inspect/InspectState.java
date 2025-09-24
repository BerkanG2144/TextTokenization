package commands.inspect;

import core.Match;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Holds the state for an inspection session.
 * Immutable state container for inspection process.
 *
 * @author ujnaa
 */
public final class InspectState {
    private final List<Match> sortedMatches;
    private final Set<Match> treatedMatches;
    private final Map<Match, String> decisions;
    private final List<Match> modifiedMatches;

    /**
     * Creates a new InspectState.
     *
     * @param sortedMatches matches sorted for display
     * @param treatedMatches matches that have been processed
     * @param decisions user decisions for each match
     * @param modifiedMatches working copy of matches for modification
     */
    public InspectState(List<Match> sortedMatches, Set<Match> treatedMatches,
                        Map<Match, String> decisions, List<Match> modifiedMatches) {
        this.sortedMatches = sortedMatches;
        this.treatedMatches = treatedMatches;
        this.decisions = decisions;
        this.modifiedMatches = modifiedMatches;
    }

    /**
     * Gets the sorted matches for display.
     *
     * @return list of sorted matches
     */
    public List<Match> getSortedMatches() {
        return sortedMatches;
    }

    /**
     * Gets the set of treated matches.
     *
     * @return set of treated matches
     */
    public Set<Match> getTreatedMatches() {
        return treatedMatches;
    }

    /**
     * Gets the user decisions for matches.
     *
     * @return map of match decisions
     */
    public Map<Match, String> getDecisions() {
        return decisions;
    }

    /**
     * Gets the modified matches list.
     *
     * @return list of modified matches
     */
    public List<Match> getModifiedMatches() {
        return modifiedMatches;
    }
}