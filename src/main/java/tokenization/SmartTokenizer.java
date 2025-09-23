package tokenization;

import core.Token;
import java.util.ArrayList;
import java.util.List;

/**
 * Structure-oriented tokenization strategy.
 * Separators: all characters except letters and digits
 * Exception: numbers may contain dots, colons, and commas internally (not at start/end)
 *
 * @author [Dein u-Kürzel]
 */
public class SmartTokenizer implements TokenizationStrategy {

    @Override
    public List<Token> tokenize(String text) {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        List<Token> tokens = new ArrayList<>();
        int i = 0;

        while (i < text.length()) {
            // Skip separators
            while (i < text.length() && isSeparator(text.charAt(i))) {
                i++;
            }

            if (i >= text.length()) {
                break;
            }

            // Build a token
            int tokenStart = i;
            String token = buildToken(text, i);
            i = tokenStart + token.length();

            // Skip any separators that were part of the original token building
            while (i < text.length() && isSeparator(text.charAt(i))) {
                i++;
            }

            if (!token.isEmpty()) {
                tokens.add(new Token(token, tokenStart, tokenStart + token.length()));
            }
        }

        return tokens;
    }

    /**
     * Builds a single token starting from the given position.
     */
    private String buildToken(String text, int start) {
        StringBuilder token = new StringBuilder();
        int i = start;

        // Collect initial letters and digits
        while (i < text.length() && Character.isLetterOrDigit(text.charAt(i))) {
            token.append(text.charAt(i));
            i++;
        }

        // If we have collected something, check if we need to handle special cases for numbers
        if (token.length() > 0) {
            // Check if the token contains digits
            boolean containsDigits = containsDigits(token.toString());

            if (containsDigits) {
                // For tokens with digits, we may include dots, colons, commas internally
                while (i < text.length()) {
                    char currentChar = text.charAt(i);

                    if (Character.isLetterOrDigit(currentChar)) {
                        // Always include letters and digits
                        token.append(currentChar);
                        i++;
                    } else if (isSpecialNumberChar(currentChar)) {
                        // Check if this special char has more letters/digits after it
                        if (i + 1 < text.length() && Character.isLetterOrDigit(text.charAt(i + 1))) {
                            // There are more letters/digits after this special char, include it
                            token.append(currentChar);
                            i++;
                        } else {
                            // No more letters/digits after this special char, stop here
                            break;
                        }
                    } else {
                        // Any other character is a separator, stop
                        break;
                    }
                }
            }
        }

        return token.toString();
    }

    /**
     * Checks if a character is a separator (not letter or digit).
     */
    private boolean isSeparator(char c) {
        return !Character.isLetterOrDigit(c);
    }

    /**
     * Checks if the token contains any digits.
     */
    private boolean containsDigits(String token) {
        for (int i = 0; i < token.length(); i++) {
            if (Character.isDigit(token.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a character is a special character allowed in numbers (dots, colons, commas).
     */
    private boolean isSpecialNumberChar(char c) {
        return c == '.' || c == ':' || c == ',';
    }

    @Override
    public String getName() {
        return "SMART";
    }
}