package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FilterStudentsCommand;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;

public class FilterStudentsCommandParserTest {
    private final FilterStudentsCommandParser parser = new FilterStudentsCommandParser();

    @Test
    public void parse_validGroup_acceptsCaseInsensitivePrefixAndWhitespace() {
        assertParseSuccess(parser, "g/MAT", groupCommand("MAT"));
        assertParseSuccess(parser, "  G/  sci  ", groupCommand("SCI"));
        assertParseSuccess(parser, "\tG/\tmat\t", groupCommand("MAT"));
    }

    @Test
    public void parse_invalidGroupVariant_throwsGroupUsageMessage() {
        String[] invalidArguments = {
            "", " ", "MAT", "g/", "g/   ", "g/MAT g/SCI", "x/MAT", "unexpected g/MAT",
            "g/MAT extra", "g/MAT al/Sec 2", "g//MAT", "g/MAT/", "g/MATg/SCI"
        };
        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, FilterStudentsCommand.MESSAGE_USAGE_GROUP);
        }
    }

    @Test
    public void parse_validAcademicLevel_acceptsCaseInsensitivePrefixAndWhitespace() {
        assertParseSuccess(parser, "al/Sec 2", levelCommand("Sec 2"));
        assertParseSuccess(parser, "  AL/  ip   4  ", levelCommand("IP 4"));
        assertParseSuccess(parser, "\tAl/\tSEC\t2\t", levelCommand("Sec 2"));
    }

    @Test
    public void parse_invalidAcademicVariant_throwsAcademicUsageMessage() {
        String[] invalidArguments = {
            "al/", "al/   ", "al/Sec 2 al/IP 4", "unexpected al/Sec 2", "al/Sec 2 g/MAT",
            "al/Sec 2 /extra", "al//Sec 2", "al/Sec 2 1/foo", "al/Sec 2g/MAT"
        };
        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, FilterStudentsCommand.MESSAGE_USAGE_ACADEMIC_LEVEL);
        }
    }

    @Test
    public void parse_invalidAcademicLevel_throwsSharedConstraintMessage() {
        assertParseFailure(parser, "al/Sec 6", AcademicLevel.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "al/Secondary 2", AcademicLevel.MESSAGE_CONSTRAINTS);
    }

    @Test
    public void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    public void parseCommand_mixedCaseCommand_dispatchesToFilterParser() throws Exception {
        assertEquals(groupCommand("MAT"),
                new AddressBookParser().parseCommand("  FiLtEr-StUdEnTs G/mat  "));
        assertEquals(levelCommand("Sec 2"),
                new AddressBookParser().parseCommand("  FILTER-STUDENTS AL/sec 2  "));
    }

    private static FilterStudentsCommand groupCommand(String code) {
        return new FilterStudentsCommand(new GroupCode(code));
    }

    private static FilterStudentsCommand levelCommand(String level) {
        return new FilterStudentsCommand(new AcademicLevel(level));
    }
}
