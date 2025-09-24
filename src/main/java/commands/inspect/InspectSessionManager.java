package commands.inspect;

import core.Match;
import matching.MatchResult;
import commands.AnalyzeCommand;
import commands.inspect.display.InspectDisplayManager;
import commands.inspect.navigation.InspectNavigationManager;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.HashSet;
import java.util.Set;

/**
 * Manages the inspection session workflow.
 * Coordinates the various components of the inspection process.
 *
 * @author ujnaa
 */
public class InspectSessionManager {
    private final Map<String, Set<Match>> treatedMatchesPerPair = new HashMap<>();
    private final InspectDisplayManager displayManager;
    private final InspectNavigationManager navigationManager;

    /**
     * Creates a new InspectSessionManager.
     */
    public InspectSessionManager() {
        this.displayManager = new InspectDisplayManager();
        this.navigationManager = new InspectNavigationManager();
    }

    /**
     * Starts an inspection session.
     *
     * @param result match result to inspect
     * @param params inspection parameters
     * @param scanner input scanner
     * @param analyzeCommand reference for updating results
     */
    public void startInspectSession(MatchResult result, InspectParameters params,
                                    Scanner scanner, AnalyzeCommand analyzeCommand) {
        InspectState state = initializeState(result, params);

        if (state.getSortedMatches().isEmpty()) {
            System.out.println("No matches found with minimum length " + params.displayMinLen());
            return;
        }

        int currentIndex = navigationManager.findFirstUntreatedMatch(
                state.getSortedMatches(), state.getTreatedMatches());
        if (currentIndex == -1) {
            updateAnalysisResult(result, state.getModifiedMatches(), params, analyzeCommand);
            return;
        }

        runInspectionLoop(result, params, state, currentIndex, scanner, analyzeCommand);
    }

    /**
     * Initializes the inspection state.
     */
    private InspectState initializeState(MatchResult result, InspectParameters params) {
        List<Match> originalMatches = result.getMatches();
        List<Match> sortedMatches = filterAndSortMatches(originalMatches, result, params);

        String pairKey = createPairKey(params.id1(), params.id2());
        Set<Match> treatedMatches = treatedMatchesPerPair.computeIfAbsent(pairKey, k -> new HashSet<>());

        Map<Match, String> decisions = new HashMap<>();
        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        return new InspectState(sortedMatches, treatedMatches, decisions, modifiedMatches);
    }

    /**
     * Filters matches by minimum length and sorts them.
     */
    private List<Match> filterAndSortMatches(List<Match> originalMatches,
                                             MatchResult result, InspectParameters params) {
        List<Match> sortedMatches = new ArrayList<>();
        boolean needSwap = !result.getText1().identifier().equals(params.id1());

        for (Match match : originalMatches) {
            if (match.length() >= params.displayMinLen()) {
                sortedMatches.add(match);
            }
        }

        if (needSwap) {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.startPosSequence2(), m2.startPosSequence2());
                return (c != 0) ? c : -Integer.compare(m1.length(), m2.length());
            });
        } else {
            sortedMatches.sort((m1, m2) -> {
                int c = Integer.compare(m1.startPosSequence1(), m2.startPosSequence1());
                return (c != 0) ? c : -Integer.compare(m1.length(), m2.length());
            });
        }

        return sortedMatches;
    }

    /**
     * Creates a consistent pair key for caching treated matches.
     */
    private String createPairKey(String id1, String id2) {
        return (id1.compareTo(id2) <= 0) ? (id1 + "-" + id2) : (id2 + "-" + id1);
    }

    /**
     * Runs the main inspection loop.
     */
    private void runInspectionLoop(MatchResult result, InspectParameters params,
                                   InspectState state, int startIndex, Scanner scanner,
                                   AnalyzeCommand analyzeCommand) {
        int currentIndex = startIndex;

        while (true) {
            Match currentMatch = state.getSortedMatches().get(currentIndex);
            displayManager.displayMatch(currentMatch, result, params, state.getDecisions());

            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                input = "C";
            }

            InspectionAction action = handleUserInput(input, currentMatch, state);

            if (action.shouldExit()) {
                updateAnalysisResult(result, state.getModifiedMatches(), params, analyzeCommand);
                return;
            }

            if (action.hasValidIndex()) {
                currentIndex = action.getNewIndex();
            }
        }
    }

    /**
     * Handles user input and returns the action to take.
     */
    private InspectionAction handleUserInput(String input, Match currentMatch, InspectState state) {
        return switch (input) {
            case "C" -> navigationManager.handleContinueCommand(state);
            case "P" -> navigationManager.handlePreviousCommand(state);
            case "A", "I", "X" -> navigationManager.handleDecisionCommand(input, currentMatch, state);
            case "B" -> InspectionAction.exit();
            default -> {
                System.out.println("Invalid command. Use C, P, A, I, X, or B.");
                yield InspectionAction.stay();
            }
        };
    }

    /**
     * Updates the analysis result with modified matches.
     */
    private void updateAnalysisResult(MatchResult originalResult, List<Match> modifiedMatches,
                                      InspectParameters params, AnalyzeCommand analyzeCommand) {
        MatchResult newResult = new MatchResult(
                originalResult.getText1(), originalResult.getText2(),
                originalResult.getSequence1(), originalResult.getSequence2(),
                modifiedMatches, originalResult.getTokenizationStrategy(),
                originalResult.getMinMatchLength()
        );
        analyzeCommand.updateMatchResult(params.id1(), params.id2(), newResult);
    }
}