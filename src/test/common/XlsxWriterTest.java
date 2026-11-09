package common;

import static org.junit.jupiter.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilderFactory;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

class XlsxWriterTest {

    private static ReportSheet sample() {
        return new ReportSheet("Báo cáo/thử:*?", "CLB A & B", "TIÊU ĐỀ <1>", "Kỳ: \"2026\"",
                List.of("Người lập: An", "Nơi nhận: Chủ nhiệm"), List.of("STT", "Tên", "Số"), List.of(6, 20, 8),
                Set.of(0, 2), List.of(List.of("1", "Trần & Lê <x>", "42"), List.of("2", "Có\u0001điều khiển", "abc")),
                List.of("Tổng: 2"), "Ngày 06 tháng 11 năm 2026", "NGƯỜI LẬP", "CHỦ NHIỆM");
    }

    private static Map<String, String> readZip(Path file) throws IOException {
        Map<String, String> parts = new HashMap<>();
        try (ZipFile zip = new ZipFile(file.toFile())) {
            for (var e = zip.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = e.nextElement();
                parts.put(entry.getName(), new String(zip.getInputStream(entry).readAllBytes(), StandardCharsets.UTF_8));
            }
        }
        return parts;
    }

    private static Document parse(String xml) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder()
                .parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));
    }

    @Test void fileXlsxCoDuCacThanhPhanVaMoiTepXmlHopLe() throws Exception {
        Path dir = Files.createTempDirectory("xlsx");
        Path file = dir.resolve("con/bao-cao.xlsx");          // thư mục con chưa tồn tại: phải tự tạo
        XlsxWriter.write(file, sample());
        Map<String, String> parts = readZip(file);
        for (String name : List.of("[Content_Types].xml", "_rels/.rels", "xl/workbook.xml", "xl/_rels/workbook.xml.rels",
                "xl/styles.xml", "xl/worksheets/sheet1.xml")) {
            assertTrue(parts.containsKey(name), "thiếu " + name);
            assertNotNull(parse(parts.get(name)));              // XML đúng cú pháp
        }
    }

    @Test void kyTuDacBietDuocThoatVaKyTuDieuKhienBiLoai() throws Exception {
        Path file = Files.createTempFile("xlsx", ".xlsx");
        XlsxWriter.write(file, sample());
        String sheet = readZip(file).get("xl/worksheets/sheet1.xml");
        assertTrue(sheet.contains("Trần &amp; Lê &lt;x&gt;"));
        assertTrue(sheet.contains("CLB A &amp; B"));
        assertTrue(sheet.contains("Kỳ: &quot;2026&quot;"));
        assertFalse(sheet.contains("\u0001"));
        assertTrue(sheet.contains("Cóđiều khiển"));
        parse(sheet);
    }

    @Test void coSoDuocGhiThanhSoVaChuDuocGhiThanhChuoi() throws Exception {
        Path file = Files.createTempFile("xlsx", ".xlsx");
        XlsxWriter.write(file, sample());
        Document doc = parse(readZip(file).get("xl/worksheets/sheet1.xml"));
        NodeList cells = doc.getElementsByTagName("c");
        int numeric = 0;
        int inline = 0;
        for (int i = 0; i < cells.getLength(); i++) {
            var cell = (org.w3c.dom.Element) cells.item(i);
            if (cell.hasAttribute("t") && cell.getAttribute("t").equals("inlineStr")) inline++;
            else if (cell.getElementsByTagName("v").getLength() > 0) numeric++;
        }
        assertEquals(3, numeric);          // STT 1, STT 2 và số 42; "abc" không phải số nên ghi chuỗi
        assertTrue(inline > 10);
        assertTrue(readZip(file).get("xl/worksheets/sheet1.xml").contains("<v>42</v>"));
    }

    @Test void cauHinhInNgangVuaTrangVaLapTieuDeBang() throws Exception {
        Path file = Files.createTempFile("xlsx", ".xlsx");
        XlsxWriter.write(file, sample());
        Map<String, String> parts = readZip(file);
        String sheet = parts.get("xl/worksheets/sheet1.xml");
        assertTrue(sheet.contains("orientation=\"landscape\""));
        assertTrue(sheet.contains("fitToWidth=\"1\""));
        assertTrue(sheet.contains("fitToHeight=\"1\""));      // báo cáo ngắn: gọn trong một trang
        assertTrue(sheet.contains("<mergeCells"));
        // Dòng tiêu đề bảng là dòng 9 (1 đơn vị, 3 tiêu đề, 4 dòng phụ, 6-7 thông tin, 9 tiêu đề bảng)
        assertTrue(parts.get("xl/workbook.xml").contains("$9:$9"));
        // Tên trang tính bị loại ký tự cấm
        assertTrue(parts.get("xl/workbook.xml").contains("name=\"Báo cáo thử"));
        assertFalse(parts.get("xl/workbook.xml").contains("*"));
    }

    @Test void tenCotVaTenTrangTinhHopLe() {
        assertEquals("A", XlsxWriter.columnName(1));
        assertEquals("Z", XlsxWriter.columnName(26));
        assertEquals("AA", XlsxWriter.columnName(27));
        assertEquals("AB", XlsxWriter.columnName(28));
        assertEquals("Báo cáo", XlsxWriter.safeSheetName(" "));
        assertEquals(31, XlsxWriter.safeSheetName("x".repeat(60)).length());
        assertEquals("a b c d e", XlsxWriter.safeSheetName("a/b:c*d?e").replace("  ", " "));
    }

    @Test void baoCaoSaiCauTrucBiTuChoi() {
        assertThrows(IllegalArgumentException.class, () -> new ReportSheet("s", "o", "t", "", List.of(), List.of("a", "b"),
                List.of(5), Set.of(), List.of(), List.of(), "", "l", "r"));
        assertThrows(IllegalArgumentException.class, () -> new ReportSheet("s", "o", "t", "", List.of(), List.of("a", "b"),
                List.of(5, 5), Set.of(), List.of(List.of("chỉ một ô")), List.of(), "", "l", "r"));
    }

    @Test void baoCaoDaiChiaTrangVaLapTieuDe() throws Exception {
        List<List<String>> rows = new java.util.ArrayList<>();
        for (int i = 1; i <= 80; i++) rows.add(List.of(String.valueOf(i), "Tên " + i, String.valueOf(i)));
        ReportSheet longReport = new ReportSheet("Dài", "CLB", "BÁO CÁO DÀI", "", List.of(), List.of("STT", "Tên", "Số"),
                List.of(6, 20, 8), Set.of(0, 2), rows, List.of(), "", "L", "R");
        Path file = Files.createTempFile("xlsx", ".xlsx");
        XlsxWriter.write(file, longReport);
        String sheet = readZip(file).get("xl/worksheets/sheet1.xml");
        assertTrue(sheet.contains("fitToHeight=\"0\""));
        parse(sheet);
    }
}
