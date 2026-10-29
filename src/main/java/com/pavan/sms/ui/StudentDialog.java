package com.pavan.sms.ui;

import com.pavan.sms.Student;
import com.pavan.sms.StudentValidator;
import com.pavan.sms.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

/** Accessible modal add/edit form. Returns null when cancelled. */
class StudentDialog extends JDialog {
    private static final long serialVersionUID = 1L;
    private static final Color NAVY = new Color(25, 43, 75);
    private static final Color BLUE = new Color(54, 103, 196);
    private final JTextField roll = new JTextField(22);
    private final JTextField name = new JTextField(22);
    private final JTextField email = new JTextField(22);
    private final JTextField phone = new JTextField(22);
    private final JComboBox<String> dept = new JComboBox<>(new String[]{
            "Information Science & Engineering", "Computer Science & Engineering",
            "Electronics & Communication", "Mechanical Engineering", "Civil Engineering"});
    private final JSpinner year = new JSpinner(new SpinnerNumberModel(1, 1, 4, 1));
    private final JSpinner cgpa = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 10.0, 0.01));
    private Student result;
    private final int editId;

    private StudentDialog(Frame owner, Student existing) {
        super(owner, existing == null ? "Add student" : "Edit student", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(Color.WHITE);
        editId = existing == null ? 0 : existing.getId();
        dept.setEditable(true);
        if (existing != null) {
            roll.setText(existing.getRollNo()); name.setText(existing.getName());
            email.setText(existing.getEmail()); phone.setText(existing.getPhone());
            dept.setSelectedItem(existing.getDepartment()); year.setValue(existing.getYear()); cgpa.setValue(existing.getCgpa());
        }
        cgpa.setEditor(new JSpinner.NumberEditor(cgpa, "0.00"));
        styleField(roll); styleField(name); styleField(email); styleField(phone);
        styleCombo(dept); styleSpinner(year); styleSpinner(cgpa);

        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(Color.WHITE);
        outer.setBorder(new EmptyBorder(22, 24, 18, 24));
        JPanel intro = new JPanel(); intro.setOpaque(false); intro.setLayout(new BoxLayout(intro, BoxLayout.Y_AXIS));
        JLabel title = new JLabel(existing == null ? "Add a student" : "Update student details");
        title.setFont(new Font("SansSerif", Font.BOLD, 21)); title.setForeground(NAVY);
        JLabel sub = new JLabel("Fields marked * are required.");
        sub.setFont(new Font("SansSerif", Font.PLAIN, 12)); sub.setForeground(new Color(105, 117, 139));
        intro.add(title); intro.add(Box.createVerticalStrut(5)); intro.add(sub);
        outer.add(intro, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(7, 4, 7, 4); g.anchor = GridBagConstraints.WEST; g.fill = GridBagConstraints.HORIZONTAL;
        String[] labels = {"Roll number *", "Full name *", "Email address", "Phone number", "Department *", "Year (1–4) *", "CGPA (0–10) *"};
        JComponent[] fields = {roll, name, email, phone, dept, year, cgpa};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0;
            JLabel label = new JLabel(labels[i]); label.setFont(new Font("SansSerif", Font.BOLD, 12)); label.setForeground(NAVY);
            form.add(label, g);
            g.gridx = 1; g.weightx = 1; form.add(fields[i], g);
        }
        outer.add(form, BorderLayout.CENTER);

        JButton cancel = new JButton("Cancel");
        cancel.setFont(new Font("SansSerif", Font.BOLD, 12)); cancel.setFocusPainted(false);
        JButton save = new JButton(existing == null ? "Save student" : "Save changes");
        save.setFont(new Font("SansSerif", Font.BOLD, 12)); save.setForeground(Color.WHITE); save.setBackground(BLUE);
        save.setFocusPainted(false); save.setBorder(new EmptyBorder(10, 16, 10, 16));
        save.addActionListener(e -> onSave()); cancel.addActionListener(e -> dispose());
        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0)); buttons.setOpaque(false);
        buttons.add(cancel); buttons.add(save);
        outer.add(buttons, BorderLayout.SOUTH);
        setContentPane(outer); getRootPane().setDefaultButton(save);
        pack(); setMinimumSize(new Dimension(490, getHeight())); setLocationRelativeTo(owner);
    }

    private static void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 13)); field.setPreferredSize(new Dimension(260, 34));
    }
    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 13)); combo.setPreferredSize(new Dimension(260, 34));
    }
    private static void styleSpinner(JSpinner spinner) {
        spinner.setFont(new Font("SansSerif", Font.PLAIN, 13)); spinner.setPreferredSize(new Dimension(260, 34));
    }

    private void onSave() {
        Student s = new Student(editId, roll.getText(), name.getText(), email.getText(), phone.getText(),
                String.valueOf(dept.getSelectedItem()), ((Number) year.getValue()).intValue(), ((Number) cgpa.getValue()).doubleValue());
        try { StudentValidator.validate(s); }
        catch (ValidationException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Check the entered details", JOptionPane.WARNING_MESSAGE);
            return;
        }
        result = s; dispose();
    }

    static Student showDialog(Frame owner, Student existing) {
        StudentDialog dialog = new StudentDialog(owner, existing);
        dialog.setVisible(true);
        return dialog.result;
    }
}
