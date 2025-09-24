package commands;

import core.Text;
import core.TextManager;
import exceptions.CommandException;

/**
 * Command to input text directly via command line.
 *
 * @author ujnaa
 */
public class InputCommand implements Command {
    private final TextManager textManager;

    /**
     * Creates a new InputCommand.
     *
     * @param textManager the text manager to use
     */
    public InputCommand(TextManager textManager) {
        this.textManager = textManager;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length < 2) {
            throw new CommandException("input command requires at least two arguments: input <id> <text>");
        }

        String identifier = args[0];

        // Validate identifier - no spaces or line breaks
        if (identifier.contains(" ") || identifier.contains("\n") || identifier.contains("\r")) {
            throw new CommandException("Identifier cannot contain spaces or line breaks");
        }

        // Join all remaining arguments as the text content
        StringBuilder textBuilder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                textBuilder.append(" ");
            }
            textBuilder.append(args[i]);
        }

        String content = textBuilder.toString();

        Text text = new Text(identifier, content);
        boolean wasUpdate = textManager.addText(text);

        if (wasUpdate) {
            return "Updated " + identifier;
        } else {
            return "Loaded " + identifier;
        }
    }

    @Override
    public String getName() {
        return "input";
    }

    @Override
    public String getUsage() {
        return "input <id> <text> - Input text directly via command line";
    }
}