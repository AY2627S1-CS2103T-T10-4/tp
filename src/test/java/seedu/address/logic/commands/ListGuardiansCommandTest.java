package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class ListGuardiansCommandTest {
    @Test
    void execute_guardiansPresent_clearsFilterAndRequestsGuardiansWorkspace() {
        ModelManager model = new ModelManager();
        model.addGuardian(guardian("G-0101", "Anita Lim", "91234567"));
        model.addGuardian(guardian("G-0102", "Raj Kumar", "61234567"));
        model.updateFilteredGuardianList(unused -> false);

        CommandResult result = new ListGuardiansCommand().execute(model);

        assertEquals("Listed 2 guardian(s).", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GUARDIANS, result.getWorkspace());
        assertEquals(2, model.getGuardianList().size());
    }

    @Test
    void execute_noGuardians_reportsEmptyMessageAndRequestsGuardiansWorkspace() {
        CommandResult result = new ListGuardiansCommand().execute(new ModelManager());

        assertEquals("No guardians registered.", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GUARDIANS, result.getWorkspace());
    }

    @Test
    void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListGuardiansCommand().execute(null));
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }
}
