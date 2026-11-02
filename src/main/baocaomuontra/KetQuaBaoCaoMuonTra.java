package baocaomuontra;

import java.time.LocalDate;
import java.util.List;

/**
 * Kết quả tổng hợp báo cáo mượn – trả cơ sở vật chất (bao gồm điều kiện lọc, số liệu tổng quan và danh sách chi tiết).
 */
public record KetQuaBaoCaoMuonTra(
        BoLocBaoCaoMuonTra boLoc,
        LocalDate ngayLapBaoCao,
        ThongKeMuonTra thongKe,
        List<BaoCaoMuonTraItem> danhSachChiTiet) {

    public boolean khongCoDuLieu() {
        return danhSachChiTiet == null || danhSachChiTiet.isEmpty();
    }
}

