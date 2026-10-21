package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UnlinkGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;

class UnlinkGuardianCommandParserTest {
    private final UnlinkGuardianCommandParser parser = new UnlinkGuardianCommandParser();

    @Test
    void parse_validId_acceptsPrefixCaseAndWhitespace() throws Exception {
        UnlinkGuardianCommand expected = new UnlinkGuardianCommand("S-0007");
        assertEquals(expected, parser.parse("sid/S-0007"));
        assertEquals(expected, parser.parse(" \tSID/ S-0007\n"));
        assertEquals(expected, new AddressBookParser().parseCommand("UnLiNk-GuArDiAn SID/S-0007"));
        assertEquals(new UnlinkGuardianCommand("S-10000"), parser.parse("sid/S-10000"));
    }

    @Test
    void parse_invalidArguments_reportsUsage() {
        String[] invalid = {"", " ", "S-0007", "sid/", "sid/s-0007", "sid/S-007", "sid/G-0007",
            "sid/S-0007 sid/S-0008", "sid/S-0007 SID/S-0007", "gid/G-0003 sid/S-0007",
            "sid/S-0007 gid/G-0003", "extra sid/S-0007", "sid/S-0007 extra"};
        for (String arguments : invalid) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(arguments));
            assertEquals(UnlinkGuardianCommand.MESSAGE_USAGE, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }
}
