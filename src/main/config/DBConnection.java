package config;

import common.Database;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Lấy kết nối MySQL cho các module. Chỉ Tú sửa file này.
 *
 * <p>Toàn bộ nhóm dùng chung MỘT cách đọc cấu hình, nằm trong {@link Database}: biến môi trường
 * DB_URL, DB_USER, DB_PASSWORD, hoặc các file db.properties, database.properties trên classpath, hoặc
 * cấu hình nhập trong cửa sổ Swing (chỉ giữ trong phiên). Nhờ vậy module dùng console và module dùng
 * Swing luôn nối cùng một CSDL.
 */
public final class DBConnection {

    private DBConnection() {
    }

    /** Mỗi lần gọi trả về 1 Connection mới; nhớ đóng bằng try-with-resources. */
    public static Connection getConnection() throws SQLException {
        return Database.getConnection();
    }
}
