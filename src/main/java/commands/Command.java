package commands;

import exceptions.AnalysisNotPerformedException;
import exceptions.CommandException;
import exceptions.InvalidMatchException;
import exceptions.TextNotFoundException;

/**
 * Interface for all commands in the text matcher application.
 *
 * @author ujnaa
 */
public interface Command {

    /**
     * Executes the command with the given arguments.
     *
     * @param args the command arguments
     * @return the result message, or null if no output
     * @throws CommandException if the command fails due to invalid usage, parameters, insufficient data, or tokenization errors
     * @throws TextNotFoundException if a referenced text could not be found
     * @throws AnalysisNotPerformedException if the command requires an analysis that was not performed yet
     * @throws InvalidMatchException if a match operation fails (e.g. invalid index or overlap)
     */
    String execute(String[] args)
            throws CommandException, TextNotFoundException,
            AnalysisNotPerformedException, InvalidMatchException;

    /**
     * Gets the name of this command.
     *
     * @return the command name
     */
    String getName();

    /**
     * Gets the usage description for this command.
     *
     * @return the usage string
     */
    String getUsage();
}