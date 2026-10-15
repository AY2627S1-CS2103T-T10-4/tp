package seedu.address.model.guardian;

/** A validated Singapore phone number stored in canonical {@code +65 XXXXXXXX} form. */
public final class GuardianPhone {
    public static final String MESSAGE_CONSTRAINTS =
            "Phone number must be an 8-digit Singapore number, optionally prefixed by +65.";
    private static final String VALIDATION_REGEX = "(?:[3689][0-9]{7}|\\+65 ?[3689][0-9]{7})";
    private final String value;

    /** Creates a canonical phone number from a supported local or +65-prefixed input. */
    public GuardianPhone(String input) {
        String trimmed = input == null ? "" : input.trim();
        if (!isValid(trimmed)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        String digits = trimmed.startsWith("+65") ? trimmed.substring(3).trim() : trimmed;
        value = "+65 " + digits;
    }

    /** Returns whether the input follows the Singapore phone-number rule. */
    public static boolean isValid(String value) {
        return value != null && value.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof GuardianPhone phone && value.equals(phone.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
