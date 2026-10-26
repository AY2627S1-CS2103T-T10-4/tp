package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

class AddGroupCommandTest {
    private static final Group PHYSICS = new Group(new GroupCode("PHY"), "Physics", GroupKind.SUBJECT);

    @Test
    void execute_newGroup_addsCatalogueEntryAndRequestsSelection() throws Exception {
        ModelManager model = new ModelManager();

        CommandResult result = new AddGroupCommand(PHYSICS).execute(model);

        assertEquals(PHYSICS, model.getGroupCatalogue().findByCode("PHY").orElseThrow());
        assertEquals("Created group PHY (Physics, Subject).", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GROUPS, result.getWorkspace());
        assertEquals("PHY", result.getSelectedGroupCode());
    }

    @Test
    void execute_duplicateCanonicalCode_rejectsWithoutChange() {
        ModelManager model = new ModelManager();
        int before = model.getGroupCatalogue().size();

        CommandException error = assertThrows(CommandException.class, () -> new AddGroupCommand(
                new Group(new GroupCode("mat"), "Other Mathematics", GroupKind.CLASS)).execute(model));

        assertEquals("Group MAT is already configured.", error.getMessage());
        assertEquals(before, model.getGroupCatalogue().size());
    }
}
