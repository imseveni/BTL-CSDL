package app;

import java.awt.GraphicsEnvironment;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Scanner;

/**
 * Điểm khởi động của ứng dụng. Mặc định mở giao diện đồ họa Swing; thêm tham số {@code --console}
 * để dùng menu dòng lệnh (hữu ích khi làm việc từ xa hoặc không có màn hình).
 */
public class Main {

    public static void main(String[] args) {
        boolean console = Arrays.asList(args).contains("--console");
        if (!console && GraphicsEnvironment.isHeadless()) {
            // Máy không có màn hình (ví dụ máy chủ, ssh): tự chuyển sang menu dòng lệnh.
            System.out.println("Không có màn hình đồ họa, chuyển sang menu dòng lệnh.");
            console = true;
        }
        if (console) {
            runConsole();
        } else {
            SwingApp.main(args);
        }
    }

    private static void runConsole() {
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
