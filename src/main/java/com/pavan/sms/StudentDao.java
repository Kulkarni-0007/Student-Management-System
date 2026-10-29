package com.pavan.sms;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/** CRUD + search for students. All SQL uses prepared statements. */
public class StudentDao {
    private final Database db;

    public StudentDao(Database db) { this.db = db; }

    // ---- Create ----
    public Student create(Student s) throws SQLException, ValidationException {
        StudentValidator.validate(s);
        String sql = "INSERT INTO students (roll_no, name, email, phone, department, year, cgpa) VALUES (?,?,?,?,?,?,?)";
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.executeUpdate();
            try (Statement st = c.createStatement();
                 ResultSet keys = st.executeQuery("SELECT last_insert_rowid()")) {
                if (keys.next()) s.setId(keys.getInt(1));
            }
            return s;
        } catch (SQLException e) {
            throw translate(e, s);
        }
    }

    // ---- Read ----
    public Student findById(int id) throws SQLException {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Student> findAll() throws SQLException {
        return search(null, null, "name");
    }

    /**
     * Search by free text (matches name, roll no, email) and optional department.
     * @param sortBy one of: name, roll_no, department, year, cgpa
     */
    public List<Student> search(String text, String department, String sortBy) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM students WHERE 1=1");
        List<Object> args = new ArrayList<>();
        if (text != null && !text.isBlank()) {
            sql.append(" AND (name LIKE ? OR roll_no LIKE ? OR email LIKE ?)");
            String like = "%" + text.trim() + "%";
            args.add(like); args.add(like); args.add(like);
        }
        if (department != null && !department.isBlank()) {
            sql.append(" AND department = ?");
            args.add(department);
        }
        sql.append(" ORDER BY ").append(safeSort(sortBy));

        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < args.size(); i++) ps.setObject(i + 1, args.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                List<Student> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    public List<String> departments() throws SQLException {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT DISTINCT department FROM students ORDER BY department")) {
            List<String> out = new ArrayList<>();
            while (rs.next()) out.add(rs.getString(1));
            return out;
        }
    }

    // ---- Update ----
    public boolean update(Student s) throws SQLException, ValidationException {
        StudentValidator.validate(s);
        String sql = "UPDATE students SET roll_no=?, name=?, email=?, phone=?, department=?, year=?, cgpa=? WHERE id=?";
        try (Connection c = db.connect(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, s);
            ps.setInt(8, s.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw translate(e, s);
        }
    }

    // ---- Delete ----
    public boolean delete(int id) throws SQLException {
        try (Connection c = db.connect();
             PreparedStatement ps = c.prepareStatement("DELETE FROM students WHERE id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // ---- Stats ----
    public int count() throws SQLException {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM students")) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    public double averageCgpa() throws SQLException {
        try (Connection c = db.connect();
             Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT AVG(cgpa) FROM students")) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    // ---- helpers ----
    private static void bind(PreparedStatement ps, Student s) throws SQLException {
        ps.setString(1, s.getRollNo().trim());
        ps.setString(2, s.getName().trim());
        ps.setString(3, blankToNull(s.getEmail()));
        ps.setString(4, blankToNull(s.getPhone()));
        ps.setString(5, s.getDepartment().trim());
        ps.setInt(6, s.getYear());
        ps.setDouble(7, s.getCgpa());
    }

    private static Student map(ResultSet rs) throws SQLException {
        return new Student(rs.getInt("id"), rs.getString("roll_no"), rs.getString("name"),
                rs.getString("email"), rs.getString("phone"), rs.getString("department"),
                rs.getInt("year"), rs.getDouble("cgpa"));
    }

    private static String safeSort(String col) {
        if (col == null) return "name";
        return switch (col) {
            case "roll_no", "department", "year", "cgpa", "name" -> col;
            default -> "name";
        };
    }

    private static String blankToNull(String v) { return v == null || v.isBlank() ? null : v.trim(); }

    private static SQLException translate(SQLException e, Student s) {
        String msg = e.getMessage() == null ? "" : e.getMessage();
        if (msg.contains("UNIQUE") && msg.contains("roll_no"))
            return new SQLException("Roll number '" + s.getRollNo() + "' already exists.", e);
        return e;
    }
}
