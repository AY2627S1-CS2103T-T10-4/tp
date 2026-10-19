# Mentora

[![CI Status](https://github.com/AY2627S1-CS2103T-T10-4/tp/workflows/Java%20CI/badge.svg)](https://github.com/AY2627S1-CS2103T-T10-4/tp/actions)

![Ui](docs/images/Ui.png)

* **Mentora is a keyboard-first desktop application for managing student and guardian contacts at small private tuition centres in Singapore.**
  * It is designed for coordinators who manage approximately 30-200 active and prospective secondary-school students.
  * It helps coordinators keep related student and guardian information together, retrieve the right contacts quickly, and maintain accurate enrolment-related records without relying on cumbersome spreadsheets or mouse-heavy systems.
* Mentora's implemented MVP includes:
  * adding, updating, viewing, searching, and deleting student records;
  * adding guardian records;
  * linking a student to one guardian while allowing siblings to share the same guardian record;
  * browsing five configured subject or class groups and assigning them to students;
  * displaying a student's academic level, assignments, and linked guardian; and
  * saving records locally, restoring them when the application is reopened, and protecting an unreadable data file from being overwritten.
* Mentora is optimized for **fast typed commands** and clear feedback, while retaining a graphical interface for viewing student profiles and contact information.
* The MVP is intended for one coordinator using the application locally at a time. It does not manage attendance, grades, fees, payroll, accounting, lesson content, or direct messaging through WhatsApp, SMS, or email.
* This project is based on the AddressBook-Level3 project created by the [SE-EDU initiative](https://se-education.org)
* For usage instructions, see the [User Guide](docs/UserGuide.md).
* For the shipped feature summary and known limitations, see the [MVP Release Notes](docs/ReleaseNotes.md).
* For development and architecture details, see the [Developer Guide](docs/DeveloperGuide.md).
