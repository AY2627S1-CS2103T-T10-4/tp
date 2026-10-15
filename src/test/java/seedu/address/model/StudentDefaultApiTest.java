package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCatalogue;
import seedu.address.model.person.Person;
import seedu.address.model.student.Student;

class StudentDefaultApiTest {
    @Test
    void modelStudentDefaults_returnEmptyReadOnlyListAndUnsupportedAdd() {
        Model model = new MinimalModel();
        assertEquals(0, model.getStudentList().size());
        assertFalse(model.hasStudent(null));
        assertThrows(UnsupportedOperationException.class, () -> model.addStudent(null));
        assertThrows(UnsupportedOperationException.class, () ->
                model.updateFilteredStudentList(Model.PREDICATE_SHOW_ALL_STUDENTS));
        assertThrows(UnsupportedOperationException.class, () -> model.getStudentList().add(null));
    }

    @Test
    void modelGuardianDefaults_returnEmptyReadOnlyListAndUnsupportedAdd() {
        Model model = new MinimalModel();

        assertEquals(0, model.getGuardianList().size());
        assertFalse(model.hasGuardian(null));
        assertFalse(model.findGuardianByPhone(null).isPresent());
        assertThrows(UnsupportedOperationException.class, () -> model.addGuardian(null));
        assertThrows(UnsupportedOperationException.class, () -> model.getGuardianList().add(null));
    }

    @Test
    void readOnlyAddressBookGuardianDefault_returnsEmptyReadOnlyList() {
        ReadOnlyAddressBook addressBook = new MinimalReadOnlyAddressBook();

        assertEquals(0, addressBook.getGuardianList().size());
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getGuardianList().add(null));
    }

    @Test
    void readOnlyAddressBookDefault_returnsEmptyReadOnlyList() {
        ReadOnlyAddressBook addressBook = new AddressBook();
        assertEquals(0, addressBook.getStudentList().size());
        assertThrows(UnsupportedOperationException.class, () -> addressBook.getStudentList().add(null));
    }

    private static class MinimalModel implements Model {
        private final AddressBook addressBook = new AddressBook();
        private final UserPrefs userPrefs = new UserPrefs();

        @Override
        public Optional<Student> findStudent(String studentId) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public void addGroupAssignment(GroupAssignment assignment) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public List<Group> getAssignedGroups(String studentId) {
            throw new AssertionError("This method should not be called.");
        }

        @Override
        public GroupCatalogue getGroupCatalogue() {
            throw new AssertionError("This student-only test does not use the catalogue.");
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
        public void setGuiSettings(GuiSettings settings) {
            userPrefs.setGuiSettings(settings);
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
        public boolean hasPerson(Person person) {
            return addressBook.hasPerson(person);
        }

        @Override
        public void deletePerson(Person person) {
            addressBook.removePerson(person);
        }

        @Override
        public void addPerson(Person person) {
            addressBook.addPerson(person);
        }

        @Override
        public void setPerson(Person target, Person edited) {
            addressBook.setPerson(target, edited);
        }

        @Override
        public ObservableList<Person> getFilteredPersonList() {
            return addressBook.getPersonList();
        }

        @Override
        public void updateFilteredPersonList(Predicate<Person> predicate) {
            // This minimal test model has no filtered view to update.
        }
    }

    private static class MinimalReadOnlyAddressBook implements ReadOnlyAddressBook {
        @Override
        public Set<GroupAssignment> getGroupAssignments() {
            return Set.of();
        }

        @Override
        public ObservableList<Student> getStudentList() {
            return FXCollections.observableArrayList();
        }
    }
}
