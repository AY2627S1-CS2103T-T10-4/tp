package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.EditGuardianCommand;
import seedu.address.model.ModelManager;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

class EditGuardianCommandParserTest {
    private final EditGuardianCommandParser parser = new EditGuardianCommandParser();

    @Test
    void parse_validFieldsInEitherOrder_executesWithCanonicalPhone() throws Exception {
        ModelManager model = new ModelManager();
        model.addGuardian(new Guardian(new GuardianId("G-0003"), new GuardianName("Anita Lim"),
                new GuardianPhone("91234567")));

        EditGuardianCommand command = parser.parse("P/+65 98765432 GID/G-0003");
        assertEquals("Updated guardian G-0003 phone to +65 98765432. Updated contact will appear in 0 linked "
                + "student profile(s).", command.execute(model).getFeedbackToUser());
        assertEquals(new GuardianPhone("98765432"), model.findGuardian("G-0003").orElseThrow().getPhone());
    }

    @Test
    void parse_missingRepeatedUnknownAndInvalidValues_fail() {
        assertParseFailure(parser, "gid/G-0003", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "p/98765432", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "gid/G-0003 gid/G-0004 p/98765432", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "gid/G-0003 p/98765432 p/81234567", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "gid/G-0003 p/98765432 x/value", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "gid/G-003 p/98765432", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "unexpected gid/G-0003 p/98765432", EditGuardianCommand.MESSAGE_USAGE);
        assertParseFailure(parser, "gid/G-0003 p/12345678", GuardianPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "gid/G-0003 p/21234567", GuardianPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "gid/G-0003 p/987-65432", GuardianPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "gid/G-0003 p/+65  98765432", GuardianPhone.MESSAGE_CONSTRAINTS);
        assertParseFailure(parser, "gid/G-0003 p/", EditGuardianCommand.MESSAGE_USAGE);
    }
}
