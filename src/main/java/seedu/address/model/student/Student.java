package seedu.address.model.student;

import java.util.Objects;

/** Immutable student record. */
public final class Student {
    private final StudentId id;
    private final StudentName name;
    private final AcademicLevel academicLevel;
    private final StudentPhone phone;

    /** Creates a student with the supplied stable ID and domain values. */
    public Student(StudentId id, StudentName name, AcademicLevel academicLevel) {
        this(id, name, academicLevel, null);
    }

    /** Creates a student with an optional independent contact number. */
    public Student(StudentId id, StudentName name, AcademicLevel academicLevel, StudentPhone phone) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNull(name);
        this.academicLevel = Objects.requireNonNull(academicLevel);
        this.phone = phone;
    }

    /** Creates a student with a newly generated ID. */
    public Student(StudentName name, AcademicLevel academicLevel) {
        this(StudentId.generate(), name, academicLevel);
    }

    /** Creates a student with a newly generated ID and optional phone. */
    public Student(StudentName name, AcademicLevel academicLevel, StudentPhone phone) {
        this(StudentId.generate(), name, academicLevel, phone);
    }

    public StudentId getId() {
        return id;
    }

    public StudentName getName() {
        return name;
    }

    public AcademicLevel getAcademicLevel() {
        return academicLevel;
    }

    public java.util.Optional<StudentPhone> getPhone() {
        return java.util.Optional.ofNullable(phone);
    }

    /** Returns whether this student has the same normalized name and academic level as {@code other}. */
    public boolean sameIdentity(Student other) {
        return name.equals(other.name) && academicLevel.equals(other.academicLevel);
    }

    @Override
    public String toString() {
        return id + ": " + name + " (" + academicLevel + ")";
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof Student student && id.equals(student.id) && name.equals(student.name)
                && academicLevel.equals(student.academicLevel) && Objects.equals(phone, student.phone);
    }
    @Override
    public int hashCode() {
        return Objects.hash(id, name, academicLevel, phone);
    }
}
