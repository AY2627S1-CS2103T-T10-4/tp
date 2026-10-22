package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.model.Model;

/**
 * Requests the existing help window without modifying persisted data.
 */
public class HelpCommand extends Command {

    public static final String COMMAND_WORD = "help";

    public static final String MESSAGE_USAGE = "Invalid command format. Use: help [COMMAND]";
    public static final String MESSAGE_UNKNOWN_COMMAND =
            "No help is available for \"%s\". Run help to see all commands.";

    public static final String SHOWING_HELP_MESSAGE = "Opened help window.";
    private final String command;

    /** Creates a request for the command overview. */
    public HelpCommand() {
        command = null;
    }

    /** Creates a request for a validated, canonical command name. */
    public HelpCommand(String command) {
        this.command = requireNonNull(command);
    }

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        String feedback = command == null ? SHOWING_HELP_MESSAGE : "Showing help for " + command + ".";
        return CommandResult.forHelp(feedback, command);
    }
}
