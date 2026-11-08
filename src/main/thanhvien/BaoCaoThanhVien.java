package thanhvien;

import common.ReportSheet;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Dựng nội dung hai báo cáo của module Thành viên theo mẫu văn bản hành chính. Chỉ xử lý dữ liệu đã đọc
 * từ CSDL; việc đọc CSDL ở {@link ThanhVienService}, việc ghi file ở {@code common.XlsxWriter}.
 */
public final class BaoCaoThanhVien {

    /** Báo cáo 1: ai nhận và dùng để làm gì (hiện trên màn hình và in trong file). */
    public static final String NOI_NHAN_DANH_SACH = "Chủ nhiệm CLB; giảng viên phụ trách và Đoàn trường (đơn vị quản lý CLB)";
    public static final String MUC_DICH_DANH_SACH = "Đăng ký danh sách thành viên sinh hoạt với đơn vị quản lý";

    /** Báo cáo 2: ai nhận và dùng để làm gì. */
    public static final String NOI_NHAN_HOAT_DONG = "Chủ nhiệm CLB và giảng viên phụ trách";
    public static final String MUC_DICH_HOAT_DONG = "Xét khen thưởng, ghi nhận đóng góp của thành viên cuối kỳ";

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final Locale VI = Locale.forLanguageTag("vi-VN");

    private BaoCaoThanhVien() {
    }

    public static ReportSheet danhSachTheoBan(String tenClb, String nguoiLap, LocalDate ngayLap, String boLoc,
                                              List<ThanhVien> thanhViens) {
        List<List<String>> rows = new ArrayList<>();
        Map<String, Integer> theoBan = new LinkedHashMap<>();
        Map<String, Integer> theoTrangThai = new LinkedHashMap<>();
        int stt = 1;
        for (ThanhVien tv : thanhViens) {
            rows.add(List.of(String.valueOf(stt++), tv.msv(), tv.hoVaTen(), ngay(tv.ngaySinh()), tr(tv.gioiTinh()),
                    tr(tv.email()), tr(tv.sdt()), ngay(tv.ngayGiaNhap()), tr(tv.tenBan()), tr(tv.chucVu()),
                    tr(tv.trangThaiHD())));
            theoBan.merge(tr(tv.tenBan()), 1, Integer::sum);
            theoTrangThai.merge(tr(tv.trangThaiHD()), 1, Integer::sum);
        }
        List<String> summary = new ArrayList<>();
        summary.add("Tổng số thành viên: " + thanhViens.size());
        if (!theoBan.isEmpty()) {
            summary.add("Theo ban: " + join(theoBan));
            summary.add("Theo trạng thái: " + join(theoTrangThai));
        }
        return new ReportSheet("Danh sách thành viên", hoa(tenClb), "DANH SÁCH THÀNH VIÊN CÂU LẠC BỘ",
                "Bộ lọc: " + boLoc,
                List.of("Người lập: " + nguoiLap, "Nơi nhận: " + NOI_NHAN_DANH_SACH, "Mục đích: " + MUC_DICH_DANH_SACH,
                        "Ngày lập: " + NGAY.format(ngayLap)),
                List.of("STT", "MSV", "Họ và tên", "Ngày sinh", "Giới tính", "Email", "Số điện thoại", "Ngày gia nhập",
                        "Ban", "Chức vụ", "Trạng thái"),
                List.of(6, 14, 26, 12, 10, 30, 15, 14, 20, 16, 16), Set.of(0), rows, summary,
                "Ngày " + String.format("%02d tháng %02d năm %d", ngayLap.getDayOfMonth(), ngayLap.getMonthValue(), ngayLap.getYear()),
                "NGƯỜI LẬP BÁO CÁO", "CHỦ NHIỆM CÂU LẠC BỘ");
    }

    public static ReportSheet hoatDong(String tenClb, String nguoiLap, LocalDate ngayLap, LocalDate tuNgay,
                                       LocalDate denNgay, List<TongHopHoatDong> dong) {
        List<List<String>> rows = new ArrayList<>();
        int stt = 1;
        long tong = 0;
        int coHoatDong = 0;
        for (TongHopHoatDong d : dong) {
            rows.add(List.of(String.valueOf(stt++), d.msv(), d.hoVaTen(), tr(d.tenBan()), tr(d.chucVu()),
                    String.valueOf(d.soKhoaHoc()), String.valueOf(d.soDuAn()), String.valueOf(d.soSuKien()),
                    String.valueOf(d.soDanhGia()), String.valueOf(d.soPhieuMuon()), String.valueOf(d.soGiaoDich()),
                    String.valueOf(d.tong())));
            tong += d.tong();
            if (d.tong() > 0) {
                coHoatDong++;
            }
        }
        List<String> summary = List.of("Tổng số thành viên trong báo cáo: " + dong.size() + "   |   Có hoạt động trong kỳ: "
                + coHoatDong + "   |   Tổng lượt hoạt động: " + tong);
        return new ReportSheet("Hoạt động thành viên", hoa(tenClb), "TỔNG HỢP HOẠT ĐỘNG CỦA THÀNH VIÊN",
                "Kỳ báo cáo: từ " + NGAY.format(tuNgay) + " đến " + NGAY.format(denNgay),
                List.of("Người lập: " + nguoiLap, "Nơi nhận: " + NOI_NHAN_HOAT_DONG, "Mục đích: " + MUC_DICH_HOAT_DONG,
                        "Ngày lập: " + NGAY.format(ngayLap)),
                List.of("STT", "MSV", "Họ và tên", "Ban", "Chức vụ", "Khóa học", "Dự án", "Sự kiện", "Đánh giá",
                        "Phiếu mượn", "Thu chi", "Tổng"),
                List.of(6, 14, 26, 20, 16, 10, 10, 10, 10, 12, 10, 10), Set.of(0, 5, 6, 7, 8, 9, 10, 11), rows, summary,
                "Ngày " + String.format("%02d tháng %02d năm %d", ngayLap.getDayOfMonth(), ngayLap.getMonthValue(), ngayLap.getYear()),
                "NGƯỜI LẬP BÁO CÁO", "CHỦ NHIỆM CÂU LẠC BỘ");
    }

    /** Tên file gợi ý khi xuất, ví dụ BaoCao_DanhSachThanhVien_20261106.xlsx. */
    public static String tenFile(String loai, LocalDate ngay) {
        return "BaoCao_" + loai + "_" + String.format("%04d%02d%02d", ngay.getYear(), ngay.getMonthValue(), ngay.getDayOfMonth()) + ".xlsx";
    }

    private static String hoa(String s) {
        return s == null ? "" : s.trim().toUpperCase(VI);
    }

    private static String ngay(LocalDate d) {
        return d == null ? "" : NGAY.format(d);
    }

    private static String tr(String s) {
        return s == null ? "" : s;
    }

    private static String join(Map<String, Integer> counts) {
        List<String> parts = new ArrayList<>();
        counts.forEach((k, v) -> parts.add(k + ": " + v));
        return String.join("; ", parts);
    }
}
