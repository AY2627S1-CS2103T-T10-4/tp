package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.guardian.Guardian;
import seedu.address.model.guardian.GuardianId;
import seedu.address.model.guardian.GuardianName;
import seedu.address.model.guardian.GuardianPhone;

/** Jackson representation of a guardian. */
class JsonAdaptedGuardian {
    private final String id;
    private final String name;
    private final String phone;

    @JsonCreator
    JsonAdaptedGuardian(
            @JsonProperty("id") @JsonDeserialize(using = StoredStringDeserializer.class) String id,
            @JsonProperty("name") @JsonDeserialize(using = StoredStringDeserializer.class) String name,
            @JsonProperty("phone") @JsonDeserialize(using = StoredStringDeserializer.class) String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    JsonAdaptedGuardian(Guardian guardian) {
        id = guardian.getId().toString();
        name = guardian.getName().toString();
        phone = guardian.getPhone().toString();
    }

    /** Rejects unsupported stored fields before they could be lost on the next successful save. */
    @JsonAnySetter
    private void rejectUnknownProperty(String property, Object ignoredValue) {
        throw new IllegalArgumentException("Unsupported guardian field: " + property + ".");
    }

    /**
     * Converts required guardian fields using their existing validation and ID-loading policy.
     *
     * @return A validated guardian record.
     * @throws IllegalValueException If a field is absent or violates a domain constraint.
     */
    Guardian toModelType() throws IllegalValueException {
        try {
            if (id == null || name == null || phone == null) {
                throw new IllegalArgumentException("Guardian record is missing a required field.");
            }
            return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
