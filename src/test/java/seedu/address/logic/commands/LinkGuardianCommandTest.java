package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;

class LinkGuardianCommandTest {
    @Test
    void execute_existingRecords_linksAndRequestsStudentSelection() throws Exception {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());

        CommandResult result = new LinkGuardianCommand("S-0007", "G-0003").execute(model);

        assertEquals("Linked student S-0007 (Mei Lin) to guardian G-0003 (Anita Lim, +65 91234567).",
                result.getFeedbackToUser());
        assertEquals("S-0007", result.getSelectedStudentId());
        assertFalse(result.isExit());
        assertFalse(result.isShowHelp());
        assertEquals("G-0003", model.getLinkedGuardian("S-0007").orElseThrow().getId().toString());
    }

    @Test
    void execute_duplicateAndReplacement_throwsWithoutChangingLink() throws Exception {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        LinkGuardianCommand command = new LinkGuardianCommand("S-0007", "G-0003");
        command.execute(model);
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException duplicate = assertThrows(CommandException.class, () -> command.execute(model));
        CommandException replacement = assertThrows(CommandException.class, () ->
                new LinkGuardianCommand("S-0007", "G-0004").execute(model));

        assertEquals("Student S-0007 is already linked to guardian G-0003.", duplicate.getMessage());
        assertEquals("Student S-0007 already has guardian G-0003. Unlink it before linking another guardian.",
                replacement.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void execute_unknownIds_reportsMissingRecordWithoutChangingModel() {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        AddressBook before = new AddressBook(model.getAddressBook());

        CommandException studentError = assertThrows(CommandException.class, () ->
                new LinkGuardianCommand("S-9999", "G-0003").execute(model));
        CommandException guardianError = assertThrows(CommandException.class, () ->
                new LinkGuardianCommand("S-0007", "G-9999").execute(model));

        assertEquals("Student S-9999 does not exist.", studentError.getMessage());
        assertEquals("Guardian G-9999 does not exist.", guardianError.getMessage());
        assertEquals(before, model.getAddressBook());
    }

    @Test
    void constructorAndExecute_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new LinkGuardianCommand(null, "G-0003"));
        assertThrows(NullPointerException.class, () -> new LinkGuardianCommand("S-0007", null));
        assertThrows(NullPointerException.class, () -> new LinkGuardianCommand("S-0007", "G-0003").execute(null));
    }

    @Test
    void equals_matchingAndDifferentIds_comparesBothIds() {
        LinkGuardianCommand command = new LinkGuardianCommand("S-0007", "G-0003");
        LinkGuardianCommand equivalent = new LinkGuardianCommand("S-0007", "G-0003");
        assertEquals(command, command);
        assertEquals(command, equivalent);
        assertEquals(command.hashCode(), equivalent.hashCode());
        assertNotEquals(command, new LinkGuardianCommand("S-0008", "G-0003"));
        assertNotEquals(command, new LinkGuardianCommand("S-0007", "G-0004"));
        assertNotEquals(command, null);
        assertNotEquals(command, new HelpCommand());
    }
}
