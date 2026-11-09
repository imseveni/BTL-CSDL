package common;

import java.sql.Connection;
import java.sql.SQLException;

/** Cho phép DAO dùng MySQL khi chạy thật, CSDL riêng khi kiểm thử. */
@FunctionalInterface
public interface ConnectionProvider {

    Connection getConnection() throws SQLException;
}
