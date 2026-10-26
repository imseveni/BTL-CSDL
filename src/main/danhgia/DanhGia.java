package danhgia;

import java.time.LocalDate;

/** Dữ liệu bất biến dùng trong module nhân sự. */
public record DanhGia(String maGhiNhan, String loaiDanhGia, String hinhThuc, String lyDo, LocalDate ngayQuyetDinh, String msv) {
}
