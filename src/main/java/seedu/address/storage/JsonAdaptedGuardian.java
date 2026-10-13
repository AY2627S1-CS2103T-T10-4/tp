package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

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
    JsonAdaptedGuardian(@JsonProperty("id") String id, @JsonProperty("name") String name,
            @JsonProperty("phone") String phone) {
        this.id = id;
        this.name = name;
        this.phone = phone;
    }

    JsonAdaptedGuardian(Guardian guardian) {
        id = guardian.getId().value;
        name = guardian.getName().toString();
        phone = guardian.getPhone().toString();
    }

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
