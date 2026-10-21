package seedu.address.model.student;

import seedu.address.model.guardian.GuardianPhone;

/** A student's independent, optional Singapore contact number. */
public final class StudentPhone {
    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must be an 8-digit Singapore number, optionally prefixed by +65.";
    private final GuardianPhone phone;

    /** Creates a canonical student phone using the shared Singapore phone rules. */
    public StudentPhone(String input) {
        phone = new GuardianPhone(input);
    }

    @Override
    public String toString() {
        return phone.toString();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof StudentPhone studentPhone && phone.equals(studentPhone.phone);
    }

    @Override
    public int hashCode() {
        return phone.hashCode();
    }
}
