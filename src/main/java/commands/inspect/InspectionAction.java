package commands.inspect;

/**
 * Action result from handling user input during inspection.
 * Immutable class representing the result of a user command.
 *
 * @author ujnaa
 */
public final class InspectionAction {
    private final boolean shouldExit;
    private final int newIndex;

    /**
     * Private constructor for creating actions.
     *
     * @param shouldExit whether the inspection should exit
     * @param newIndex new index to move to (-1 if staying at current)
     */
    private InspectionAction(boolean shouldExit, int newIndex) {
        this.shouldExit = shouldExit;
        this.newIndex = newIndex;
    }

    /**
     * Creates an exit action.
     *
     * @return action indicating inspection should exit
     */
    public static InspectionAction exit() {
        return new InspectionAction(true, -1);
    }

    /**
     * Creates a move action.
     *
     * @param index index to move to
     * @return action indicating movement to specified index
     */
    public static InspectionAction moveTo(int index) {
        return new InspectionAction(false, index);
    }

    /**
     * Creates a stay action.
     *
     * @return action indicating to stay at current position
     */
    public static InspectionAction stay() {
        return new InspectionAction(false, -1);
    }

    /**
     * Checks if inspection should exit.
     *
     * @return true if should exit
     */
    public boolean shouldExit() {
        return shouldExit;
    }

    /**
     * Gets the new index to move to.
     *
     * @return new index, or -1 if no movement
     */
    public int getNewIndex() {
        return newIndex;
    }

    /**
     * Checks if there's a valid index to move to.
     *
     * @return true if newIndex is valid (>= 0)
     */
    public boolean hasValidIndex() {
        return newIndex >= 0;
    }
}