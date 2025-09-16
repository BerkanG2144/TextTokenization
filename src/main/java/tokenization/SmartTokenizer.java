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

            // Start of a token
            int tokenStart = i;
            StringBuilder tokenBuilder = new StringBuilder();

            // Build the token
            while (i < text.length() && !isSeparator(text.charAt(i))) {
                tokenBuilder.append(text.charAt(i));
                i++;
            }

            // Clean the token (remove separators at start/end if it contains numbers)
            String token = cleanToken(tokenBuilder.toString());

            if (!token.isEmpty()) {
                tokens.add(new Token(token, tokenStart, i));
            }
        }

        return tokens;
    }

    /**
     * Checks if a character is a separator (not letter or digit).
     *
     * @param c the character to check
     * @return true if the character is a separator
     */
    private boolean isSeparator(char c) {
        return !Character.isLetterOrDigit(c);
    }

    /**
     * Cleans a token by removing separators at start/end if it contains numbers.
     * Exception: numbers may contain dots, colons, and commas internally.
     *
     * @param token the token to clean
     * @return the cleaned token
     */
    private String cleanToken(String token) {
        if (token.isEmpty()) {
            return token;
        }

        // Check if token contains any digits
        boolean containsDigits = false;
        for (int i = 0; i < token.length(); i++) {
            if (Character.isDigit(token.charAt(i))) {
                containsDigits = true;
                break;
            }
        }

        if (!containsDigits) {
            // No digits, remove all non-letter-or-digit characters
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < token.length(); i++) {
                char c = token.charAt(i);
                if (Character.isLetterOrDigit(c)) {
                    result.append(c);
                }
            }
            return result.toString();
        } else {
            // Contains digits, allow dots, colons, and commas internally
            String result = token;

            // Remove separators from start
            while (!result.isEmpty() &&
                    !Character.isLetterOrDigit(result.charAt(0)) &&
                    isRemovableSeparator(result.charAt(0))) {
                result = result.substring(1);
            }

            // Remove separators from end
            while (!result.isEmpty() &&
                    !Character.isLetterOrDigit(result.charAt(result.length() - 1)) &&
                    isRemovableSeparator(result.charAt(result.length() - 1))) {
                result = result.substring(0, result.length() - 1);
            }

            return result;
        }
    }

    /**
     * Checks if a separator should be removed from start/end of number tokens.
     * Dots, colons, and commas are allowed internally in numbers.
     *
     * @param c the character to check
     * @return true if the character should be removed from start/end
     */
    private boolean isRemovableSeparator(char c) {
        return c == '.' || c == ':' || c == ',';
    }

    @Override
    public String getName() {
        return "SMART";
    }
}