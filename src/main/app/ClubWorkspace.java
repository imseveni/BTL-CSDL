package app;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import common.Database;
import common.Refreshable;
import common.SwingModule;
import common.SwingSupport;
import common.Validation;

/**
 * Nội dung cửa sổ chính: một tab cho mỗi {@link SwingModule}. Tab được tạo khi người dùng mở lần đầu
 * (tạo xong sẽ tự nạp dữ liệu nếu panel cài đặt {@link Refreshable}), nên mở ứng dụng không phải chờ
 * mọi tab cùng truy vấn MySQL. Tách khỏi JFrame để dễ kiểm thử.
 */
public class ClubWorkspace extends JPanel {

    private final List<SwingModule> modules;
    private final JComponent[] panels;
    private final JTabbedPane tabs = new JTabbedPane();
    private final JLabel connectionStatus = new JLabel("Dùng cấu hình hiện tại hoặc chọn Kết nối MySQL.");

    private final String nguoiDung;
    private final Runnable dangXuat;

    public ClubWorkspace(List<SwingModule> modules) {
        this(modules, null, null);
    }

    /**
     * @param nguoiDung mô tả người đang đăng nhập (hiện ở đầu cửa sổ); null nếu không qua màn hình đăng nhập
     * @param dangXuat  việc làm khi bấm Đăng xuất; null thì không hiện nút
     */
    public ClubWorkspace(List<SwingModule> modules, String nguoiDung, Runnable dangXuat) {
        super(new BorderLayout());
        this.nguoiDung = nguoiDung;
        this.dangXuat = dangXuat;
        this.modules = List.copyOf(modules);
        this.panels = new JComponent[this.modules.size()];

        add(createHeader(), BorderLayout.NORTH);

        tabs.setName("app.tabs");
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 13f));
        tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
        for (SwingModule module : this.modules) {
            tabs.addTab("  " + module.getTitle() + "  ", new JPanel());
        }
        tabs.addChangeListener(event -> ensureCreated(tabs.getSelectedIndex()));
        add(tabs, BorderLayout.CENTER);
        add(createFooter(), BorderLayout.SOUTH);

        ensureCreated(tabs.getSelectedIndex());
    }

    private JPanel createHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 8));
        header.setBackground(SwingSupport.ACCENT);
        header.setBorder(BorderFactory.createEmptyBorder(14, 22, 14, 22));

        JPanel titles = new JPanel(new BorderLayout(0, 4));
        titles.setOpaque(false);
        JLabel title = new JLabel("QUẢN LÝ CÂU LẠC BỘ");
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        title.setForeground(Color.WHITE);
        titles.add(title, BorderLayout.NORTH);
        JLabel subtitle = new JLabel(modules.stream().map(SwingModule::getTitle).collect(Collectors.joining("  ·  ")));
        subtitle.setForeground(new Color(221, 236, 245));
        titles.add(subtitle, BorderLayout.SOUTH);
        header.add(titles, BorderLayout.CENTER);

        JPanel connection = new JPanel(new BorderLayout(0, 5));
        connection.setOpaque(false);
        if (nguoiDung != null) {
            JLabel who = new JLabel(nguoiDung);
            who.setName("app.nguoidung");
            who.setForeground(Color.WHITE);
            connection.add(who, BorderLayout.NORTH);
        }
        JPanel buttons = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT, 6, 0));
        buttons.setOpaque(false);
        buttons.add(SwingSupport.button("Kết nối MySQL", "app.connection", this::configure));
        if (dangXuat != null) {
            buttons.add(SwingSupport.button("Đăng xuất", "app.dangxuat", dangXuat));
        }
        connection.add(buttons, BorderLayout.SOUTH);
        header.add(connection, BorderLayout.EAST);
        return header;
    }

    private JPanel createFooter() {
        JPanel footer = new JPanel(new BorderLayout(12, 0));
        footer.setBorder(BorderFactory.createEmptyBorder(8, 16, 9, 16));
        connectionStatus.setName("app.connection.status");
        footer.add(connectionStatus, BorderLayout.CENTER);
        footer.add(new JLabel("Chọn một dòng trong bảng để xem chi tiết"), BorderLayout.EAST);
        return footer;
    }

    /** Chọn tab đang hiển thị theo thứ tự module: theo thứ tự trong danh sách module của vai trò đang đăng nhập. */
    public void showTab(int index) {
        if (index < 0 || index >= panels.length) {
            throw new IllegalArgumentException("Không có tab số " + index + ".");
        }
        tabs.setSelectedIndex(index);
        ensureCreated(index);
    }

    /** Panel của tab đã được tạo, hoặc null nếu người dùng chưa mở tab đó. Dùng cho kiểm thử. */
    public JComponent panelAt(int index) {
        return panels[index];
    }

    private void ensureCreated(int index) {
        if (index < 0 || index >= panels.length || panels[index] != null) {
            return;
        }
        JComponent panel;
        try {
            panel = modules.get(index).createPanel();
        } catch (RuntimeException | LinkageError e) {
            // Một module lỗi không được làm hỏng cả cửa sổ: hiện lỗi ngay trong tab của nó.
            panel = errorPanel(modules.get(index).getTitle(), e);
        }
        panels[index] = panel;
        tabs.setComponentAt(index, panel);
        if (panel instanceof Refreshable refreshable) {
            refreshable.resetAndReload();
        }
    }

    private static JComponent errorPanel(String title, Throwable error) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        JLabel label = new JLabel("<html><b>Không mở được module " + title + ".</b><br>" + error + "</html>");
        label.setForeground(new Color(150, 40, 40));
        panel.add(label, BorderLayout.NORTH);
        return panel;
    }

    private void configure() {
        for (JComponent panel : panels) {
            if (panel instanceof Refreshable refreshable && refreshable.isBusy()) {
                throw new IllegalArgumentException(
                        "Vui lòng đợi thao tác đang chạy kết thúc trước khi đổi kết nối.");
            }
        }
        try {
            hopThoaiKetNoi(this, () -> {
                connectionStatus.setText("Đã kiểm tra kết nối MySQL. Cấu hình chỉ dùng trong phiên này.");
                // Nạp lại mọi tab đã mở; tab chưa mở sẽ dùng cấu hình mới khi được mở lần đầu.
                for (JComponent panel : panels) {
                    if (panel instanceof Refreshable refreshable) {
                        refreshable.resetAndReload();
                    }
                }
            });
        } catch (SQLException e) {
            SwingSupport.error(this, e);
        }
    }

    /** Hộp thoại kiểm tra và đặt kết nối MySQL cho phiên hiện tại; dùng cả ở màn hình đăng nhập. */
    public static void hopThoaiKetNoi(java.awt.Component owner, Runnable onConnected) throws SQLException {
        Properties config = Database.settings();
        JTextField url = new JTextField(config.getProperty("db.url"), 43);
        JTextField user = new JTextField(config.getProperty("db.user"));
        JPasswordField password = new JPasswordField(config.getProperty("db.password"));
        JPanel form = SwingSupport.form(new String[] { "JDBC URL MySQL *", "Tài khoản *", "Mật khẩu" },
                url, user, password);
        SwingSupport.editor(owner, "Kiểm tra và kết nối MySQL",
                "* Bắt buộc. Kiểm tra kết nối trước khi sử dụng; mật khẩu chỉ giữ trong phiên này.",
                "Kết nối", form,
                () -> {
                    String address = Validation.required(url.getText(), "Địa chỉ kết nối");
                    if (!address.startsWith("jdbc:mysql://")) {
                        throw new IllegalArgumentException(
                                "URL cần có dạng jdbc:mysql://localhost:3306/QuanLyCauLacBo");
                    }
                    return new Settings(address, Validation.required(user.getText(), "Tài khoản"),
                            new String(password.getPassword()));
                },
                settings -> {
                    Database.checkConnection(settings.url(), settings.user(), settings.password());
                    Database.configureSession(settings.url(), settings.user(), settings.password());
                },
                onConnected);
    }

    private record Settings(String url, String user, String password) {
    }
}
