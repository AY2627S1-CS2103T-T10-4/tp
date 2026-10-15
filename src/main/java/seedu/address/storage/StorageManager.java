package seedu.address.storage;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Optional;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;

/**
 * Manages storage of AddressBook data in local storage.
 */
public class StorageManager implements Storage {

    private static final Logger logger = LogsCenter.getLogger(StorageManager.class);
    private final JsonAddressBookStorage addressBookStorage;
    private final JsonUserPrefsStorage userPrefsStorage;
    /** The first load failure permanently disables data writes for this storage instance. */
    private DataLoadingException dataLoadFailure;

    /**
     * Creates a {@code StorageManager} with the given address book and user prefs storage.
     *
     * @param addressBookStorage Storage for students and their relationships.
     * @param userPrefsStorage Independent storage for window preferences.
     */
    public StorageManager(JsonAddressBookStorage addressBookStorage, JsonUserPrefsStorage userPrefsStorage) {
        this.addressBookStorage = addressBookStorage;
        this.userPrefsStorage = userPrefsStorage;
    }

    // ================ UserPrefs methods ==============================

    @Override
    public Path getUserPrefsFilePath() {
        return userPrefsStorage.getUserPrefsFilePath();
    }

    @Override
    public Optional<UserPrefs> readUserPrefs() throws DataLoadingException {
        return userPrefsStorage.readUserPrefs();
    }

    @Override
    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
        userPrefsStorage.saveUserPrefs(userPrefs);
    }


    // ================ AddressBook methods ==============================

    @Override
    public Path getAddressBookFilePath() {
        return addressBookStorage.getAddressBookFilePath();
    }

    @Override
    public Optional<ReadOnlyAddressBook> readAddressBook() throws DataLoadingException {
        logger.fine("Attempting to read data from file: " + addressBookStorage.getAddressBookFilePath());
        try {
            return addressBookStorage.readAddressBook();
        } catch (DataLoadingException e) {
            if (dataLoadFailure == null) {
                dataLoadFailure = e;
            }
            throw e;
        }
    }

    @Override
    public Optional<String> getDataRecoveryReason() {
        if (dataLoadFailure == null) {
            return Optional.empty();
        }
        Throwable cause = dataLoadFailure.getCause();
        String reason = cause == null ? dataLoadFailure.getMessage() : cause.getMessage();
        return Optional.of(reason == null || reason.isBlank()
                ? "The local data file could not be read or validated." : reason);
    }

    @Override
    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
        if (dataLoadFailure != null) {
            throw new IOException(DataLoadingException.MESSAGE_RECOVERY, dataLoadFailure);
        }
        logger.fine("Attempting to write to data file: " + addressBookStorage.getAddressBookFilePath());
        addressBookStorage.saveAddressBook(addressBook);
    }

}
