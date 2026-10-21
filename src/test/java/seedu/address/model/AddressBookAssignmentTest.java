package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.testutil.GuardianLinkTestUtil.getUnlinkedAddressBook;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.group.ConfiguredGroups;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupAssignment;
import seedu.address.model.group.GroupCode;
import seedu.address.model.student.AcademicLevel;
import seedu.address.model.student.Student;
import seedu.address.model.student.StudentId;
import seedu.address.model.student.StudentName;

class AddressBookAssignmentTest {
    private static final Student MEI = new Student(new StudentId("S-0601"), new StudentName("Mei Lin"),
            new AcademicLevel("Sec 3"));
    private static final Student AMY = new Student(new StudentId("S-0602"), new StudentName("Amy Tan"),
            new AcademicLevel("Sec 2"));

    @Test
    void addGroupAssignment_allowsSubjectsClassesAndSharedGroups() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "mat"));
        register.addGroupAssignment(assignment(MEI, "S2-MAT-A"));
        register.addGroupAssignment(assignment(AMY, "MAT"));

        assertEquals(Set.of(assignment(MEI, "MAT"), assignment(MEI, "S2-MAT-A"), assignment(AMY, "MAT")),
                register.getGroupAssignments());
    }

    @Test
    void addGroupAssignment_invalidOrDuplicatePair_doesNotPartiallyMutate() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "MAT"));
        AddressBook before = new AddressBook(register);

        IllegalArgumentException duplicate = assertThrows(IllegalArgumentException.class, () ->
                register.addGroupAssignment(assignment(MEI, " mat ")));
        assertEquals("Student S-0601 is already assigned to MAT.", duplicate.getMessage());
        assertThrows(IllegalArgumentException.class, () -> register.addGroupAssignment(
                new GroupAssignment(new StudentId("S-0603"), new GroupCode("MAT"))));
        assertThrows(IllegalArgumentException.class, () -> register.addGroupAssignment(assignment(MEI, "UNKNOWN")));
        assertThrows(NullPointerException.class, () -> register.addGroupAssignment(null));
        assertEquals(before, register);
    }

    @Test
    void removeGroupAssignment_subjectInMixedRegister_removesOnlySpecifiedPair() {
        AddressBook register = registerWithAssignmentsAndGuardianLinks();
        Student mei = register.findStudent("S-0007").orElseThrow();
        Student amy = register.findStudent("S-0008").orElseThrow();
        AddressBook before = new AddressBook(register);
        List<Group> catalogueBefore = List.copyOf(ConfiguredGroups.getCatalogue().getGroups());
        Set<GroupAssignment> assignmentsView = register.getGroupAssignments();

        register.removeGroupAssignment(assignment(mei, " mat "));

        Set<GroupAssignment> expected = Set.of(assignment(mei, "S2-MAT-A"), assignment(amy, "MAT"));
        assertEquals(expected, assignmentsView);
        assertEquals(expected, register.getGroupAssignments());
        assertEquals(before.getStudentList(), register.getStudentList());
        assertEquals(before.getGuardianList(), register.getGuardianList());
        assertEquals(before.getGuardianLinks(), register.getGuardianLinks());
        assertSame(mei, register.findStudent("S-0007").orElseThrow());
        assertSame(amy, register.findStudent("S-0008").orElseThrow());
        assertEquals(catalogueBefore, ConfiguredGroups.getCatalogue().getGroups());
        assertThrows(UnsupportedOperationException.class, () -> assignmentsView.remove(assignment(amy, "MAT")));
        assertThrows(UnsupportedOperationException.class, assignmentsView::clear);
        assertEquals(expected, register.getGroupAssignments());
    }

    @Test
    void removeGroupAssignment_finalClassAssignment_preservesStudentAndOtherAssignments() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "S2-MAT-A"));
        register.addGroupAssignment(assignment(AMY, "MAT"));

        register.removeGroupAssignment(assignment(MEI, "s2-mat-a"));

        assertEquals(Set.of(assignment(AMY, "MAT")), register.getGroupAssignments());
        assertSame(MEI, register.findStudent(MEI.getId()).orElseThrow());
        assertSame(AMY, register.findStudent(AMY.getId()).orElseThrow());
    }

    @Test
    void removeGroupAssignment_null_preservesRegister() {
        AddressBook register = registerWithAssignmentsAndGuardianLinks();
        AddressBook before = new AddressBook(register);

        assertThrows(NullPointerException.class, () -> register.removeGroupAssignment(null));

        assertEquals(before, register);
    }

    @Test
    void removeGroupAssignment_invalidOrAbsentPair_preservesRegister() {
        AddressBook register = registerWithAssignmentsAndGuardianLinks();
        Student mei = register.findStudent("S-0007").orElseThrow();
        AddressBook before = new AddressBook(register);
        Map<GroupAssignment, String> invalidAssignments = Map.of(
                new GroupAssignment(new StudentId("S-0999"), new GroupCode("MAT")),
                "Student S-0999 does not exist.",
                assignment(mei, "unknown"),
                "Group UNKNOWN is not configured. Run list-groups to see available groups.",
                assignment(mei, "sci"),
                "Student S-0007 is not assigned to SCI.");

        for (Map.Entry<GroupAssignment, String> entry : invalidAssignments.entrySet()) {
            IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                    register.removeGroupAssignment(entry.getKey()));
            assertEquals(entry.getValue(), error.getMessage());
            assertEquals(before, register);
        }
    }

    @Test
    void removeGroupAssignment_repeatedRemoval_preservesEmptyAssignments() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "S2-MAT-A"));

        register.removeGroupAssignment(assignment(MEI, "S2-MAT-A"));

        assertTrue(register.getGroupAssignments().isEmpty());
        assertSame(MEI, register.findStudent(MEI.getId()).orElseThrow());
        AddressBook before = new AddressBook(register);
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class, () ->
                register.removeGroupAssignment(assignment(MEI, "s2-mat-a")));
        assertEquals("Student S-0601 is not assigned to S2-MAT-A.", error.getMessage());
        assertEquals(before, register);
    }

    @Test
    void removeGroupAssignment_copyAndReset_preserveIndependentSnapshots() {
        AddressBook register = registerWithAssignmentsAndGuardianLinks();
        Student mei = register.findStudent("S-0007").orElseThrow();
        Student amy = register.findStudent("S-0008").orElseThrow();
        AddressBook snapshot = new AddressBook(register);
        Set<GroupAssignment> originalAssignments = Set.copyOf(register.getGroupAssignments());
        int originalHash = snapshot.hashCode();

        register.removeGroupAssignment(assignment(mei, "MAT"));

        assertNotEquals(snapshot, register);
        assertEquals(originalAssignments, snapshot.getGroupAssignments());
        assertEquals(originalHash, snapshot.hashCode());
        register.resetData(snapshot);
        assertEquals(snapshot, register);
        assertEquals(snapshot.hashCode(), register.hashCode());
        snapshot.removeGroupAssignment(assignment(amy, "MAT"));
        assertEquals(originalAssignments, register.getGroupAssignments());
        assertNotEquals(snapshot, register);
    }

    @Test
    void copyAndReset_includeAssignmentsWithoutAliasing() {
        AddressBook source = register();
        source.addGroupAssignment(assignment(MEI, "MAT"));
        AddressBook copy = new AddressBook(source);

        assertEquals(source, copy);
        assertEquals(source.hashCode(), copy.hashCode());
        copy.addGroupAssignment(assignment(MEI, "SCI"));
        assertNotEquals(source, copy);
        assertEquals(Set.of(assignment(MEI, "MAT")), source.getGroupAssignments());
        source.resetData(source);
        assertEquals(Set.of(assignment(MEI, "MAT")), source.getGroupAssignments());
        source.resetData(copy);
        assertEquals(copy, source);
        copy.addGroupAssignment(assignment(MEI, "ENG"));
        assertNotEquals(copy, source);
        assertThrows(UnsupportedOperationException.class, () -> source.getGroupAssignments().clear());
    }

    @Test
    void equality_assignmentOrderDoesNotChangeMembershipIdentity() {
        AddressBook first = register();
        AddressBook second = register();
        first.addGroupAssignment(assignment(MEI, "MAT"));
        first.addGroupAssignment(assignment(MEI, "SCI"));
        second.addGroupAssignment(assignment(MEI, "SCI"));
        second.addGroupAssignment(assignment(MEI, "MAT"));

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
        assertNotEquals(first, register());
    }

    @Test
    void studentReplacementAndDeletion_preserveOtherRelationshipsAndCatalogue() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "S2-MAT-A"));
        register.addGroupAssignment(assignment(AMY, "MAT"));
        Student updated = new Student(MEI.getId(), MEI.getName(), new AcademicLevel("Sec 4"));
        List<Student> replacements = new ArrayList<>(List.of(updated, AMY));

        register.setStudents(replacements);
        replacements.clear();

        assertEquals(2, register.getStudentList().size());
        assertEquals(Set.of(assignment(MEI, "S2-MAT-A"), assignment(AMY, "MAT")), register.getGroupAssignments());
        assertEquals(updated, register.findStudent(MEI.getId().value).orElseThrow());
        register.deleteStudent(MEI.getId());
        assertEquals(Set.of(assignment(AMY, "MAT")), register.getGroupAssignments());
        assertEquals(List.of(AMY), register.getStudentList());
        assertEquals(5, ConfiguredGroups.getCatalogue().size());
    }

    @Test
    void resetData_invalidCandidate_preservesCompleteOriginal() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "MAT"));
        AddressBook before = new AddressBook(register);
        ObservableList<Student> incomingStudents = FXCollections.observableArrayList(MEI);
        Set<GroupAssignment> incomingAssignments = new LinkedHashSet<>(Set.of(assignment(AMY, "MAT")));
        ReadOnlyAddressBook invalid = new ReadOnlyAddressBook() {
            @Override
            public ObservableList<Student> getStudentList() {
                return incomingStudents;
            }

            @Override
            public Set<GroupAssignment> getGroupAssignments() {
                return incomingAssignments;
            }
        };

        assertThrows(IllegalArgumentException.class, () -> register.resetData(invalid));
        assertThrows(IllegalArgumentException.class, () -> register.setStudents(List.of(MEI, MEI)));
        assertEquals(before, register);
        incomingAssignments.clear();
        incomingAssignments.add(assignment(MEI, "SCI"));
        register.resetData(invalid);
        incomingStudents.clear();
        incomingAssignments.clear();
        assertEquals(List.of(MEI), register.getStudentList());
        assertEquals(Set.of(assignment(MEI, "SCI")), register.getGroupAssignments());
    }

    @Test
    void setStudents_removedIdsLoseOnlyTheirAssignments() {
        AddressBook register = register();
        register.addGroupAssignment(assignment(MEI, "MAT"));
        register.addGroupAssignment(assignment(AMY, "ENG"));

        register.setStudents(List.of(MEI));

        assertEquals(Set.of(assignment(MEI, "MAT")), register.getGroupAssignments());
        assertTrue(register.findStudent(AMY.getId().value).isEmpty());
    }

    private static AddressBook register() {
        AddressBook register = new AddressBook();
        register.addStudent(MEI);
        register.addStudent(AMY);
        return register;
    }

    private static AddressBook registerWithAssignmentsAndGuardianLinks() {
        AddressBook register = getUnlinkedAddressBook();
        Student mei = register.findStudent("S-0007").orElseThrow();
        Student amy = register.findStudent("S-0008").orElseThrow();
        register.linkGuardian("S-0007", "G-0003");
        register.linkGuardian("S-0008", "G-0003");
        register.addGroupAssignment(assignment(mei, "MAT"));
        register.addGroupAssignment(assignment(mei, "S2-MAT-A"));
        register.addGroupAssignment(assignment(amy, "MAT"));
        return register;
    }

    private static GroupAssignment assignment(Student student, String code) {
        return new GroupAssignment(student.getId(), new GroupCode(code));
    }
}
