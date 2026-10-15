package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.ModelManager;
import seedu.address.model.group.GroupCatalogue;

class ListGroupsCommandTest {
    @Test
    void execute_configuredCatalogue_returnsCountAndGroupsWorkspace() {
        CommandResult result = new ListGroupsCommand().execute(new ModelManager());

        assertEquals("Listed 5 configured group(s).", result.getFeedbackToUser());
        assertEquals(CommandResult.Workspace.GROUPS, result.getWorkspace());
    }

    @Test
    void execute_emptyCatalogue_reportsActualCount() {
        ModelManager model = new ModelManager() {
            @Override
            public GroupCatalogue getGroupCatalogue() {
                return new GroupCatalogue(List.of());
            }
        };

        assertEquals("Listed 0 configured group(s).", new ListGroupsCommand().execute(model).getFeedbackToUser());
    }

    @Test
    void execute_nullModel_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new ListGroupsCommand().execute(null));
    }
}
