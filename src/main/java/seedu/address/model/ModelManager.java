package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.group.GroupCode;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianPhone;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;

/** In-memory model of the complete register, filtered Student view and user preferences. */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private static final Comparator<Student> STUDENT_DISPLAY_ORDER =
            Comparator.comparing((Student student) -> student.getName().normalized())
                    .thenComparing(Student::getId);
    private final AddressBook addressBook;
    private final FilteredList<Student> filteredStudents;
    private final FilteredList<Guardian> filteredGuardians;
    private final UserPrefs userPrefs;
    private final GroupCatalogue groupCatalogue = ConfiguredGroups.getCatalogue();

    /** Creates a model manager with the supplied student data and user preferences. */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);
        logger.fine("Initializing with student register: " + addressBook + " and user prefs " + userPrefs);
        this.addressBook = new AddressBook(addressBook);
        filteredStudents = new FilteredList<>(this.addressBook.getStudentList());
        filteredGuardians = new FilteredList<>(this.addressBook.getGuardianList());
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
    public GroupCatalogue getGroupCatalogue() {
        return groupCatalogue;
    }

    @Override
    public Optional<Student> findStudent(String studentId) {
        return addressBook.findStudent(studentId);
    }

    @Override
    public Optional<Student> findStudent(StudentId id) {
        return addressBook.findStudent(id);
    }

    @Override
    public void addGroupAssignment(GroupAssignment assignment) {
        addressBook.addGroupAssignment(assignment);
    }

    @Override
    public void removeGroupAssignment(GroupAssignment assignment) {
        addressBook.removeGroupAssignment(assignment);
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
    public void updateStudent(Student student) {
        addressBook.updateStudent(student);
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
        return filteredGuardians;
    }

    @Override
    public void updateFilteredGuardianList(Predicate<Guardian> predicate) {
        filteredGuardians.setPredicate(requireNonNull(predicate));
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
    public GuardianId unlinkGuardian(String studentId) {
        GuardianId guardianId = addressBook.unlinkGuardian(studentId);
        logger.fine(() -> "Removed in-memory guardian link for student " + studentId);
        return guardianId;
    }

    @Override
    public void linkGuardian(String studentId, String guardianId) {
        addressBook.linkGuardian(studentId, guardianId);
    }

    @Override
    public List<Student> getLinkedStudents(String guardianId) {
        requireNonNull(guardianId);
        return addressBook.getGuardianLinks().entrySet().stream()
                .filter(entry -> entry.getValue().toString().equals(guardianId))
                .map(entry -> addressBook.findStudent(entry.getKey()).orElseThrow())
                .sorted(STUDENT_DISPLAY_ORDER)
                .toList();
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
