package commands.inspect;

import commands.AnalyzeCommand;
import commands.inspect.display.InspectDisplayManager;
import commands.inspect.navigation.InspectNavigationManager;
import core.Match;
import exceptions.CommandException;
import matching.MatchResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;


/**
 * Manages the inspection session workflow.
 * Coordinates the various components of the inspection process.
 *
 * @author ujnaa
 */
public class InspectSessionManager {
    private static final String TOKEN_JOINER = "\u0001";
    private final Map<String, Set<Match>> treatedMatchesPerPair = new HashMap<>();
    private final Map<String, Map<Match, String>> decisionsPerPair = new HashMap<>();  // ← NEW: persist decisions

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
     * @return the exit reason (COMPLETED or USER_ABORT)
     * @throws CommandException for invalid input.
     */
    public InspectExitReason startInspectSession(MatchResult result, InspectParameters params,
                                                 Scanner scanner, AnalyzeCommand analyzeCommand) throws CommandException {
        InspectState state = initializeState(result, params);

        if (state.getSortedMatches().isEmpty()) {
            System.out.println("No matches found with minimum length " + params.displayMinLen());
            return InspectExitReason.COMPLETED;
        }

        int currentIndex = navigationManager.findFirstUntreatedMatch(
                state.getSortedMatches(), state.getTreatedMatches());
        if (currentIndex == -1) {
            updateAnalysisResult(result, state.getModifiedMatches(), params, analyzeCommand);
            return InspectExitReason.COMPLETED;
        }

        return runInspectionLoop(result, params, state, currentIndex, scanner, analyzeCommand);
    }

    /**
     * Initializes the inspection state.
     */
    private InspectState initializeState(MatchResult result, InspectParameters params) {
        List<Match> originalMatches = result.getMatches();
        List<Match> sortedMatches = filterAndSortMatches(originalMatches, result, params);

        String pairKey = createPairKey(params.id1(), params.id2());
        Set<Match> treatedMatches = treatedMatchesPerPair.computeIfAbsent(pairKey, k -> new HashSet<>());

        // ← FIXED: Get persisted decisions instead of creating empty map
        Map<Match, String> decisions = decisionsPerPair.computeIfAbsent(pairKey, k -> new HashMap<>());

        List<Match> modifiedMatches = new ArrayList<>(originalMatches);

        return new InspectState(params, sortedMatches, treatedMatches, decisions, modifiedMatches, result);
    }

