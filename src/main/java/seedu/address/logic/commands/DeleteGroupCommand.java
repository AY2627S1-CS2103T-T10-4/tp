package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.model.AddressBook.MESSAGE_UNKNOWN_GROUP;

import java.util.Objects;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;

/** Deletes one existing group only when it has no student assignments. */
public class DeleteGroupCommand extends Command {
    public static final String COMMAND_WORD = "delete-group";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: delete-group g/GROUP_CODE";
    public static final String MESSAGE_SUCCESS = "Deleted group %s: %s (%s).";
    public static final String MESSAGE_SAVE_FAILURE = "Could not save changes. Group %s was not deleted.";

    private final GroupCode groupCode;

    public DeleteGroupCommand(GroupCode groupCode) {
        this.groupCode = requireNonNull(groupCode);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Group group = model.getGroupCatalogue().findByCode(groupCode.getValue()).orElseThrow(() ->
                new CommandException(String.format(MESSAGE_UNKNOWN_GROUP, groupCode)));
        try {
            model.deleteGroup(groupCode);
        } catch (IllegalArgumentException e) {
            throw new CommandException(e.getMessage(), e);
        }
        String feedback = String.format(MESSAGE_SUCCESS, group.getCode(), group.getLabel(),
                group.getKind().toDisplayString());
        return new CommandResult(feedback, CommandResult.Workspace.GROUPS);
    }

    public GroupCode getGroupCode() {
        return groupCode;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof DeleteGroupCommand command && groupCode.equals(command.groupCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(groupCode);
    }
}
