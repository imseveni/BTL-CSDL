package app;

import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

// Điểm khởi động giao diện Swing
public final class SwingApp {

    private SwingApp() {
    }

    public static void main(String[] args) {

        // Đoạn này đưa công việc tạo giao diện sang Event Dispatch Thread — EDT,
        // là luồng Swing dùng để xử lý giao diện và sự kiện.
        SwingUtilities.invokeLater(() -> {

            // Mở cửa sổ độc lập: bấm nút X thì thoát cả chương trình.
            JFrame frame = createFrame(JFrame.EXIT_ON_CLOSE, 0);
            frame.setVisible(true);
        });
    }

    /**
     * Tạo cửa sổ chính gồm 10 tab (chưa hiển thị). Phải gọi trên luồng giao diện (EDT).
     *
     * @param closeOperation JFrame.EXIT_ON_CLOSE khi chạy riêng, JFrame.DISPOSE_ON_CLOSE khi mở từ menu chính
     * @param tab            tab mở sẵn, đếm từ 0 theo thứ tự module (0 = Thành viên ... 9 = Mượn – Trả)
     */
    public static JFrame createFrame(int closeOperation, int tab) {

        // Thiết lập giao diện.
        configureLookAndFeel();

        // Tạo JFrame.
        JFrame frame = new JFrame(
                "Quản lý Câu lạc bộ");

        // Hành vi khi người dùng bấm nút X của cửa sổ chính.
        frame.setDefaultCloseOperation(closeOperation);

        // Đưa nội dung các module vào cửa sổ
        ClubWorkspace workspace = new ClubWorkspace(SwingModules.all());
        workspace.showTab(tab);
        frame.setContentPane(workspace);

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

        return frame;
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
