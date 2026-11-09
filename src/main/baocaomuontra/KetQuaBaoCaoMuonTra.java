package baocaomuontra;

import java.time.LocalDate;
import java.util.List;

/**
 * Kết quả tổng hợp báo cáo mượn – trả cơ sở vật chất (bao gồm điều kiện lọc, người lập báo cáo,
 * số liệu tổng quan và danh sách chi tiết).
 */
public record KetQuaBaoCaoMuonTra(
        BoLocBaoCaoMuonTra boLoc,
        LocalDate ngayLapBaoCao,
        ThongKeMuonTra thongKe,
        List<BaoCaoMuonTraItem> danhSachChiTiet,
        NguoiLapBaoCao nguoiLapBaoCao) {

    public KetQuaBaoCaoMuonTra(
            BoLocBaoCaoMuonTra boLoc,
            LocalDate ngayLapBaoCao,
            ThongKeMuonTra thongKe,
            List<BaoCaoMuonTraItem> danhSachChiTiet) {
        this(boLoc, ngayLapBaoCao, thongKe, danhSachChiTiet, NguoiLapBaoCao.MAC_DINH);
    }

    public KetQuaBaoCaoMuonTra voiNguoiLap(NguoiLapBaoCao nguoiLap) {
        return new KetQuaBaoCaoMuonTra(
                boLoc,
                ngayLapBaoCao,
                thongKe,
                danhSachChiTiet,
                nguoiLap == null ? NguoiLapBaoCao.MAC_DINH : nguoiLap);
    }

    public boolean khongCoDuLieu() {
        return danhSachChiTiet == null || danhSachChiTiet.isEmpty();
    }
}
