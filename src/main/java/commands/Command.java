package commands;

/**
 * Interface for all commands in the text matcher application.
 *
 * @author [Dein u-Kürzel]
 */
public interface Command {

    /**
     * Executes the command with the given arguments.
     *
     * @param args the command arguments
     * @return the result message, or null if no output
     * @throws CommandException if the command fails
     */
    String execute(String[] args) throws CommandException;

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

