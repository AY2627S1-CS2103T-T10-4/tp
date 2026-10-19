package seedu.address.model.guardian;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a guardian's normalized name contains a normalized query. */
public class GuardianNameContainsQueryPredicate implements Predicate<Guardian> {
    private final String normalizedQuery;

    /** Creates a predicate using the normalized form of the supplied query. */
    public GuardianNameContainsQueryPredicate(GuardianName query) {
        normalizedQuery = requireNonNull(query).getNormalized();
    }

    @Override
    public boolean test(Guardian guardian) {
        return requireNonNull(guardian).getName().getNormalized().contains(normalizedQuery);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof GuardianNameContainsQueryPredicate predicate
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
