package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.HelpContent;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.ModelManager;

class HelpCommandParserTest {
    private final AddressBookParser parser = new AddressBookParser();

    @Test
    void parse_allSupportedCommands_requestsHelpWithoutExecutingTarget() throws Exception {
        Set<String> commands = Set.of("add-student", "edit-student", "delete-student", "list-students",
                "find-student", "view-student", "add-guardian", "edit-guardian", "list-guardians",
                "find-guardian", "link-guardian", "unlink-guardian", "list-groups", "assign-group",
                "unassign-group", "add-group", "delete-group", "filter-students", "help", "exit");
        assertEquals(commands, Set.copyOf(HelpContent.getCommands()));
        ModelManager model = new ModelManager();
        for (String command : commands) {
            var parsed = parser.parseCommand("HeLp \t" + command.toUpperCase(Locale.ROOT) + " ");
            CommandResult result = parsed.execute(model);
            assertTrue(parsed.isReadOnly());
            assertTrue(result.isShowHelp());
            assertFalse(result.isExit());
            assertEquals(command, result.getHelpCommand());
            assertEquals("Showing help for " + command + ".", result.getFeedbackToUser());
            String content = HelpContent.getTopic(command);
            assertTrue(content.contains("Syntax: " + command));
            assertTrue(content.contains("Examples:"));
            assertTrue(content.contains("Validation:"));
            assertTrue(content.contains("Common errors:"));
            assertTrue(HelpContent.getOverview().contains("Syntax: " + command));
        }
        assertEquals(new ModelManager(), model);
    }

    @Test
    void parse_noArgument_requestsOverview() throws Exception {
        CommandResult result = parser.parseCommand(" help \t").execute(new ModelManager());
        assertNull(result.getHelpCommand());
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE, result.getFeedbackToUser());
        assertTrue(result.isShowHelp());
    }

    @Test
    void exitTopic_documentsRejectedExtraArguments() {
        assertTrue(HelpContent.getTopic("exit").contains("Extra arguments are rejected."));
    }

    @Test
    void addGroupTopic_documentsAsciiCodeRestriction() {
        assertTrue(HelpContent.getTopic("add-group").contains("ASCII letters (A-Z or a-z)"));
    }

    @Test
    void topics_includeOnlyApplicableInputRules() {
        Set<String> commandsWithoutPhones = Set.of("delete-student", "list-students", "find-student",
                "view-student", "list-guardians", "find-guardian", "link-guardian", "unlink-guardian",
                "list-groups", "add-group", "delete-group", "assign-group", "unassign-group",
                "filter-students", "help", "exit");
        Set<String> commandsWithoutIds = Set.of("add-student", "list-students", "find-student",
                "add-guardian", "list-guardians", "find-guardian", "list-groups", "add-group", "delete-group",
                "filter-students", "help", "exit");
        Set<String> commandsWithoutPrefixes = Set.of("list-students", "list-guardians", "list-groups", "help",
                "exit");

        commandsWithoutPhones.forEach(command ->
                assertFalse(HelpContent.getTopic(command).contains("Singapore phones:")));
        commandsWithoutIds.forEach(command ->
                assertFalse(HelpContent.getTopic(command).contains("IDs are uppercase")));
        commandsWithoutPrefixes.forEach(command ->
                assertFalse(HelpContent.getTopic(command).contains("Each supplied prefix")));
        assertTrue(HelpContent.getTopic("add-student").contains("Singapore phones:"));
        assertTrue(HelpContent.getTopic("link-guardian").contains("IDs are uppercase"));
        assertTrue(HelpContent.getTopic("add-group").contains("Each supplied prefix"));
    }

    @Test
    void parse_unknownAndMultipleArguments_reportsSpecifiedErrors() {
        ParseException unknown = assertThrows(ParseException.class, () -> parser.parseCommand("help UNKNOWN"));
        assertEquals("No help is available for \"UNKNOWN\". Run help to see all commands.", unknown.getMessage());
        for (String input : new String[]{"help add-student extra", "help unknown extra", "help add-student\nextra"}) {
            ParseException error = assertThrows(ParseException.class, () -> parser.parseCommand(input));
            assertEquals(HelpCommand.MESSAGE_USAGE, error.getMessage());
        }
        assertThrows(NullPointerException.class, () -> new HelpCommandParser().parse(null));
    }
}
