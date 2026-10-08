package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class AddressBookParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    void parseCommand_sharedCommandsAreSupported() throws Exception {
        assertInstanceOf(ExitCommand.class, parser.parseCommand("exit"));
        assertInstanceOf(HelpCommand.class, parser.parseCommand("help"));
    }

    @Test
    void parseCommand_personCommandsAreRejected() {
        for (String retiredCommand : new String[] {"add", "edit 1", "delete 1", "clear", "find Mei", "list"}) {
            assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand(retiredCommand));
        }
    }

    @Test
    void parseCommand_emptyInput_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, HelpCommand.MESSAGE_USAGE);
        assertThrows(ParseException.class, expectedMessage, () -> parser.parseCommand(""));
    }

    @Test
    void parseCommand_unknownInput_throwsParseException() {
        assertThrows(ParseException.class, MESSAGE_UNKNOWN_COMMAND, () -> parser.parseCommand("unknown-command"));
    }
}
