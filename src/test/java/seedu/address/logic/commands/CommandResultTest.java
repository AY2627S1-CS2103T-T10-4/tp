package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CommandResultTest {
    @Test
    void studentSelection_usesStableStudentId() {
        CommandResult result = new CommandResult("Added student", false, false, "S-0001");
        assertEquals("Added student", result.getFeedbackToUser());
        assertEquals("S-0001", result.getSelectedStudentId());
        assertNull(new CommandResult("No selection").getSelectedStudentId());
    }

    @Test
    void equalsAndHashCode_includeStudentSelection() {
        CommandResult result = new CommandResult("Opened", false, false, "S-0001");
        CommandResult copy = new CommandResult("Opened", false, false, "S-0001");
        assertEquals(result, copy);
        assertEquals(result.hashCode(), copy.hashCode());
        assertNotEquals(result, new CommandResult("Opened", false, false, "S-0002"));
    }
}
