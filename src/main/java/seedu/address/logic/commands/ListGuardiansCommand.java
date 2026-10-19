package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.Locale;

import seedu.address.model.Model;

/** Restores and displays the complete guardian list. */
public class ListGuardiansCommand extends Command {
    public static final String COMMAND_WORD = "list-guardians";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: list-guardians";
    public static final String MESSAGE_SUCCESS_FORMAT = "Listed %d guardian(s).";
    public static final String MESSAGE_EMPTY = "No guardians registered.";

    @Override
    public boolean isReadOnly() {
        return true;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        model.updateFilteredGuardianList(Model.PREDICATE_SHOW_ALL_GUARDIANS);
        int guardianCount = model.getGuardianList().size();
        String feedback = guardianCount == 0
                ? MESSAGE_EMPTY
                : String.format(Locale.ROOT, MESSAGE_SUCCESS_FORMAT, guardianCount);
        return new CommandResult(feedback, CommandResult.Workspace.GUARDIANS);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ListGuardiansCommand;
    }

    @Override
    public int hashCode() {
        return ListGuardiansCommand.class.hashCode();
    }
}
