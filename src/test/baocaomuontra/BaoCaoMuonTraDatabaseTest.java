package baocaomuontra;

import static org.junit.jupiter.api.Assertions.*;

import baocaomuontra.BoLocBaoCaoMuonTra.LoaiThoiGian;
import baocaomuontra.BoLocBaoCaoMuonTra.SapXepTheo;
import baocaomuontra.BoLocBaoCaoMuonTra.TrangThaiLoc;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.io.TempDir;

/**
 * Kiểm thử trực tiếp với cơ sở dữ liệu MySQL thực tế (bật bằng -Dclb.mysql.tests=true).
 */
@EnabledIfSystemProperty(named = "clb.mysql.tests", matches = "true")
class BaoCaoMuonTraDatabaseTest {

    private final BaoCaoMuonTraService service = new BaoCaoMuonTraService();
    private final BaoCaoMuonTraPdfExporter pdfExporter = new BaoCaoMuonTraPdfExporter();

    @Test
    void truyVanDuLieuThucTeTuMySQLVaXuatPdf(@TempDir Path tempDir) throws Exception {
        BoLocBaoCaoMuonTra boLoc = new BoLocBaoCaoMuonTra(
                LocalDate.of(2025, 1, 1),
                LocalDate.of(2026, 12, 31),
                LoaiThoiGian.NGAY_MUON,
                "",
                TrangThaiLoc.TAT_CA,
                SapXepTheo.NGAY_MUON_TANG);

        KetQuaBaoCaoMuonTra kq = service.lapBaoCao(boLoc);
        assertNotNull(kq);
        assertNotNull(kq.thongKe());
        assertNotNull(kq.danhSachChiTiet());

        Path pdf = pdfExporter.xuatFile(kq, tempDir.resolve(BaoCaoMuonTraPdfExporter.tenFileGoiY(boLoc)));
        assertTrue(Files.exists(pdf));
        assertTrue(Files.size(pdf) > 0);
    }
}

