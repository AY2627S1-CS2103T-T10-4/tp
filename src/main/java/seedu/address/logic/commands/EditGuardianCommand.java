package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianPhone;

/** Updates a guardian's shared phone record, which is used by every linked student profile. */
public class EditGuardianCommand extends Command {
    public static final String COMMAND_WORD = "edit-guardian";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: edit-guardian gid/GUARDIAN_ID p/PHONE";
    public static final String MESSAGE_NOT_FOUND_FORMAT = "Guardian %s does not exist.";
    public static final String MESSAGE_DUPLICATE_FORMAT = "Phone %s is already used by guardian %s.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Updated guardian %s phone to %s. Updated contact will appear "
            + "in %d linked student profile(s).";
    public static final String MESSAGE_SAVE_FAILURE_FORMAT = "Could not save changes. Guardian %s was not updated.";

    private final String id;
    private final GuardianPhone phone;

    /** Creates the command from a parsed stable guardian ID and validated phone. */
    public EditGuardianCommand(String id, GuardianPhone phone) {
        this.id = requireNonNull(id);
        this.phone = requireNonNull(phone);
    }

    public String getGuardianId() {
        return id;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        Guardian current = model.findGuardian(id)
                .orElseThrow(() -> new CommandException(String.format(MESSAGE_NOT_FOUND_FORMAT, id)));
        Guardian duplicate = model.findGuardianByPhone(phone).orElse(null);
        if (duplicate != null && !duplicate.getId().equals(current.getId())) {
            throw new CommandException(String.format(MESSAGE_DUPLICATE_FORMAT, phone, duplicate.getId()));
        }
        model.updateGuardian(new Guardian(current.getId(), current.getName(), phone));
        int linkedCount = model.getLinkedStudents(id).size();
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, id, phone, linkedCount));
    }
}
