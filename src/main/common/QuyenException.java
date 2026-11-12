package common;

/**
 * Người dùng hiện tại không có quyền thực hiện thao tác. Là một {@link IllegalArgumentException} để các
 * màn hình hiện thông báo như mọi lỗi nhập liệu khác, không cần xử lý riêng.
 */
public class QuyenException extends IllegalArgumentException {
    public QuyenException(String message) {
        super(message);
    }
}
