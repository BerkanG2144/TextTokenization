package src.tokenization;

import core.Token;
import exceptions.TokenizationException;
import tokenization.TokenizationStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenization strategy that splits text by separators and removes special characters.
 * Separators: spaces and line breaks
 * Special characters: . , : ; ! _ ( ) { }
 *
 * @author ujnaa
 */
public class WordTokenizer implements TokenizationStrategy {

    // Separators: spaces and line breaks (different OS line break variants)
    private static final String SEPARATORS = " \n\r";

    // Special characters to remove: . , : ; ! _ ( ) { }
    private static final String SPECIAL_CHARS = ".,;:!_(){}?";

    @Override
    public List<Token> tokenize(String text) throws TokenizationException {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        List<Token> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();
        int tokenStart = 0;
        int i = 0;

        while (i < text.length()) {
            char c = text.charAt(i);

            if (isSeparator(c)) {
                // Finish current token if it exists
                if (currentToken.length() > 0) {
                    String tokenValue = removeSpecialChars(currentToken.toString());
                    if (!tokenValue.isEmpty()) {
                        tokens.add(new Token(tokenValue, tokenStart, i));
                    }
                    currentToken.setLength(0);
                }

                // Skip multiple consecutive separators
                while (i < text.length() && isSeparator(text.charAt(i))) {
                    i++;
                }
                tokenStart = i;
                continue;
            } else {
                // Add character to current token
                if (currentToken.length() == 0) {
                    tokenStart = i;
                }
                currentToken.append(c);
                i++;
            }
        }

        // Handle last token
        if (currentToken.length() > 0) {
            String tokenValue = removeSpecialChars(currentToken.toString());
            if (!tokenValue.isEmpty()) {
                tokens.add(new Token(tokenValue, tokenStart, text.length()));
            }
        }

        return tokens;
    }

    /**
     * Checks if a character is a separator.
     *
     * @param c the character to check
     * @return true if the character is a separator
     */
    private boolean isSeparator(char c) {
        return SEPARATORS.indexOf(c) >= 0;
    }

    /**
     * Removes special characters from a token.
     *
     * @param token the token to clean
     * @return the cleaned token
     */
    private String removeSpecialChars(String token) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < token.length(); i++) {
            char c = token.charAt(i);
            if (SPECIAL_CHARS.indexOf(c) < 0) {
                result.append(c);
            }
        }

        return result.toString();
    }

    @Override
    public String getName() {
        return "WORD";
    }
}