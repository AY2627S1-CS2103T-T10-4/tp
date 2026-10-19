package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListGuardiansCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class ListGuardiansCommandParserTest {
    private final ListGuardiansCommandParser parser = new ListGuardiansCommandParser();

    @Test
    void parse_blankArguments_acceptsCommand() throws ParseException {
        assertInstanceOf(ListGuardiansCommand.class, parser.parse(""));
        assertInstanceOf(ListGuardiansCommand.class, parser.parse(" \t\n "));
    }

    @Test
    void parse_extraInput_reportsExactFormatError() {
        for (String args : new String[] {" extra", " n/Anita", " gid/G-0001", " /", "\nall"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(args));
            assertEquals("Invalid command format. Use: list-guardians", error.getMessage());
        }
    }

    @Test
    void parseCommand_commandCasingAndWhitespace_acceptsCommand() throws ParseException {
        AddressBookParser commandParser = new AddressBookParser();
        for (String input : new String[] {"list-guardians", "LIST-GUARDIANS", "LiSt-GuArDiAnS",
            "  list-guardians \t"}) {
            assertInstanceOf(ListGuardiansCommand.class, commandParser.parseCommand(input));
        }
    }

    @Test
    void parseCommand_extraArguments_reportsExactFormatError() {
        AddressBookParser commandParser = new AddressBookParser();
        ParseException error = assertThrows(ParseException.class, () ->
                commandParser.parseCommand("list-guardians extra"));
        assertEquals("Invalid command format. Use: list-guardians", error.getMessage());
    }
}
