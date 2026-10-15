package seedu.address.model.guardian;

import java.util.Objects;

/** Immutable guardian contact record. */
public final class Guardian {
    private final GuardianId id;
    private final GuardianName name;
    private final GuardianPhone phone;

    /** Creates a guardian with the supplied stable ID and domain values. */
    public Guardian(GuardianId id, GuardianName name, GuardianPhone phone) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.phone = Objects.requireNonNull(phone);
    }

    /** Creates a guardian with a newly generated ID. */
    public Guardian(GuardianName name, GuardianPhone phone) {
        this(GuardianId.generate(), name, phone);
    }

    public GuardianId getId() {
        return id;
    }

    public GuardianName getName() {
        return name;
    }

    public GuardianPhone getPhone() {
        return phone;
    }

    /** Returns whether this guardian has the same unique canonical phone number as {@code other}. */
    public boolean hasSameIdentityAs(Guardian other) {
        return phone.equals(other.phone);
    }

    @Override
    public String toString() {
        return id + ": " + name + " (" + phone + ")";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Guardian guardian && id.equals(guardian.id) && name.equals(guardian.name)
                && phone.equals(guardian.phone);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, phone);
    }
}
