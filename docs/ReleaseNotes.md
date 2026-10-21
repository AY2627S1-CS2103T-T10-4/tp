---
layout: page
title: Mentora MVP Release Notes
---

# Mentora MVP Release Notes

The current Mentora MVP source build provides a keyboard-first Java desktop workflow for one tuition-centre coordinator managing students, guardians, and group assignments locally.

## Highlights

- Add students, update their academic level, permanently delete mistaken records, list all students, search by name, and open profiles using stable student IDs.
- Add and list guardian contacts, link one guardian to each student, and unlink a guardian without deleting either record or other students' links. The same guardian can be shared by siblings, and linked contact details appear in student profiles.
- Browse the five configured subject/class groups, assign multiple distinct groups to a student, and remove exactly one assignment, including the final one. Removal preserves the student, other assignments, guardian links, and catalogue.
- Use the redesigned JavaFX Students, Guardians, and Groups workspaces while keeping command entry as the primary workflow. Successful unlinking and unassignment open Students, select the target, and refresh the profile after saving.
- Save students, guardians, links, and assignments automatically. On an invalid or unreadable data file, Mentora preserves the original file and disables writes for that session.

## Supported commands

`add-student`, `edit-student`, `delete-student`, `list-students`, `find-student`, `view-student`, `add-guardian`, `list-guardians`, `link-guardian`, `unlink-guardian`, `list-groups`, `assign-group`, `unassign-group`, `help`, and `exit`.

## Known limitations

- The group catalogue is fixed to `ENG`, `MAT`, `SCI`, `CHI`, and `S2-MAT-A`.
- Editing a guardian's phone is not yet available.
- `help` opens the general help window; command-specific help is not yet available.
- Normal saves are not atomic. Keep backups of `data/addressbook.json`, especially before testing failure scenarios.
- Mentora is a single-user, local application. Attendance, grades, fees, accounting, messaging, imports, and exports are outside this MVP.

See the [User Guide](UserGuide.md) for command formats, validation rules, and recovery instructions.
