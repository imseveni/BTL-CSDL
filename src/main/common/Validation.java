package common;

import java.math.BigDecimal;
import java.time.LocalDate;

// Tập hợp các hàm kiểm tra và chuẩn hóa dữ liệu trước khi ghi xuống database.
public final class Validation {

    private Validation() {
    }

    // Kiểm tra chuỗi bắt buộc và bỏ khoảng trắng đầu/cuối
    public static String required(
            String value,
            String label) {

        if (value == null || value.isBlank()) {

            throw new IllegalArgumentException(
                    label + " không được để trống.");
        }

        return value.trim();
    }

    // Xử lý chuỗi không bắt buộc
    public static String optional(String value) {

        if (value == null || value.isBlank()) {

            return null;

        } else {

            return value.trim();
        }
    }

    // Kiểm tra bắt buộc và có thêm độ dài
    public static String required(
            String value,
            String label,
            int maxLength) {

        String normalized = required(value, label);

        maxLength(normalized, label, maxLength);

        return normalized;
    }

    // Xử lý chuỗi không bắt buộc có thêm độ dài
    public static String optional(
            String value,
            String label,
            int maxLength) {

        String normalized = optional(value);

        if (normalized != null) {

            maxLength(normalized, label, maxLength);
        }

        return normalized;
    }

    // Kiểm tra chuỗi có quá dài không
    private static void maxLength(
            String value,
            String label,
            int maxLength) {

        // Đếm số Unicode code point trong toàn bộ chuỗi.
        if (value.codePointCount(0, value.length()) > maxLength) {

            throw new IllegalArgumentException(
                    label + " không được dài quá "
                            + maxLength + " ký tự.");
        }
    }

    // Kiểm tra ngày bắt buộc
    public static void requiredDate(
            LocalDate value,
            String label) {

        if (value == null) {

            throw new IllegalArgumentException(
                    label + " không được để trống.");
        }
    }

    // Kiểm tra thứ tự hai ngày
    public static void dates(
            LocalDate start,
            LocalDate end) {

        if (start != null
                && end != null
                && end.isBefore(start)) {

            throw new IllegalArgumentException(
                    "Ngày kết thúc không được trước ngày bắt đầu.");
        }
    }

    // Kiểm tra tiến độ từ 0 đến 100
    public static void progress(BigDecimal value) {

        if (value != null
                && (value.signum() < 0
                        || value.compareTo(BigDecimal.valueOf(100)) > 0)) {

            throw new IllegalArgumentException(
                    "Tiến độ phải nằm trong khoảng 0 đến 100.");
        }
    }
}
