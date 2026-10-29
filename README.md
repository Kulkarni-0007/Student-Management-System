# CampusDesk — Student Management System

A polished desktop application for maintaining student academic records. Built with **Java 17+, Swing, JDBC and SQLite**. Data is stored locally, so no server or internet connection is required after the application dependencies have been downloaded.

## Highlights

- **Modern dashboard UI** with record totals, current result count, average CGPA and department count.
- **Student CRUD** — add, edit, and delete records, with a confirmation before deletion.
- **Live search** by student name, roll number or email, plus a department filter.
- **Sorting** by name, roll number, department, year or CGPA; click a table header to sort directly.
- **Validation** for required fields, email, phone, year (1–4) and CGPA (0–10).
- **SQLite persistence** with a unique roll number constraint, check constraints and useful indexes.
- **CSV export** of the currently visible/filtered records, including spreadsheet formula-injection protection and overwrite confirmation.
- **Keyboard shortcuts:** `Ctrl+N` add student, `Ctrl+F` focus search, `Delete` delete selected student.
- **Safer database access** using prepared statements and a whitelist for sort columns.
- **Dependency-free data-layer self-test** covering CRUD, search, validation and CSV safeguards.

## Requirements

- JDK 17 or later
- Maven 3.8+ (first build downloads the SQLite JDBC dependency)

## Run on Windows

1. Extract the ZIP into a folder.
2. Install a JDK 17+ and Maven if not already installed.
3. Open Command Prompt in the extracted project folder.
4. Run:

```bat
mvn clean package
java -jar target\student-management-system-1.0.0.jar
```

The database `students.db` is created in the current working directory on first launch. To use a different database file:

```bat
java -jar target\student-management-system-1.0.0.jar "D:\\College\\students.db"
```

You can also run `run-windows.bat` to package and launch the app in one step.

## Run on macOS / Linux

```bash
mvn clean package
java -jar target/student-management-system-1.0.0.jar
```

## Run the self-test

After building the executable JAR:

```bash
mkdir -p target/test-classes
javac -d target/test-classes -cp target/student-management-system-1.0.0.jar src/test/java/com/pavan/sms/SelfTest.java
java -cp target/test-classes:target/student-management-system-1.0.0.jar com.pavan.sms.SelfTest
```

On Windows Command Prompt, replace the classpath colon (`:`) with a semicolon (`;`). The self-test uses a temporary database and does not change your real `students.db`.

## Project layout

```text
src/main/java/com/pavan/sms/
  Main.java                 Application entry point
  Student.java              Student model
  Database.java             SQLite connection and schema setup
  StudentDao.java            CRUD, search, filtering and statistics
  StudentValidator.java      Input validation
  CsvExporter.java           Safe CSV export
  ValidationException.java   Validation feedback
  ui/
    MainFrame.java           Dashboard, directory and actions
    StudentDialog.java       Add/edit form
    StudentTableModel.java   Table data model
src/test/java/com/pavan/sms/SelfTest.java
```

## Database schema

`students(id, roll_no, name, email, phone, department, year, cgpa)`

- `id` is an auto-incrementing primary key.
- `roll_no` is required and unique.
- `year` is constrained to 1–4.
- `cgpa` is constrained to 0–10.
- Indexes support common name and department lookups.

## Notes

- CSV export exports the records currently shown after search/filtering, not hidden records.
- Keep a copy of `students.db` if you need to back up your student records.
- This is a local desktop application, not a multi-user web application.
