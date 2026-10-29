import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.sql.SQLException;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import common.Database;
import common.SwingSupport;
import common.Validation;
import duan.DuAnSwingUi;
import khoahoc.KhoaHocSwingUi;

/** Nội dung cửa sổ tách khỏi JFrame để dễ kiểm thử bằng CSDL riêng. */
// Lớp ghép các thành phần thành giao diện chính của ứng dụng.
public class ClubWorkspace extends JPanel {

    // Hai biến này giữ tham chiếu đến giao diện của hai module.
    private final KhoaHocSwingUi khoaHoc;
    private final DuAnSwingUi duAn;

    // Đây là nhãn thông báo nằm phía dưới cửa sổ.
    private final JLabel connectionStatus = new JLabel(
            "Dùng cấu hình hiện tại hoặc chọn Kết nối MySQL.");

    public ClubWorkspace(KhoaHocSwingUi khoaHoc, DuAnSwingUi duAn) {

        super(new BorderLayout());

        this.khoaHoc = khoaHoc;
        this.duAn = duAn;

        // Tạo phần đầu cửa sổ
        JPanel header = new JPanel(
                new BorderLayout(
                        18,
                        8));

        // Màu nền header
        header.setBackground(SwingSupport.ACCENT);

        // Phần khoảng trống bên trong header
        header.setBorder(BorderFactory.createEmptyBorder(
                18,
                22,
                18,
                22));

        // Tạo tiêu đề và dòng mô tả
        JPanel titles = new JPanel(
                new BorderLayout(
                        0,
                        5));

        // Yêu cầu panel không tự tô kín nền của nó.
        // Nhờ đó màu nền header có thể hiện xuyên qua,
        // tránh xuất hiện một khối nền khác màu.
        titles.setOpaque(false);

        // Tiêu đề chính
        JLabel title = new JLabel("QUẢN LÝ CÂU LẠC BỘ");

        // Tạo font cho tiêu đề chính
        title.setFont(title.getFont().deriveFont(
                Font.BOLD,
                24f));

        // Tạo màu chữ cho tiêu đề chính
        title.setForeground(Color.WHITE);

        // Tiêu đề được đặt ở trên
        titles.add(title, BorderLayout.NORTH);

        // Dòng mô tả
        JLabel subtitle = new JLabel(
                "Khóa học & Dự án  ·  Theo dõi hoạt động và thành viên tham gia");

        // Tạo màu cho dòng mô tả
        subtitle.setForeground(
                new Color(
                        221,
                        236,
                        245));

        // Dòng mô tả được đặt ở dưới
        titles.add(subtitle, BorderLayout.SOUTH);

        // Đưa khối tiêu đề vào vùng giữa của header
        header.add(titles, BorderLayout.CENTER);

        // Tạo nút “Kết nối MySQL”
        JPanel connection = new JPanel(
                new BorderLayout(
                        0,
                        5));

        // Yêu cầu panel không tự tô kín nền của nó.
        // Nhờ đó màu nền connnection có thể hiện xuyên qua,
        // tránh xuất hiện một khối nền khác màu.
        connection.setOpaque(false);

        // Nút được tạo bằng hàm hỗ trợ
        connection.add(SwingSupport.button(
                "Kết nối MySQL",
                "app.connection",
                this::configure),
                BorderLayout.NORTH);

        // Đặt panel kết nối ở bên phải header
        header.add(connection, BorderLayout.EAST);

        // Rồi đặt cả header lên trên panel chính
        add(header, BorderLayout.NORTH);

        // Tạo hai tab
        // Là thành phần hiển thị nhiều trang dưới dạng tab.
        JTabbedPane tabs = new JTabbedPane();

        // Tên component: được dùng để code kiểm thử tìm đúng bộ tab.
        // Đây không phải tên hiển thị trên giao diện.
        tabs.setName("app.tabs");

        // Font tên tab được đặt in đậm, cỡ 14
        tabs.setFont(tabs.getFont().deriveFont(Font.BOLD, 14f));

        // Hai tab được thêm vào
        tabs.addTab("  Khóa học  ", khoaHoc);
        tabs.addTab("  Dự án  ", duAn);

        // Đặt bộ tab vào vùng lớn nhất của panel chính.
        add(tabs, BorderLayout.CENTER);

        // Tạo thanh thông báo phía dưới
        JPanel footer = new JPanel(
                new BorderLayout(
                        12,
                        0));

        // Tạo khoảng đệm
        footer.setBorder(BorderFactory.createEmptyBorder(
                8,
                16,
                9,
                16));

        // Nhãn trạng thái kết nối được đặt vào vùng giữa
        connectionStatus.setName("app.connection.status");
        footer.add(connectionStatus, BorderLayout.CENTER);

        // Bên phải là hướng dẫn
        footer.add(new JLabel(
                "Chọn một dòng để xem người tham gia"),
                BorderLayout.EAST);

        // Đưa footer xuống đáy panel chính.
        add(footer, BorderLayout.SOUTH);
    }

