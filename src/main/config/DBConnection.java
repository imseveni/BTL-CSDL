package config;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Lấy kết nối MySQL. Đọc cấu hình từ db.properties (classpath).
 * File này do Tú quản lý – các thành viên KHÔNG sửa.
 */
public final class DBConnection {

    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = DBConnection.class.getClassLoader()
                .getResourceAsStream("db.properties")) {
            if (in == null) {
                throw new IllegalStateException(
                        "Không tìm thấy db.properties. Hãy copy db.properties.example "
                        + "thành db.properties trong src/main/resources và sửa lại.");
            }
            PROPS.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private DBConnection() {
    }

    /** Mỗi lần gọi trả về 1 Connection mới; nhớ đóng bằng try-with-resources. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                PROPS.getProperty("db.url"),
                PROPS.getProperty("db.user"),
                PROPS.getProperty("db.password"));
    }
}
