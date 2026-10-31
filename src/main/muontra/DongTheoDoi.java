package muontra;

import java.time.LocalDate;

/**
 * Một dòng của vw_ThietBiChuaTra hoặc vw_ThietBiTraMuon.
 * tinhTrang: DangMuon, QuaHan hoặc TraMuon; ngayTraThucTe = null nếu chưa trả.
 */
public record DongTheoDoi(String maPhieuMuon, String msvNguoiMuon, String hoVaTen, String maThietBi,
                          String tenThietBi, int soLuongMuon, LocalDate ngayMuon, LocalDate ngayHenTra,
                          LocalDate ngayTraThucTe, int soNgayTre, String tinhTrang) {
}
