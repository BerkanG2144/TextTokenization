package commands.inspect;

/**
 * Reason why the inspect mode is exited.
 *
 * @author ujnaa
 */
public enum InspectExitReason {
    /**
     * User explicitly aborted the inspection (e.g., command 'B').
     * Print: "OK, exit inspection mode".
     */
    USER_ABORT,

    /**
     * Inspection finished normally (e.g., command 'X' or no more matches).
     * Print: "Inspection complete. Exit inspection mode".
     */
    COMPLETED
}