    /**
     * Creates a consistent pair key for caching treated matches.
     */
    private String createPairKey(String id1, String id2) {
        return (id1.compareTo(id2) <= 0) ? (id1 + "-" + id2) : (id2 + "-" + id1);
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
     * Runs the main inspection loop.
     *
     * @return the exit reason
     */
    private InspectExitReason runInspectionLoop(MatchResult result, InspectParameters params,
                                                InspectState state, int startIndex, Scanner scanner,
                                                AnalyzeCommand analyzeCommand) {
        int currentIndex = startIndex;

        // Initial display
        displayManager.displayMatch(state.getSortedMatches().get(currentIndex), result, params, state.getDecisions());

        while (true) {
            // Check input before reading
            if (!scanner.hasNextLine()) {
                updateAnalysisResult(result, state.getModifiedMatches(), params, analyzeCommand);
                return InspectExitReason.COMPLETED;
            }

            String input = scanner.nextLine().trim().toUpperCase();
            if (input.isEmpty()) {
                input = "C";
            }
            String command = input.split("\\s+")[0];

            try {
                InspectionAction action = handleUserInput(
                        command,
                        state.getSortedMatches().get(currentIndex),
                        state,
                        currentIndex,
                        result
                );

                if (action.shouldExit()) {
                    updateAnalysisResult(result, state.getModifiedMatches(), params, analyzeCommand);
                    return action.getExitReason();
                }
                boolean decisionCmd = "A".equals(command) || "I".equals(command) || "X".equals(command);
                if (decisionCmd) {
                    // Re-render *current* match to show updated decision ("Current decision: Accept")
                    displayManager.displayMatch(
                            state.getSortedMatches().get(currentIndex), result, params, state.getDecisions());
                }

                // Now advance if the action decided a next index; otherwise keep current.
                if (action.hasValidIndex()) {
                    currentIndex = action.getNewIndex();
                }

                // Render the (possibly advanced) current match for the user's next choice
                displayManager.displayMatch(
                        state.getSortedMatches().get(currentIndex), result, params, state.getDecisions());

                if (action.hasValidIndex()) {
                    currentIndex = action.getNewIndex();
                }
                displayManager.displayMatch(state.getSortedMatches().get(currentIndex), result, params, state.getDecisions());

            } catch (CommandException e) {
                String msg = e.getMessage();
                if (msg != null && !msg.isBlank()) {
                    System.out.println(msg.startsWith("ERROR:") ? msg : "ERROR: " + msg);
                }
            }
        }
    }

    /**
     * Handles user input and returns the action to take.
     */
    private InspectionAction handleUserInput(String command, Match currentMatch,
                                             InspectState state, int currentIndex,
                                             MatchResult result)
            throws CommandException {
        return switch (command) {
            case "C" -> navigationManager.handleContinueCommand(currentIndex, state);
            case "P" -> navigationManager.handlePreviousCommand(currentIndex, state);
            case "A", "I" -> navigationManager.handleDecisionCommand(command, currentMatch, currentIndex, state);
            case "X" -> {
                ExclusionRegistry reg = ExclusionRegistry.getInstance();

                // 1) Wenn noch keine Entscheidung in dieser Inspect-Session:
                //    -> ganze Search-ID ausschließen und sauber beenden
                if (state.getDecisions().isEmpty()) {
                    reg.exclude(state.getParams().id1());      // globale Dokument-ID
                    yield InspectionAction.exitComplete();    // nur eine Abschlusszeile
                }

                // 2) Sonst: lokale Subsequenzen (k1/k2) ausschließen
                String k1 = buildKeyForSeq1(currentMatch, result);
                String k2 = buildKeyForSeq2(currentMatch, result);
                reg.exclude(k1);
                reg.exclude(k2);

                // WICHTIG: jetzt nicht "stay", sondern Navigation entscheiden lassen,
                // damit zum nächsten unbehandelten Match gesprungen wird.
                // handleDecisionCommand("X", ...) soll currentMatch als "behandelt" markieren
                // und einen neuen Index liefern.
                yield navigationManager.handleDecisionCommand("X", currentMatch, currentIndex, state);
            }
            case "B" -> InspectionAction.exitUser();
            default -> {
                throw new CommandException("Error: Invalid command. Use C, P, A, I, X, or B.");
            }
        };
    }

    private String buildKeyForSeq1(Match m, MatchResult result) {
        return joinTokens(result.getSequence1(), m.startPosSequence1(), m.length());
    }

    private String buildKeyForSeq2(Match m, MatchResult result) {
        return joinTokens(result.getSequence2(), m.startPosSequence2(), m.length());
    }

    private String joinTokens(List<?> seq, int start, int len) {
        List<String> parts = new ArrayList<>(len);
        for (int i = start; i < start + len; i++) {
            Object t = seq.get(i);
            if (t == null) {
                parts.add("<null>");
            } else if (t instanceof CharSequence cs) {
                parts.add(cs.toString());
            } else {
                parts.add(t.toString());
            }
        }
        return String.join(TOKEN_JOINER, parts);
    }

    private String buildExclusionKey(Match m, MatchResult result, InspectParameters params) {
        // Wir nehmen immer die Token-Folge aus der "ersten" Sequenz der Anzeige.
        // Ob Text1/Text2 vertauscht angezeigt wird, ist oben bereits per needSwap konsistent.
        boolean needSwap = !result.getText1().identifier().equals(params.id1());

        // Hole Tokens aus der passenden Sequenz
        List<?> seq = needSwap ? result.getSequence2() : result.getSequence1();
        int start = needSwap ? m.startPosSequence2() : m.startPosSequence1();
        int len = m.length();

        // Extrahiere Subsequenz
        List<?> sub = seq.subList(start, start + len);

        // In String normalisieren (Token->String)
        List<String> parts = new ArrayList<>(sub.size());
        for (Object t : sub) {
            if (t == null) {
                parts.add("<null>");
                continue;
            }
            parts.add(tokenToString(t));
        }

        return String.join(TOKEN_JOINER, parts);
    }

    private String tokenToString(Object token) {
        // String/CharSequence direkt behandeln (kein Reflection)
        if (token instanceof CharSequence cs) {
            return cs.toString();
        }

        // 1) value()
        try {
            Method m = token.getClass().getMethod("value");
            Object v = m.invoke(token);
            return (v == null) ? "<null>" : v.toString();
        } catch (NoSuchMethodException e) {
            // weiter zu getText()
        } catch (IllegalAccessException | InvocationTargetException e) {
            // Zugriff/Invocation fehlgeschlagen -> Fallback
            return token.toString();
        }

        // 2) getText()
        try {
            Method m = token.getClass().getMethod("getText");
            Object v = m.invoke(token);
            return (v == null) ? "<null>" : v.toString();
        } catch (NoSuchMethodException e) {
            // weder value() noch getText() vorhanden -> Fallback
            return token.toString();
        } catch (IllegalAccessException | InvocationTargetException e) {
            return token.toString();
        }
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