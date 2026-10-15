---
layout: page
title: Mentora MVP Release Notes
---

# Mentora MVP Release Notes

Mentora's first MVP release provides a keyboard-first Java desktop workflow for one tuition-centre coordinator managing students, guardians, and group assignments locally.

## Highlights

- Add students, update their academic level, permanently delete mistaken records, list all students, search by name, and open profiles using stable student IDs.
- Add guardian contacts and link one guardian to each student. The same guardian can be shared by siblings, and linked contact details appear in student profiles.
- Browse the five configured subject/class groups and assign multiple distinct groups to a student.
- Use the redesigned JavaFX Students and Groups workspaces while keeping command entry as the primary workflow.
- Save students, guardians, links, and assignments automatically. On an invalid or unreadable data file, Mentora preserves the original file and disables writes for that session.

## Supported commands

`add-student`, `edit-student`, `delete-student`, `list-students`, `find-student`, `view-student`, `add-guardian`, `link-guardian`, `list-groups`, `assign-group`, `help`, and `exit`.

## Known limitations

- The group catalogue is fixed to `ENG`, `MAT`, `SCI`, `CHI`, and `S2-MAT-A`.
- Editing a guardian's phone, unlinking a guardian, and removing a group assignment are not yet available.
- `help` opens the general help window; command-specific help is not yet available.
- Normal saves are not atomic. Keep backups of `data/addressbook.json`, especially before testing failure scenarios.
- Mentora is a single-user, local application. Attendance, grades, fees, accounting, messaging, imports, and exports are outside this MVP.

See the [User Guide](UserGuide.md) for command formats, validation rules, and recovery instructions.
