package com.pavan.sms;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/** Opens SQLite connections and creates the schema on first run. */
public class Database {
    private final String url;

    public Database(String path) {
        this.url = "jdbc:sqlite:" + path;
    }

    public Connection connect() throws SQLException {
        Connection c = DriverManager.getConnection(url);
        try (Statement st = c.createStatement()) {
            st.execute("PRAGMA foreign_keys = ON");
        }
        return c;
    }

    public void init() throws SQLException {
        try (Connection c = connect(); Statement st = c.createStatement()) {
            st.execute("""
                CREATE TABLE IF NOT EXISTS students (
                    id          INTEGER PRIMARY KEY AUTOINCREMENT,
                    roll_no     TEXT    NOT NULL UNIQUE,
                    name        TEXT    NOT NULL,
                    email       TEXT,
                    phone       TEXT,
                    department  TEXT    NOT NULL,
                    year        INTEGER NOT NULL CHECK (year BETWEEN 1 AND 4),
                    cgpa        REAL    NOT NULL CHECK (cgpa BETWEEN 0 AND 10)
                )""");
            st.execute("CREATE INDEX IF NOT EXISTS idx_students_name ON students(name)");
            st.execute("CREATE INDEX IF NOT EXISTS idx_students_dept ON students(department)");
        }
    }
}
