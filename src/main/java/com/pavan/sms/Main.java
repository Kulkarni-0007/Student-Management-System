package com.pavan.sms;

import com.pavan.sms.ui.MainFrame;

import javax.swing.*;

public class Main {
    public static void main(String[] args) throws Exception {
        String dbPath = args.length > 0 ? args[0] : "students.db";
        Database db = new Database(dbPath);
        db.init();
        StudentDao dao = new StudentDao(db);

        SwingUtilities.invokeLater(() -> {
            try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignored) {}
            new MainFrame(dao).setVisible(true);
        });
    }
}
