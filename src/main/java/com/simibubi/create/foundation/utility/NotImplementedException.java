package com.simibubi.create.foundation.utility;

/**
 * {@code NotImplementedException} signals that some behavior/code is not implemented yet.
 */
public class NotImplementedException extends Exception {
    /**
     * Constructs a new exception with {@code null} as its detail message.
     * The cause is not initialized, and may subsequently be initialized by a
     * call to {@link #initCause}.
     */
    public NotImplementedException() {
        super();
    }

    /**
     * Constructs a new {@code NotImplementedException} with the specified detail message.  The
     * cause is not initialized, and may subsequently be initialized by
     * a call to {@link #initCause}.
     *
     * @param message the detail message. The detail message is saved for
     *                later retrieval by the {@link #getMessage()} method.
     */
    public NotImplementedException(String message) {
        super(message);
    }
}
