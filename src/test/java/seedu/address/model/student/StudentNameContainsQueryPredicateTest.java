package seedu.address.model.student;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StudentNameContainsQueryPredicateTest {
    private final Student meiLin = student("S-1001", "Mei Lin");
    private final Student meiWen = student("S-1002", "Mei Wen");
    private final Student amyTan = student("S-1003", "Amy Tan");

    @Test
    void test_partialCaseInsensitiveQuery_matchesSubstringOnly() {
        StudentNameContainsQueryPredicate predicate = predicate("MEI");

        assertTrue(predicate.test(meiLin));
        assertTrue(predicate.test(meiWen));
        assertFalse(predicate.test(amyTan));
    }

    @Test
    void test_multiwordQuery_matchesOneContinuousPhrase() {
        StudentNameContainsQueryPredicate predicate = predicate("mei lin");

        assertTrue(predicate.test(meiLin));
        assertFalse(predicate.test(meiWen));
    }

    @Test
    void test_unicodeAndWhitespaceQuery_usesStudentNameNormalization() {
        StudentNameContainsQueryPredicate predicate = predicate("  Ｍｅｉ   Ｌｉｎ  ");

        assertTrue(predicate.test(meiLin));
    }

    @Test
    void equals_equivalentNormalizedQueries_areEqual() {
        StudentNameContainsQueryPredicate first = predicate("Mei Lin");
        StudentNameContainsQueryPredicate equivalent = predicate("  ＭＥＩ   ＬＩＮ ");

        assertEquals(first, equivalent);
        assertEquals(first.hashCode(), equivalent.hashCode());
        assertNotEquals(first, predicate("Mei Wen"));
        assertNotEquals(first, null);
    }

    @Test
    void nullArguments_throwNullPointerException() {
        assertThrows(NullPointerException.class, () -> new StudentNameContainsQueryPredicate(null));
        assertThrows(NullPointerException.class, () -> predicate("Mei").test(null));
    }

    private static StudentNameContainsQueryPredicate predicate(String query) {
        return new StudentNameContainsQueryPredicate(new StudentName(query));
    }

    private static Student student(String id, String name) {
        return new Student(new StudentId(id), new StudentName(name), new AcademicLevel("Sec 2"));
    }
}
