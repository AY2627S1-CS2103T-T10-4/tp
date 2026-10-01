package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_AMY;
import static seedu.address.logic.commands.CommandTestUtil.VALID_REMARK_BOB;
import static seedu.address.logic.commands.CommandTestUtil.assertCommandSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;
import static seedu.address.testutil.TypicalPersons.getTypicalAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Person;
import seedu.address.model.person.Remark;

public class RemarkCommandTest {
    private final Model model = new ModelManager(getTypicalAddressBook(), new UserPrefs());

    @Test
    public void execute() {
        Remark remark = new Remark("Some remark");
        Person original = model.getFilteredPersonList().get(INDEX_FIRST_PERSON.getZeroBased());
        Person edited = new Person(original.getName(), original.getPhone(), original.getEmail(), original.getAddress(),
                remark, original.getTags());
        assertCommandSuccess(new RemarkCommand(INDEX_FIRST_PERSON, remark), model,
                String.format(RemarkCommand.MESSAGE_ADD_REMARK_SUCCESS, Messages.format(edited)), model);
    }

    @Test
    public void equals() {
        RemarkCommand standardCommand = new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY));
        assertTrue(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_AMY))));
        assertTrue(standardCommand.equals(standardCommand));
        assertFalse(standardCommand.equals(null));
        assertFalse(standardCommand.equals(new ClearCommand()));
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_SECOND_PERSON, new Remark(VALID_REMARK_AMY))));
        assertFalse(standardCommand.equals(new RemarkCommand(INDEX_FIRST_PERSON, new Remark(VALID_REMARK_BOB))));
    }
}
