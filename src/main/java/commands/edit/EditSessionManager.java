package commands.edit;

import core.Match;
import matching.MatchResult;
import metrics.SimilarityMetric;
import commands.AnalyzeCommand;
import commands.edit.handlers.EditCommandHandler;
import exceptions.CommandException;
import exceptions.InvalidMatchException;
import exceptions.InvalidMetricException;

import java.util.List;
import java.util.ArrayList;
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
     */
    public void startEditSession(MatchResult result, String id1, String id2) {
        List<Match> matches = new ArrayList<>(result.getMatches());
        SimilarityMetric currentMetric = new metrics.SymmetricSimilarity();
        printEditState(result, matches, currentMetric, id1, id2);

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) {
                continue;
            }

            String[] parts = input.split("\\s+");
            String command = parts[0].toLowerCase();

            try {
                if ("exit".equals(command)) {
                    updateAnalysisResult(result, matches, id1, id2);
                    break;
                } else if ("matches".equals(command)) {
                    commandHandler.printMatches(matches, result, id1, id2);
                } else if ("print".equals(command)) {
                    commandHandler.handlePrintCommand(parts, matches, result, id1, id2);
                } else if ("add".equals(command)) {
                    commandHandler.handleAddCommand(parts, matches);
                } else if ("discard".equals(command)) {
                    commandHandler.handleDiscardCommand(parts, matches, result, id1);
                } else if ("extend".equals(command)) {
                    commandHandler.handleExtendCommand(parts, matches, result, id1);
                } else if ("truncate".equals(command)) {
                    commandHandler.handleTruncateCommand(parts, matches, result, id1);
                } else if ("set".equals(command)) {
                    currentMetric = commandHandler.handleSetCommand(parts);
                } else {
                    System.out.println("ERROR: Unknown command in edit mode: " + command);
                    continue;
                }

                if (!"exit".equals(command)) {
                    printEditState(result, matches, currentMetric, id1, id2);
                }

            } catch (CommandException | InvalidMatchException | InvalidMetricException e) {
                System.out.println(e.getMessage());
                printEditState(result, matches, currentMetric, id1, id2);
            }
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