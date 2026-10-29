package common;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;

// Chứa các hàm hỗ trợ chuyển đổi ngày giữa Java và JDBC, 
// đồng thời tạo mẫu tìm kiếm chuỗi con cho SQL LIKE
public final class JdbcValues {

    private JdbcValues() {
    }

    // Đưa ngày Java vào câu SQL
    public static void date(
            PreparedStatement statement,
            int index,
            LocalDate value) throws SQLException {

        // Nếu chưa có ngày
        if (value == null) {

            // Dòng này yêu cầu truyền SQL NULL cho tham số đó.
            // Types.DATE cho JDBC biết tham số đang được đặt NULL có kiểu SQL là DATE.
            statement.setNull(index, Types.DATE);

            // Nếu có ngày
        } else {

            // Chuyển LocalDate thành java.sql.Date,
            // phù hợp với cách code đang sử dụng setDate()
            statement.setDate(index, Date.valueOf(value));
        }
    }

    // Đọc ngày từ kết quả SQL
    public static LocalDate date(
            ResultSet result,
            String column) throws SQLException {

        // Lấy giá trị ngày của cột trên dòng hiện tại của ResultSet.
        Date value = result.getDate(column);

        if (value == null) {

            return null;

        } else {

            return value.toLocalDate();
        }
    }

    // Tạo mẫu tìm kiếm chuỗi con
    // Tìm chuỗi con theo nghĩa đen: %, _ và ! không trở thành ký tự đại diện.
    public static String contains(String keyword) {

        return "%"
                + keyword
                        .replace("!", "!!")
                        .replace("%", "!%")
                        .replace("_", "!_")
                + "%";
    }
}
