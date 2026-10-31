package common;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class Database {
    private static volatile Properties sessionConfig;

    private Database() {
    }

    // Cung cấp một kết nối mới
    public static Connection getConnection() throws SQLException {

        Properties config = settings();

        return connect(
                config.getProperty("db.url"),
                config.getProperty("db.user"),
                config.getProperty("db.password"));
    }

    // Kiểm tra cấu hình người dùng vừa nhập
    public static void checkConnection(
            String url,
            String user,
            String password) throws SQLException {

        // Thử mở kết nối
        // Nếu sai địa chỉ hoặc tài khoản, việc mở kết nối có thể ném SQLException ngay
        // tại đây.
        try (Connection connection = connect(url, user, password)) {

            if (!connection.isValid(3))

                throw new SQLException("Kết nối MySQL không hợp lệ.");
        }
    }

    // Thực sự mở kết nối
    private static Connection connect(
            String url,
            String user,
            String password) throws SQLException {

        // Nó tạo một bộ thuộc tính mới
        Properties credentials = new Properties();

        // Rồi thêm tài khoản và mật khẩu
        credentials.setProperty("user", user);
        credentials.setProperty("password", password);

        // Đặt thời gian chờ thiết lập kết nối là 5.000 ms.
        credentials.setProperty("connectTimeout", "5000");

        // Đặt thời gian chờ đọc dữ liệu qua socket là 15.000 ms.
        credentials.setProperty("socketTimeout", "15000");

        // Yêu cầu JDBC tìm driver phù hợp với URL và mở kết nối.
        return DriverManager.getConnection(url, credentials);
    }

    /** Bản sao cấu hình; giao diện không ghi mật khẩu xuống file. */
    public static Properties settings() throws SQLException {

        // Phương thức kiểm tra cấu hình phiên trước
        Properties current = sessionConfig;

        // Nếu đã có
        if (current != null) {

            // Thì tạo một bản sao
            Properties copy = new Properties();
            copy.putAll(current);

            // Trả về bản sao giúp nơi gọi không trực tiếp thay đổi đối tượng cấu hình mà
            // lớp đang giữ.
            return copy;
        }

        Properties config = new Properties();

        // Nếu chưa có cấu hình phiên, chương trình đọc hai file theo thứ tự
        load(config, "database.properties");
        load(config, "database-local.properties");

        config.setProperty("db.url", setting(config, "DB_URL", "db.url"));
        config.setProperty("db.user", setting(config, "DB_USER", "db.user"));
        config.setProperty("db.password", setting(config, "DB_PASSWORD", "db.password"));

        return config;
    }

    // Lưu cấu hình vào bộ nhớ
    public static void configureSession(
            String url,
            String user,
            String password) {

        // Phương thức tạo một bộ cấu hình mới rồi gán vào
        Properties config = new Properties();

        config.setProperty("db.url", url);
        config.setProperty("db.user", user);
        config.setProperty("db.password", password);

        sessionConfig = config;
    }

    // Đọc file cấu hình từ classpath
    private static void load(
            Properties config,
            String resource) throws SQLException {

        try (InputStream input = Database.class.getClassLoader().getResourceAsStream(resource)) {

            if (input != null) {

                config.load(new java.io.InputStreamReader(input, java.nio.charset.StandardCharsets.UTF_8));
            }

        } catch (IOException e) {

            throw new SQLException("Không đọc được cấu hình " + resource, e);
        }
    }

    // Chọn giữa biến môi trường và file cấu hình
    private static String setting(
            Properties config,
            String envName,
            String property) {

        String value = System.getenv(envName);

        if (value == null) {

            return config.getProperty(property, "");

        } else {

            return value;
        }
    }
}
