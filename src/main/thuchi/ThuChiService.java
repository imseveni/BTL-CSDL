package thuchi;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Tầng nghiệp vụ (Service) cho Module 07 – Quản lý Thu – Chi.
 * Kiểm tra tính hợp lệ của dữ liệu và quy tắc nghiệp vụ trước khi gọi ThuChiDao.
 */
public class ThuChiService {

    private final ThuChiDao thuChiDao;

    public ThuChiService() {
        this.thuChiDao = new ThuChiDao();
    }

    public ThuChiService(ThuChiDao thuChiDao) {
        this.thuChiDao = thuChiDao;
    }

    public List<ThuChi> layTatCaGiaoDich() {
        return thuChiDao.layTatCa();
    }

    public ThuChi timGiaoDichTheoMa(String maGD) {
        if (maGD == null || maGD.isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch không được để trống.");
        }
        return thuChiDao.timTheoMa(maGD.trim());
    }

    public void themGiaoDich(ThuChi gd) {
        chuanHoaVaKiemTraGiaoDich(gd);
        if (thuChiDao.timTheoMa(gd.getMaGD()) != null) {
            throw new IllegalArgumentException("Mã giao dịch '" + gd.getMaGD() + "' đã tồn tại.");
        }
        thuChiDao.them(gd);
    }

    public void capNhatGiaoDich(ThuChi gd) {
        chuanHoaVaKiemTraGiaoDich(gd);
        if (thuChiDao.timTheoMa(gd.getMaGD()) == null) {
            throw new IllegalArgumentException("Không tìm thấy giao dịch có mã '" + gd.getMaGD() + "'.");
        }
        thuChiDao.capNhat(gd);
    }

    public void xoaGiaoDich(String maGD) {
        if (maGD == null || maGD.isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch không được để trống.");
        }
        String ma = maGD.trim();
        if (thuChiDao.timTheoMa(ma) == null) {
            throw new IllegalArgumentException("Không tìm thấy giao dịch có mã '" + ma + "'.");
        }
        thuChiDao.xoa(ma);
    }

    public List<ThuChi> traCuuTheoThanhVien(String msv) {
        if (msv == null || msv.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên (MSV) không được để trống.");
        }
        return thuChiDao.traCuuTheoThanhVien(msv.trim());
    }

    public List<ThuChi> traCuuTheoSuKien(String maSK) {
        if (maSK == null || maSK.isBlank()) {
            throw new IllegalArgumentException("Mã sự kiện (MaSK) không được để trống.");
        }
        return thuChiDao.traCuuTheoSuKien(maSK.trim());
    }

    public List<ThuChi> locTheoLoai(String loaiGD) {
        String loaiChuan = chuanHoaLoaiGD(loaiGD);
        return thuChiDao.traCuuTheoLoai(loaiChuan);
    }

    public List<ThongKeThuChiSuKien> thongKeTheoTungSuKien() {
        return thuChiDao.thongKeTheoTungSuKien();
    }

    private void chuanHoaVaKiemTraGiaoDich(ThuChi gd) {
        if (gd == null) {
            throw new IllegalArgumentException("Thông tin giao dịch không hợp lệ.");
        }
        if (gd.getMaGD() == null || gd.getMaGD().isBlank()) {
            throw new IllegalArgumentException("Mã giao dịch (MaGD) không được để trống.");
        }
        String maGD = gd.getMaGD().trim();
        if (maGD.length() > 20) {
            throw new IllegalArgumentException("Mã giao dịch không được vượt quá 20 ký tự.");
        }
        gd.setMaGD(maGD);

        String loaiChuan = chuanHoaLoaiGD(gd.getLoaiGD());
        gd.setLoaiGD(loaiChuan);

        if (gd.getMucDich() == null || gd.getMucDich().isBlank()) {
            throw new IllegalArgumentException("Mục đích giao dịch không được để trống.");
        }
        gd.setMucDich(gd.getMucDich().trim());

        if (gd.getSoTien() == null || gd.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền giao dịch phải lớn hơn 0.");
        }

        if (gd.getThoiGian() == null) {
            throw new IllegalArgumentException("Thời gian thực hiện giao dịch không được để trống.");
        }

        if (gd.getMinhChung() != null) {
            String mc = gd.getMinhChung().trim();
            gd.setMinhChung(mc.isEmpty() ? null : mc);
        }

        if (gd.getMsv() != null && !gd.getMsv().isBlank()) {
            String msv = gd.getMsv().trim();
            if (!thuChiDao.tonTaiThanhVien(msv)) {
                throw new IllegalArgumentException(
                        "Không tìm thấy thành viên thực hiện có MSV '" + msv + "' trong hệ thống.");
            }
            gd.setMsv(msv);
        } else {
            gd.setMsv(null);
        }

        if (gd.getMaSK() != null && !gd.getMaSK().isBlank()) {
            String maSK = gd.getMaSK().trim();
            if (!thuChiDao.tonTaiSuKien(maSK)) {
                throw new IllegalArgumentException(
                        "Không tìm thấy sự kiện liên kết có mã '" + maSK + "' trong hệ thống.");
            }
            gd.setMaSK(maSK);
        } else {
            gd.setMaSK(null);
        }

        List<String> dsPhuongThuc = gd.getPhuongThucThanhToan();
        Set<String> phuongThucHopLe = new LinkedHashSet<>();
        if (dsPhuongThuc != null) {
            for (String pt : dsPhuongThuc) {
                if (pt != null && !pt.isBlank()) {
                    phuongThucHopLe.add(pt.trim());
                }
            }
        }
        if (phuongThucHopLe.isEmpty()) {
            throw new IllegalArgumentException("Phải nhập ít nhất một phương thức thanh toán.");
        }
        gd.setPhuongThucThanhToan(new ArrayList<>(phuongThucHopLe));
    }

    private String chuanHoaLoaiGD(String loaiGD) {
        if (loaiGD == null || loaiGD.isBlank()) {
            throw new IllegalArgumentException("Loại giao dịch không được để trống (chỉ nhận 'Thu' hoặc 'Chi').");
        }
        String s = loaiGD.trim();
        if (s.equalsIgnoreCase("Thu")) {
            return "Thu";
        }
        if (s.equalsIgnoreCase("Chi")) {
            return "Chi";
        }
        throw new IllegalArgumentException("Loại giao dịch không hợp lệ: '" + loaiGD + "'. Chỉ chấp nhận 'Thu' hoặc 'Chi'.");
    }
}
