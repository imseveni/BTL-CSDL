package common;

/**
 * Panel nào đọc dữ liệu từ MySQL thì cài đặt interface này để cửa sổ chính biết:
 * khi nào panel đang bận (không đổi kết nối giữa chừng) và khi nào cần nạp lại (lần đầu mở tab,
 * hoặc sau khi người dùng đổi kết nối MySQL).
 */
public interface Refreshable {

    /** True khi panel đang chạy một thao tác nền với CSDL. */
    boolean isBusy();

    /** Xóa dữ liệu đang hiển thị và nạp lại từ đầu. Gọi trên luồng giao diện (EDT). */
    void resetAndReload();
}
