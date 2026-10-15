package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.LinkGuardianCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.student.StudentId;

class LinkGuardianCommandParserTest {
    private final LinkGuardianCommandParser parser = new LinkGuardianCommandParser();

    @Test
    void parse_validArguments_acceptsBothOrdersCasingAndWhitespace() throws Exception {
        LinkGuardianCommand expected = new LinkGuardianCommand("S-0007", "G-0003");
        assertParseSuccess(parser, "sid/S-0007 gid/G-0003", expected);
        assertParseSuccess(parser, "gid/G-0003 sid/S-0007", expected);
        assertParseSuccess(parser, " \tGiD/ G-0003\tSID/  S-0007  ", expected);
        assertParseSuccess(parser, "sid/S-10000 gid/G-10000", new LinkGuardianCommand("S-10000", "G-10000"));
        assertEquals(expected, new AddressBookParser().parseCommand("LiNk-GuArDiAn GID/G-0003 SID/S-0007"));
    }

    @Test
    void parse_missingRepeatedMalformedOrExtraArguments_reportsUsage() {
        String[] invalidArguments = {
            "", " ", "S-0007 G-0003", "sid/S-0007", "gid/G-0003", "sid/ gid/G-0003", "sid/S-0007 gid/",
            "sid/S-0007 sid/S-0008 gid/G-0003", "sid/S-0007 gid/G-0003 GID/G-0004",
            "unexpected sid/S-0007 gid/G-0003", "sid/S-0007 gid/G-0003 extra",
            "sid/S-0007 x/value gid/G-0003", "sid/S-0007 gid/G-0003 n/Anita",
            "sid/S-007 gid/G-0003", "sid/S-0007 gid/G-003", "sid/s-0007 gid/G-0003",
            "sid/S-0007 gid/g-0003", "sid/G-0003 gid/S-0007", "sid/S-0007gid/G-0003",
            "sid/S-0007 gid/G-000x", "sid/S- 0007 gid/G-0003"
        };
        for (String arguments : invalidArguments) {
            assertParseFailure(parser, arguments, LinkGuardianCommand.MESSAGE_USAGE);
        }
    }

    @Test
    void parseAndExecute_largeUnknownIds_doesNotAdvanceGenerators() throws Exception {
        ModelManager model = new ModelManager(getUnlinkedAddressBook(), new UserPrefs());
        StudentId beforeStudent = StudentId.generate();
        GuardianId beforeGuardian = GuardianId.generate();
        LinkGuardianCommand missingStudent = parser.parse("sid/S-999999999999999999999999 gid/G-0003");
        LinkGuardianCommand missingGuardian = parser.parse("sid/S-0007 gid/G-999999999999999999999999");

        assertThrows(CommandException.class, () -> missingStudent.execute(model));
        assertThrows(CommandException.class, () -> missingGuardian.execute(model));

        assertEquals(Long.parseLong(beforeStudent.value.substring(2)) + 1,
                Long.parseLong(StudentId.generate().value.substring(2)));
        assertEquals(Long.parseLong(beforeGuardian.toString().substring(2)) + 1,
                Long.parseLong(GuardianId.generate().toString().substring(2)));
    }

    @Test
    void parse_nullArguments_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
