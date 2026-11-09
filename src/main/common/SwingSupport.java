package common;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

/** Các tiện ích Swing nhỏ dùng chung cho hai module. */
public final class SwingSupport {
    public static final Color ACCENT = new Color(23, 83, 114);
    public static final Color BACKGROUND = new Color(245, 247, 250);

    private SwingSupport() {
    }

    public static JButton button(String text, String name, Runnable action) {
        JButton button = new JButton(text);
        button.setName(name);
        button.addActionListener(event -> {
            try {
                action.run();
            } catch (IllegalArgumentException e) {
                error(button, e);
            }
        });
        return button;
    }

    public static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static JTable table(DefaultTableModel model, String name) {
        JTable table = new JTable(model);
        table.setName(name);
        table.setRowHeight(31);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setAutoCreateRowSorter(true);
        table.setFillsViewportHeight(true);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 1));
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setPreferredSize(new Dimension(100, 34));
        table.getTableHeader().setFont(table.getFont().deriveFont(Font.BOLD));
        table.setSelectionBackground(new Color(217, 234, 244));
        table.setSelectionForeground(Color.BLACK);
        return table;
    }

    public static JScrollPane scroll(JComponent component) {
        JScrollPane scroll = new JScrollPane(component);
        if (component instanceof JTable table)
            scroll.setColumnHeaderView(table.getTableHeader());
        scroll.setBorder(BorderFactory.createLineBorder(new Color(215, 222, 231)));
        return scroll;
    }

    public static String text(Object value) {
        return value == null ? "—" : value.toString();
    }

    public static LocalDate date(JTextField field, String label, boolean required) {
        String value = field.getText().trim();
        if (value.isEmpty()) {
            if (required)
                throw new IllegalArgumentException(label + " không được để trống.");
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException(label + " cần có dạng yyyy-MM-dd và là ngày hợp lệ.");
        }
    }

    public static BigDecimal progress(JTextField field) {
        String value = field.getText().trim();
        if (value.isEmpty())
            return null;
        try {
            BigDecimal number = new BigDecimal(value);
            Validation.progress(number);
            if (number.stripTrailingZeros().scale() > 2) {
                throw new IllegalArgumentException("Tiến độ chỉ nhập tối đa 2 chữ số thập phân.");
            }
            return number;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Tiến độ cần là số từ 0 đến 100, dùng dấu chấm thập phân.");
        }
    }

    public static void error(Component parent, Throwable error) {
        String message = error instanceof SQLException sql ? SqlErrors.message(sql) : error.getMessage();
        JOptionPane.showMessageDialog(parent, message == null ? "Không thực hiện được thao tác." : message,
                "Không thể thực hiện", JOptionPane.ERROR_MESSAGE);
    }

    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Xác nhận xóa",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION;
    }

    public static JPanel form(String[] labels, JComponent... fields) {
        if (labels.length != fields.length)
            throw new IllegalArgumentException("Sai số trường của form.");
        JPanel form = new JPanel(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.insets = new Insets(6, 4, 6, 4);
        constraints.anchor = GridBagConstraints.NORTHWEST;
        for (int index = 0; index < labels.length; index++) {
            constraints.gridy = index;
            constraints.gridx = 0;
            constraints.weightx = 0;
            constraints.fill = GridBagConstraints.NONE;
            JLabel label = new JLabel(labels[index]);
            label.setLabelFor(fields[index]);
            form.add(label, constraints);
            constraints.gridx = 1;
            constraints.weightx = 1;
            constraints.fill = GridBagConstraints.HORIZONTAL;
            form.add(fields[index], constraints);
        }
        return form;
    }

    @FunctionalInterface
    public interface SaveAction<T> {
        void save(T value) throws Exception;
    }

    /** Đọc form trên EDT; ghi CSDL trong SwingWorker. Lỗi lưu giữ nguyên form. */
    public static <T> void editor(Component owner, String title, JPanel form,
            Supplier<T> read, SaveAction<T> save, Runnable onSaved) {
        editor(owner, title, "* Bắt buộc. Ngày: yyyy-MM-dd. Ô tùy chọn để trống = chưa có.",
                "Lưu", form, read, save, onSaved);
    }

    public static <T> void editor(Component owner, String title, String note, String saveText, JPanel form,
            Supplier<T> read, SaveAction<T> save, Runnable onSaved) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(owner), title,
                java.awt.Dialog.ModalityType.APPLICATION_MODAL);
        JPanel content = new JPanel(new BorderLayout(8, 8));
        content.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));
        content.add(new JLabel(note), BorderLayout.NORTH);
        content.add(form, BorderLayout.CENTER);
        JPanel bottom = new JPanel(new BorderLayout());
        JLabel status = new JLabel(" ");
        bottom.add(status, BorderLayout.CENTER);
        JPanel buttons = new JPanel();
        Tasks tasks = new Tasks(content, status);
        JButton saveButton = button(saveText, "editor.save", () -> {
            try {
                T value = read.get();
                tasks.run("Đang lưu...", () -> {
                    save.save(value);
                    return value;
                }, saved -> {
                    dialog.dispose();
                    onSaved.run();
                });
            } catch (IllegalArgumentException e) {
                error(dialog, e);
            }
        });
        buttons.add(saveButton);
        buttons.add(button("Hủy", "editor.cancel", dialog::dispose));
        bottom.add(buttons, BorderLayout.EAST);
        content.add(bottom, BorderLayout.SOUTH);
        dialog.setContentPane(content);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent event) {
                if (!tasks.isBusy())
                    dialog.dispose();
            }
        });
        dialog.getRootPane().setDefaultButton(saveButton);
        dialog.setMinimumSize(new Dimension(630, 280));
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setVisible(true);
    }

    /**
     * Mỗi panel chỉ chạy một việc tại một thời điểm, khóa điều khiển khi đang chờ.
     */
    public static final class Tasks {
        private final JComponent root;
        private final JLabel status;
        private boolean busy;

        public Tasks(JComponent root, JLabel status) {
            this.root = root;
            this.status = status;
        }

        public boolean isBusy() {
            return busy;
        }

        public <T> void run(String message, Callable<T> work, Consumer<T> success) {
            if (busy)
                return;
            busy = true;
            status.setText(message);
            // Ghi lại trạng thái cũ để không mở khóa các ô mã đang chỉ đọc.
            java.util.Map<Component, Boolean> states = new java.util.IdentityHashMap<>();
            disable(root, states);
            root.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            new SwingWorker<T, Void>() {
                @Override
                protected T doInBackground() throws Exception {
                    return work.call();
                }

                @Override
                protected void done() {
                    states.forEach(Component::setEnabled);
                    root.setCursor(Cursor.getDefaultCursor());
                    busy = false;
                    try {
                        T result = get();
                        status.setText("Hoàn tất.");
                        success.accept(result);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        status.setText("Đã ngắt thao tác.");
                    } catch (ExecutionException e) {
                        status.setText("Chưa thực hiện được. Kiểm tra dữ liệu hoặc kết nối MySQL.");
                        error(root, e.getCause());
                    }
                }
            }.execute();
        }

        private void disable(Component component, java.util.Map<Component, Boolean> states) {
            if (component instanceof JButton || component instanceof javax.swing.text.JTextComponent
                    || component instanceof JTable || component instanceof javax.swing.table.JTableHeader) {
                states.put(component, component.isEnabled());
                component.setEnabled(false);
            }
            if (component instanceof Container container) {
                for (Component child : container.getComponents())
                    disable(child, states);
            }
        }
    }
}
