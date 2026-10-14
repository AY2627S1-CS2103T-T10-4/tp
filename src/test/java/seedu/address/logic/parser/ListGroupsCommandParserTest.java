package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListGroupsCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class ListGroupsCommandParserTest {
    private final ListGroupsCommandParser parser = new ListGroupsCommandParser();

    @Test
    void parse_blankArguments_acceptsCommand() throws ParseException {
        assertInstanceOf(ListGroupsCommand.class, parser.parse(""));
        assertInstanceOf(ListGroupsCommand.class, parser.parse(" \t\n "));
    }

    @Test
    void parse_extraInput_reportsExactFormatError() {
        for (String args : new String[] {" extra", " g/MAT", " sid/S-0001", " /", "\nMAT"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals("Invalid command format. Use: list-groups", error.getMessage());
        }
    }

    @Test
    void parseCommand_commandCasingAndWhitespace_acceptsCommand() throws ParseException {
        AddressBookParser commandParser = new AddressBookParser();
        for (String input : new String[] {"list-groups", "LIST-GROUPS", "LiSt-GrOuPs", "  list-groups \t"}) {
            assertInstanceOf(ListGroupsCommand.class, commandParser.parseCommand(input));
        }
    }

    @Test
    void parseCommand_extraArguments_reportsExactFormatError() {
        AddressBookParser commandParser = new AddressBookParser();
        for (String input : new String[] {"list-groups extra", "LIST-GROUPS g/MAT", "list-groups\tunknown",
            "list-groups\nextra", "list-groups\r\nextra"}) {
            ParseException error = assertThrows(ParseException.class, () -> commandParser.parseCommand(input));
            assertEquals("Invalid command format. Use: list-groups", error.getMessage());
        }
    }
}
