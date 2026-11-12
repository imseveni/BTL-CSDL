package baocaomuontra;

import common.BaoCaoPdf;
import common.NguoiLap;
import common.ReportSheet;
import common.ReportSheet.Align;
import common.ReportSheet.Tone;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Xuất báo cáo mượn – trả cơ sở vật chất ra PDF. Nội dung được đổi sang mẫu báo cáo chung {@link ReportSheet}
 * và vẽ bằng {@link BaoCaoPdf}, nên cùng bố cục (quốc hiệu, tên CLB, khối thông tin, thẻ số liệu, bảng, chữ ký)
 * với các báo cáo của module Thành viên.
 */
public class BaoCaoMuonTraPdfExporter {

    public static final String NOI_NHAN = "Chủ nhiệm CLB; giảng viên phụ trách và Đoàn trường (đơn vị quản lý CLB)";
    public static final String MUC_DICH = "Kiểm kê thiết bị chưa trả, trả quá hạn cuối tháng hoặc cuối học kỳ";

    private static final DateTimeFormatter NGAY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    /** Gợi ý tên file PDF bao gồm khoảng thời gian báo cáo. */
    public static String tenFileGoiY(BoLocBaoCaoMuonTra boLoc) {
        if (boLoc == null || boLoc.tuNgay() == null || boLoc.denNgay() == null) {
            return "BaoCaoMuonTra_CSVC.pdf";
        }
        return String.format("BaoCaoMuonTra_CSVC_%s_den_%s.pdf", boLoc.tuNgay(), boLoc.denNgay());
    }

    /** Xuất kết quả báo cáo ra file PDF tại đường dẫn người dùng chọn; trả về đường dẫn thực tế (có đuôi .pdf). */
    public Path xuatFile(KetQuaBaoCaoMuonTra baoCao, Path duongDanFile) throws IOException {
        if (baoCao == null) {
            throw new IllegalArgumentException("Dữ liệu báo cáo không được để trống.");
        }
        if (duongDanFile == null) {
            throw new IllegalArgumentException("Đường dẫn lưu file PDF không được để trống.");
        }
        return BaoCaoPdf.write(dungBaoCao(baoCao), duongDanFile);
    }

    /** Kết xuất các trang báo cáo thành ảnh (phục vụ xem trước và kiểm thử). */
    public List<BufferedImage> ketXuatCacTrang(KetQuaBaoCaoMuonTra baoCao) {
        try {
            return BaoCaoPdf.render(dungBaoCao(baoCao));
        } catch (IOException e) {
            throw new IllegalStateException("Không vẽ được báo cáo: " + e.getMessage(), e);
        }
    }

