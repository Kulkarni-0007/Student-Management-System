package com.pavan.sms.ui;

import com.pavan.sms.CsvExporter;
import com.pavan.sms.Student;
import com.pavan.sms.StudentDao;
import com.pavan.sms.ValidationException;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Main dashboard for the Student Management System. */
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private static final Color NAVY = new Color(25, 43, 75);
    private static final Color BLUE = new Color(54, 103, 196);
    private static final Color BG = new Color(245, 247, 251);
    private static final Color MUTED = new Color(105, 117, 139);
    private static final String ALL = "All departments";
    private static final String[] SORT_LABELS = {"Name", "Roll No", "Department", "Year", "CGPA"};
    private static final String[] SORT_COLS = {"name", "roll_no", "department", "year", "cgpa"};

    private final StudentDao dao;
    private final StudentTableModel model = new StudentTableModel();
    private final JTable table = new JTable(model);
    private final JTextField searchField = new JTextField(22);
    private final JComboBox<String> deptFilter = new JComboBox<>();
    private final JComboBox<String> sortBox = new JComboBox<>(SORT_LABELS);
    private final JLabel status = new JLabel("Ready");
    private final JLabel totalValue = new JLabel("0");
    private final JLabel shownValue = new JLabel("0");
    private final JLabel averageValue = new JLabel("0.00");
    private final JLabel departmentValue = new JLabel("0");
    private final JLabel emptyLabel = new JLabel("No students found. Add a student or change your search.", SwingConstants.CENTER);
    private final Timer debounce;

    public MainFrame(StudentDao dao) {
        super("CampusDesk | Student Management System");
        this.dao = dao;
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(900, 600));
        setSize(1180, 760);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        debounce = new Timer(250, e -> refresh());
        debounce.setRepeats(false);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildStatusBar(), BorderLayout.SOUTH);

        configureTable();
        reloadDepartments();
        refresh();
        installShortcuts();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setBackground(NAVY);
        header.setBorder(new EmptyBorder(22, 28, 22, 28));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("CampusDesk");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 27));
        JLabel subtitle = new JLabel("STUDENT MANAGEMENT  /  ACADEMIC RECORDS");
        subtitle.setForeground(new Color(194, 207, 231));
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setBorder(new EmptyBorder(5, 1, 0, 0));
        brand.add(title);
        brand.add(subtitle);

        JButton add = actionButton("+  Add student", BLUE, Color.WHITE);
        add.setPreferredSize(new Dimension(155, 42));
        add.addActionListener(e -> onAdd());
        header.add(brand, BorderLayout.WEST);
        header.add(add, BorderLayout.EAST);
        return header;
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setBackground(BG);
        content.setBorder(new EmptyBorder(22, 26, 18, 26));
        content.add(buildWelcome(), BorderLayout.NORTH);
        content.add(buildDirectory(), BorderLayout.CENTER);
        return content;
    }

    private JComponent buildWelcome() {
        JPanel panel = new JPanel(new BorderLayout(0, 16));
        panel.setOpaque(false);
        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel h = new JLabel("Overview");
        h.setFont(new Font("SansSerif", Font.BOLD, 21));
        h.setForeground(NAVY);
        JLabel desc = new JLabel("Manage student profiles, academic details and records in one place.");
        desc.setForeground(MUTED);
        desc.setFont(new Font("SansSerif", Font.PLAIN, 13));
        JPanel texts = new JPanel();
        texts.setOpaque(false);
        texts.setLayout(new BoxLayout(texts, BoxLayout.Y_AXIS));
        texts.add(h); texts.add(Box.createVerticalStrut(5)); texts.add(desc);
        heading.add(texts, BorderLayout.WEST);
        panel.add(heading, BorderLayout.NORTH);

        JPanel cards = new JPanel(new GridLayout(1, 4, 14, 0));
        cards.setOpaque(false);
        cards.add(statCard("TOTAL STUDENTS", totalValue, "All saved records", new Color(54, 103, 196)));
        cards.add(statCard("CURRENT VIEW", shownValue, "Matching students", new Color(16, 145, 125)));
        cards.add(statCard("AVERAGE CGPA", averageValue, "Out of 10.00", new Color(142, 93, 201)));
        cards.add(statCard("DEPARTMENTS", departmentValue, "Unique departments", new Color(211, 133, 44)));
        panel.add(cards, BorderLayout.CENTER);
        return panel;
    }

    private JPanel statCard(String label, JLabel value, String hint, Color accent) {
        JPanel card = new JPanel(new BorderLayout(0, 9));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, accent),
                new EmptyBorder(15, 17, 14, 15)));
        JLabel name = new JLabel(label);
        name.setFont(new Font("SansSerif", Font.BOLD, 10));
        name.setForeground(MUTED);
        value.setFont(new Font("SansSerif", Font.BOLD, 27));
        value.setForeground(NAVY);
        JLabel foot = new JLabel(hint);
        foot.setFont(new Font("SansSerif", Font.PLAIN, 11));
        foot.setForeground(MUTED);
        card.add(name, BorderLayout.NORTH);
        card.add(value, BorderLayout.CENTER);
        card.add(foot, BorderLayout.SOUTH);
        return card;
    }

    private JComponent buildDirectory() {
        JPanel directory = new JPanel(new BorderLayout(0, 0));
        directory.setBackground(Color.WHITE);
        directory.setBorder(BorderFactory.createLineBorder(new Color(225, 230, 239)));

        JPanel top = new JPanel(new BorderLayout(10, 12));
        top.setBackground(Color.WHITE);
        top.setBorder(new EmptyBorder(17, 18, 15, 18));
        JPanel labels = new JPanel();
        labels.setOpaque(false);
        labels.setLayout(new BoxLayout(labels, BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Student directory");
        title.setFont(new Font("SansSerif", Font.BOLD, 17));
        title.setForeground(NAVY);
        JLabel note = new JLabel("Search, filter, edit or export student data.");
        note.setFont(new Font("SansSerif", Font.PLAIN, 12));
        note.setForeground(MUTED);
        labels.add(title); labels.add(Box.createVerticalStrut(4)); labels.add(note);
        top.add(labels, BorderLayout.WEST);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        controls.setOpaque(false);
        searchField.setToolTipText("Search name, roll number or email");
        searchField.setFont(new Font("SansSerif", Font.PLAIN, 13));
        searchField.setPreferredSize(new Dimension(210, 34));
        styleCombo(deptFilter);
        deptFilter.setPreferredSize(new Dimension(170, 34));
        styleCombo(sortBox);
        sortBox.setPreferredSize(new Dimension(105, 34));
        controls.add(searchField); controls.add(deptFilter); controls.add(sortBox);
        JButton export = actionButton("Export CSV", new Color(236, 241, 250), NAVY);
        export.setPreferredSize(new Dimension(105, 34));
        export.addActionListener(e -> onExport());
        controls.add(export);
        JButton edit = actionButton("Edit", new Color(236, 241, 250), NAVY);
        edit.setPreferredSize(new Dimension(68, 34));
        edit.addActionListener(e -> onEdit());
        controls.add(edit);
        JButton delete = actionButton("Delete", new Color(255, 237, 237), new Color(170, 48, 48));
        delete.setPreferredSize(new Dimension(75, 34));
        delete.addActionListener(e -> onDelete());
        controls.add(delete);
        top.add(controls, BorderLayout.SOUTH);
        directory.add(top, BorderLayout.NORTH);

        table.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 233, 241)));
        scroll.getViewport().setBackground(Color.WHITE);
        directory.add(scroll, BorderLayout.CENTER);
        emptyLabel.setOpaque(true);
        emptyLabel.setBackground(Color.WHITE);
        emptyLabel.setForeground(MUTED);
        emptyLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
        directory.add(emptyLabel, BorderLayout.SOUTH);
        emptyLabel.setVisible(false);

        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { debounce.restart(); }
            public void removeUpdate(DocumentEvent e) { debounce.restart(); }
            public void changedUpdate(DocumentEvent e) { debounce.restart(); }
        });
        deptFilter.addActionListener(e -> refresh());
        sortBox.addActionListener(e -> refresh());
        return directory;
    }

    private JComponent buildStatusBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(Color.WHITE);
        bar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(225, 230, 239)),
                new EmptyBorder(8, 27, 8, 27)));
        status.setFont(new Font("SansSerif", Font.PLAIN, 11));
        status.setForeground(MUTED);
        JLabel version = new JLabel("CAMPUSDESK  •  LOCAL SQLITE DATABASE");
        version.setFont(new Font("SansSerif", Font.BOLD, 10));
        version.setForeground(MUTED);
        bar.add(status, BorderLayout.WEST);
        bar.add(version, BorderLayout.EAST);
        return bar;
    }

    private void configureTable() {
        table.setAutoCreateRowSorter(true);
        table.setRowHeight(34);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setForeground(new Color(44, 55, 75));
        table.setBackground(Color.WHITE);
        table.setSelectionBackground(new Color(226, 237, 255));
        table.setSelectionForeground(NAVY);
        table.setGridColor(new Color(237, 240, 246));
        table.setShowVerticalLines(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setForeground(NAVY);
        table.getTableHeader().setBackground(new Color(248, 250, 253));
        table.getTableHeader().setPreferredSize(new Dimension(0, 38));
        table.getTableHeader().setReorderingAllowed(false);
        DefaultTableCellRenderer center = new DefaultTableCellRenderer();
        center.setHorizontalAlignment(SwingConstants.CENTER);
        table.getColumnModel().getColumn(5).setCellRenderer(center);
        DefaultTableCellRenderer cgpa = new DefaultTableCellRenderer() {
            @Override public Component getTableCellRendererComponent(JTable t, Object v, boolean selected,
                    boolean focus, int row, int column) {
                Component c = super.getTableCellRendererComponent(t, v, selected, focus, row, column);
                setHorizontalAlignment(SwingConstants.CENTER);
                if (!selected) setForeground(v instanceof Number && ((Number) v).doubleValue() >= 8.0
                        ? new Color(16, 130, 96) : new Color(44, 55, 75));
                setText(v instanceof Number ? String.format("%.2f", ((Number) v).doubleValue()) : String.valueOf(v));
                return c;
            }
        };
        table.getColumnModel().getColumn(6).setCellRenderer(cgpa);
        table.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && SwingUtilities.isLeftMouseButton(e)) onEdit();
            }
        });
    }

    private void installShortcuts() {
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control N"), "addStudent");
        getRootPane().getActionMap().put("addStudent", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { onAdd(); } });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("DELETE"), "deleteStudent");
        getRootPane().getActionMap().put("deleteStudent", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { onDelete(); } });
        getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("control F"), "focusSearch");
        getRootPane().getActionMap().put("focusSearch", new AbstractAction() { public void actionPerformed(java.awt.event.ActionEvent e) { searchField.requestFocusInWindow(); searchField.selectAll(); } });
    }

    private static void styleCombo(JComboBox<String> combo) {
        combo.setFont(new Font("SansSerif", Font.PLAIN, 12));
        combo.setBackground(Color.WHITE);
        combo.setForeground(NAVY);
    }

    private static JButton actionButton(String text, Color bg, Color fg) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(fg);
        button.setBackground(bg);
        button.setFocusPainted(false);
        button.setBorder(new EmptyBorder(9, 13, 9, 13));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return button;
    }

    private void reloadDepartments() {
        try {
            Object selected = deptFilter.getSelectedItem();
            java.awt.event.ActionListener[] listeners = deptFilter.getActionListeners();
            for (var l : listeners) deptFilter.removeActionListener(l);
            deptFilter.removeAllItems();
            deptFilter.addItem(ALL);
            for (String d : dao.departments()) deptFilter.addItem(d);
            if (selected != null) deptFilter.setSelectedItem(selected);
            for (var l : listeners) deptFilter.addActionListener(l);
        } catch (SQLException e) { showError(e); }
    }

    private void refresh() {
        try {
            String dept = (String) deptFilter.getSelectedItem();
            if (ALL.equals(dept)) dept = null;
            int sortIndex = Math.max(0, sortBox.getSelectedIndex());
            List<Student> rows = dao.search(searchField.getText(), dept, SORT_COLS[sortIndex]);
            model.setRows(new ArrayList<>(rows));
            int total = dao.count();
            totalValue.setText(String.valueOf(total));
            shownValue.setText(String.valueOf(rows.size()));
            averageValue.setText(String.format("%.2f", dao.averageCgpa()));
            departmentValue.setText(String.valueOf(dao.departments().size()));
            emptyLabel.setVisible(rows.isEmpty());
            status.setText(String.format("Showing %d of %d students", rows.size(), total));
        } catch (SQLException e) { showError(e); }
    }

    private Student selected() {
        int viewRow = table.getSelectedRow();
        if (viewRow < 0) {
            JOptionPane.showMessageDialog(this, "Select a student first.", "No student selected", JOptionPane.INFORMATION_MESSAGE);
            return null;
        }
        return model.getAt(table.convertRowIndexToModel(viewRow));
    }

    private void onAdd() {
        Student s = StudentDialog.showDialog(this, null);
        if (s == null) return;
        try { dao.create(s); reloadDepartments(); refresh(); status.setText("Student " + s.getRollNo() + " added successfully."); }
        catch (SQLException | ValidationException e) { showError(e); }
    }

    private void onEdit() {
        Student cur = selected();
        if (cur == null) return;
        Student s = StudentDialog.showDialog(this, cur);
        if (s == null) return;
        try { dao.update(s); reloadDepartments(); refresh(); status.setText("Student " + s.getRollNo() + " updated successfully."); }
        catch (SQLException | ValidationException e) { showError(e); }
    }

    private void onDelete() {
        Student cur = selected();
        if (cur == null) return;
        int ok = JOptionPane.showConfirmDialog(this,
                "Delete " + cur.getName() + " (" + cur.getRollNo() + ")?\nThis action cannot be undone.",
                "Confirm deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (ok != JOptionPane.YES_OPTION) return;
        try { dao.delete(cur.getId()); reloadDepartments(); refresh(); status.setText("Student " + cur.getRollNo() + " deleted."); }
        catch (SQLException e) { showError(e); }
    }

    private void onExport() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Export visible students to CSV");
        fc.setSelectedFile(new File("students.csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        File file = fc.getSelectedFile();
        if (file.exists() && JOptionPane.showConfirmDialog(this, "Overwrite " + file.getName() + "?", "Confirm export",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) return;
        try {
            CsvExporter.export(model.getRows(), file.toPath());
            status.setText("Exported " + model.getRowCount() + " visible students to " + file.getName());
            JOptionPane.showMessageDialog(this, "CSV export completed.\n" + file.getAbsolutePath(), "Export successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) { showError(e); }
    }

    private void showError(Exception e) {
        String message = e.getMessage() == null || e.getMessage().isBlank() ? "An unexpected error occurred." : e.getMessage();
        JOptionPane.showMessageDialog(this, message, "Something went wrong", JOptionPane.ERROR_MESSAGE);
    }
}
