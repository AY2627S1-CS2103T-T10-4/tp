package seedu.address.model.student;

import java.util.Objects;

/** Immutable student record. */
public final class Student {
    private final StudentId id;
    private final StudentName name;
    private final AcademicLevel academicLevel;
    public Student(StudentId id, StudentName name, AcademicLevel academicLevel) {
        this.id = Objects.requireNonNull(id); this.name = Objects.requireNonNull(name);
        this.academicLevel = Objects.requireNonNull(academicLevel);
    }
    public Student(StudentName name, AcademicLevel academicLevel) { this(StudentId.generate(), name, academicLevel); }
    public StudentId getId() { return id; }
    public StudentName getName() { return name; }
    public AcademicLevel getAcademicLevel() { return academicLevel; }
    public boolean sameIdentity(Student other) {
        return name.equals(other.name) && academicLevel.equals(other.academicLevel);
    }
    @Override public String toString() { return id + ": " + name + " (" + academicLevel + ")"; }
    @Override
    public boolean equals(Object other) {
        return other instanceof Student student && id.equals(student.id) && name.equals(student.name)
                && academicLevel.equals(student.academicLevel);
    }
    @Override public int hashCode() { return Objects.hash(id, name, academicLevel); }
}
