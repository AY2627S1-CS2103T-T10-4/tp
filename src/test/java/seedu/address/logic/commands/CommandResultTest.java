package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;
import static seedu.address.testutil.TypicalIndexes.INDEX_SECOND_PERSON;

import org.junit.jupiter.api.Test;

import seedu.address.commons.core.index.Index;

public class CommandResultTest {
    @Test
    public void constructor_validIndex_preservesFeedbackAndRequestsSelection() {
        CommandResult result = new CommandResult("Opened profile", INDEX_SECOND_PERSON);

        assertEquals("Opened profile", result.getFeedbackToUser());
        assertEquals(INDEX_SECOND_PERSON, result.getSelectedIndex().orElseThrow());
        assertFalse(result.isShowHelp());
        assertFalse(result.isExit());
        assertTrue(new CommandResult("feedback").getSelectedIndex().isEmpty());
        assertTrue(new CommandResult("help", true, false).getSelectedIndex().isEmpty());
        assertTrue(new CommandResult("exit", false, true).getSelectedIndex().isEmpty());
        assertThrows(NullPointerException.class, () -> new CommandResult("feedback", null));
        assertThrows(NullPointerException.class, () -> new CommandResult(null, INDEX_FIRST_PERSON));
    }

    @Test
    public void equals_selectionRequest_comparesIndex() {
        CommandResult result = new CommandResult("feedback", INDEX_FIRST_PERSON);
        CommandResult copy = new CommandResult("feedback", Index.fromOneBased(1));

        assertEquals(result, copy);
        assertEquals(result.hashCode(), copy.hashCode());
        assertNotEquals(result, new CommandResult("feedback", INDEX_SECOND_PERSON));
        assertNotEquals(result, new CommandResult("feedback"));
    }

    @Test
    public void equals() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns true
        assertTrue(commandResult.equals(new CommandResult("feedback")));
        assertTrue(commandResult.equals(new CommandResult("feedback", false, false)));

        // same object -> returns true
        assertTrue(commandResult.equals(commandResult));

        // null -> returns false
        assertFalse(commandResult.equals(null));

        // different types -> returns false
        assertFalse(commandResult.equals(0.5f));

        // different feedbackToUser value -> returns false
        assertFalse(commandResult.equals(new CommandResult("different")));

        // different showHelp value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", true, false)));

        // different exit value -> returns false
        assertFalse(commandResult.equals(new CommandResult("feedback", false, true)));
    }

    @Test
    public void hashcode() {
        CommandResult commandResult = new CommandResult("feedback");

        // same values -> returns same hashcode
        assertEquals(commandResult.hashCode(), new CommandResult("feedback").hashCode());

        // different feedbackToUser value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("different").hashCode());

        // different showHelp value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", true, false).hashCode());

        // different exit value -> returns different hashcode
        assertNotEquals(commandResult.hashCode(), new CommandResult("feedback", false, true).hashCode());
    }

    @Test
    public void toStringMethod() {
        CommandResult commandResult = new CommandResult("feedback");
        String expected = CommandResult.class.getCanonicalName() + "{feedbackToUser="
                + commandResult.getFeedbackToUser() + ", showHelp=" + commandResult.isShowHelp()
                + ", exit=" + commandResult.isExit() + ", selectedIndex=null}";
        assertEquals(expected, commandResult.toString());
    }
}
