package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.AddGuardianCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class AddGuardianCommandParserTest {
    private final AddGuardianCommandParser parser = new AddGuardianCommandParser();

    @Test
    void parse_validFieldsAndPrefixOrder_success() throws Exception {
        ModelManager firstModel = new ModelManager();
        parser.parse("n/Anita   Lim p/91234567").execute(firstModel);
        assertEquals("Anita Lim", firstModel.getGuardianList().getFirst().getName().toString());
        assertEquals("+65 91234567", firstModel.getGuardianList().getFirst().getPhone().toString());

        ModelManager secondModel = new ModelManager();
        parser.parse("P/+65 61234567 N/Raj Kumar").execute(secondModel);
        assertEquals("Raj Kumar", secondModel.getGuardianList().getFirst().getName().toString());
        assertEquals("+65 61234567", secondModel.getGuardianList().getFirst().getPhone().toString());
    }

    @Test
    void parse_supplementaryUnicodeNameBelowLimit_success() throws Exception {
        String name = "𐐀".repeat(41);
        ModelManager model = new ModelManager();

        parser.parse("n/" + name + " p/60000000").execute(model);

        assertEquals(name, model.getGuardianList().getFirst().getName().toString());
    }

    @Test
    void parse_missingRepeatedEmptyUnknownOrStrayArgument_failure() {
        assertParseFailure(parser, "n/Anita Lim", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "p/91234567", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Anita Lim n/Raj Kumar p/91234567", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Anita Lim p/91234567 p/81234567", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Anita Lim p/91234567 x/value", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected n/Anita Lim p/91234567", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/ p/91234567", AddGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "n/Anita Lim p/", AddGuardianCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_validPhoneWithTrailingText_failure() {
        assertParseFailure(parser, "n/Ann p/61239999 extra", AddGuardianCommand.MESSAGE_USAGE);
    }

    @Test
    void parse_invalidNameOrPhone_failure() {
        assertParseFailure(parser, "n/Anita2 Lim p/91234567", GuardianName.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "n/Anita Lim p/51234567", GuardianPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "n/Anita Lim p/+65  91234567", GuardianPhone.MESSAGE_CONSTRAINTS);
    }
}
