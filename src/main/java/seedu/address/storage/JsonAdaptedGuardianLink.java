package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;

/** JSON representation of a link, storing only the IDs of its two existing records. */
class JsonAdaptedGuardianLink {
    private final String studentId;
    private final String guardianId;

    @JsonCreator
    JsonAdaptedGuardianLink(@JsonProperty("studentId") String studentId,
            @JsonProperty("guardianId") String guardianId) {
        this.studentId = studentId;
        this.guardianId = guardianId;
    }

    /** Adds this link only after its referenced student and guardian have been loaded and validated. */
    void addTo(AddressBook register) throws IllegalValueException {
        if (studentId == null || guardianId == null) {
            throw new IllegalValueException("Guardian link is missing a required ID.");
        }
        try {
            register.linkGuardian(studentId, guardianId);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }
}
