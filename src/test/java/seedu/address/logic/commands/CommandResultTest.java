package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class CommandResultTest {
    @Test
    void workspaceRequest_preservesExistingDefaultsAndParticipatesInEquality() {
        CommandResult groups = new CommandResult("Listed groups", CommandResult.Workspace.GROUPS);
        CommandResult copy = new CommandResult("Listed groups", CommandResult.Workspace.GROUPS);

        assertEquals(CommandResult.Workspace.UNCHANGED, new CommandResult("Listed groups").getWorkspace());
        assertEquals(groups, copy);
        assertEquals(groups.hashCode(), copy.hashCode());
        assertNotEquals(groups, new CommandResult("Listed groups"));
        assertNull(groups.getSelectedStudentId());
    }

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
