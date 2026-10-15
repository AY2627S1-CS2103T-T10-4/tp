package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.commons.util.JsonUtil;

class StoredStringDeserializerTest {
    @Test
    void deserialize_nonStringStudentFields_rejectsScalarAndContainerCoercion() {
        assertRejectsNonStringFields(JsonAdaptedStudent.class, List.of("id", "name", "academicLevel"));
    }

    @Test
    void deserialize_nonStringAssignmentFields_rejectsScalarAndContainerCoercion() {
        assertRejectsNonStringFields(JsonAdaptedGroupAssignment.class, List.of("studentId", "groupCode"));
    }

    @Test
    void deserialize_stringThatLooksLikeBoolean_retainsValidName() throws Exception {
        JsonAdaptedStudent student = JsonUtil.fromJsonString(
                "{\"id\":\"S-0401\",\"name\":\"true\",\"academicLevel\":\"Sec 2\"}", JsonAdaptedStudent.class);
        assertEquals("true", student.toModelType().getName().toString());
    }

    private static void assertRejectsNonStringFields(Class<?> type, List<String> fields) {
        for (String field : fields) {
            for (String value : List.of("true", "false", "123", "1.5", "[]", "{}")) {
                String json = "{\"" + field + "\":" + value + "}";
                IOException error = assertThrows(IOException.class, () -> JsonUtil.fromJsonString(json, type), json);
                assertTrue(error.getMessage().contains("Expected a JSON string for " + field + "."), json);
            }
        }
    }
}
