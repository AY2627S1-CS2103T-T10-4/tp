package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;

/** Adds a reusable subject or class group without assigning students to it. */
public class AddGroupCommand extends Command {
    public static final String COMMAND_WORD = "add-group";
    public static final String MESSAGE_USAGE =
            "Invalid command format. Use: add-group g/GROUP_CODE l/GROUP_LABEL k/GROUP_KIND";
    public static final String MESSAGE_SUCCESS = "Created group %s (%s, %s).";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. No group was created.";

    private final Group group;

    public AddGroupCommand(Group group) {
        this.group = requireNonNull(group);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        try {
            model.addGroup(group);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        String feedback = String.format(MESSAGE_SUCCESS, group.getCode(), group.getLabel(),
                group.getKind().toDisplayString());
        return CommandResult.forGroup(feedback, group.getCode().getValue());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof AddGroupCommand command && group.equals(command.group);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group);
    }
}
