package com.pavan.sms.ui;

import com.pavan.sms.Student;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

class StudentTableModel extends AbstractTableModel {
    private static final long serialVersionUID = 1L;
    private static final String[] COLS = {"Roll No", "Name", "Email", "Phone", "Department", "Year", "CGPA"};
    private List<Student> rows = new ArrayList<>();

    void setRows(List<Student> rows) { this.rows = rows; fireTableDataChanged(); }
    Student getAt(int row) { return rows.get(row); }
    List<Student> getRows() { return rows; }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return COLS.length; }
    @Override public String getColumnName(int c) { return COLS[c]; }

    @Override public Class<?> getColumnClass(int c) {
        return switch (c) { case 5 -> Integer.class; case 6 -> Double.class; default -> String.class; };
    }

    @Override public Object getValueAt(int r, int c) {
        Student s = rows.get(r);
        return switch (c) {
            case 0 -> s.getRollNo();
            case 1 -> s.getName();
            case 2 -> s.getEmail() == null ? "" : s.getEmail();
            case 3 -> s.getPhone() == null ? "" : s.getPhone();
            case 4 -> s.getDepartment();
            case 5 -> s.getYear();
            case 6 -> s.getCgpa();
            default -> "";
        };
    }
}
