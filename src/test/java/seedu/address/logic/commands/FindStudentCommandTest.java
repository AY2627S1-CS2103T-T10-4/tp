package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

public class FindStudentCommandTest {
    private final Student amyTan = student("S-1101", "Amy Tan");
    private final Student meiLin = student("S-1102", "Mei Lin");
    private final Student meiWen = student("S-1103", "Mei Wen");
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        model.addStudent(meiWen);
        model.addStudent(amyTan);
        model.addStudent(meiLin);
    }

    @Test
    public void execute_matchingQuery_filtersVisibleStudentsWithoutChangingStoredData() {
        AddressBook beforeCommand = new AddressBook(model.getAddressBook());

        CommandResult result = command("Mei").execute(model);

        assertEquals("Found 2 student(s) matching \"Mei\".", result.getFeedbackToUser());
        assertEquals(List.of(meiLin, meiWen), model.getStudentList());
        assertEquals(beforeCommand, model.getAddressBook());
    }

    @Test
    public void execute_noMatches_returnsSpecificMessageAndEmptyView() {
        CommandResult result = command("Zara").execute(model);

        assertEquals("No students match \"Zara\".", result.getFeedbackToUser());
        assertEquals(List.of(), model.getStudentList());
        assertEquals(3, model.getAddressBook().getStudentList().size());
    }

    @Test
    public void execute_secondQuery_replacesPreviousFilter() {
        command("Mei").execute(model);

        command("Amy").execute(model);

        assertEquals(List.of(amyTan), model.getStudentList());
    }

    @Test
    public void equals_usesNormalizedQuery() {
        FindStudentCommand command = command("Mei Lin");
        FindStudentCommand equivalent = command("  ＭＥＩ   ＬＩＮ ");

        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, command("Mei Wen"));
        assertNotEquals(command, new HelpCommand());
    }

    @Test
    public void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new FindStudentCommand(null));
        assertThrows(NullPointerException.class, () -> command("Mei").execute(null));
    }

    private static FindStudentCommand command(String query) {
        return new FindStudentCommand(new StudentName(query));
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
