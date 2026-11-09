package baocaomuontra;

import static org.junit.jupiter.api.Assertions.*;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import baocaomuontra.BoLocBaoCaoMuonTra.SapXepTheo;
import baocaomuontra.BoLocBaoCaoMuonTra.TrangThaiLoc;
import java.awt.Component;
import java.awt.Container;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Bộ kiểm thử tự động toàn diện cho module Báo cáo mượn – trả cơ sở vật chất (Mục 6).
 */
class BaoCaoMuonTraTest {

    private static final LocalDate NGAY_MOC = LocalDate.of(2025, 11, 20);

    /**
     * DAO giả lập bám sát dữ liệu thực tế trong sql/modules/10_muontra/seed.sql để kiểm thử độc lập.
     */
    private static class FakeBaoCaoDao extends BaoCaoMuonTraDao {
        private final List<BaoCaoMuonTraItem> seedItems;

        FakeBaoCaoDao() {
            this(taoDuLieuMau(NGAY_MOC));
        }

        FakeBaoCaoDao(List<BaoCaoMuonTraItem> seedItems) {
            this.seedItems = seedItems;
        }

        @Override
        public List<BaoCaoMuonTraItem> truyVanTheoThoiGian(
                LocalDate tuNgay,
                LocalDate denNgay,
                LoaiThoiGian loaiThoiGian,
                LocalDate ngayMoc) {
            List<BaoCaoMuonTraItem> out = new ArrayList<>();
            for (BaoCaoMuonTraItem it : seedItems) {
                LocalDate checkDate = loaiThoiGian == LoaiThoiGian.NGAY_TRA
                        ? it.ngayTraThucTe()
                        : it.ngayMuon();
                if (checkDate != null && !checkDate.isBefore(tuNgay) && !checkDate.isAfter(denNgay)) {
                    out.add(it);
                }
            }
            return out;
        }
    }

