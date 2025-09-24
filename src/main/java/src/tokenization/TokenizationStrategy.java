package src.tokenization;

import core.Token;
import exceptions.TokenizationException;

import java.util.List;

/**
 * Strategy interface for different tokenization approaches.
 *
 * @author ujnaa
 */
public interface TokenizationStrategy {

    /**
     * Tokenizes the given text according to this strategy.
     *
     * @param text the text to tokenize
     * @return a list of tokens
     * @throws TokenizationException if tokenization fails due to invalid input or processing errors
     */
    List<Token> tokenize(String text) throws TokenizationException;

    /**
     * Gets the name of this tokenization strategy.
     *
     * @return the strategy name
     */
    String getName();
}