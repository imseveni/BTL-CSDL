package common;

import java.util.Scanner;

/** Các hàm tiện ích đọc dữ liệu từ console (dùng chung). */
public final class ConsoleUtil {

    private ConsoleUtil() {
    }

    public static String readLine(Scanner in, String prompt) {
        System.out.print(prompt);
        return in.nextLine().trim();
    }

    public static int readInt(Scanner in, String prompt) {
        while (true) {
            String s = readLine(in, prompt);
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.println("Vui lòng nhập một số nguyên hợp lệ.");
            }
        }
    }

    public static void todo(String moduleName) {
        System.out.println("[" + moduleName + "] Chưa cài đặt – xem docs/PHAN_CONG.md");
    }
}
