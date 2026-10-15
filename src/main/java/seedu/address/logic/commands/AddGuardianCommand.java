package seedu.address.logic.commands;

import java.util.Optional;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

/** Adds a reusable guardian contact record without linking it to a student. */
public class AddGuardianCommand extends Command {
    public static final String COMMAND_WORD = "add-guardian";
    public static final String MESSAGE_USAGE = "Invalid command format. Use: add-guardian n/NAME p/PHONE";
    public static final String DUPLICATE_FORMAT =
            "A guardian with phone %s already exists (%s). Link that guardian instead.";
    public static final String MESSAGE_SUCCESS_FORMAT = "Added guardian %s: %s (%s).";
    private final GuardianName name;
    private final GuardianPhone phone;

    /** Creates an add command for the supplied guardian name and phone number. */
    public AddGuardianCommand(GuardianName name, GuardianPhone phone) {
        this.name = name;
        this.phone = phone;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        Optional<Guardian> duplicate = model.findGuardianByPhone(phone);
        if (duplicate.isPresent()) {
            Guardian existing = duplicate.get();
            throw new CommandException(String.format(DUPLICATE_FORMAT, phone, existing.getId()));
        }
        Guardian guardian = new Guardian(name, phone);
        model.addGuardian(guardian);
        return new CommandResult(String.format(MESSAGE_SUCCESS_FORMAT, guardian.getId(), name, phone));
    }
}
