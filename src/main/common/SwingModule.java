package common;

import javax.swing.JComponent;

/**
 * Một module hiển thị thành một tab trong cửa sổ chính (tương tự {@link AppModule} của menu console).
 *
 * <p>Mỗi module có đúng một lớp {@code XxxSwingModule} nằm trong package của mình. Cửa sổ chính chỉ
 * gọi {@link #createPanel()} khi người dùng mở tab lần đầu, nên module không cần lo việc đăng ký hay
 * thứ tự tab: file đăng ký ({@code app.SwingModules}) do Tú quản lý và không phải sửa khi làm giao diện.
 */
public interface SwingModule {

    /** Tên hiển thị trên tab. */
    String getTitle();

    /**
     * Tạo giao diện của module. Được gọi một lần, trên luồng giao diện (EDT).
     * Nếu panel có nạp dữ liệu từ MySQL, hãy cho panel cài đặt {@link Refreshable}.
     */
    JComponent createPanel();
}
