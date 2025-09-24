package commands.inspect.navigation;

import commands.inspect.InspectState;
import commands.inspect.InspectionAction;
import core.Match;

import java.util.List;
import java.util.Set;

/**
 * Handles navigation logic during inspection.
 * Manages movement between matches and user decision processing.
 *
 * @author ujnaa
 */
public class InspectNavigationManager {

    /**
     * Finds the first untreated match in the sorted list.
     *
     * @param sortedMatches list of sorted matches
     * @param treatedMatches set of treated matches
     * @return index of first untreated match, or -1 if none found
     */
    public int findFirstUntreatedMatch(List<Match> sortedMatches, Set<Match> treatedMatches) {
        for (int i = 0; i < sortedMatches.size(); i++) {
            if (!treatedMatches.contains(sortedMatches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Handles the continue command.
     *
     * @param currentIndex current match index
     * @param state current inspection state
     * @return action to take
     */
    public InspectionAction handleContinueCommand(int currentIndex, InspectState state) {
        int nextIndex = findNextUntreatedWrap(currentIndex, state.getSortedMatches(), state.getTreatedMatches());
        if (nextIndex == -1) {
            // Inspection finished normally (no more untreated matches)
            return InspectionAction.exitComplete();
        }
        return InspectionAction.moveTo(nextIndex);
    }

    /**
     * Handles the previous command.
     *
     * @param currentIndex current match index
     * @param state current inspection state
     * @return action to take
     */
    public InspectionAction handlePreviousCommand(int currentIndex, InspectState state) {
        int prevIndex = findPrevUntreatedWrap(currentIndex, state.getSortedMatches(), state.getTreatedMatches());
        if (prevIndex == -1) {
            System.out.println("No previous untreated matches.");
            return InspectionAction.stay();
        }
        return InspectionAction.moveTo(prevIndex);
    }

    /**
     * Handles decision commands (Accept, Ignore, eXclude).
     *
     * @param input user input command
     * @param currentMatch current match being processed
     * @param currentIndex current match index
     * @param state current inspection state
     * @return action to take
     */
    public InspectionAction handleDecisionCommand(String input, Match currentMatch, int currentIndex, InspectState state) {
        String decision = getDecisionText(input);
        state.getDecisions().put(currentMatch, decision);
        state.getTreatedMatches().add(currentMatch);

        if (!"A".equals(input)) {
            state.getModifiedMatches().removeIf(m -> m.equals(currentMatch));
        }

        // KORREKTUR: X soll NICHT sofort beenden, sondern wie A/I den nächsten Match suchen
        // Alle Entscheidungen (A, I, X) verhalten sich gleich: nächsten unbehandelten Match finden
        int nextIndex = findNextUntreatedWrap(currentIndex, state.getSortedMatches(), state.getTreatedMatches());
        if (nextIndex == -1) {
            // No more untreated matches -> completed exit
            return InspectionAction.exitComplete();
        }
        return InspectionAction.moveTo(nextIndex);
    }

    /**
     * Converts input to decision text.
     *
     * @param input user input
     * @return decision text
     */
    private String getDecisionText(String input) {
        return switch (input) {
            case "A" -> "Accept";
            case "I" -> "Ignore";
            case "X" -> "Exclude";
            default -> "Unknown";
        };
    }

    /**
     * Finds next untreated match with wraparound.
     *
     * @param currentIndex current index
     * @param matches list of matches
     * @param treated set of treated matches
     * @return next untreated index, or -1 if none found
     */
    private int findNextUntreatedWrap(int currentIndex, List<Match> matches, Set<Match> treated) {
        int n = matches.size();
        for (int step = 1; step <= n; step++) {
            int i = (currentIndex + step) % n;
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Finds previous untreated match with wraparound.
     *
     * @param currentIndex current index
     * @param matches list of matches
     * @param treated set of treated matches
     * @return previous untreated index, or -1 if none found
     */
    private int findPrevUntreatedWrap(int currentIndex, List<Match> matches, Set<Match> treated) {
        int n = matches.size();
        for (int step = 1; step <= n; step++) {
            int i = (currentIndex - step + n) % n;
            if (!treated.contains(matches.get(i))) {
                return i;
            }
        }
        return -1;
    }
}