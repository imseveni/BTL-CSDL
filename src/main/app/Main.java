package app;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        configureConsoleEncoding();
        try (Scanner in = new Scanner(System.in, StandardCharsets.UTF_8)) {
            new MainMenu().run(in);
        }
    }

    /**
     * Ép console xuất và nhập tiếng Việt bằng UTF-8, tránh lỗi hiện dấu "?" thay cho ký tự có dấu.
     * Trên Windows, đổi luôn bảng mã của cửa sổ console sang UTF-8 (tương đương lệnh chcp 65001).
     * Nếu chạy trong IDE, vẫn cần đặt encoding của cửa sổ Run/Console là UTF-8 (xem README).
     */
    static void configureConsoleEncoding() {
        if (System.getProperty("os.name", "").toLowerCase().contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "chcp 65001 >nul").inheritIO().start().waitFor();
            } catch (Exception e) {
                // Không đổi được thì bỏ qua; người dùng có thể tự chạy chcp 65001.
            }
        }
        System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, StandardCharsets.UTF_8));
        System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true, StandardCharsets.UTF_8));
    }
}
