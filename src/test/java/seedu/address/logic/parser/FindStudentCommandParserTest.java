package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindStudentCommand;
import seedu.address.model.student.StudentName;

public class FindStudentCommandParserTest {
    private final FindStudentCommandParser parser = new FindStudentCommandParser();

    @Test
    public void parse_validQuery_acceptsPhraseCaseInsensitivePrefixAndWhitespace() {
        assertParseSuccess(parser, "n/mei", command("mei"));
        assertParseSuccess(parser, "  N/  Mei   Lin  ", command("Mei Lin"));
        assertParseSuccess(parser, "\tN/\tＭＥＩ\tＬＩＮ\t", command("mei lin"));
        assertParseSuccess(parser, "n/" + "a".repeat(80), command("a".repeat(80)));
    }

    @Test
    public void parse_missingBlankRepeatedUnknownOrPreamble_throwsUsageMessage() {
        String[] invalidArguments = {
            "", " ", "mei", "n/", "n/   ", "n/Mei n/Lin", "n/Mei N/Lin",
            "al/Sec 2 n/Mei", "n/Mei al/Sec 2", "unexpected n/Mei"
        };
        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, FindStudentCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_queryViolatingSharedNameRules_throwsQueryConstraintMessage() {
        String expected = "Search query must be 1-80 characters and contain name characters only.";
        for (String query : new String[] {"Mei2", "Mei!", "S-0001", "a".repeat(81)}) {
            assertParseFailure(parser, "n/" + query, expected);
        }
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_mixedCaseCommand_dispatchesToFindStudentParser() throws Exception {
        assertEquals(command("Mei Lin"),
                new AddressBookParser().parseCommand("  FiNd-StUdEnT N/Mei Lin  "));
    }

    private static FindStudentCommand command(String query) {
        return new FindStudentCommand(new StudentName(query));
    }
}
