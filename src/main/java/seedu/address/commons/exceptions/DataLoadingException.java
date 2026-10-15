package seedu.address.commons.exceptions;

/**
 * Represents an error during loading of data from a file.
 */
public class DataLoadingException extends Exception {
    /** User-facing explanation when the local data file could not be loaded safely. */
    public static final String MESSAGE_RECOVERY = "Could not load local data. Your original data file was not changed; "
            + "restore or replace it before saving.";

    public DataLoadingException(Exception cause) {
        super(cause);
    }

}
