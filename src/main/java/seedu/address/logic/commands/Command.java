package seedu.address.logic.commands;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;

/**
 * Represents a command with hidden internal logic and the ability to be executed.
 */
public abstract class Command {

    /**
     * Returns whether execution leaves persisted register data unchanged.
     * Commands are treated as mutating by default, so new commands are blocked during recovery unless
     * they explicitly declare themselves read-only. Read-only commands do not trigger a data save.
     *
     * @return Whether the command guarantees that persisted model state remains unchanged.
     */
    public boolean isReadOnly() {
        return false;
    }

    /**
     * Executes the command against the model and returns feedback and UI requests.
     * The logic component handles recovery protection and persistence; this method alone does not save changes.
     *
     * @param model {@code Model} which the command should operate on.
     * @return The command result, which logic releases to the UI after any required save succeeds.
     * @throws CommandException If an error occurs during command execution.
     */
    public abstract CommandResult execute(Model model) throws CommandException;

}
