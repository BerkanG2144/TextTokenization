package src.commands.inspect;

import commands.inspect.InspectParameters;
import core.Match;
import matching.MatchResult;

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
    private final InspectParameters params;      // ← NEU
    private final List<Match> sortedMatches;
    private final Set<Match> treatedMatches;
    private final Map<Match, String> decisions;
    private final List<Match> modifiedMatches;
    private final MatchResult resultRef;

    /**
     * Creates a new InspectState.
     *
     * @param sortedMatches matches sorted for display
     * @param treatedMatches matches that have been processed
     * @param decisions user decisions for each match
     * @param modifiedMatches working copy of matches for modification
     * @param params for parameter
     * @param resultRef for res
     */
    public InspectState(InspectParameters params,
                        List<Match> sortedMatches,
                        Set<Match> treatedMatches,
                        Map<Match, String> decisions,
                        List<Match> modifiedMatches,
                        MatchResult resultRef) {   // <--- hinzufügen
        this.params = params;
        this.sortedMatches = sortedMatches;
        this.treatedMatches = treatedMatches;
        this.decisions = decisions;
        this.modifiedMatches = modifiedMatches;
        this.resultRef = resultRef;        // <--- setzen
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

    /**
     * Gets the inspect parameters.
     *
     * @return Param
     */
    public InspectParameters getParams() {
        return this.params;
    }

    /**
     * Gets result.
     *
     * @return res
     */
    public MatchResult getResultRef() {
        return resultRef;
    }

}