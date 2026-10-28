package seedu.address.model.guardian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class GuardianNameContainsQueryPredicateTest {
    @Test
    void test_normalizedSubstring_matchesCaseSpacingAndUnicode() {
        Guardian guardian = guardian("Anita   Lim");
        assertTrue(predicate("NITA").test(guardian));
        assertTrue(predicate("  Anita   Lim ").test(guardian));
        assertTrue(predicate("ＡＮＩＴＡ").test(guardian));
        assertTrue(predicate("ta Li").test(guardian));
        assertFalse(predicate("Lim Anita").test(guardian));
        assertFalse(predicate("Raj").test(guardian));
        assertTrue(predicate("O'Con").test(guardian("Mary O'Connor")));
    }

    @Test
    void test_unicodeCaseVariants_matchesWholeNamesAndSubstrings() {
        for (String name : new String[]{"ΟΣ", "οσ", "ος"}) {
            Guardian guardian = guardian(name);
            for (String query : new String[]{"ΟΣ", "οσ", "ος", "Σ", "σ", "ς"}) {
                assertTrue(predicate(query).test(guardian), name + " should match " + query);
            }
        }
        assertTrue(predicate("i").test(guardian("İpek")));
        assertTrue(predicate("İ").test(guardian("Ipek")));
        assertTrue(predicate("ß").test(guardian("ẞ")));
        assertFalse(predicate("οτ").test(guardian("ΟΣ")));
        assertFalse(predicate("e").test(guardian("É")));
    }

    @Test
    void equals_unicodeCaseVariants_comparesSearchMeaning() {
        GuardianNameContainsQueryPredicate uppercase = predicate("ΟΣ");
        GuardianNameContainsQueryPredicate lowercase = predicate("οσ");
        GuardianNameContainsQueryPredicate finalSigma = predicate("ος");
        assertEquals(uppercase, lowercase);
        assertEquals(lowercase, finalSigma);
        assertEquals(uppercase.hashCode(), lowercase.hashCode());
        assertEquals(lowercase.hashCode(), finalSigma.hashCode());
    }

    @Test
    void equals_normalizedQueries_comparesSearchMeaning() {
        GuardianNameContainsQueryPredicate predicate = predicate("Anita Lim");
        GuardianNameContainsQueryPredicate equivalent = predicate(" anita   LIM ");
        assertEquals(predicate, equivalent);
        assertEquals(predicate.hashCode(), equivalent.hashCode());
        assertFalse(predicate.equals(predicate("Anita")));
        assertFalse(predicate.equals(null));
        assertThrows(NullPointerException.class, () -> new GuardianNameContainsQueryPredicate(null));
        assertThrows(NullPointerException.class, () -> predicate.test(null));
    }

    private static GuardianNameContainsQueryPredicate predicate(String query) {
        return new GuardianNameContainsQueryPredicate(new GuardianName(query));
    }

    private static Guardian guardian(String name) {
        return new Guardian(new GuardianId("G-0003"), new GuardianName(name), new GuardianPhone("91234567"));
    }
}
