package baocaomuontra;

import java.time.LocalDate;

/**
 * Điều kiện lọc, tìm kiếm và sắp xếp cho báo cáo mượn – trả cơ sở vật chất.
 */
public record BoLocBaoCaoMuonTra(
        LocalDate tuNgay,
        LocalDate denNgay,
        LoaiThoiGian loaiThoiGian,
        String tuKhoa,
        TrangThaiLoc trangThaiLoc,
        SapXepTheo sapXepTheo) {

    /** Tiêu chí thống kê theo thời gian: Ngày mượn hoặc Ngày trả. */
    public enum LoaiThoiGian {
        NGAY_MUON("Theo ngày mượn"),
        NGAY_TRA("Theo ngày trả");

        private final String nhan;

        LoaiThoiGian(String nhan) {
            this.nhan = nhan;
        }

        public String nhan() {
            return nhan;
        }

        @Override
        public String toString() {
            return nhan;
        }
    }

    /** Lọc theo trạng thái mượn – trả. */
    public enum TrangThaiLoc {
        TAT_CA("Tất cả trạng thái"),
        DA_TRA("Đã trả"),
        CHUA_TRA("Chưa trả"),
        QUA_HAN("Quá hạn");

        private final String nhan;

        TrangThaiLoc(String nhan) {
            this.nhan = nhan;
        }

        public String nhan() {
            return nhan;
        }

        @Override
        public String toString() {
            return nhan;
        }
    }

    /** Sắp xếp theo ngày mượn hoặc ngày trả. */
    public enum SapXepTheo {
        NGAY_MUON_TANG("Ngày mượn (Tăng dần)"),
        NGAY_MUON_GIAM("Ngày mượn (Giảm dần)"),
        NGAY_TRA_TANG("Ngày trả (Tăng dần)"),
        NGAY_TRA_GIAM("Ngày trả (Giảm dần)");

        private final String nhan;

        SapXepTheo(String nhan) {
            this.nhan = nhan;
        }

        public String nhan() {
            return nhan;
        }

        @Override
        public String toString() {
            return nhan;
        }
    }

    public static BoLocBaoCaoMuonTra macDinh(LocalDate tuNgay, LocalDate denNgay) {
        return new BoLocBaoCaoMuonTra(
                tuNgay,
                denNgay,
                LoaiThoiGian.NGAY_MUON,
                "",
                TrangThaiLoc.TAT_CA,
                SapXepTheo.NGAY_MUON_TANG);
    }
}

