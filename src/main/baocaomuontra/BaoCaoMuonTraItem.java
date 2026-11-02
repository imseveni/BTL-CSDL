package baocaomuontra;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Một dòng giao dịch mượn – trả cơ sở vật chất trong bảng chi tiết báo cáo.
 */
public record BaoCaoMuonTraItem(
        String maPhieuMuon,
        String maThietBi,
        String tenThietBi,
        String msvNguoiMuon,
        String tenNguoiMuon,
        int soLuongMuon,
        LocalDate ngayMuon,
        LocalDate ngayHenTra,
        LocalDate ngayTraThucTe,
        String mucDichMuon,
        String trangThai,
        int soNgayQuaHan) {

    public static final String DA_TRA = "Đã trả";
    public static final String TRA_QUA_HAN = "Trả quá hạn";
    public static final String CHUA_TRA = "Chưa trả";
    public static final String QUA_HAN = "Quá hạn";

    /**
     * Khởi tạo một dòng báo cáo và tự động xác định trạng thái mượn – trả dựa trên ngày mốc.
     */
    public static BaoCaoMuonTraItem of(
            String maPhieuMuon,
            String maThietBi,
            String tenThietBi,
            String msvNguoiMuon,
            String tenNguoiMuon,
            int soLuongMuon,
            LocalDate ngayMuon,
            LocalDate ngayHenTra,
            LocalDate ngayTraThucTe,
            String mucDichMuon,
            LocalDate ngayMoc) {
        LocalDate moc = ngayMoc == null ? LocalDate.now() : ngayMoc;
        String trangThai;
        int soNgayTre = 0;

        if (ngayTraThucTe != null) {
            if (ngayHenTra != null && ngayTraThucTe.isAfter(ngayHenTra)) {
                trangThai = TRA_QUA_HAN;
                soNgayTre = (int) ChronoUnit.DAYS.between(ngayHenTra, ngayTraThucTe);
            } else {
                trangThai = DA_TRA;
            }
        } else {
            if (ngayHenTra != null && moc.isAfter(ngayHenTra)) {
                trangThai = QUA_HAN;
                soNgayTre = (int) ChronoUnit.DAYS.between(ngayHenTra, moc);
            } else {
                trangThai = CHUA_TRA;
            }
        }

        return new BaoCaoMuonTraItem(
                maPhieuMuon,
                maThietBi,
                tenThietBi,
                msvNguoiMuon,
                tenNguoiMuon,
                soLuongMuon,
                ngayMuon,
                ngayHenTra,
                ngayTraThucTe,
                mucDichMuon,
                trangThai,
                Math.max(soNgayTre, 0));
    }

    public boolean daTra() {
        return ngayTraThucTe != null;
    }

    public boolean chuaTra() {
        return ngayTraThucTe == null;
    }

    public boolean laQuaHan() {
        return TRA_QUA_HAN.equals(trangThai) || QUA_HAN.equals(trangThai);
    }

    public String tenCoSoVatChatHienThi() {
        if (tenThietBi == null || tenThietBi.isBlank()) {
            return maThietBi == null ? "" : maThietBi;
        }
        return maThietBi == null || maThietBi.isBlank()
                ? tenThietBi
                : tenThietBi + " (" + maThietBi + ")";
    }

    public String nguoiMuonHienThi() {
        if (tenNguoiMuon == null || tenNguoiMuon.isBlank()) {
            return msvNguoiMuon == null ? "" : msvNguoiMuon;
        }
        return msvNguoiMuon == null || msvNguoiMuon.isBlank()
                ? tenNguoiMuon
                : tenNguoiMuon + " (" + msvNguoiMuon + ")";
    }

    public String trangThaiHienThi() {
        if (soNgayQuaHan > 0 && laQuaHan()) {
            return trangThai + " (+" + soNgayQuaHan + " ngày)";
        }
        return trangThai;
    }
}