    private static List<BaoCaoMuonTraItem> taoDuLieuMau(LocalDate moc) {
        return List.of(
                // PM10_01: 2025-09-01 -> 2025-09-05, trả đúng hạn ngày 2025-09-05
                BaoCaoMuonTraItem.of(
                        "PM10_01", "TB09_01", "Máy chiếu Epson",
                        "SV01001", "Nguyễn Văn An", 1,
                        LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5), LocalDate.of(2025, 9, 5),
                        "Buổi tập huấn đầu năm", moc),
                BaoCaoMuonTraItem.of(
                        "PM10_01", "TB09_02", "Loa kéo công suất lớn",
                        "SV01001", "Nguyễn Văn An", 2,
                        LocalDate.of(2025, 9, 1), LocalDate.of(2025, 9, 5), LocalDate.of(2025, 9, 5),
                        "Buổi tập huấn đầu năm", moc),
                // PM10_02: 2025-10-10 -> 2025-10-12, trả quá hạn ngày 2025-10-14 (+2 ngày)
                BaoCaoMuonTraItem.of(
                        "PM10_02", "TB09_04", "Bộ đàm cầm tay",
                        "SV01002", "Trần Thị Bình", 3,
                        LocalDate.of(2025, 10, 10), LocalDate.of(2025, 10, 12), LocalDate.of(2025, 10, 14),
                        "Tổ chức gian hàng CLB", moc),
                // PM10_03: 2025-11-03 -> 2025-11-07, chưa trả & đã quá hạn so với mốc 2025-11-20 (+13 ngày)
                BaoCaoMuonTraItem.of(
                        "PM10_03", "TB09_01", "Máy chiếu Epson",
                        "SV01003", "Lê Hoàng Cường", 1,
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 7), null,
                        "Họp ban chuyên môn", moc),
                BaoCaoMuonTraItem.of(
                        "PM10_03", "TB09_03", "Bàn gấp sự kiện",
                        "SV01003", "Lê Hoàng Cường", 4,
                        LocalDate.of(2025, 11, 3), LocalDate.of(2025, 11, 7), null,
                        "Họp ban chuyên môn", moc),
                // PM10_06: 2025-11-18 -> 2025-11-25, đang mượn chưa trả (trong hạn so với mốc 2025-11-20)
                BaoCaoMuonTraItem.of(
                        "PM10_06", "TB09_05", "Phông nền sân khấu CLB",
                        "SV01004", "Đặng Quốc Khánh", 2,
                        LocalDate.of(2025, 11, 18), LocalDate.of(2025, 11, 25), null,
                        "Đêm nhạc thường niên", moc));
    }

    // 1. Kiểm tra báo cáo trong một ngày
    @Test
    void test1_BaoCaoTrongMotNgay() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        LocalDate ngay = LocalDate.of(2025, 9, 1);
        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(ngay, ngay));

        assertEquals(2, kq.danhSachChiTiet().size());
        assertEquals(2, kq.thongKe().tongSoLuotMuon());
        assertEquals(2, kq.thongKe().tongSoLuotTra());
        assertEquals(0, kq.thongKe().soCSVCDangMuon());
        assertEquals(3, kq.thongKe().soCSVCDaTra());
        assertEquals(0, kq.thongKe().soTruongHopQuaHan());
        assertEquals(0, kq.thongKe().soTruongHopChuaTra());
    }

    // 2. Kiểm tra báo cáo trong một tháng
    @Test
    void test2_BaoCaoTrongMotThang() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 31)));

        assertEquals(1, kq.danhSachChiTiet().size());
        BaoCaoMuonTraItem item = kq.danhSachChiTiet().get(0);
        assertEquals("PM10_02", item.maPhieuMuon());
        assertEquals(BaoCaoMuonTraItem.TRA_QUA_HAN, item.trangThai());
        assertEquals(2, item.soNgayQuaHan());
        assertEquals(1, kq.thongKe().soTruongHopQuaHan());
    }

    // 3. Kiểm tra báo cáo trong nhiều tháng
    @Test
    void test3_BaoCaoTrongNhieuThang() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 11, 30)));

        assertEquals(6, kq.danhSachChiTiet().size());
        assertEquals(6, kq.thongKe().tongSoLuotMuon());
        assertEquals(3, kq.thongKe().tongSoLuotTra());
        assertEquals(7, kq.thongKe().soCSVCDangMuon()); // 1 + 4 + 2
        assertEquals(6, kq.thongKe().soCSVCDaTra());    // 1 + 2 + 3
        assertEquals(3, kq.thongKe().soTruongHopQuaHan()); // PM10_02 (1) + PM10_03 (2)
        assertEquals(3, kq.thongKe().soTruongHopChuaTra()); // PM10_03 (2) + PM10_06 (1)
    }

    // 4. Kiểm tra khoảng thời gian không có dữ liệu
    @Test
    void test4_KhoangThoiGianKhongCoDuLieu() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2024, 1, 1),
                LocalDate.of(2024, 6, 30)));

        assertTrue(kq.khongCoDuLieu());
        assertEquals(0, kq.danhSachChiTiet().size());
        assertEquals(ThongKeMuonTra.TRONG, kq.thongKe());
    }

    // 5. Kiểm tra ngày bắt đầu lớn hơn ngày kết thúc
    @Test
    void test5_NgayBatDauLonHonNgayKetThuc() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        BoLocBaoCaoMuonTra boLocSai = BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2025, 12, 31),
                LocalDate.of(2025, 1, 1));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.lapBaoCao(boLocSai));
        assertTrue(ex.getMessage().contains("không được lớn hơn"));
    }

    // 6. Kiểm tra có giao dịch chưa trả
    @Test
    void test6_CoGiaoDichChuaTra() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        BoLocBaoCaoMuonTra locChuaTra = new BoLocBaoCaoMuonTra(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 11, 30),
                LoaiThoiGian.NGAY_MUON,
                "",
                TrangThaiLoc.CHUA_TRA,
                SapXepTheo.NGAY_MUON_TANG);

        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(locChuaTra);
        assertEquals(3, kq.danhSachChiTiet().size());
        assertTrue(kq.danhSachChiTiet().stream().allMatch(BaoCaoMuonTraItem::chuaTra));
        assertEquals(3, kq.thongKe().soTruongHopChuaTra());
        assertEquals(7, kq.thongKe().soCSVCDangMuon());
        // Kiểm tra phân biệt giữa Chưa trả (trong hạn) và Quá hạn (chưa trả)
        assertTrue(kq.danhSachChiTiet().stream()
                .anyMatch(i -> BaoCaoMuonTraItem.CHUA_TRA.equals(i.trangThai())));
        assertTrue(kq.danhSachChiTiet().stream()
                .anyMatch(i -> BaoCaoMuonTraItem.QUA_HAN.equals(i.trangThai())));
    }

    // 7. Kiểm tra có giao dịch trả quá hạn & lọc theo ngày trả
    @Test
    void test7_CoGiaoDichTraQuaHanVaThongKeTheoNgayTra() {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);

        // Lọc trạng thái Quá hạn
        BoLocBaoCaoMuonTra locQuaHan = new BoLocBaoCaoMuonTra(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 11, 30),
                LoaiThoiGian.NGAY_MUON,
                "",
                TrangThaiLoc.QUA_HAN,
                SapXepTheo.NGAY_MUON_GIAM);
        KetQuaBaoCaoMuonTra kqQuaHan = service.lapBaoCao(locQuaHan);
        assertEquals(3, kqQuaHan.danhSachChiTiet().size());
        assertTrue(kqQuaHan.danhSachChiTiet().stream().allMatch(BaoCaoMuonTraItem::laQuaHan));

        // Thống kê theo Ngày trả
        BoLocBaoCaoMuonTra locTheoNgayTra = new BoLocBaoCaoMuonTra(
                LocalDate.of(2025, 10, 1),
                LocalDate.of(2025, 10, 31),
                LoaiThoiGian.NGAY_TRA,
                "",
                TrangThaiLoc.TAT_CA,
                SapXepTheo.NGAY_TRA_GIAM);
        KetQuaBaoCaoMuonTra kqNgayTra = service.lapBaoCao(locTheoNgayTra);
        assertEquals(1, kqNgayTra.danhSachChiTiet().size());
        assertEquals("PM10_02", kqNgayTra.danhSachChiTiet().get(0).maPhieuMuon());
        assertEquals(BaoCaoMuonTraItem.TRA_QUA_HAN, kqNgayTra.danhSachChiTiet().get(0).trangThai());
    }

    // 8. Kiểm tra xuất PDF với số lượng bản ghi lớn (đa trang, đánh số trang, không mất dữ liệu)
    @Test
    void test8_XuatPdfVoiSoLuongBanGhiLon(@TempDir Path tempDir) throws Exception {
        List<BaoCaoMuonTraItem> danhSachLon = new ArrayList<>();
        LocalDate baseDate = LocalDate.of(2025, 1, 1);
        for (int i = 1; i <= 60; i++) {
            LocalDate ngayMuon = baseDate.plusDays(i % 25);
            LocalDate ngayHen = ngayMuon.plusDays(5);
            LocalDate ngayTra = (i % 3 == 0) ? null : (i % 2 == 0 ? ngayHen.plusDays(2) : ngayHen);
            danhSachLon.add(BaoCaoMuonTraItem.of(
                    String.format("PM_LON_%03d", i),
                    String.format("TB_%02d", (i % 10) + 1),
                    "Thiết bị sự kiện số " + i,
                    String.format("SV%05d", i),
                    "Sinh viên thứ " + i,
                    (i % 4) + 1,
                    ngayMuon,
                    ngayHen,
                    ngayTra,
                    "Sự kiện lớn " + i,
                    NGAY_MOC));
        }

        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(danhSachLon), NGAY_MOC);
        KetQuaBaoCaoMuonTra baoCao = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2025, 12, 31)));
        assertEquals(60, baoCao.danhSachChiTiet().size());

        BaoCaoMuonTraPdfExporter exporter = new BaoCaoMuonTraPdfExporter();
        List<BufferedImage> cacTrang = exporter.ketXuatCacTrang(baoCao);
        assertTrue(cacTrang.size() >= 3, "60 bản ghi phải được phân thành ít nhất 3 trang A4");

        Path filePdf = exporter.xuatFile(baoCao, tempDir.resolve("BaoCao_60_BanGhi.pdf"));
        assertTrue(Files.exists(filePdf));
        assertTrue(Files.size(filePdf) > 20_000, "File PDF nhiều trang phải có dung lượng hợp lệ");

        String noiDungPdf = new String(Files.readAllBytes(filePdf), StandardCharsets.UTF_8);
        assertTrue(noiDungPdf.startsWith("%PDF-1.4"));
        assertTrue(noiDungPdf.contains("/Count " + cacTrang.size()));
        assertTrue(noiDungPdf.contains("PM_LON_001"), "Không mất bản ghi đầu tiên");
        assertTrue(noiDungPdf.contains("PM_LON_030"), "Không mất bản ghi ở giữa");
        assertTrue(noiDungPdf.contains("PM_LON_060"), "Không mất bản ghi cuối cùng");
    }

    // 9. Kiểm tra tiếng Việt đầy đủ trong file PDF
    @Test
    void test9_KiemTraTiengVietTrongFilePdf(@TempDir Path tempDir) throws Exception {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        KetQuaBaoCaoMuonTra baoCao = service.lapBaoCao(BoLocBaoCaoMuonTra.macDinh(
                LocalDate.of(2025, 9, 1),
                LocalDate.of(2025, 11, 30)));

        BaoCaoMuonTraPdfExporter exporter = new BaoCaoMuonTraPdfExporter();
        String tenFile = BaoCaoMuonTraPdfExporter.tenFileGoiY(baoCao.boLoc());
        assertEquals("BaoCaoMuonTra_CSVC_2025-09-01_den_2025-11-30.pdf", tenFile);

        Path pdfPath = exporter.xuatFile(baoCao, tempDir.resolve(tenFile));
        assertTrue(Files.exists(pdfPath));

        String utf8Content = new String(Files.readAllBytes(pdfPath), StandardCharsets.UTF_8);
        assertTrue(utf8Content.contains("BÁO CÁO MƯỢN – TRẢ CƠ SỞ VẬT CHẤT"));
        assertTrue(utf8Content.contains("Máy chiếu Epson"));
        assertTrue(utf8Content.contains("Nguyễn Văn An"));
        assertTrue(utf8Content.contains("Trần Thị Bình"));
        assertTrue(utf8Content.contains("Lê Hoàng Cường"));
        assertTrue(utf8Content.contains("Đặng Quốc Khánh"));
        assertTrue(utf8Content.contains("Trả quá hạn"));
        assertTrue(utf8Content.contains("Chưa trả"));
    }

    // 10. Kiểm tra giao diện Swing BaoCaoMuonTraSwingUi
    @Test
    void test10_GiaoDienSwingLocTimKiemVaXuatPdf(@TempDir Path tempDir) throws Exception {
        var service = new BaoCaoMuonTraService(new FakeBaoCaoDao(), NGAY_MOC);
        var ui = new BaoCaoMuonTraSwingUi(service, new BaoCaoMuonTraPdfExporter());

        JTable table = (JTable) find(ui, "baocaomuontra.table");
        JLabel lblTongMuon = (JLabel) find(ui, "baocaomuontra.card.tongLuotMuon");
        JLabel lblQuaHan = (JLabel) find(ui, "baocaomuontra.card.quaHan");
        JTextField tuNgay = (JTextField) find(ui, "baocaomuontra.tuNgay");
        JTextField denNgay = (JTextField) find(ui, "baocaomuontra.denNgay");
        JTextField tuKhoa = (JTextField) find(ui, "baocaomuontra.tuKhoa");
        JComboBox<?> trangThai = (JComboBox<?>) find(ui, "baocaomuontra.trangThai");
        JComboBox<?> sapXep = (JComboBox<?>) find(ui, "baocaomuontra.sapXep");

        SwingUtilities.invokeAndWait(() -> {
            tuNgay.setText("2025-09-01");
            denNgay.setText("2025-11-30");
            ui.xemBaoCao();
        });
        waitFor(() -> table.getRowCount() == 6 && !ui.isBusy());
        assertEquals("6", lblTongMuon.getText());
        assertEquals("3", lblQuaHan.getText());

        // Kiểm tra sắp xếp Ngày trả (Giảm dần): Chưa trả đứng trên cùng, tiếp theo là ngày trả muộn nhất -> sớm nhất
        SwingUtilities.invokeAndWait(() -> {
            sapXep.setSelectedItem(SapXepTheo.NGAY_TRA_GIAM);
            ((JButton) find(ui, "baocaomuontra.loc")).doClick();
        });
        waitFor(() -> "Chưa trả".equals(table.getValueAt(0, 7)) && !ui.isBusy());
        assertEquals("Chưa trả", table.getValueAt(0, 7));
        assertEquals("Chưa trả", table.getValueAt(1, 7));
        assertEquals("Chưa trả", table.getValueAt(2, 7));
        assertEquals("2025-10-14", table.getValueAt(3, 7));
        assertEquals("2025-09-05", table.getValueAt(4, 7));
        assertEquals("2025-09-05", table.getValueAt(5, 7));

        // Kiểm tra sắp xếp Ngày mượn (Giảm dần)
        SwingUtilities.invokeAndWait(() -> {
            sapXep.setSelectedItem(SapXepTheo.NGAY_MUON_GIAM);
            ((JButton) find(ui, "baocaomuontra.loc")).doClick();
        });
        waitFor(() -> "PM10_06".equals(table.getValueAt(0, 1)) && !ui.isBusy());
        assertEquals("2025-11-18", table.getValueAt(0, 5));
        assertEquals("2025-09-01", table.getValueAt(5, 5));

        // Đưa về Ngày mượn (Tăng dần)
        SwingUtilities.invokeAndWait(() -> {
            sapXep.setSelectedItem(SapXepTheo.NGAY_MUON_TANG);
        });

        // Tìm kiếm theo từ khóa tiếng Việt "Epson"
        SwingUtilities.invokeAndWait(() -> {
            tuKhoa.setText("Epson");
            ((JButton) find(ui, "baocaomuontra.loc")).doClick();
        });
        waitFor(() -> table.getRowCount() == 2 && !ui.isBusy());
        assertEquals("PM10_01", table.getValueAt(0, 1));
        assertEquals("PM10_03", table.getValueAt(1, 1));

        // Lọc theo trạng thái Quá hạn
        SwingUtilities.invokeAndWait(() -> {
            tuKhoa.setText("");
            trangThai.setSelectedItem(TrangThaiLoc.QUA_HAN);
            ((JButton) find(ui, "baocaomuontra.loc")).doClick();
        });
        waitFor(() -> table.getRowCount() == 3 && !ui.isBusy());

        // Xuất PDF trực tiếp từ trạng thái đang lọc trên giao diện
        Path outPdf = tempDir.resolve("GiaoDien_DangLoc.pdf");
        SwingUtilities.invokeAndWait(() -> {
            BoLocBaoCaoMuonTra boLoc = ui.docBoLocTuGiaoDien();
            ui.xuatPdfDenDuongDan(boLoc, outPdf, false);
        });
        waitFor(() -> Files.exists(outPdf) && !ui.isBusy());

        String pdfText = new String(Files.readAllBytes(outPdf), StandardCharsets.UTF_8);
        assertTrue(pdfText.contains("Tổng số lượt mượn: 3"));
        assertTrue(pdfText.contains("PM10_02"));
        assertTrue(pdfText.contains("PM10_03"));
        assertFalse(pdfText.contains("PM10_01"), "File PDF chỉ chứa đúng dữ liệu đang lọc trên giao diện");
    }

    private static Component find(Component root, String name) {
        if (name.equals(root.getName())) {
            return root;
        }
        if (root instanceof Container c) {
            for (Component child : c.getComponents()) {
                Component found = find(child, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private static void waitFor(BooleanSupplier condition) throws Exception {
        for (int i = 0; i < 200; i++) {
            SwingUtilities.invokeAndWait(() -> {
            });
            if (condition.getAsBoolean()) {
                return;
            }
            Thread.sleep(25);
        }
        fail("Hết thời gian chờ giao diện cập nhật.");
    }
}
