package src.commands;

import commands.Command;
import core.TextManager;
import exceptions.CommandException;

/**
 * Command to clear all texts from the system.
 * Usage: clear
 *
 * @author ujnaa
 */
public class ClearCommand implements Command {
    private final TextManager textManager;

    /**
     * Creates a new ClearCommand.
     *
     * @param textManager the text manager to use
     */
    public ClearCommand(TextManager textManager) {
        this.textManager = textManager;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 0) {
            throw new CommandException("clear command takes no arguments: clear");
        }

        textManager.clear();
        return "Cleared all texts.";
    }

    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public String getUsage() {
        return "clear - Clear all texts from the system";
    }
}