    // Kiểm tra có được phép đổi kết nối lúc này không
    // Phương thức này được gọi khi nhấn nút kết nối.
    private void configure() {

        // Chỉ cần một trong hai module đang bận thì chương trình yêu cầu chờ,
        // tránh thay cấu hình kết nối giữa lúc một thao tác đang thực hiện.
        if (khoaHoc.isBusy() || duAn.isBusy()) {

            throw new IllegalArgumentException(
                    "Vui lòng đợi thao tác đang chạy kết thúc trước khi đổi kết nối.");
        }

        try {

            // Đọc cấu hình và tạo các ô nhập
            Properties config = Database.settings();

            // Ô URL
            JTextField url = new JTextField(
                    config.getProperty("db.url"),
                    43);

            // Ô tài khoản
            JTextField user = new JTextField(
                    config.getProperty("db.user"));

            // Ô mật khẩu
            // JPasswordField che các ký tự khi hiển thị.
            JPasswordField password = new JPasswordField(
                    config.getProperty("db.password"));

            // Các ô được ghép thành form
            // SwingSupport.form() bố trí nhãn ở cột trái và ô nhập ở cột phải.
            JPanel form = SwingSupport.form(new String[] {
                    "JDBC URL MySQL *", "Tài khoản *", "Mật khẩu" },
                    url,
                    user,
                    password);

            SwingSupport.editor(
                    this,
                    "Kiểm tra và kết nối MySQL",
                    "* Bắt buộc. Kiểm tra kết nối trước khi sử dụng; mật khẩu chỉ giữ trong phiên này.",
                    "Kết nối",
                    form,

                    () -> {

                        // Đọc và kiểm tra form
                        String address = Validation.required(
                                url.getText(),
                                "Địa chỉ kết nối");

                        if (!address.startsWith("jdbc:mysql://")) {

                            throw new IllegalArgumentException(
                                    "URL cần có dạng jdbc:mysql://localhost:3306/QuanLyCauLacBo");
                        }

                        return new Settings(
                                address,
                                Validation.required(user.getText(), "Tài khoản"),
                                new String(password.getPassword()));

                    }, settings -> {

                        // Kiểm tra và áp dụng kết nối
                        Database.checkConnection(settings.url(), settings.user(),
                                settings.password());
                        Database.configureSession(settings.url(), settings.user(),
                                settings.password());

                    }, () -> {

                        // Cập nhật giao diện sau khi thành công
                        connectionStatus.setText(
                                "Đã kiểm tra kết nối MySQL. Cấu hình chỉ dùng trong phiên này.");

                        khoaHoc.resetAndReload();
                        duAn.resetAndReload();

                    });

        } catch (SQLException e) {

            SwingSupport.error(this, e);
        }
    }

    // Đối tượng gom dữ liệu kết nối
    private record Settings(String url, String user, String password) {
    }
}
