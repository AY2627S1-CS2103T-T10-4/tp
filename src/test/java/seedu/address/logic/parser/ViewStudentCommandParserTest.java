package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.ViewStudentCommand;
import seedu.address.model.student.StudentId;

public class ViewStudentCommandParserTest {

    private final ViewStudentCommandParser parser = new ViewStudentCommandParser();

    @Test
    public void parse_validId_acceptsCaseInsensitivePrefixAndWhitespace() {
        ViewStudentCommand expected = new ViewStudentCommand("S-0007");
        assertParseSuccess(parser, "sid/S-0007", expected);
        assertParseSuccess(parser, "  SiD/  S-0007  ", expected);
        assertParseSuccess(parser, "\tSID/\tS-0007\t", expected);
        assertParseSuccess(parser, "sid/S-10000", new ViewStudentCommand("S-10000"));
    }

    @Test
    public void parse_invalidArguments_throwsParseException() {
        String[] invalidArguments = {
            "", " ", "S-0007", "sid/", "sid/  ", "sid/S-007", "sid/s-0007", "sid/G-0007",
            "sid/S-000a", "sid/S--0007", "sid/S-0007 sid/S-0008", "sid/S-0007 SID/S-0007",
            "sid/S-0007 n/Mei", "gid/G-0007 sid/S-0007", "unexpected sid/S-0007",
            "sid/S-0007 extra", "sid/S-0007sid/S-0008", "sid/S-0007/", "sid/S- 0007"
        };
        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, ViewStudentCommand.MESSAGE_USAGE);
        }
    }

    @Test
    public void parse_largeUnknownId_doesNotAdvanceIdGenerator() throws Exception {
        StudentId beforeParsing = StudentId.generate();

        // Looking up an ID must not allocate it or parse its digits into a bounded numeric type.
        parser.parse("sid/S-999999999999999999999999999999");

        StudentId afterParsing = StudentId.generate();
        assertEquals(Long.parseLong(beforeParsing.value.substring(2)) + 1,
                Long.parseLong(afterParsing.value.substring(2)));
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_mixedCaseCommand_dispatchesToViewStudentParser() throws Exception {
        assertEquals(new ViewStudentCommand("S-0007"),
                new AddressBookParser().parseCommand("  ViEw-StUdEnT SID/S-0007  "));
    }
}
