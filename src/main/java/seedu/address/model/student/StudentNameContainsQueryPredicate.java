package seedu.address.model.student;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a student's normalized name contains a normalized query. */
public class StudentNameContainsQueryPredicate implements Predicate<Student> {
    private final String normalizedQuery;

    /** Creates a predicate using the normalized form of the supplied query. */
    public StudentNameContainsQueryPredicate(StudentName query) {
        normalizedQuery = requireNonNull(query).normalized();
    }

    @Override
    public boolean test(Student student) {
        return requireNonNull(student).getName().normalized().contains(normalizedQuery);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentNameContainsQueryPredicate predicate
                && normalizedQuery.equals(predicate.normalizedQuery);
    }

    @Override
    public int hashCode() {
        return normalizedQuery.hashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("normalizedQuery", normalizedQuery).toString();
    }
}
