package tokenization;

import core.Token;
import java.util.List;

/**
 * Strategy interface for different tokenization approaches.
 *
 * @author [Dein u-Kürzel]
 */
public interface TokenizationStrategy {

    /**
     * Tokenizes the given text according to this strategy.
     *
     * @param text the text to tokenize
     * @return a list of tokens
     */
    List<Token> tokenize(String text);

    /**
     * Gets the name of this tokenization strategy.
     *
     * @return the strategy name
     */
    String getName();
}