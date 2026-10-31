package thanhvien;

import java.time.LocalDate;

/** Một dòng trong lịch sử hoạt động của thành viên (khóa học, dự án, sự kiện, đánh giá, phiếu mượn, thu chi). */
public record HoatDong(String loai, String ma, String ten, String chiTiet, LocalDate ngay) {
}
