package commands;

import core.Text;
import core.TextManager;
import core.Token;
import exceptions.CommandException;
import exceptions.TokenizationException;
import tokenization.CharTokenizer;
import tokenization.SmartTokenizer;
import tokenization.TokenizationStrategy;
import tokenization.WordTokenizer;

import java.util.List;

/**
 * Command to display tokenized text.
 *
 * @author ujnaa
 */
public class TokenizationCommand implements Command {
    private final TextManager textManager;

    /**
     * Creates a new TokenizationCommand.
     *
     * @param textManager the text manager to use
     */
    public TokenizationCommand(TextManager textManager) {
        this.textManager = textManager;
    }

    @Override
    public String execute(String[] args) throws CommandException {
        if (args.length != 2) {
            throw new CommandException("tokenization command requires exactly two arguments: tokenization <id> <strategy>");
        }

        String identifier = args[0];
        String strategyName = args[1].toUpperCase();

        // Check if text exists
        Text text = textManager.getText(identifier);
        if (text == null) {
            throw new CommandException("Text with identifier '" + identifier + "' not found");
        }

        // Get tokenization strategy
        TokenizationStrategy strategy = getStrategy(strategyName);
        if (strategy == null) {
            throw new CommandException("Unknown tokenization strategy: " + strategyName
                    + ". Available strategies: CHAR, WORD, SMART");
        }

        try {
            // Tokenize and format output
            List<Token> tokens = strategy.tokenize(text.content());

            StringBuilder result = new StringBuilder();
            for (int i = 0; i < tokens.size(); i++) {
                if (i > 0) {
                    result.append("~");
                }
                result.append(tokens.get(i).getValue());
            }

            return result.toString();
        } catch (TokenizationException e) {
            throw new CommandException("Tokenization failed: " + e.getMessage(), e);
        }
    }

    private TokenizationStrategy getStrategy(String strategyName) {
        switch (strategyName) {
            case "CHAR":
                return new CharTokenizer();
            case "WORD":
                return new WordTokenizer();
            case "SMART":
                return new SmartTokenizer();
            default:
                return null;
        }
    }

    @Override
    public String getName() {
        return "tokenization";
    }

    @Override
    public String getUsage() {
        return "tokenization <id> <strategy> - Display tokenized text";
    }
}