package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class GroupAssignmentStorageTest {
    private static final String STUDENTS = "\"students\":[{\"id\":\"S-0801\",\"name\":\"Mei Lin\","
            + "\"academicLevel\":\"Sec 2\"}]";
    private static final String VALID_PAIR = "{\"studentId\":\"S-0801\",\"groupCode\":\"MAT\"}";

    @TempDir
    private Path testFolder;

    @Test
    void saveAndReload_preservesMultipleCanonicalAssignments() throws Exception {
        AddressBook source = new AddressBook();
        Student mei = new Student(new StudentId("S-0801"), new StudentName("Mei Lin"), new AcademicLevel("Sec 2"));
        Student amy = new Student(new StudentId("S-0802"), new StudentName("Amy Tan"), new AcademicLevel("Sec 3"));
        source.addStudent(mei);
        source.addStudent(amy);
        source.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("mat")));
        source.addGroupAssignment(new GroupAssignment(mei.getId(), new GroupCode("s2-mat-a")));
        source.addGroupAssignment(new GroupAssignment(amy.getId(), new GroupCode("MAT")));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(testFolder.resolve("round-trip.json"));

        storage.saveAddressBook(source);
        ReadOnlyAddressBook loaded = storage.readAddressBook().orElseThrow();

        assertEquals(source, loaded);
        assertEquals(3, loaded.getGroupAssignments().size());
        assertTrue(loaded.getGroupAssignments().contains(new GroupAssignment(mei.getId(), new GroupCode("MAT"))));
    }

    @Test
    void read_legacyStudentFileWithoutAssignments_remainsLoadable() throws Exception {
        Path path = testFolder.resolve("legacy.json");
        Files.writeString(path, "{" + STUDENTS + "}");

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow();

        assertEquals(1, loaded.getStudentList().size());
        assertTrue(loaded.getGroupAssignments().isEmpty());
    }

    @Test
    void read_groupCaseVariant_usesExistingStudentIdAndCanonicalCode() throws Exception {
        Path path = testFolder.resolve("canonical.json");
        Files.writeString(path, "{" + STUDENTS + ",\"groupAssignments\":[" + VALID_PAIR.replace("MAT", "mat") + "]}");

        ReadOnlyAddressBook loaded = new JsonAddressBookStorage(path).readAddressBook().orElseThrow();

        assertEquals(Set.of(new GroupAssignment(loaded.getStudentList().get(0).getId(), new GroupCode("MAT"))),
                loaded.getGroupAssignments());
    }

    @Test
    void read_invalidAssignmentData_entersRecoveryAndPreservesBytes() throws Exception {
        String[] invalidArrays = {"null", "{}", "[null]", "[\"MAT\"]", "[{}]",
            "[{\"studentId\":null,\"groupCode\":\"MAT\"}]", "[{\"studentId\":\"S-0801\",\"groupCode\":null}]",
            "[" + VALID_PAIR.replace("S-0801", "S-0809") + "]", "[" + VALID_PAIR.replace("MAT", "UNKNOWN") + "]",
            "[" + VALID_PAIR.replace("MAT", " ") + "]", "[" + VALID_PAIR.replace("S-0801", "bad-id") + "]",
            "[" + VALID_PAIR + "," + VALID_PAIR.replace("MAT", "mat") + "]",
            "[{\"studentId\":\"S-0801\",\"groupCode\":\"MAT\",\"extra\":true}]"};
        for (int i = 0; i < invalidArrays.length; i++) {
            Path path = testFolder.resolve("invalid-" + i + ".json");
            byte[] original = ("{" + STUDENTS + ",\"groupAssignments\":" + invalidArrays[i] + "}")
                    .getBytes(StandardCharsets.UTF_8);
            Files.write(path, original);
            StorageManager storage = new StorageManager(new JsonAddressBookStorage(path),
                    new JsonUserPrefsStorage(testFolder.resolve("prefs.json")));

            assertThrows(DataLoadingException.class, storage::readAddressBook, invalidArrays[i]);
            assertTrue(storage.getDataRecoveryReason().isPresent());
            assertThrows(IOException.class, () -> storage.saveAddressBook(new AddressBook()));
            assertArrayEquals(original, Files.readAllBytes(path));
        }
    }
}
