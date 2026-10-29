import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import duan.DuAnModule;
import khoahoc.KhoaHocModule;

// Điểm khởi động giao diện Swing
public final class SwingApp {

    private SwingApp() {
    }

    public static void main(String[] args) {

        // Đoạn này đưa công việc tạo giao diện sang Event Dispatch Thread — EDT,
        // là luồng Swing dùng để xử lý giao diện và sự kiện.
        SwingUtilities.invokeLater(() -> {

            // Thiết lập giao diện.
            configureLookAndFeel();

            // Tạo JFrame.
            JFrame frame = new JFrame(
                    "Quản lý Câu lạc bộ — Khóa học & Dự án");

            // Dòng này quy định rằng khi người dùng bấm nút X của cửa sổ chính,
            // chương trình sẽ kết thúc.
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

            // Đưa nội dung hai module vào cửa sổ
            frame.setContentPane(
                    new ClubWorkspace(
                            KhoaHocModule.createSwingUi(),
                            DuAnModule.createSwingUi()));

            // Đặt kích thước, vị trí và hiển thị cửa sổ
            // Giới hạn kích thước nhỏ nhất
            frame.setMinimumSize(
                    new Dimension(
                            1080,
                            720));

            // Đặt kích thước khi cửa sổ được mở:
            frame.setSize(1220, 800);

            // Đưa cửa sổ ra giữa màn hình theo cách định vị của Swing.
            frame.setLocationRelativeTo(null);

            // Hiển thị cửa sổ.
            frame.setVisible(true);
        });
    }

    // Phương thức chọn kiểu giao diện
    public static void configureLookAndFeel() {

        try {

            // Lấy danh sách các kiểu giao diện
            // mà môi trường Java cung cấp rồi duyệt từng kiểu.
            for (UIManager.LookAndFeelInfo theme : UIManager.getInstalledLookAndFeels()) {

                // Với mỗi kiểu, chương trình kiểm tra:
                // Nếu tên là Nimbus thì chạy:
                if ("Nimbus".equals(theme.getName())) {

                    UIManager.setLookAndFeel(theme.getClassName());
                    break;
                }
            }

        } catch (ReflectiveOperationException | javax.swing.UnsupportedLookAndFeelException ignored) {
            // Vẫn mở được ứng dụng với giao diện mặc định của JDK.
        }

        // Thiết lập font chữ và màu
        FontUIResource font = new FontUIResource(
                "Segoe UI",
                Font.PLAIN,
                13);

        // Thay một màu trong cấu hình Nimbus bằng màu có ba thành phần RGB:
        UIManager.put("nimbusOrange",
                new java.awt.Color(
                        55,
                        135,
                        164));

        // Áp dụng font cơ sở cho các thiết lập font của Swing
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();

        // 1. Kiểm tra còn thuộc tính nào chưa đọc không.
        while (keys.hasMoreElements()) {

            // 2. Lấy tên thuộc tính tiếp theo.
            Object key = keys.nextElement();

            // 3. Đọc giá trị của thuộc tính đó.
            // 4. Nếu giá trị là font kiểu FontUIResource, thay bằng font vừa tạo.
            if (UIManager.get(key) instanceof FontUIResource)

                UIManager.put(key, font);
        }
    }
}
