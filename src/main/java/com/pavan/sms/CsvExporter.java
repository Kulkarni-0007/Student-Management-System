package com.pavan.sms;

import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public final class CsvExporter {
    private CsvExporter() {}

    public static void export(List<Student> students, Path file) throws IOException {
        try (Writer w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write("Roll No,Name,Email,Phone,Department,Year,CGPA\n");
            for (Student s : students) {
                w.write(String.join(",",
                        esc(s.getRollNo()), esc(s.getName()), esc(s.getEmail()), esc(s.getPhone()),
                        esc(s.getDepartment()), String.valueOf(s.getYear()),
                        String.format(Locale.US, "%.2f", s.getCgpa())));
                w.write("\n");
            }
        }
    }

    static String esc(String v) {
        if (v == null) return "";
        // Neutralise spreadsheet formula injection
        if (!v.isEmpty() && "=+-@".indexOf(v.charAt(0)) >= 0) v = "'" + v;
        if (v.contains(",") || v.contains("\"") || v.contains("\n"))
            return "\"" + v.replace("\"", "\"\"") + "\"";
        return v;
    }
}
