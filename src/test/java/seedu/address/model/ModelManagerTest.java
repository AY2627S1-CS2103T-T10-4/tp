package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.GuiSettings;
import seedu.address.model.person.Person;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentName;

class ModelManagerTest {
    @Test
    void studentOperations_useStudentRegister() {
        ModelManager model = new ModelManager();
        Student student = new Student(new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));

        model.addStudent(student);

        assertTrue(model.hasStudent(student));
        assertEquals(student, model.getStudentList().get(0));
        assertEquals(new AddressBook(model.getAddressBook()), model.getAddressBook());
    }

    @Test
    void preferences_areCopiedAndUpdated() {
        UserPrefs prefs = new UserPrefs();
        prefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        ModelManager model = new ModelManager(new AddressBook(), prefs);
        GuiSettings savedSettings = model.getGuiSettings();
        prefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        assertEquals(savedSettings, model.getGuiSettings());
        assertThrows(NullPointerException.class, () -> model.setGuiSettings(null));
    }

    @Test
    void retiredPersonOperations_areUnsupported() {
        ModelManager model = new ModelManager();
        assertThrows(UnsupportedOperationException.class, () -> model.addPerson((Person) null));
        assertTrue(model.getFilteredPersonList().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> model.hasPerson(null));
    }
}
