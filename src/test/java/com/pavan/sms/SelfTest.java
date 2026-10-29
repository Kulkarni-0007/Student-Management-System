package com.pavan.sms;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Dependency-free tests for the data layer. Run: see README. Exits non-zero on failure. */
public class SelfTest {
    static int passed = 0, failed = 0;

    static void check(String name, boolean cond) {
        if (cond) { passed++; System.out.println("  PASS  " + name); }
        else { failed++; System.out.println("  FAIL  " + name); }
    }

    static boolean throwsValidation(Student s, StudentDao dao) {
        try { dao.create(s); return false; } catch (ValidationException e) { return true; } catch (Exception e) { return false; }
    }

    public static void main(String[] args) throws Exception {
        Path tmp = Files.createTempFile("sms-test", ".db");
        Database db = new Database(tmp.toString());
        db.init();
        StudentDao dao = new StudentDao(db);

        Student a = dao.create(new Student(0, "1SJ23IS001", "Asha Rao", "asha@x.com", "9876543210", "ISE", 3, 8.4));
        Student b = dao.create(new Student(0, "1SJ23CS002", "Ravi Kumar", null, null, "CSE", 2, 7.1));
        dao.create(new Student(0, "1SJ23IS003", "Meera Joshi", "meera@x.com", "", "ISE", 4, 9.2));

        System.out.println("CRUD");
        check("create assigns id", a.getId() > 0 && b.getId() > a.getId());
        check("findById", "Asha Rao".equals(dao.findById(a.getId()).getName()));
        check("findById missing -> null", dao.findById(9999) == null);
        check("count == 3", dao.count() == 3);

        a.setCgpa(8.9);
        check("update returns true", dao.update(a));
        check("update persisted", dao.findById(a.getId()).getCgpa() == 8.9);

        check("delete returns true", dao.delete(b.getId()));
        check("delete again returns false", !dao.delete(b.getId()));
        check("count == 2 after delete", dao.count() == 2);

        System.out.println("Search");
        check("search by name", dao.search("meera", null, "name").size() == 1);
        check("search by roll", dao.search("IS001", null, "name").size() == 1);
        check("filter by dept", dao.search(null, "ISE", "name").size() == 2);
        check("sort by cgpa", dao.search(null, null, "cgpa").get(0).getName().equals("Asha Rao"));
        check("bad sort column falls back safely", dao.search(null, null, "name; DROP TABLE students").size() == 2);
        check("SQL injection in text is inert", dao.search("' OR '1'='1", null, "name").isEmpty());
        check("departments distinct", dao.departments().equals(List.of("ISE")));

        System.out.println("Validation & constraints");
        check("blank name rejected", throwsValidation(new Student(0, "R9", " ", null, null, "ISE", 1, 5), dao));
        check("bad email rejected", throwsValidation(new Student(0, "R9", "N", "nope", null, "ISE", 1, 5), dao));
        check("bad year rejected", throwsValidation(new Student(0, "R9", "N", null, null, "ISE", 5, 5), dao));
        check("bad cgpa rejected", throwsValidation(new Student(0, "R9", "N", null, null, "ISE", 1, 10.5), dao));
        boolean dup = false;
        try { dao.create(new Student(0, "1SJ23IS001", "Dup", null, null, "ISE", 1, 5)); }
        catch (java.sql.SQLException e) { dup = e.getMessage().contains("already exists"); }
        check("duplicate roll no gives friendly error", dup);

        System.out.println("CSV");
        check("csv escapes commas", CsvExporter.esc("a,b").equals("\"a,b\""));
        check("csv neutralises formulas", CsvExporter.esc("=1+1").startsWith("'"));

        Files.deleteIfExists(tmp);
        System.out.printf("%n%d passed, %d failed%n", passed, failed);
        if (failed > 0) System.exit(1);
    }
}
