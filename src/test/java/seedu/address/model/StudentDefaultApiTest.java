package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.function.Predicate;

import org.junit.jupiter.api.Test;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;

class StudentDefaultApiTest {
    @Test
    void modelStudentDefaults_returnEmptyReadOnlyListAndUnsupportedAdd() {
        Model model = new MinimalModel();
        assertEquals(0, model.getStudentList().size());
        assertFalse(model.hasStudent(null));
        assertThrows(UnsupportedOperationException.class, () -> model.addStudent(null));
        assertThrows(UnsupportedOperationException.class, () -> model.getStudentList().add(null));
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
}
