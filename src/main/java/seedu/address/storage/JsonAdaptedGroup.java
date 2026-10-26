package seedu.address.storage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.group.Group;
import seedu.address.model.group.GroupCode;
import seedu.address.model.group.GroupKind;

/** JSON representation of one configured group. */
class JsonAdaptedGroup {
    private final String code;
    private final String label;
    private final String kind;

    @JsonCreator
    JsonAdaptedGroup(
            @JsonProperty("code") @JsonDeserialize(using = StoredStringDeserializer.class) String code,
            @JsonProperty("label") @JsonDeserialize(using = StoredStringDeserializer.class) String label,
            @JsonProperty("kind") @JsonDeserialize(using = StoredStringDeserializer.class) String kind) {
        this.code = code;
        this.label = label;
        this.kind = kind;
    }

    JsonAdaptedGroup(Group group) {
        code = group.getCode().getValue();
        label = group.getLabel();
        kind = group.getKind().toDisplayString();
    }

    /** Converts stored fields into a validated group value. */
    Group toModelType() throws IllegalValueException {
        if (code == null || label == null || kind == null) {
            throw new IllegalValueException("Group is missing a code, label, or kind.");
        }
        String normalizedLabel = label.strip().replaceAll("\\s+", " ");
        if (!GroupCode.isValidNewCode(code)) {
            throw new IllegalValueException(GroupCode.MESSAGE_NEW_CODE_CONSTRAINTS);
        }
        if (!Group.isValidNewLabel(normalizedLabel)) {
            throw new IllegalValueException(Group.MESSAGE_LABEL_CONSTRAINTS);
        }
        GroupKind parsedKind = switch (kind.toLowerCase(java.util.Locale.ROOT)) {
            case "subject" -> GroupKind.SUBJECT;
            case "class" -> GroupKind.CLASS;
            default -> throw new IllegalValueException("Group kind must be Subject or Class.");
        };
        return new Group(new GroupCode(code), normalizedLabel, parsedKind);
    }

    @JsonAnySetter
    private void rejectUnknownProperty(String property, Object ignoredValue) {
        throw new IllegalArgumentException("Unsupported group field: " + property + ".");
    }
}
