package muontra;

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
        String thietBi = required(maThietBi, "Mã thiết bị", 20);
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng mượn phải lớn hơn 0.");
        }
        dao.themChiTiet(phieu, thietBi, soLuong);
    }

    public void duyet(String maPhieu) {
        dao.duyet(required(maPhieu, "Mã phiếu mượn", 20));
    }

    public void tuChoi(String maPhieu) {
        dao.tuChoi(required(maPhieu, "Mã phiếu mượn", 20));
    }

    /** ngayTra null -> hôm nay (do thủ tục xử lý). */
    public void traThietBi(String maPhieu, String maThietBi, LocalDate ngayTra) {
        dao.traThietBi(required(maPhieu, "Mã phiếu mượn", 20), required(maThietBi, "Mã thiết bị", 20), ngayTra);
    }

    public void traTatCa(String maPhieu, LocalDate ngayTra) {
        dao.traTatCa(required(maPhieu, "Mã phiếu mượn", 20), ngayTra);
    }

    public PhieuMuon xemPhieu(String maPhieu) {
        PhieuMuon phieu = dao.timPhieu(required(maPhieu, "Mã phiếu mượn", 20));
        if (phieu == null) {
            throw new IllegalArgumentException("Phiếu mượn không tồn tại.");
        }
        return phieu;
    }

    public List<ChiTietMuon> chiTiet(String maPhieu) {
        return dao.chiTietCuaPhieu(required(maPhieu, "Mã phiếu mượn", 20));
    }

    /** trangThai null/trống -> tất cả; giá trị khác phải là ChoDuyet, DaDuyet hoặc TuChoi. */
    public List<PhieuMuon> danhSachPhieu(String trangThai) {
        if (trangThai == null || trangThai.isBlank()) {
            return dao.danhSachPhieu(null);
        }
        String t = trangThai.strip();
        if (!t.equals(PhieuMuon.CHO_DUYET) && !t.equals(PhieuMuon.DA_DUYET) && !t.equals(PhieuMuon.TU_CHOI)) {
            throw new IllegalArgumentException("Trạng thái phải là ChoDuyet, DaDuyet hoặc TuChoi.");
        }
        return dao.danhSachPhieu(t);
    }

    public List<DongTheoDoi> thietBiChuaTra() {
        return dao.thietBiChuaTra();
    }

    public List<DongTheoDoi> thietBiTraMuon() {
        return dao.thietBiTraMuon();
    }

    public List<LichSuMuon> lichSuMuon(String msv) {
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
