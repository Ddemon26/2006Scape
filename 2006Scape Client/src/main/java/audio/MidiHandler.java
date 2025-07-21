/* MidiHandler - refactored from Runnable_Impl1 */

public interface MidiHandler extends Runnable {
    /**
     * Process a block of MIDI events.
     *
     * @param events events to process
     * @param length number of valid entries in the array
     */
    void processBuffer(int[] events, int length);

    /** Request that the handler shuts down. */
    void requestShutdown(boolean wait);

    /**
     * Enable or disable the handler.
     *
     * @param active true if active
     */
    void setActive(boolean active);

    void close();

    /**
     * Query the current playback position.
     */
    int getPosition(int dummy);

    /** Initialise the handler. */
    void initialize(byte mode);
}
