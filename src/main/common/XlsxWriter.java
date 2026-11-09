package common;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Ghi báo cáo ra file Excel (.xlsx) mà không cần thư viện ngoài: .xlsx là một file zip chứa các tệp XML
 * (chuẩn Office Open XML). Bố cục theo mẫu văn bản hành chính: khổ A4 nằm ngang, vừa chiều ngang trang,
 * tiêu đề bảng lặp lại ở mỗi trang in, đánh số trang ở chân trang.
 */
public final class XlsxWriter {

    // Chỉ số kiểu ô (cellXfs) trong styles.xml bên dưới.
    private static final int S_ORG = 1;
    private static final int S_TITLE = 2;
    private static final int S_ITALIC_CENTER = 3;
    private static final int S_INFO = 4;
    private static final int S_HEADER = 5;
    private static final int S_TEXT = 6;
    private static final int S_NUMBER = 7;
    private static final int S_SUMMARY = 8;
    private static final int S_SIGN_BOLD = 9;
    private static final int S_SIGN_ITALIC = 10;

    private XlsxWriter() {
    }

    public static void write(Path file, ReportSheet report) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
        String sheetName = safeSheetName(report.sheetName());
        SheetXml sheet = buildSheet(report);
        try (OutputStream out = Files.newOutputStream(file); ZipOutputStream zip = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            put(zip, "[Content_Types].xml", CONTENT_TYPES);
            put(zip, "_rels/.rels", ROOT_RELS);
            put(zip, "xl/workbook.xml", workbook(sheetName, sheet.headerRow));
            put(zip, "xl/_rels/workbook.xml.rels", WORKBOOK_RELS);
            put(zip, "xl/styles.xml", STYLES);
            put(zip, "xl/worksheets/sheet1.xml", sheet.xml);
        }
    }

    // ------------------------------------------------------------------------------------------

    private record SheetXml(String xml, int headerRow) {
    }

    private static SheetXml buildSheet(ReportSheet r) {
        int cols = r.columns().size();
        String lastCol = columnName(cols);
        StringBuilder data = new StringBuilder();
        List<String> merges = new ArrayList<>();
        int row = 1;

        data.append(textRow(row, S_ORG, 20, r.organization(), cols));
        merges.add("A" + row + ":" + lastCol + row);
        row += 2;
        data.append(textRow(row, S_TITLE, 28, r.title(), cols));
        merges.add("A" + row + ":" + lastCol + row);
        row++;
        if (r.subtitle() != null && !r.subtitle().isBlank()) {
            data.append(textRow(row, S_ITALIC_CENTER, 18, r.subtitle(), cols));
            merges.add("A" + row + ":" + lastCol + row);
            row++;
        }
        row++;
        for (String line : r.infoLines()) {
            data.append(textRow(row, S_INFO, 18, line, cols));
            merges.add("A" + row + ":" + lastCol + row);
            row++;
        }
        row++;

        int headerRow = row;
        data.append("<row r=\"").append(row).append("\" ht=\"32\" customHeight=\"1\">");
        for (int c = 0; c < cols; c++) {
            data.append(stringCell(columnName(c + 1) + row, S_HEADER, r.columns().get(c)));
        }
        data.append("</row>");
        row++;
        for (List<String> values : r.rows()) {
            data.append("<row r=\"").append(row).append("\">");
            for (int c = 0; c < cols; c++) {
                String ref = columnName(c + 1) + row;
                String value = values.get(c);
                if (r.numericColumns().contains(c) && isInteger(value)) {
                    data.append("<c r=\"").append(ref).append("\" s=\"").append(S_NUMBER)
                            .append("\"><v>").append(value.trim()).append("</v></c>");
                } else {
                    data.append(stringCell(ref, r.numericColumns().contains(c) ? S_NUMBER : S_TEXT, value));
                }
            }
            data.append("</row>");
            row++;
        }
        for (String line : r.summaryLines()) {
            data.append("<row r=\"").append(row).append("\" ht=\"20\" customHeight=\"1\">");
            data.append(stringCell("A" + row, S_SUMMARY, line));
            for (int c = 1; c < cols; c++) {
                data.append(stringCell(columnName(c + 1) + row, S_SUMMARY, ""));
            }
            data.append("</row>");
            merges.add("A" + row + ":" + lastCol + row);
            row++;
        }
        row++;

        // Khối chữ ký: ngày tháng bên phải, hai chức danh ở hai nửa bảng.
        int half = Math.max(1, cols / 2);
        if (r.signDate() != null && !r.signDate().isBlank()) {
            data.append("<row r=\"").append(row).append("\" ht=\"20\" customHeight=\"1\">")
                    .append(stringCell(columnName(half + 1) + row, S_SIGN_ITALIC, r.signDate())).append("</row>");
            merges.add(columnName(half + 1) + row + ":" + lastCol + row);
            row++;
        }
        data.append("<row r=\"").append(row).append("\" ht=\"20\" customHeight=\"1\">")
                .append(stringCell("A" + row, S_SIGN_BOLD, r.signLeft()))
                .append(stringCell(columnName(half + 1) + row, S_SIGN_BOLD, r.signRight())).append("</row>");
        merges.add("A" + row + ":" + columnName(half) + row);
        merges.add(columnName(half + 1) + row + ":" + lastCol + row);
        row++;
        data.append("<row r=\"").append(row).append("\" ht=\"20\" customHeight=\"1\">")
                .append(stringCell("A" + row, S_SIGN_ITALIC, "(Ký, ghi rõ họ tên)"))
                .append(stringCell(columnName(half + 1) + row, S_SIGN_ITALIC, "(Ký, ghi rõ họ tên)")).append("</row>");
        merges.add("A" + row + ":" + columnName(half) + row);
        merges.add(columnName(half + 1) + row + ":" + lastCol + row);
        int lastRow = row;

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n")
                .append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">")
                .append("<sheetPr><pageSetUpPr fitToPage=\"1\"/></sheetPr>")
                .append("<dimension ref=\"A1:").append(lastCol).append(lastRow).append("\"/>")
                .append("<sheetViews><sheetView showGridLines=\"0\" workbookViewId=\"0\"/></sheetViews>")
                .append("<sheetFormatPr defaultRowHeight=\"18\"/>")
                .append("<cols>");
        for (int c = 0; c < cols; c++) {
            xml.append("<col min=\"").append(c + 1).append("\" max=\"").append(c + 1).append("\" width=\"")
                    .append(r.widths().get(c)).append("\" customWidth=\"1\"/>");
        }
        xml.append("</cols><sheetData>").append(data).append("</sheetData>")
                .append("<mergeCells count=\"").append(merges.size()).append("\">");
        for (String m : merges) {
            xml.append("<mergeCell ref=\"").append(m).append("\"/>");
        }
        xml.append("</mergeCells>")
                .append("<printOptions horizontalCentered=\"1\"/>")
                .append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.75\" bottom=\"0.75\" header=\"0.3\" footer=\"0.3\"/>")
                // Báo cáo ngắn (đến ~50 dòng) in gọn trong 1 trang để khối chữ ký không bị tách sang trang riêng;
                // báo cáo dài thì chia trang và lặp lại tiêu đề bảng.
                .append("<pageSetup paperSize=\"9\" orientation=\"landscape\" fitToWidth=\"1\" fitToHeight=\"").append(lastRow <= 50 ? 1 : 0).append("\"/>")
                .append("<headerFooter><oddFooter>&amp;CTrang &amp;P / &amp;N</oddFooter></headerFooter>")
                .append("</worksheet>");
        return new SheetXml(xml.toString(), headerRow);
    }

    private static String textRow(int row, int style, int height, String text, int cols) {
        StringBuilder b = new StringBuilder();
        b.append("<row r=\"").append(row).append("\" ht=\"").append(height).append("\" customHeight=\"1\">");
        b.append(stringCell("A" + row, style, text));
        for (int c = 1; c < cols; c++) {
            b.append("<c r=\"").append(columnName(c + 1)).append(row).append("\" s=\"").append(style).append("\"/>");
        }
        return b.append("</row>").toString();
    }

    private static String stringCell(String ref, int style, String text) {
        return "<c r=\"" + ref + "\" s=\"" + style + "\" t=\"inlineStr\"><is><t xml:space=\"preserve\">"
                + escape(text == null ? "" : text) + "</t></is></c>";
    }

    private static boolean isInteger(String s) {
        return s != null && s.trim().matches("-?\\d{1,15}");
    }

    /** 1 -> A, 26 -> Z, 27 -> AA. */
    static String columnName(int index) {
        StringBuilder name = new StringBuilder();
        for (int n = index; n > 0; n = (n - 1) / 26) {
            name.insert(0, (char) ('A' + (n - 1) % 26));
        }
        return name.toString();
    }

    static String safeSheetName(String name) {
        String cleaned = (name == null || name.isBlank() ? "Báo cáo" : name).replaceAll("[\\[\\]:*?/\\\\]", " ").trim();
        return cleaned.length() > 31 ? cleaned.substring(0, 31) : cleaned;
    }

    private static String escape(String s) {
        StringBuilder b = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '&' -> b.append("&amp;");
                case '<' -> b.append("&lt;");
                case '>' -> b.append("&gt;");
                case '"' -> b.append("&quot;");
                default -> {
                    // XML 1.0 không cho phép các ký tự điều khiển (trừ tab, xuống dòng).
                    if (c >= 0x20 || c == '\t' || c == '\n' || c == '\r') {
                        b.append(c);
                    }
                }
            }
        }
        return b.toString();
    }

    private static void put(ZipOutputStream zip, String name, String content) throws IOException {
        zip.putNextEntry(new ZipEntry(name));
        zip.write(content.getBytes(StandardCharsets.UTF_8));
        zip.closeEntry();
    }

    private static String workbook(String sheetName, int headerRow) {
        String escaped = escape(sheetName);
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
                + "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">"
                + "<sheets><sheet name=\"" + escaped + "\" sheetId=\"1\" r:id=\"rId1\"/></sheets>"
                + "<definedNames><definedName name=\"_xlnm.Print_Titles\" localSheetId=\"0\">'" + escaped.replace("'", "''")
                + "'!$" + headerRow + ":$" + headerRow + "</definedName></definedNames></workbook>";
    }

    private static final String CONTENT_TYPES = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
            + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
            + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>"
            + "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>"
            + "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>"
            + "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>"
            + "</Types>";

    private static final String ROOT_RELS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>"
            + "</Relationships>";

    private static final String WORKBOOK_RELS = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
            + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>"
            + "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>"
            + "</Relationships>";

    // Phông: 0 thường, 1 đậm, 2 đậm cỡ 16, 3 nghiêng. Nền: 0 không, 1 bắt buộc theo chuẩn, 2 xám nhạt. Viền: 0 không, 1 mảnh.
    private static final String STYLES = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n"
            + "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">"
            + "<fonts count=\"4\">"
            + "<font><sz val=\"11\"/><name val=\"Arial\"/></font>"
            + "<font><b/><sz val=\"11\"/><name val=\"Arial\"/></font>"
            + "<font><b/><sz val=\"16\"/><name val=\"Arial\"/></font>"
            + "<font><i/><sz val=\"11\"/><name val=\"Arial\"/></font>"
            + "</fonts>"
            + "<fills count=\"3\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>"
            + "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFD9E2EC\"/><bgColor indexed=\"64\"/></patternFill></fill></fills>"
            + "<borders count=\"2\"><border><left/><right/><top/><bottom/><diagonal/></border>"
            + "<border><left style=\"thin\"><color auto=\"1\"/></left><right style=\"thin\"><color auto=\"1\"/></right>"
            + "<top style=\"thin\"><color auto=\"1\"/></top><bottom style=\"thin\"><color auto=\"1\"/></bottom><diagonal/></border></borders>"
            + "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>"
            + "<cellXfs count=\"11\">"
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\"/>"                                                          // 0 mặc định
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"       // 1 tên đơn vị
            + "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 2 tiêu đề
            + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 3 dòng phụ
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>"  // 4 thông tin
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyFill=\"1\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" // 5 tiêu đề bảng
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\" wrapText=\"1\" indent=\"1\"/></xf>" // 6 ô chữ
            + "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyBorder=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 7 ô số
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" // 8 dòng tổng hợp
            + "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 9 chữ ký đậm
            + "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\" applyAlignment=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" // 10 chữ ký nghiêng
            + "</cellXfs>"
            + "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>"
            + "</styleSheet>";
}
