package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @Test
    void conditionalClear_preservesWorkspaceDefaultsAndParticipatesInValueContracts() {
        CommandResult result = new CommandResult("Deleted", false, false, null, true, "S-0001");
        CommandResult copy = new CommandResult("Deleted", false, false, null, true, "S-0001");

        assertEquals(result, copy);
        assertEquals(result.hashCode(), copy.hashCode());
        assertTrue(result.shouldClearStudentSelection());
        assertEquals("S-0001", result.getStudentIdToClearSelection());
        assertNull(result.getSelectedStudentId());
        assertEquals(CommandResult.Workspace.UNCHANGED, result.getWorkspace());
        assertNotEquals(result, new CommandResult("Deleted", false, false, null, true, "S-0002"));
        assertNotEquals(result, new CommandResult("Deleted", false, false, null, false, "S-0001"));
        assertNotEquals(result, new CommandResult("Deleted", false, false, null, true));
        assertTrue(result.toString().contains("studentIdToClearSelection=S-0001"));
        CommandResult groups = new CommandResult("Groups", CommandResult.Workspace.GROUPS);
        assertFalse(groups.shouldClearStudentSelection());
        assertNull(groups.getStudentIdToClearSelection());
    }
}
