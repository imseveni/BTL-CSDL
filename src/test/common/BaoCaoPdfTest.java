package common;

import static org.junit.jupiter.api.Assertions.*;

import common.ReportSheet.Align;
import common.ReportSheet.Tone;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BaoCaoPdfTest {

    private static ReportSheet mau(int soDong) {
        List<List<String>> rows = new ArrayList<>();
        List<Tone> tones = new ArrayList<>();
        for (int i = 1; i <= soDong; i++) {
            rows.add(List.of(String.valueOf(i), "Nguyễn Văn Số " + i, "Đang hoạt động"));
            tones.add(Tone.GOOD);
        }
        return ReportSheet.builder().title("BÁO CÁO THỬ").subtitle("Kỳ thử")
                .info("Người lập", "Nguyễn Văn An – Chủ nhiệm")
                .stat("Tổng", String.valueOf(soDong), Tone.INFO)
                .column("STT", 6, Align.CENTER).column("Họ và tên", 30, Align.LEFT).column("Trạng thái", 14, Align.CENTER)
                .rows(rows).badge(2, tones).summary("TỔNG KẾT", List.of("Có " + soDong + " dòng."))
                .issueDate(LocalDate.of(2026, 11, 7))
                .signLeft("CHỦ NHIỆM CÂU LẠC BỘ", "(Ký, ghi rõ họ tên)")
                .signRight("NGƯỜI LẬP BÁO CÁO", "(Ký, ghi rõ họ tên)", "Nguyễn Văn An")
                .footerLabel("Báo cáo thử").build();
    }

    @Test void baoCaoNganCoMotTrangA4NamNgang() throws Exception {
        var pages = BaoCaoPdf.render(mau(5));
        assertEquals(1, pages.size());
        assertEquals(1754, pages.get(0).getWidth());
        assertEquals(1240, pages.get(0).getHeight());
    }

    @Test void baoCaoDaiTuDongSangNhieuTrang() throws Exception {
        assertTrue(BaoCaoPdf.render(mau(80)).size() >= 3);
    }

    @Test void baoCaoRongVanVeDuoc() throws Exception {
        assertEquals(1, BaoCaoPdf.render(mau(0)).size());
    }

    @Test void filePdfHopLeVaMangNoiDungTiengViet(@TempDir Path dir) throws Exception {
        Path p = BaoCaoPdf.write(mau(3), dir.resolve("thu"));
        assertTrue(p.getFileName().toString().endsWith(".pdf"));
        String raw = new String(Files.readAllBytes(p), StandardCharsets.ISO_8859_1);
        assertTrue(raw.startsWith("%PDF-1.4"));
        assertTrue(raw.contains("/Count 1"));
        assertTrue(raw.trim().endsWith("%%EOF"));
        assertTrue(BaoCaoPdf.vanBan(mau(3)).contains("Nguyễn Văn Số 3"));
    }

    @Test void ghiRaLuongBatKy() throws Exception {
        var out = new ByteArrayOutputStream();
        BaoCaoPdf.write(mau(2), out);
        assertTrue(out.size() > 10_000);
    }

    @Test void duoiPdfDuocBoSung() {
        assertEquals("a.pdf", BaoCaoPdf.withPdfExtension(Path.of("a")).getFileName().toString());
        assertEquals("a.pdf", BaoCaoPdf.withPdfExtension(Path.of("a.PDF")).getFileName().toString().toLowerCase());
    }
}
