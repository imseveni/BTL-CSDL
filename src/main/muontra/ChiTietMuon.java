package muontra;

import java.time.LocalDate;

/** Một dòng chi tiết của phiếu. ngayTraThucTe = null nghĩa là chưa trả. */
public record ChiTietMuon(String maPhieuMuon, String maThietBi, String tenThietBi, int soLuongMuon,
                          LocalDate ngayTraThucTe) {
    public boolean daTra() {
        return ngayTraThucTe != null;
    }
}
