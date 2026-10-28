package seedu.address.model.guardian;

import static java.util.Objects.requireNonNull;

import java.text.Normalizer;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/** Tests whether a guardian's normalized name contains a normalized query. */
public class GuardianNameContainsQueryPredicate implements Predicate<Guardian> {
    private final String normalizedQuery;

    /** Creates a predicate using the normalized form of the supplied query. */
    public GuardianNameContainsQueryPredicate(GuardianName query) {
        normalizedQuery = normalizeForSearch(requireNonNull(query));
    }

    @Override
    public boolean test(Guardian guardian) {
        return normalizeForSearch(requireNonNull(guardian).getName()).contains(normalizedQuery);
    }

    /** Folds each Unicode code point independently to avoid context-dependent casing, such as final sigma. */
    private static String normalizeForSearch(GuardianName name) {
        StringBuilder foldedName = new StringBuilder();
        Normalizer.normalize(name.toString(), Normalizer.Form.NFKC).codePoints()
                .map(Character::toUpperCase)
                .map(Character::toLowerCase)
                .forEach(foldedName::appendCodePoint);
        return foldedName.toString();
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
