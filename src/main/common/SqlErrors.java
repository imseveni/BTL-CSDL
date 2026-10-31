package common;

import java.sql.SQLException;

// Đổi lỗi JDBC/MySQL thành thông báo dễ đọc.
public final class SqlErrors {

    private SqlErrors() {
    }

    // Trả về thông báo tương ứng với lỗi
    public static String message(SQLException error) {

        // Kiểm tra lỗi kết nối bằng SQLState
        if (error.getSQLState() != null
                && error.getSQLState().startsWith("08")) {

            return "Không kết nối được MySQL. Kiểm tra dịch vụ MySQL, địa chỉ và cổng kết nối.";
        }

        // Chọn thông báo bằng mã lỗi MySQL
        return switch (error.getErrorCode()) {

            case 1062 -> "Mã đã tồn tại hoặc thành viên đã tham gia.";
            case 1452 -> "Mã thành viên, khóa học hoặc dự án không tồn tại.";
            case 1451 -> "Dữ liệu đang được tham chiếu. Hãy xóa các lượt tham gia trước.";
            case 1045 -> "Sai tài khoản hoặc mật khẩu MySQL. Kiểm tra cấu hình kết nối.";
            case 1049 -> "Database không tồn tại. Hãy cấu hình tên database hiện có.";

            // Xử lý những lỗi chưa được viết thông báo riêng
            default -> "Lỗi CSDL (SQLState=" + error.getSQLState() + "): " + error.getMessage();
        };
    }
}
