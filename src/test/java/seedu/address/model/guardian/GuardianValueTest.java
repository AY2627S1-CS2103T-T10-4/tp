package seedu.address.model.guardian;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GuardianValueTest {
    @Test
    void guardianName_normalizesAndValidates() {
        GuardianName name = new GuardianName("  Anita   Lim  ");

        assertEquals("Anita Lim", name.toString());
        assertEquals("anita lim", name.getNormalized());
        assertEquals(name, new GuardianName("anita lim"));
        assertEquals(name.hashCode(), new GuardianName("ANITA LIM").hashCode());
        assertEquals(0, name.compareTo(new GuardianName("ANITA LIM")));
        assertNotEquals(name, new GuardianName("Raj Kumar"));
        assertNotEquals(name, "Anita Lim");
        assertTrue(GuardianName.isValid("O'Connor"));
        assertTrue(GuardianName.isValid("S. Kumar"));
        assertTrue(GuardianName.isValid("李明"));
        assertFalse(GuardianName.isValid(null));
        assertFalse(GuardianName.isValid(""));
        assertFalse(GuardianName.isValid("2Anita"));
        assertFalse(GuardianName.isValid("Anita!"));
        assertFalse(GuardianName.isValid("A".repeat(81)));
        Executable nullName = () -> new GuardianName(null);
        Executable invalidName = () -> new GuardianName("-Anita");
        assertThrows(IllegalArgumentException.class, GuardianName.MESSAGE_CONSTRAINTS, nullName);
        assertThrows(IllegalArgumentException.class, GuardianName.MESSAGE_CONSTRAINTS, invalidName);
    }

    @Test
    void guardianPhone_canonicalizesAndValidatesSingaporeNumbers() {
        GuardianPhone localPhone = new GuardianPhone(" 91234567 ");

        assertEquals("+65 91234567", localPhone.toString());
        assertEquals(localPhone, new GuardianPhone("+6591234567"));
        assertEquals(localPhone, new GuardianPhone("+65 91234567"));
        assertEquals(localPhone.hashCode(), new GuardianPhone("91234567").hashCode());
        assertNotEquals(localPhone, new GuardianPhone("81234567"));
        assertNotEquals(localPhone, "+65 91234567");
        assertTrue(GuardianPhone.isValid("31234567"));
        assertTrue(GuardianPhone.isValid("61234567"));
        assertTrue(GuardianPhone.isValid("81234567"));
        assertTrue(GuardianPhone.isValid("91234567"));
        assertTrue(GuardianPhone.isValid("+6591234567"));
        assertTrue(GuardianPhone.isValid("+65 91234567"));
        assertFalse(GuardianPhone.isValid(null));
        assertFalse(GuardianPhone.isValid("51234567"));
        assertFalse(GuardianPhone.isValid("9123456"));
        assertFalse(GuardianPhone.isValid("+65  91234567"));
        assertFalse(GuardianPhone.isValid("9123-4567"));
        Executable nullPhone = () -> new GuardianPhone(null);
        Executable invalidPhone = () -> new GuardianPhone("(65) 91234567");
        assertThrows(IllegalArgumentException.class, GuardianPhone.MESSAGE_CONSTRAINTS, nullPhone);
        assertThrows(IllegalArgumentException.class, GuardianPhone.MESSAGE_CONSTRAINTS, invalidPhone);
    }

    @Test
    void guardianId_validatesComparesAndGeneratesPastExistingId() {
        GuardianId laterId = new GuardianId("G-900000");
        GuardianId earlierId = new GuardianId("G-0007");

        assertTrue(earlierId.compareTo(laterId) < 0);
        assertEquals("G-0007", earlierId.toString());
        assertEquals(earlierId, new GuardianId("G-0007"));
        assertEquals(earlierId.hashCode(), new GuardianId("G-0007").hashCode());
        assertNotEquals(earlierId, laterId);
        assertNotEquals(earlierId, "G-0007");
        assertThrows(IllegalArgumentException.class, () -> new GuardianId(null));
        assertThrows(IllegalArgumentException.class, () -> new GuardianId("S-0001"));
        assertThrows(IllegalArgumentException.class, () -> new GuardianId("G-123"));
        assertTrue(GuardianId.generate().compareTo(laterId) > 0);
    }

    @Test
    void guardian_identityEqualityAndNullChecks() {
        Guardian first = guardian("G-9101", "Anita Lim", "91234567");
        Guardian sameValues = guardian("G-9101", "anita lim", "+65 91234567");
        Guardian samePhone = guardian("G-9102", "Different Name", "91234567");
        Guardian sameIdDifferentName = guardian("G-9101", "Different Name", "91234567");
        Guardian differentPhone = guardian("G-9101", "Anita Lim", "81234567");

        assertTrue(first.hasSameIdentityAs(samePhone));
        assertFalse(first.hasSameIdentityAs(differentPhone));
        assertEquals(first, sameValues);
        assertEquals(first.hashCode(), sameValues.hashCode());
        assertNotEquals(first, samePhone);
        assertNotEquals(first, sameIdDifferentName);
        assertNotEquals(first, differentPhone);
        assertNotEquals(first, null);
        assertEquals("G-9101: Anita Lim (+65 91234567)", first.toString());
        assertEquals(new GuardianId("G-9101"), first.getId());
        assertEquals(new GuardianName("Anita Lim"), first.getName());
        assertEquals(new GuardianPhone("91234567"), first.getPhone());
        assertTrue(new Guardian(new GuardianName("Raj Kumar"), new GuardianPhone("61234567"))
                .getId().toString().startsWith("G-"));
        Executable nullId = () ->
                new Guardian(null, new GuardianName("Anita Lim"), new GuardianPhone("91234567"));
        Executable nullName = () ->
                new Guardian(new GuardianId("G-9201"), null, new GuardianPhone("91234567"));
        Executable nullPhone = () ->
                new Guardian(new GuardianId("G-9202"), new GuardianName("Anita Lim"), null);
        assertThrows(NullPointerException.class, nullId);
        assertThrows(NullPointerException.class, nullName);
        assertThrows(NullPointerException.class, nullPhone);
    }

    private static Guardian guardian(String id, String name, String phone) {
        return new Guardian(new GuardianId(id), new GuardianName(name), new GuardianPhone(phone));
    }
}
