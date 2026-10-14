package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** In-memory model of Mentora's student register and user preferences. */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final GroupCatalogue groupCatalogue = ConfiguredGroups.getCatalogue();

    /** Creates a model manager with the supplied student data and user preferences. */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);
        logger.fine("Initializing with student register: " + addressBook + " and user prefs " + userPrefs);
        this.addressBook = new AddressBook(addressBook);
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
        return addressBook.getStudentList();
    }

    @Override
    public GroupCatalogue getGroupCatalogue() {
        return groupCatalogue;
    }

    @Override
    public Optional<Student> findStudent(String studentId) {
        return addressBook.findStudent(studentId);
    }

    @Override
    public void addGroupAssignment(GroupAssignment assignment) {
        addressBook.addGroupAssignment(assignment);
    }

    @Override
    public List<Group> getAssignedGroups(String studentId) {
        requireNonNull(studentId);
        Set<GroupCode> codes = addressBook.getGroupAssignments().stream()
                .filter(assignment -> assignment.getStudentId().value.equals(studentId))
                .map(GroupAssignment::getGroupCode)
                .collect(Collectors.toSet());
        return groupCatalogue.getGroups().stream().filter(group -> codes.contains(group.getCode())).toList();
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
    public boolean equals(Object other) {
        return other == this || other instanceof ModelManager manager
                && addressBook.equals(manager.addressBook) && userPrefs.equals(manager.userPrefs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(addressBook, userPrefs);
    }
}
