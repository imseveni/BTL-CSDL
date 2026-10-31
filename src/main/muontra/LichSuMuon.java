package muontra;

import java.time.LocalDate;

/** Một dòng lịch sử mượn của thành viên (một thiết bị trong một phiếu); phiếu rỗng thì maThietBi = null. */
public record LichSuMuon(String maPhieuMuon, LocalDate ngayMuon, LocalDate ngayHenTra, String mucDichMuon,
                         String trangThaiDuyet, String maThietBi, String tenThietBi, int soLuongMuon,
                         LocalDate ngayTraThucTe) {
}
