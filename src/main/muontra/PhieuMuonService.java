package muontra;

import common.PhienLamViec;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** Kiểm tra dữ liệu nhập; trạng thái phiếu và số lượng kho do trigger/thủ tục đảm bảo lại ở DB. */
public class PhieuMuonService {
    private final PhieuMuonDao dao;

    public PhieuMuonService() {
        this(new PhieuMuonDao());
    }

    public PhieuMuonService(PhieuMuonDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public void taoPhieu(String ma, LocalDate ngayMuon, LocalDate ngayHenTra, String mucDich, String msv) {
        String maSach = required(ma, "Mã phiếu mượn", 20);
        String msvSach = required(msv, "MSV người mượn", 20);
        PhienLamViec.yeuCauChinhMinhHoacBanChuNhiem(msvSach, "lập phiếu mượn");
        if (ngayMuon == null || ngayHenTra == null) {
            throw new IllegalArgumentException("Phải nhập ngày mượn và ngày hẹn trả.");
        }
        if (ngayHenTra.isBefore(ngayMuon)) {
            throw new IllegalArgumentException("Ngày hẹn trả không được trước ngày mượn.");
        }
        dao.taoPhieu(maSach, ngayMuon, ngayHenTra, mucDich == null || mucDich.isBlank() ? null : mucDich.strip(), msvSach);
    }

    public void themThietBi(String maPhieu, String maThietBi, int soLuong) {
        String phieu = required(maPhieu, "Mã phiếu mượn", 20);
        kiemTraPhieuCuaMinh(phieu, "thêm thiết bị vào phiếu mượn");
        String thietBi = required(maThietBi, "Mã thiết bị", 20);
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng mượn phải lớn hơn 0.");
        }
        dao.themChiTiet(phieu, thietBi, soLuong);
    }

    public void duyet(String maPhieu) {
        PhienLamViec.yeuCauBanChuNhiem("duyệt phiếu mượn");
        dao.duyet(required(maPhieu, "Mã phiếu mượn", 20));
    }

    public void tuChoi(String maPhieu) {
        PhienLamViec.yeuCauBanChuNhiem("từ chối phiếu mượn");
        dao.tuChoi(required(maPhieu, "Mã phiếu mượn", 20));
    }

    /** ngayTra null -> hôm nay (do thủ tục xử lý). */
    public void traThietBi(String maPhieu, String maThietBi, LocalDate ngayTra) {
        PhienLamViec.yeuCauBanChuNhiem("ghi nhận trả thiết bị");
        dao.traThietBi(required(maPhieu, "Mã phiếu mượn", 20), required(maThietBi, "Mã thiết bị", 20), ngayTra);
    }

    public void traTatCa(String maPhieu, LocalDate ngayTra) {
        PhienLamViec.yeuCauBanChuNhiem("ghi nhận trả thiết bị");
        dao.traTatCa(required(maPhieu, "Mã phiếu mượn", 20), ngayTra);
    }

    public PhieuMuon xemPhieu(String maPhieu) {
        PhieuMuon phieu = dao.timPhieu(required(maPhieu, "Mã phiếu mượn", 20));
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu mượn không tồn tại.");
        }
        PhienLamViec.yeuCauChinhMinhHoacBanChuNhiem(phieu.msvNguoiMuon(), "xem phiếu mượn");
        return phieu;
    }

    public List<ChiTietMuon> chiTiet(String maPhieu) {
        String ma = required(maPhieu, "Mã phiếu mượn", 20);
        kiemTraPhieuCuaMinh(ma, "xem chi tiết phiếu mượn");
        return dao.chiTietCuaPhieu(ma);
    }

    /** trangThai null/trống -> tất cả; giá trị khác phải là ChoDuyet, DaDuyet hoặc TuChoi. */
    public List<PhieuMuon> danhSachPhieu(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return chiPhieuCuaMinh(dao.danhSachPhieu(null));
        }
        String t = trangThai.strip();
        if (!t.equals(PhieuMuon.CHO_DUYET) && !t.equals(PhieuMuon.DA_DUYET) && !t.equals(PhieuMuon.TU_CHOI)) {
            throw new IllegalArgumentException("Trạng thái phải là ChoDuyet, DaDuyet hoặc TuChoi.");
        }
        return chiPhieuCuaMinh(dao.danhSachPhieu(t));
    }

    /** Thành viên chỉ thấy phiếu của chính mình; Ban Chủ nhiệm thấy tất cả. */
    private static List<PhieuMuon> chiPhieuCuaMinh(List<PhieuMuon> ds) {
        if (PhienLamViec.laBanChuNhiem()) {
            return ds;
        }
        String msv = PhienLamViec.msvHienTai();
        return ds.stream().filter(p -> p.msvNguoiMuon() != null && p.msvNguoiMuon().equalsIgnoreCase(msv)).toList();
    }

    private void kiemTraPhieuCuaMinh(String maPhieu, String hanhDong) {
        if (PhienLamViec.laBanChuNhiem()) {
            return;
        }
        PhieuMuon phieu = dao.timPhieu(maPhieu);
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu mượn không tồn tại.");
        }
        PhienLamViec.yeuCauChinhMinhHoacBanChuNhiem(phieu.msvNguoiMuon(), hanhDong);
    }

    public List<DongTheoDoi> thietBiChuaTra() {
        PhienLamViec.yeuCauBanChuNhiem("xem danh sách thiết bị chưa trả");
        return dao.thietBiChuaTra();
    }

    public List<DongTheoDoi> thietBiTraMuon() {
        PhienLamViec.yeuCauBanChuNhiem("xem theo dõi mượn trả");
        return dao.thietBiTraMuon();
    }

    public List<LichSuMuon> lichSuMuon(String msv) {
        PhienLamViec.yeuCauChinhMinhHoacBanChuNhiem(msv, "xem lịch sử mượn");
        return dao.lichSuMuon(required(msv, "MSV", 20));
    }

    private String required(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        String result = value.strip();
        if (result.codePointCount(0, result.length()) > max) {
            throw new IllegalArgumentException(label + " tối đa " + max + " ký tự.");
        }
        return result;
    }
}
