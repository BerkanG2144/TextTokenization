package src.tokenization;

import core.Token;
import exceptions.TokenizationException;
import tokenization.TokenizationStrategy;

import java.util.ArrayList;
import java.util.List;

/**
 * Tokenization strategy that treats each character as a separate token.
 *
 * @author ujnaa
 */
public class CharTokenizer implements TokenizationStrategy {

    @Override
    public List<Token> tokenize(String text) throws TokenizationException {
        if (text == null) {
            throw new IllegalArgumentException("Text cannot be null");
        }

        List<Token> tokens = new ArrayList<>();

        for (int i = 0; i < text.length(); i++) {
            String charValue = String.valueOf(text.charAt(i));
            tokens.add(new Token(charValue, i, i + 1));
        }

        return tokens;
    }

    @Override
    public String getName() {
        return "CHAR";
    }
}