package main;

import commands.*;
import core.TextManager;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

/**
 * Main class for the Text Matcher application.
 * Provides command-line interface for text sequence matching.
 *
 * @author [Dein u-Kürzel]
 */
public class TextMatcher {
    private final TextManager textManager;
    private final Map<String, Command> commands;
    private final Scanner scanner;
    private final AnalyzeCommand analyzeCommand;

    /**
     * Creates a new TextMatcher instance.
     */
    public TextMatcher() {
        this.textManager = new TextManager();
        this.commands = new HashMap<>();
        this.scanner = new Scanner(System.in);
        this.analyzeCommand = new AnalyzeCommand(textManager);

        initializeCommands();
    }

    /**
     * Initializes all available commands.
     */
    private void initializeCommands() {
        commands.put("load", new LoadCommand(textManager));
        commands.put("input", new InputCommand(textManager));
        commands.put("tokenization", new TokenizationCommand(textManager));
        commands.put("analyze", analyzeCommand);
        commands.put("list", new ListCommand(analyzeCommand));
        commands.put("matches", new MatchesCommand(analyzeCommand));
        commands.put("clear", new ClearCommand(textManager));
        commands.put("edit", new EditCommand(analyzeCommand, scanner));
        // TODO: Add other commands as they are implemented
    }

    /**
     * Starts the interactive command loop.
     */
    public void run() {
        System.out.println("Use one of the following commands: load, input, tokenization, analyze, clear, list, top, matches, histogram, edit, inspect, quit.");

        while (true) {
            System.out.print("> ");
            String inputLine = scanner.nextLine().trim();

            if (inputLine.isEmpty()) {
                continue;
            }

            String[] parts = parseCommand(inputLine);
            String commandName = parts[0].toLowerCase();

            if ("quit".equals(commandName)) {
                break;
            }

            try {
                Command command = commands.get(commandName);
                if (command == null) {
                    System.out.println("ERROR: Unknown command: " + commandName);
                    continue;
                }

                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);

                String result = command.execute(args);
                if (result != null && !result.isEmpty()) {
                    System.out.println(result);
                }

            } catch (CommandException e) {
                System.out.println("ERROR: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("ERROR: Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    /**
     * Parses a command line into command and arguments.
     * Handles simple space-separated parsing.
     *
     * @param commandLine the command line to parse
     * @return array of command parts
     */
    private String[] parseCommand(String commandLine) {
        return commandLine.split("\\s+");
    }

    /**
     * Main entry point.
     *
     * @param args command line arguments (not used)
     */
    public static void main(String[] args) {
        TextMatcher matcher = new TextMatcher();
        matcher.run();
    }
}