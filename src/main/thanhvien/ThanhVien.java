package thanhvien;

import java.time.LocalDate;

/**
 * Thành viên câu lạc bộ. {@code tenBan} chỉ có khi đọc từ CSDL (view vw_ThanhVienChiTiet);
 * khi ghi xuống CSDL chỉ dùng {@code maBan}.
 */
public record ThanhVien(String msv, String hoVaTen, LocalDate ngaySinh, String gioiTinh, String email,
                        String sdt, LocalDate ngayGiaNhap, String trangThaiHD, String maBan,
                        String tenBan, String chucVu) {
}
