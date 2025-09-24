package commands.inspect;

/**
 * Action result from handling user input during inspection.
 * Immutable class representing the result of a user command.
 *
 * @author ujnaa
 */
public final class InspectionAction {
    private final boolean shouldExit;
    private final int newIndex; // -1 = stay
    private final InspectExitReason exitReason; // null if shouldExit == false

    private InspectionAction(boolean shouldExit, int newIndex, InspectExitReason exitReason) {
        this.shouldExit = shouldExit;
        this.newIndex = newIndex;
        this.exitReason = exitReason;
    }

    /**
     * Create an exit action with reason {@link InspectExitReason#USER_ABORT}.
     *
     * @return a new {@code InspectionAction} representing user abort
     */
    public static InspectionAction exitUser() {
        return new InspectionAction(true, -1, InspectExitReason.USER_ABORT);
    }

    /**
     * Create an exit action with reason {@link InspectExitReason#COMPLETED}.
     *
     * @return a new {@code InspectionAction} representing completed inspection
     */
    public static InspectionAction exitComplete() {
        return new InspectionAction(true, -1, InspectExitReason.COMPLETED);
    }

    /**
     * Create a move action to a specific match index.
     *
     * @param index the target index to move to
     * @return a new {@code InspectionAction} pointing to the given index
     */
    public static InspectionAction moveTo(int index) {
        return new InspectionAction(false, index, null);
    }

    /**
     * Create a stay action (no movement, keep inspecting).
     *
     * @return a new {@code InspectionAction} staying at the current match
     */
    public static InspectionAction stay() {
        return new InspectionAction(false, -1, null);
    }

    /**
     * Checks whether the inspection loop should exit.
     *
     * @return {@code true} if the inspection loop should exit, {@code false} otherwise
     */
    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Gets the index to move to.
     *
     * @return the new index, or -1 if no movement
     */
    public int getNewIndex() {
        return newIndex;
    }

    /**
     * Checks whether this action contains a valid index.
     *
     * @return {@code true} if the index is valid (>= 0), {@code false} otherwise
     */
    public boolean hasValidIndex() {
        return newIndex >= 0;
    }

    /**
     * Gets the reason for exit.
     *
     * @return the exit reason if this action indicates exit, otherwise {@code null}
     */
    public InspectExitReason getExitReason() {
        return exitReason;
    }
}
