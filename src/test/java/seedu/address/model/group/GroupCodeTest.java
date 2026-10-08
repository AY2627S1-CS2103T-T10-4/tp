package seedu.address.model.group;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.Assert.assertThrows;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

public class GroupCodeTest {

    @Test
    public void constructor_null_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> new GroupCode(null));
    }

    @Test
    public void constructor_blank_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new GroupCode(""));
        assertThrows(IllegalArgumentException.class, () -> new GroupCode(" "));
        assertThrows(IllegalArgumentException.class, () -> new GroupCode("\t\r\n"));
        assertThrows(IllegalArgumentException.class, () -> new GroupCode("\u2003"));
    }

    @Test
    public void constructor_mixedCase_normalizesToUppercase() {
        assertEquals("MAT", new GroupCode("mAt").getValue());
        assertEquals("S2-MAT-A", new GroupCode("s2-Mat-a").getValue());
    }

    @Test
    public void constructor_surroundingWhitespace_trimsWhitespace() {
        assertEquals("MAT", new GroupCode(" \tmat\r\n ").getValue());
        assertEquals("MAT", new GroupCode("\u2003mat\u2003").getValue());
    }

    @Test
    public void constructor_turkishDefaultLocale_normalizesIndependentlyOfLocale() {
        Locale originalLocale = Locale.getDefault();
        Locale originalDisplayLocale = Locale.getDefault(Locale.Category.DISPLAY);
        Locale originalFormatLocale = Locale.getDefault(Locale.Category.FORMAT);
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"));
            assertEquals("SCI", new GroupCode("sci").getValue());
        } finally {
            Locale.setDefault(originalLocale);
            Locale.setDefault(Locale.Category.DISPLAY, originalDisplayLocale);
            Locale.setDefault(Locale.Category.FORMAT, originalFormatLocale);
        }
    }

    @Test
    public void constructor_documentedCodes_acceptsSubjectAndClassCodes() {
        assertEquals("MAT", new GroupCode("MAT").getValue());
        assertEquals("S2-MAT-A", new GroupCode("S2-MAT-A").getValue());
    }

    @Test
    public void constructor_otherNonBlankCodes_doesNotRestrictPatternOrLength() {
        assertEquals("CUSTOM.CODE_1", new GroupCode("custom.code_1").getValue());
        assertEquals("X  Y", new GroupCode("x  y").getValue());
        assertEquals("A".repeat(256), new GroupCode("a".repeat(256)).getValue());
    }

    @Test
    public void constructor_unicodeCaseVariants_shareCanonicalIdentity() {
        String[][] variants = {
            {"\u00df", "\u1e9e", "SS"},
            {"\u212a", "k", "K"},
            {"\u0130", "i", "I"},
            {"\u0131", "I", "I"},
            {"\u03c2", "\u03c3", "\u03a3"},
            {"\ud801\udc28", "\ud801\udc00", "\ud801\udc00"}
        };
        for (String[] variant : variants) {
            GroupCode first = new GroupCode(variant[0]);
            GroupCode second = new GroupCode(variant[1]);

            assertEquals(variant[2], first.toString());
            assertEquals(variant[2], second.toString());
            assertEquals(first, second);
            assertEquals(first.hashCode(), second.hashCode());
            assertEquals(first, new GroupCode(first.toString()));
        }
    }

    @Test
    public void equals() {
        GroupCode code = new GroupCode("MAT");
        GroupCode lowercaseCode = new GroupCode("mat");
        GroupCode paddedCode = new GroupCode(" \tmAt\n ");

        assertTrue(code.equals(code));
        assertTrue(code.equals(lowercaseCode));
        assertTrue(lowercaseCode.equals(code));
        assertTrue(lowercaseCode.equals(paddedCode));
        assertTrue(code.equals(paddedCode));
        assertFalse(code.equals(new GroupCode("SCI")));
        assertFalse(code.equals(null));
        assertFalse(code.equals("MAT"));
    }

    @Test
    public void hashCode_equivalentCodes_returnsSameHashCode() {
        GroupCode code = new GroupCode("MAT");
        GroupCode equivalentCode = new GroupCode(" \tmat\n ");

        assertEquals(code.hashCode(), equivalentCode.hashCode());
    }

    @Test
    public void hashSet_equivalentCodes_storesOneEntry() {
        Set<GroupCode> codes = new HashSet<>();
        codes.add(new GroupCode("MAT"));
        codes.add(new GroupCode(" mat "));
        codes.add(new GroupCode("SCI"));

        assertEquals(2, codes.size());
        assertTrue(codes.contains(new GroupCode("mAt")));
        assertTrue(codes.remove(new GroupCode(" MAT ")));
        assertEquals(Set.of(new GroupCode("SCI")), codes);
    }

    @Test
    public void hashMap_equivalentCodes_reusesKey() {
        Map<GroupCode, String> labels = new HashMap<>();
        labels.put(new GroupCode("MAT"), "Mathematics");

        assertEquals("Mathematics", labels.get(new GroupCode(" mat ")));
        labels.put(new GroupCode("mAt"), "Updated label");

        assertEquals(1, labels.size());
        assertEquals("Updated label", labels.get(new GroupCode("MAT")));
    }

    @Test
    public void hashCollections_unicodeCaseVariants_shareKey() {
        GroupCode lowercaseCode = new GroupCode("\u00df");
        GroupCode uppercaseCode = new GroupCode("\u1e9e");
        Set<GroupCode> codes = new HashSet<>();
        codes.add(lowercaseCode);
        codes.add(uppercaseCode);

        assertEquals(1, codes.size());

        Map<GroupCode, String> labels = new HashMap<>();
        labels.put(lowercaseCode, "Example label");
        assertEquals("Example label", labels.get(uppercaseCode));
    }

    @Test
    public void toString_returnsCanonicalCode() {
        assertEquals("S2-MAT-A", new GroupCode(" s2-mat-a ").toString());
    }
}
