package commands;

import core.Text;
import core.TextManager;
import utils.FileUtils;
import java.io.IOException;

/**
 * Command to load text from a file.
 * Usage: load <path>
 *
 * @author [Dein u-Kürzel]
 */
public class LoadCommand implements Command {
    private final TextManager textManager;

    /**
     * Creates a new LoadCommand.
     *
     * @param textManager the text manager to use
     */
    public LoadCommand(TextManager textManager) {
        this.textManager = textManager;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 1) {
            throw new CommandException("load command requires exactly one argument: load <path>");
        }

        String filePath = args[0];

        try {
            String content = FileUtils.readFile(filePath);
            String identifier = FileUtils.extractFilename(filePath);

            Text text = new Text(identifier, content);
            boolean wasUpdate = textManager.addText(text);

            if (wasUpdate) {
                return "Updated " + identifier;
            } else {
                return "Loaded " + identifier;
            }

        } catch (IOException e) {
            throw new CommandException("Failed to read file: " + e.getMessage());
        }
    }

    @Override
    public String getName() {
        return "load";
    }

    @Override
    public String getUsage() {
        return "load <path> - Load text from a file";
    }
}