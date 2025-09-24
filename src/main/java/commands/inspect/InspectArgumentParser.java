package commands.inspect;

import exceptions.CommandException;

/**
 * Handles argument parsing for the inspect command.
 * Separates argument validation and parsing logic.
 *
 * @author ujnaa
 */
public class InspectArgumentParser {

    /**
     * Parses command arguments and validates them.
     *
     * @param args command line arguments
     * @return validated InspectParameters
     * @throws CommandException if arguments are invalid
     */
    public InspectParameters parseArguments(String[] args) throws CommandException {
        if (args.length < 2 || args.length > 4) {
            throw new CommandException("inspect command requires: inspect <id> <id> [context] [minLen]");
        }

        String id1 = args[0];
        String id2 = args[1];
        int contextSize = parseContextSize(args);
        int displayMinLen = parseDisplayMinLen(args);

        return new InspectParameters(id1, id2, contextSize, displayMinLen);
    }

    /**
     * Parses context size from arguments.
     */
    private int parseContextSize(String[] args) throws CommandException {
        if (args.length < 3) {
            return 0;
        }

        try {
            int contextSize = Integer.parseInt(args[2]);
            if (contextSize < 0) {
                throw new CommandException("Context size must be non-negative");
            }
            return contextSize;
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for context size");
        }
    }

    /**
     * Parses display minimum length from arguments.
     */
    private int parseDisplayMinLen(String[] args) throws CommandException {
        if (args.length < 4) {
            return 1;
        }

        try {
            int displayMinLen = Integer.parseInt(args[3]);
            if (displayMinLen <= 0) {
                throw new CommandException("minLen must be positive");
            }
            return displayMinLen;
        } catch (NumberFormatException e) {
            throw new CommandException("Invalid number format for minLen");
        }
    }
}