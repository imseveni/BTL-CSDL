package common;

import java.util.Scanner;

/**
 * Mỗi nhóm chức năng (module) cài đặt interface này.
 * MainMenu chỉ làm việc với interface, nên các module phát triển độc lập.
 */
public interface AppModule {

    /** Tên hiển thị trong menu chính. */
    String getName();

    /** Chạy menu con của module; thoát khi người dùng chọn quay lại. */
    void run(Scanner in);
}
