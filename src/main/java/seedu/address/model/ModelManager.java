package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** In-memory model of Mentora's student register and user preferences. */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private final AddressBook addressBook;
    private final FilteredList<Student> filteredStudents;
    private final UserPrefs userPrefs;

    /** Creates a model manager with the supplied student data and user preferences. */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);
        logger.fine("Initializing with student register: " + addressBook + " and user prefs " + userPrefs);
        this.addressBook = new AddressBook(addressBook);
        filteredStudents = new FilteredList<>(this.addressBook.getStudentList());
        this.userPrefs = new UserPrefs(userPrefs);
    }

    /** Creates a model manager with empty student data and default preferences. */
    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        userPrefs.setGuiSettings(requireNonNull(guiSettings));
    }

    @Override
    public void setAddressBook(ReadOnlyAddressBook data) {
        addressBook.resetData(data);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public ObservableList<Student> getStudentList() {
        return filteredStudents;
    }

    @Override
    public void updateFilteredStudentList(Predicate<Student> predicate) {
        filteredStudents.setPredicate(requireNonNull(predicate));
    }

    @Override
    public boolean hasStudent(Student student) {
        return addressBook.hasStudent(student);
    }

    @Override
    public void addStudent(Student student) {
        addressBook.addStudent(student);
    }

    @Override
    public void deleteStudent(StudentId id) {
        addressBook.deleteStudent(id);
    }

    @Override
    public ObservableList<Guardian> getGuardianList() {
        return addressBook.getGuardianList();
    }

    @Override
    public boolean hasGuardian(Guardian guardian) {
        return addressBook.hasGuardian(guardian);
    }

    @Override
    public Optional<Guardian> findGuardianByPhone(GuardianPhone phone) {
        return addressBook.findGuardianByPhone(phone);
    }

    @Override
    public void addGuardian(Guardian guardian) {
        addressBook.addGuardian(guardian);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ModelManager manager
                && addressBook.equals(manager.addressBook) && userPrefs.equals(manager.userPrefs);
    }
}
