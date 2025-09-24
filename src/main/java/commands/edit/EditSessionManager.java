package commands.edit;

import commands.AnalyzeCommand;
import commands.edit.handlers.EditCommandHandler;
import core.Match;
import exceptions.CommandException;
import exceptions.InvalidMatchException;
import exceptions.InvalidMetricException;
import exceptions.QuitCommandException;
import matching.MatchResult;
import metrics.SimilarityMetric;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Manages the edit session workflow and command processing.
 *
 * @author ujnaa
 */
public class EditSessionManager {
    private final Scanner scanner;
    private final AnalyzeCommand analyzeCommand;
    private final EditCommandHandler commandHandler;

    /**
     * Creates a new EditSessionManager.
     *
     * @param scanner scanner for user input
     * @param analyzeCommand reference to analyze command
     */
    public EditSessionManager(Scanner scanner, AnalyzeCommand analyzeCommand) {
        this.scanner = scanner;
        this.analyzeCommand = analyzeCommand;
        this.commandHandler = new EditCommandHandler();
    }

    /**
     * Starts an edit session for the given match result.
     *
     * @param result match result to edit
     * @param id1 first text identifier
     * @param id2 second text identifier
     * @throws QuitCommandException if quit command is issued
     */
    public void startEditSession(MatchResult result, String id1, String id2) throws QuitCommandException {
        List<Match> matches = new ArrayList<>(result.getMatches());
        SimilarityMetric currentMetric = new metrics.SymmetricSimilarity();
        printEditState(result, matches, currentMetric, id1, id2);

        while (true) {
            if (!scanner.hasNextLine()) {
                updateAnalysisResult(result, matches, id1, id2);
                break;
            }
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                boolean printState = processCommand(command, parts, result, matches, id1, id2);
                if ("exit".equals(command)) {
                    return;
                }
                if (printState) {
                    printEditState(result, matches, currentMetric, id1, id2);
                }
                if ("set".equals(command)) {
                    currentMetric = commandHandler.handleSetCommand(parts);
                    printEditState(result, matches, currentMetric, id1, id2);
                }
            } catch (QuitCommandException e) {
                throw e;
            } catch (CommandException | InvalidMatchException | InvalidMetricException e) {
                printError(e);
            }
        }
    }

    /**
     * Handles a single edit-mode command. Returns true if the edit state should be printed afterwards.
     */
    private boolean processCommand(String command, String[] parts, MatchResult result,
                                   List<Match> matches, String id1, String id2)
            throws CommandException, InvalidMatchException, InvalidMetricException, QuitCommandException {
        if ("exit".equals(command)) {
            if (parts.length != 1) {
                throw new CommandException("exit command requires no arguments: exit");
            }
            updateAnalysisResult(result, matches, id1, id2);
            return false;
        } else if ("quit".equals(command)) {
            if (parts.length != 1) {
                throw new CommandException("quit command requires no arguments: quit");
            }
            updateAnalysisResult(result, matches, id1, id2);
            throw new QuitCommandException();
        } else if ("matches".equals(command)) {
            if (parts.length != 1) {
                throw new CommandException("matches command requires no arguments: matches");
            }
            commandHandler.printMatches(matches, result, id1, id2);
            return true;
        } else if ("print".equals(command)) {
            commandHandler.handlePrintCommand(parts, matches, result, id1, id2);
            return true;
        } else if ("add".equals(command)) {
            commandHandler.handleAddCommand(parts, matches);
            return true;
        } else if ("discard".equals(command)) {
            commandHandler.handleDiscardCommand(parts, matches, result, id1);
            return true;
        } else if ("extend".equals(command)) {
            commandHandler.handleExtendCommand(parts, matches, result, id1);
            return true;
        } else if ("truncate".equals(command)) {
            commandHandler.handleTruncateCommand(parts, matches, result, id1);
            return true;
        } else if ("set".equals(command)) {
            // set wird im Aufrufer weiterbehandelt, hier kein State-Print
            return false;
        } else {
            System.out.println("ERROR: Unknown command in edit mode: " + command);
            return false;
        }
    }

    private void printError(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) {
            return;
        }
        if (msg.startsWith("ERROR:")) {
            System.out.println(msg);
        } else {
            System.out.println("ERROR: " + msg);
        }
    }

    /**
     * Prints the current edit state.
     */
    private void printEditState(MatchResult result, List<Match> matches,
                                SimilarityMetric metric, String id1, String id2) {
        MatchResult tempResult = new MatchResult(
                result.getText1(), result.getText2(),
                result.getSequence1(), result.getSequence2(),
                matches, result.getTokenizationStrategy(), result.getMinMatchLength()
        );
        double similarity = metric.calculate(tempResult);
        String formattedSimilarity = metric.format(similarity);
        System.out.println("Comparison of " + id1 + ", " + id2 + ": "
                + formattedSimilarity + " similarity, " + matches.size()
                + " matches. Available commands: matches, print, add, extend, truncate, discard, set, exit.");
    }

    /**
     * Updates the analysis result with modified matches.
     */
    private void updateAnalysisResult(MatchResult originalResult, List<Match> modifiedMatches,
                                      String id1, String id2) {
        MatchResult newResult = new MatchResult(
                originalResult.getText1(), originalResult.getText2(),
                originalResult.getSequence1(), originalResult.getSequence2(),
                modifiedMatches, originalResult.getTokenizationStrategy(), originalResult.getMinMatchLength()
        );
        analyzeCommand.updateMatchResult(id1, id2, newResult);
    }
}
