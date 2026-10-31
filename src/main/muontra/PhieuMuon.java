package muontra;

import java.time.LocalDate;

/** Dữ liệu bất biến của một phiếu mượn. */
public record PhieuMuon(String maPhieuMuon, LocalDate ngayMuon, LocalDate ngayHenTra, String mucDichMuon,
                        String trangThaiDuyet, String msvNguoiMuon) {
    public static final String CHO_DUYET = "ChoDuyet";
    public static final String DA_DUYET = "DaDuyet";
    public static final String TU_CHOI = "TuChoi";
}
