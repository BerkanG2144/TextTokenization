package commands.inspect.display;

import core.Match;
import core.Token;
import matching.MatchResult;
import commands.inspect.InspectParameters;

import java.util.List;
import java.util.Map;

/**
 * Handles the display of matches during inspection.
 * Manages context display and match visualization.
 *
 * @author ujnaa
 */
public class InspectDisplayManager {

    /**
     * Displays the current match with context and options.
     *
     * @param match current match to display
     * @param result match result containing text data
     * @param params inspection parameters
     * @param decisions current decisions map
     */
    public void displayMatch(Match match, MatchResult result, InspectParameters params,
                             Map<Match, String> decisions) {
        boolean needSwap = !result.getText1().identifier().equals(params.id1());

        String firstText = needSwap ? result.getText2().content() : result.getText1().content();
        String secondText = needSwap ? result.getText1().content() : result.getText2().content();
        List<Token> firstSeq = needSwap ? result.getSequence2() : result.getSequence1();
        List<Token> secondSeq = needSwap ? result.getSequence1() : result.getSequence2();

        int firstPos = needSwap ? match.startPosSequence2() : match.startPosSequence1();
        int secondPos = needSwap ? match.startPosSequence1() : match.startPosSequence2();

        MatchContextData.PositionPair positions = new MatchContextData.PositionPair(firstPos, secondPos);
        MatchContextData contextData = new MatchContextData(
                match, firstText, secondText, firstSeq, secondSeq, positions, params.contextSize());

        displayMatchContext(contextData);
        displayMatchDecision(match, decisions);
    }

    /**
     * Displays the match context with underlining.
     *
     * @param contextData container with all context display parameters
     */
    private void displayMatchContext(MatchContextData contextData) {
        if (contextData.getFirstPosition() >= contextData.getFirstSequence().size()
                || contextData.getSecondPosition() >= contextData.getSecondSequence().size()) {
            return;
        }

        int startChar1 = contextData.getFirstSequence().get(contextData.getFirstPosition()).getStartPosition();
        int lastTokenIndex1 = Math.min(
                contextData.getFirstPosition() + contextData.getMatch().length() - 1,
                contextData.getFirstSequence().size() - 1);
        int endChar1 = contextData.getFirstSequence().get(lastTokenIndex1).getEndPosition();

        int startChar2 = contextData.getSecondSequence().get(contextData.getSecondPosition()).getStartPosition();
        int lastTokenIndex2 = Math.min(
                contextData.getSecondPosition() + contextData.getMatch().length() - 1,
                contextData.getSecondSequence().size() - 1);
        int endChar2 = contextData.getSecondSequence().get(lastTokenIndex2).getEndPosition();

        String context1 = extractContext(contextData.getFirstText(), startChar1, endChar1, contextData.getContextSize());
        String context2 = extractContext(contextData.getSecondText(), startChar2, endChar2, contextData.getContextSize());

        System.out.println(context1);
        printUnderline(contextData.getFirstText(), startChar1, endChar1, contextData.getContextSize());
        System.out.println(context2);
        printUnderline(contextData.getSecondText(), startChar2, endChar2, contextData.getContextSize());
    }

    /**
     * Prints underline for matched text.
     *
     * @param text the text content
     * @param startChar start character position
     * @param endChar end character position
     * @param contextSize context size around match
     */
    private void printUnderline(String text, int startChar, int endChar, int contextSize) {
        int contextStart = Math.max(0, startChar - contextSize);
        int prefixLength = (contextStart > 0) ? 3 : 0;
        int matchStartInContext = prefixLength + (startChar - contextStart);

        String matchedPart = text.substring(startChar, endChar);
        StringBuilder underline = new StringBuilder();

        for (int i = 0; i < matchStartInContext; i++) {
            underline.append(" ");
        }
        underline.append("^".repeat(matchedPart.length()));
        System.out.println(underline.toString());
    }

    /**
     * Displays the current decision for the match.
     *
     * @param match current match
     * @param decisions decisions map
     */
    private void displayMatchDecision(Match match, Map<Match, String> decisions) {
        String currentDecision = decisions.getOrDefault(match, "None");
        System.out.println("Current decision: " + currentDecision);
        System.out.println();
        System.out.println("[C]ontinue, [P]revious, [A]ccept, [I]gnore, e[X]clude, [B]ack? (C)");
    }

    /**
     * Extracts context around a match.
     *
     * @param text the text content
     * @param start start position
     * @param end end position
     * @param contextSize context size
     * @return extracted context string
     */
    private String extractContext(String text, int start, int end, int contextSize) {
        int contextStart = Math.max(0, start - contextSize);
        int contextEnd = Math.min(text.length(), end + contextSize);

        StringBuilder result = new StringBuilder();

        if (contextStart > 0) {
            result.append("...");
        }

        result.append(text.substring(contextStart, contextEnd));

        if (contextEnd < text.length()) {
            result.append("...");
        }

        return result.toString();
    }
}