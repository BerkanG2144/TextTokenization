package commands;

import commands.inspect.InspectArgumentParser;
import commands.inspect.InspectExitReason;
import commands.inspect.InspectParameters;
import commands.inspect.InspectSessionManager;
import core.AnalysisResult;
import exceptions.AnalysisNotPerformedException;
import exceptions.CommandException;
import exceptions.TextNotFoundException;
import matching.MatchResult;

import java.util.Scanner;

/**
 * Command to enter interactive inspect mode for a text pair comparison.
 * Refactored to reduce complexity and improve maintainability.
 *
 * @author ujnaa
 */
public class InspectCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;
    private final InspectSessionManager sessionManager;
    private final InspectArgumentParser argumentParser;

    /**
     * Creates a new InspectCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     * @param scanner scanner for user input
     */
    public InspectCommand(AnalyzeCommand analyzeCommand, Scanner scanner) {
        this.analyzeCommand = analyzeCommand;
        this.scanner = scanner;
        this.sessionManager = new InspectSessionManager();
        this.argumentParser = new InspectArgumentParser();
    }

    @Override
    public String execute(String[] args) throws CommandException, TextNotFoundException, AnalysisNotPerformedException {
        InspectParameters params = argumentParser.parseArguments(args);
        MatchResult result = getMatchResult(params.id1(), params.id2());

        if (result.getMatches().isEmpty()) {
            throw new CommandException("Insufficient matches: need at least 1 but only 0 available");
        }

        InspectExitReason exitReason = sessionManager.startInspectSession(result, params, scanner, analyzeCommand);

        if (exitReason == InspectExitReason.COMPLETED) {
            return "Inspection complete. Exit inspection mode";
        } else {
            return "OK, exit inspection mode";
        }
    }

    /**
     * Gets the match result for the specified text pair.
     */
    private MatchResult getMatchResult(String id1, String id2) throws TextNotFoundException, AnalysisNotPerformedException {
        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new AnalysisNotPerformedException();
        }
        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new TextNotFoundException(id1 + "/" + id2,
                    "No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }
        return result;
    }

    @Override
    public String getName() {
        return "inspect";
    }

    @Override
    public String getUsage() {
        return "inspect <id> <id> [context] [minLen] - Enter interactive inspect mode for a text pair";
    }
}