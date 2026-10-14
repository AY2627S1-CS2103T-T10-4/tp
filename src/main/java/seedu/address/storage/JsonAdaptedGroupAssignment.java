package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.Student;

/** JSON representation of one stable student ID and canonical group code. */
class JsonAdaptedGroupAssignment {
    private final String studentId;
    private final String groupCode;

    @JsonCreator
    JsonAdaptedGroupAssignment(
            @JsonProperty("studentId") @JsonDeserialize(using = StoredStringDeserializer.class) String studentId,
            @JsonProperty("groupCode") @JsonDeserialize(using = StoredStringDeserializer.class) String groupCode) {
        this.studentId = studentId;
        this.groupCode = groupCode;
    }

    JsonAdaptedGroupAssignment(GroupAssignment assignment) {
        studentId = assignment.getStudentId().value;
        groupCode = assignment.getGroupCode().getValue();
    }

    /**
     * Resolves an ID from already validated students without constructing an ID for a reference.
     * The owning aggregate subsequently validates catalogue membership and pair uniqueness.
     *
     * @param register Register containing the already loaded students.
     * @return An immutable relationship using the existing student's ID and a canonical code.
     * @throws IllegalValueException If a field is absent, the student is unknown or the code is blank.
     */
    GroupAssignment toModelType(AddressBook register) throws IllegalValueException {
        if (studentId == null || groupCode == null) {
            throw new IllegalValueException("Group assignment is missing a student ID or group code.");
        }
        Student student = register.findStudent(studentId).orElseThrow(() ->
                new IllegalValueException(String.format(AddressBook.MESSAGE_UNKNOWN_STUDENT, studentId)));
        try {
            return new GroupAssignment(student.getId(), new GroupCode(groupCode));
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

    @JsonAnySetter
    private void rejectUnknownProperty(String property, Object ignoredValue) {
        throw new IllegalArgumentException("Unsupported group assignment field: " + property + ".");
    }
}
