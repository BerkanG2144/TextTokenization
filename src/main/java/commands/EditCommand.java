package commands;

import core.AnalysisResult;
import exceptions.CommandException;
import exceptions.AnalysisNotPerformedException;
import exceptions.TextNotFoundException;
import matching.MatchResult;
import commands.edit.EditSessionManager;

import java.util.Scanner;

/**
 * Command to enter interactive edit mode for a text pair comparison.
 *
 * @author ujnaa
 */
public class EditCommand implements Command {
    private final AnalyzeCommand analyzeCommand;
    private final Scanner scanner;
    private final EditSessionManager sessionManager;

    /**
     * Creates a new EditCommand.
     *
     * @param analyzeCommand reference to analyze command for results
     * @param scanner scanner for user input
     */
    public EditCommand(AnalyzeCommand analyzeCommand, Scanner scanner) {
        this.analyzeCommand = analyzeCommand;
        this.scanner = scanner;
        this.sessionManager = new EditSessionManager(scanner, analyzeCommand);
    }

    @Override
    public String execute(String[] args)
            throws CommandException, TextNotFoundException, AnalysisNotPerformedException {
        if (args.length != 2) {
            throw new CommandException("ERROR: edit command requires exactly two arguments: edit <id> <id>");
        }

        String id1 = args[0];
        String id2 = args[1];

        AnalysisResult analysisResult = analyzeCommand.getLastAnalysisResult();
        if (analysisResult == null) {
            throw new AnalysisNotPerformedException();
        }

        MatchResult result = analysisResult.getResult(id1, id2);
        if (result == null) {
            throw new TextNotFoundException(id1 + "/" + id2,
                    "ERROR: No comparison found for texts '" + id1 + "' and '" + id2 + "'");
        }

        sessionManager.startEditSession(result, id1, id2);
        return "OK, exit editing mode.";
    }

    @Override
    public String getName() {
        return "edit";
    }

    @Override
    public String getUsage() {
        return "edit <id> <id> - Enter interactive edit mode for a text pair";
    }
}