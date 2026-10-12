package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class CommandResultStudentSelectionTest {
    @Test
    void selectedStudentId_isStoredComparedAndDisplayed() {
        CommandResult selected = new CommandResult("Added student", false, false, "S-0020");
        CommandResult same = new CommandResult("Added student", false, false, "S-0020");
        CommandResult another = new CommandResult("Added student", false, false, "S-0021");
        CommandResult withoutSelection = new CommandResult("Added student", false, false);

        assertEquals("S-0020", selected.getSelectedStudentId());
        assertEquals(selected, same);
        assertNotEquals(selected, another);
        assertNotEquals(selected, withoutSelection);
        assertTrue(selected.toString().contains("selectedStudentId=S-0020"));
        assertNull(withoutSelection.getSelectedStudentId());
    }
}
