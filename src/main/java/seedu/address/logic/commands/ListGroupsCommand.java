package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.model.Model;

/**
 * Displays the configured groups without changing student data.
 */
public class ListGroupsCommand extends Command {
    public static final String COMMAND_WORD = "list-groups";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: list-groups";
    public static final String MESSAGE_SUCCESS = "Listed %d configured group(s).";

    @Override
    public boolean isReadOnly() {
        return true;
    }

    /**
     * Reports the actual catalogue size and requests the Groups workspace without changing the model.
     *
     * @param model The model supplying the application-owned catalogue.
     * @return The count feedback and Groups workspace request.
     * @throws NullPointerException If {@code model} is null.
     */
    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        return new CommandResult(String.format(Locale.ROOT, MESSAGE_SUCCESS, model.getGroupCatalogue().size()),
                CommandResult.Workspace.GROUPS);
    }
}
