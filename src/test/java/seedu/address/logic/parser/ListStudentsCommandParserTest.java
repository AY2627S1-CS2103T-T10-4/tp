package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ListStudentsCommand;

public class ListStudentsCommandParserTest {
    private final ListStudentsCommandParser parser = new ListStudentsCommandParser();

    @Test
    public void parse_noArguments_returnsCommand() {
        assertParseSuccess(parser, "", new ListStudentsCommand());
        assertParseSuccess(parser, "  \t", new ListStudentsCommand());
    }

    @Test
    public void parse_extraInput_throwsUsageMessage() {
        assertParseFailure(parser, "students", ListStudentsCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Mei", ListStudentsCommand.MESSAGE_USAGE);
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_mixedCaseCommand_dispatchesToListStudentsParser() throws Exception {
        assertEquals(new ListStudentsCommand(), new AddressBookParser().parseCommand("  LiSt-StUdEnTs  "));
    }
}
