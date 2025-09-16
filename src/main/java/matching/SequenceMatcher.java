package matching;

import core.Match;
import core.Token;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;

/**
 * Implements the greedy sequence matching algorithm.
 * Finds matching subsequences between two token sequences.
 *
 * @author [Dein u-Kürzel]
 */
public class SequenceMatcher {

    /**
     * Finds matches between two token sequences using the greedy algorithm.
     * Always returns matches with positions relative to sequence1, sequence2 (in that order).
     *
     * @param sequence1 the first token sequence
     * @param sequence2 the second token sequence
     * @param minMatchLength minimum length for matches (MML)
     * @return list of found matches with positions relative to sequence1 and sequence2
     */
    public List<Match> findMatches(List<Token> sequence1, List<Token> sequence2, int minMatchLength) {
        if (sequence1 == null || sequence2 == null) {
            throw new IllegalArgumentException("Sequences cannot be null");
        }
        if (minMatchLength <= 0) {
            throw new IllegalArgumentException("Minimum match length must be positive");
        }

        // Determine search sequence (longer) and pattern sequence (shorter)
        List<Token> searchSequence;
        List<Token> patternSequence;
        boolean seq1IsSearch;

        if (sequence1.size() > sequence2.size()) {
            searchSequence = sequence1;
            patternSequence = sequence2;
            seq1IsSearch = true;
        } else if (sequence1.size() < sequence2.size()) {
            searchSequence = sequence2;
            patternSequence = sequence1;
            seq1IsSearch = false;
        } else {
            // Same length - always use sequence1 as search sequence for consistency
            searchSequence = sequence1;
            patternSequence = sequence2;
            seq1IsSearch = true;
        }

        List<Match> rawMatches = findRawMatches(searchSequence, patternSequence, minMatchLength);

        // Convert raw matches to positions relative to sequence1, sequence2
        List<Match> finalMatches = new ArrayList<>();
        for (Match rawMatch : rawMatches) {
            if (seq1IsSearch) {
                // sequence1 was search, sequence2 was pattern
                // rawMatch positions are (searchPos, patternPos) = (seq1Pos, seq2Pos)
                finalMatches.add(new Match(rawMatch.getStartPosSequence1(), rawMatch.getStartPosSequence2(), rawMatch.getLength()));
            } else {
                // sequence2 was search, sequence1 was pattern
                // rawMatch positions are (searchPos, patternPos) = (seq2Pos, seq1Pos)
                // We want (seq1Pos, seq2Pos), so swap them
                finalMatches.add(new Match(rawMatch.getStartPosSequence2(), rawMatch.getStartPosSequence1(), rawMatch.getLength()));
            }
        }

        return finalMatches;
    }

    /**
     * Internal method that finds raw matches between search and pattern sequences.
     */
    private List<Match> findRawMatches(List<Token> searchSequence, List<Token> patternSequence, int minMatchLength) {
        List<Match> matches = new ArrayList<>();
        Set<Integer> excludedFromSearch = new HashSet<>();
        Set<Integer> excludedFromPattern = new HashSet<>();

        int patternLength = patternSequence.size();

        // Iterate through decreasing window sizes
        for (int windowLength = patternLength; windowLength >= minMatchLength; windowLength--) {

            // Try all possible patterns of this length
            for (int patternStart = 0; patternStart <= patternLength - windowLength; patternStart++) {

                // Skip if pattern overlaps with existing matches
                if (isRangeExcluded(patternStart, patternStart + windowLength, excludedFromPattern)) {
                    continue;
                }

                // Extract pattern
                List<Token> pattern = patternSequence.subList(patternStart, patternStart + windowLength);

                // Search for this pattern in the search sequence
                for (int searchStart = 0; searchStart <= searchSequence.size() - windowLength; searchStart++) {

                    // Skip if search position overlaps with existing matches
                    if (isRangeExcluded(searchStart, searchStart + windowLength, excludedFromSearch)) {
                        continue;
                    }

                    // Check if pattern matches at this position
                    if (sequencesMatch(searchSequence, searchStart, pattern)) {
                        // Found a match! Store as (searchPos, patternPos)
                        Match match = new Match(searchStart, patternStart, windowLength);
                        matches.add(match);

                        // Mark positions as excluded
                        for (int i = searchStart; i < searchStart + windowLength; i++) {
                            excludedFromSearch.add(i);
                        }
                        for (int i = patternStart; i < patternStart + windowLength; i++) {
                            excludedFromPattern.add(i);
                        }

                        // Break to next pattern (greedy - take first match found)
                        break;
                    }
                }
            }
        }

        return matches;
    }

    /**
     * Checks if a range overlaps with excluded positions.
     *
     * @param start start position (inclusive)
     * @param end end position (exclusive)
     * @param excluded set of excluded positions
     * @return true if any position in range is excluded
     */
    private boolean isRangeExcluded(int start, int end, Set<Integer> excluded) {
        for (int i = start; i < end; i++) {
            if (excluded.contains(i)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a pattern matches the search sequence at a given position.
     *
     * @param searchSequence the sequence to search in
     * @param searchStart the start position in search sequence
     * @param pattern the pattern to match
     * @return true if the pattern matches at the given position
     */
    private boolean sequencesMatch(List<Token> searchSequence, int searchStart, List<Token> pattern) {
        if (searchStart + pattern.size() > searchSequence.size()) {
            return false;
        }

        for (int i = 0; i < pattern.size(); i++) {
            if (!searchSequence.get(searchStart + i).equals(pattern.get(i))) {
                return false;
            }
        }

        return true;
    }
}