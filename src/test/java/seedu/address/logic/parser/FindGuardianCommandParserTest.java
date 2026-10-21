package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindGuardianCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.guardian.GuardianName;

class FindGuardianCommandParserTest {
    private final FindGuardianCommandParser parser = new FindGuardianCommandParser();

    @Test
    void parse_validQuery_acceptsCasingWhitespaceAndBoundaryLengths() throws Exception {
        FindGuardianCommand expected = new FindGuardianCommand(new GuardianName("Anita Lim"));
        assertEquals(expected, parser.parse(" \tN/ Anita\t Lim "));
        assertEquals(expected, new AddressBookParser().parseCommand("FiNd-GuArDiAn N/Anita Lim"));
        assertEquals(new FindGuardianCommand(new GuardianName("A")), parser.parse("n/A"));
        assertEquals(new FindGuardianCommand(new GuardianName("A".repeat(80))),
                parser.parse("n/" + "A".repeat(80)));
    }

    @Test
    void parse_invalidStructure_reportsUsage() {
        String[] invalid = {"", " ", "Anita", "n/", "n/ \t", "p/Anita", "extra n/Anita",
            "n/Anita n/Lim", "N/Anita N/Lim", "n/Anita p/91234567", "gid/G-0003 n/Anita"};
        for (String arguments : invalid) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse(arguments));
            assertEquals(FindGuardianCommand.MESSAGE_USAGE, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> parser.parse(null));
    }

    @Test
    void parse_invalidQuery_reportsSharedQueryError() {
        String[] invalid = {"Anita2", "91234567", "G-0003", "Anita!", "-Anita", "A".repeat(81)};
        for (String query : invalid) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parse("n/" + query));
            assertEquals(FindGuardianCommand.MESSAGE_QUERY_CONSTRAINTS, error.getMessage());
        }
    }
}