    /** Đổi kết quả báo cáo sang mẫu chung. */
    public static ReportSheet dungBaoCao(KetQuaBaoCaoMuonTra baoCao) {
        BoLocBaoCaoMuonTra loc = baoCao.boLoc();
        ThongKeMuonTra tk = baoCao.thongKe();
        NguoiLapBaoCao lap = baoCao.nguoiLapBaoCao() == null ? NguoiLapBaoCao.MAC_DINH : baoCao.nguoiLapBaoCao();
        LocalDate ngayLap = baoCao.ngayLapBaoCao() == null ? LocalDate.now() : baoCao.ngayLapBaoCao();
        NguoiLap nguoiLap = new NguoiLap(lap.hoTenHienThi(), lap.chucVuHienThi());

        List<List<String>> rows = new ArrayList<>();
        List<Tone> tones = new ArrayList<>();
        List<BaoCaoMuonTraItem> ds = baoCao.danhSachChiTiet() == null ? List.of() : baoCao.danhSachChiTiet();
        int stt = 1;
        for (BaoCaoMuonTraItem it : ds) {
            rows.add(List.of(String.valueOf(stt++), tr(it.maPhieuMuon()), tenKemMa(it.tenThietBi(), it.maThietBi(), "Mã thiết bị"),
                    tenKemMa(it.tenNguoiMuon(), it.msvNguoiMuon(), "Mã sinh viên"), String.valueOf(it.soLuongMuon()),
                    ngay(it.ngayMuon()), ngay(it.ngayHenTra()), it.ngayTraThucTe() == null ? "Chưa trả" : ngay(it.ngayTraThucTe()),
                    it.trangThaiHienThi()));
            tones.add(tone(it.trangThai()));
        }

        ReportSheet.Builder b = ReportSheet.builder()
                .title("BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT")
                .subtitle("Từ ngày " + ngay(loc.tuNgay()) + " đến ngày " + ngay(loc.denNgay()) + " ("
                        + loc.loaiThoiGian().nhan().toLowerCase() + ")")
                .info("Kính gửi", NOI_NHAN)
                .info("Người lập", nguoiLap.hienThi())
                .info("Mục đích", MUC_DICH);
        String dieuKien = dieuKienLoc(loc);
        if (!dieuKien.isEmpty()) {
            b.info("Điều kiện lọc", dieuKien);
        }
        return b.stat("Tổng số lượt mượn", tk.tongSoLuotMuon(), Tone.INFO)
                .stat("Tổng số lượt trả", tk.tongSoLuotTra(), Tone.GOOD)
                .stat("CSVC đang được mượn", tk.soCSVCDangMuon(), Tone.WARN)
                .stat("CSVC đã trả", tk.soCSVCDaTra(), Tone.GOOD)
                .stat("Trường hợp quá hạn", tk.soTruongHopQuaHan(), Tone.BAD)
                .stat("Trường hợp chưa trả", tk.soTruongHopChuaTra(), Tone.WARN)
                .column("STT", 5, Align.CENTER)
                .column("Mã phiếu mượn", 12, Align.LEFT)
                .column("Cơ sở vật chất", 18, Align.LEFT)
                .column("Người mượn", 16, Align.LEFT)
                .column("Số lượng", 7, Align.CENTER)
                .column("Ngày mượn", 10, Align.CENTER)
                .column("Ngày hẹn trả", 10, Align.CENTER)
                .column("Ngày trả thực tế", 12, Align.CENTER)
                .column("Trạng thái", 17, Align.CENTER)
                .rows(rows)
                .badge(8, tones)
                .summary("Tổng kết", List.of(
                        "Trong kỳ có " + tk.tongSoLuotMuon() + " lượt mượn; đã trả " + tk.tongSoLuotTra() + " lượt ("
                                + tk.soCSVCDaTra() + " thiết bị), chưa trả " + tk.soTruongHopChuaTra() + " trường hợp ("
                                + tk.soCSVCDangMuon() + " thiết bị).",
                        "Có " + tk.soTruongHopQuaHan() + " trường hợp quá hạn (trả muộn sau ngày hẹn hoặc quá hạn chưa trả)."))
                .issueDate(ngayLap)
                .signRight("NGƯỜI LẬP BÁO CÁO", "(Ký, ghi rõ họ tên)", nguoiLap.hoVaTen())
                .footerLabel("Báo cáo mượn – trả cơ sở vật chất")
                .build();
    }

    private static String dieuKienLoc(BoLocBaoCaoMuonTra loc) {
        List<String> parts = new ArrayList<>();
        if (loc.tuKhoa() != null && !loc.tuKhoa().isBlank()) {
            parts.add("từ khóa \"" + loc.tuKhoa().strip() + "\"");
        }
        if (loc.trangThaiLoc() != null && loc.trangThaiLoc() != BoLocBaoCaoMuonTra.TrangThaiLoc.TAT_CA) {
            parts.add("trạng thái " + loc.trangThaiLoc().nhan());
        }
        return String.join("; ", parts);
    }

    private static Tone tone(String trangThai) {
        if (BaoCaoMuonTraItem.DA_TRA.equals(trangThai)) {
            return Tone.GOOD;
        }
        if (BaoCaoMuonTraItem.CHUA_TRA.equals(trangThai)) {
            return Tone.WARN;
        }
        return Tone.BAD;
    }

    private static String tenKemMa(String ten, String ma, String nhanMa) {
        boolean coTen = ten != null && !ten.isBlank();
        boolean coMa = ma != null && !ma.isBlank();
        if (coTen && coMa) {
            return ten + "\n" + nhanMa + ": " + ma;
        }
        return coTen ? ten : tr(ma);
    }

    private static String ngay(LocalDate d) {
        return d == null ? "—" : NGAY.format(d);
    }

    private static String tr(String s) {
        return s == null ? "" : s;
    }
}
