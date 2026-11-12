package common;

import common.ReportSheet.Align;
import common.ReportSheet.Stat;
import common.ReportSheet.Tone;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;

/**
 * Xuất {@link ReportSheet} ra PDF khổ A4 ngang theo mẫu văn bản hành chính. Mọi báo cáo của hệ thống đi qua
 * lớp này nên có cùng bố cục: quốc hiệu, tên đơn vị, tiêu đề, khối thông tin, thẻ số liệu, bảng, tổng kết,
 * chữ ký và chân trang "Trang x/y".
 *
 * <p>Không cần thư viện ngoài: mỗi trang được vẽ bằng Java2D ở 150 DPI với phông DejaVu Sans đóng gói trong
 * resources/fonts (đủ dấu tiếng Việt, không phụ thuộc phông của máy), rồi nhúng vào PDF 1.4 nén Flate. Hệ quả:
 * hiển thị giống nhau ở mọi trình đọc, nhưng chữ là ảnh nên không bôi đen được.
 */
public final class BaoCaoPdf {

    private static final int DPI = 150;
    static final int PAGE_W = 1754;
    static final int PAGE_H = 1240;
    private static final int MARGIN = 64;
    private static final int FOOTER_H = 60;

    private static final Color INK = new Color(33, 37, 41);
    private static final Color MUTED = new Color(90, 101, 119);
    private static final Color PRIMARY = new Color(24, 78, 119);
    private static final Color HEAD_BG = new Color(30, 96, 145);
    private static final Color BORDER = new Color(185, 200, 215);
    private static final Color ZEBRA = new Color(246, 250, 253);
    private static final Color BOX_BG = new Color(243, 248, 252);

    private BaoCaoPdf() {
    }

    public static Path withPdfExtension(Path path) {
        String name = path.getFileName().toString();
        return name.toLowerCase().endsWith(".pdf") ? path : path.resolveSibling(name + ".pdf");
    }

    public static Path write(ReportSheet sheet, Path file) throws IOException {
        Path target = withPdfExtension(file);
        if (target.getParent() != null) {
            Files.createDirectories(target.getParent());
        }
        try (OutputStream out = Files.newOutputStream(target)) {
            write(sheet, out);
        }
        return target;
    }

    public static void write(ReportSheet sheet, OutputStream out) throws IOException {
        writePdf(render(sheet), sheet.title(), vanBan(sheet), out);
    }

    /**
     * Toàn bộ nội dung báo cáo dạng chữ thường, được ghi vào thuộc tính Subject của PDF để tìm kiếm và đối chiếu
     * (chữ trên trang là ảnh nên không bôi đen được).
     */
    public static String vanBan(ReportSheet s) {
        StringBuilder sb = new StringBuilder();
        sb.append(s.organization()).append(" - ").append(s.unit()).append('\n').append(s.title()).append('\n');
        if (s.subtitle() != null && !s.subtitle().isBlank()) {
            sb.append(s.subtitle()).append('\n');
        }
        s.info().forEach(i -> sb.append(i.label()).append(": ").append(i.value()).append('\n'));
        s.stats().forEach(t -> sb.append(t.label()).append(": ").append(t.value()).append('\n'));
        sb.append(String.join(" | ", s.columns())).append('\n');
        s.rows().forEach(r -> sb.append(String.join(" | ", r).replace('\n', ' ')).append('\n'));
        s.summaryLines().forEach(l -> sb.append(l).append('\n'));
        sb.append(s.signRightTitle()).append(": ").append(s.signRightName());
        return sb.toString();
    }

    /** Vẽ báo cáo thành các ảnh trang; dùng để xuất PDF, xem trước và kiểm thử. */
    public static List<BufferedImage> render(ReportSheet sheet) throws IOException {
        Fonts fonts = new Fonts(font("fonts/DejaVuSans.ttf"), font("fonts/DejaVuSans-Bold.ttf"));
        Layout layout = new Layout(sheet, fonts);
        layout.build();
        List<BufferedImage> images = new ArrayList<>();
        int total = layout.pages.size();
        for (int i = 0; i < total; i++) {
            BufferedImage image = new BufferedImage(PAGE_W, PAGE_H, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = image.createGraphics();
            hints(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, PAGE_W, PAGE_H);
            layout.pages.get(i).forEach(draw -> draw.accept(g));
            drawFooter(g, fonts, sheet, i + 1, total);
            g.dispose();
            images.add(image);
        }
        return images;
    }

    private static void hints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    private static Font font(String resource) throws IOException {
        try (InputStream in = BaoCaoPdf.class.getClassLoader().getResourceAsStream(resource)) {
            if (in == null) {
                throw new IOException("Thiếu phông chữ " + resource + " trong resources.");
            }
            return Font.createFont(Font.TRUETYPE_FONT, in);
        } catch (FontFormatException e) {
            throw new IOException("Không đọc được phông chữ " + resource + ": " + e.getMessage(), e);
        }
    }

    private record Fonts(Font regular, Font bold) {
        Font plain(float size) {
            return regular.deriveFont(size);
        }

        Font strong(float size) {
            return bold.deriveFont(size);
        }
    }

    private static void drawFooter(Graphics2D g, Fonts f, ReportSheet s, int page, int total) {
        int y = PAGE_H - 34;
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(MARGIN, y - 22, PAGE_W - MARGIN, y - 22);
        g.setColor(MUTED);
        g.setFont(f.plain(15));
        g.drawString(s.footerLabel() + "  |  " + s.organization(), MARGIN, y);
        String right = "Trang " + page + "/" + total;
        g.drawString(right, PAGE_W - MARGIN - g.getFontMetrics().stringWidth(right), y);
    }

    /** Bố cục: gom lệnh vẽ theo từng trang rồi mới vẽ thật để biết tổng số trang. */
    private static final class Layout {
        final ReportSheet s;
        final Fonts f;
        final List<List<Consumer<Graphics2D>>> pages = new ArrayList<>();
        final Graphics2D measure;
        List<Consumer<Graphics2D>> page;
        int y;
        final int contentW = PAGE_W - 2 * MARGIN;
        final int bottom = PAGE_H - MARGIN - FOOTER_H + 10;
        int[] colX;
        int[] colW;

        Layout(ReportSheet s, Fonts f) {
            this.s = s;
            this.f = f;
            BufferedImage scratch = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
            measure = scratch.createGraphics();
            hints(measure);
        }

        FontMetrics fm(Font font) {
            return measure.getFontMetrics(font);
        }

        void newPage(boolean first) {
            page = new ArrayList<>();
            pages.add(page);
            y = MARGIN - 10;
            if (first) {
                header();
            } else {
                miniHeader();
            }
        }

        void build() {
            computeColumns();
            newPage(true);
            infoBlock();
            statCards();
            table();
            summary();
            signature();
            measure.dispose();
        }

        // ---- đầu trang ----

        void header() {
            int leftW = (int) (contentW * 0.40);
            int rightW = (int) (contentW * 0.50);
            int rightX = PAGE_W - MARGIN - rightW;
            int top = y;
            centered(s.organization(), MARGIN, leftW, top + 22, f.strong(19), INK);
            centered(s.unit(), MARGIN, leftW, top + 50, f.plain(17), INK);
            line(MARGIN + leftW / 2 - 90, top + 62, MARGIN + leftW / 2 + 90, top + 62, INK, 1.4f);
            centered("CỘNG HÒA XÃ HỘI CHỦ NGHĨA VIỆT NAM", rightX, rightW, top + 22, f.strong(18), INK);
            centered("Độc lập – Tự do – Hạnh phúc", rightX, rightW, top + 50, f.strong(17), INK);
            line(rightX + rightW / 2 - 130, top + 62, rightX + rightW / 2 + 130, top + 62, INK, 1.4f);
            centered(ngayThang(s.issueDate()), rightX, rightW, top + 96, f.plain(17).deriveFont(Font.ITALIC), INK);
            y = top + 140;
            centered(s.title(), MARGIN, contentW, y + 30, f.strong(34), PRIMARY);
            y += 44;
            if (s.subtitle() != null && !s.subtitle().isBlank()) {
                centered(s.subtitle(), MARGIN, contentW, y + 18, f.plain(18).deriveFont(Font.ITALIC), MUTED);
                y += 30;
            }
            y += 16;
        }

        void miniHeader() {
            int top = y;
            text(s.organization(), MARGIN, top + 20, f.strong(16), INK);
            String right = s.title();
            Font font = f.strong(16);
            text(right, PAGE_W - MARGIN - fm(font).stringWidth(right), top + 20, font, PRIMARY);
            line(MARGIN, top + 32, PAGE_W - MARGIN, top + 32, BORDER, 1.2f);
            y = top + 52;
        }

        // ---- khối thông tin ----

        void infoBlock() {
            if (s.info().isEmpty()) {
                return;
            }
            Font label = f.strong(18);
            Font value = f.plain(18);
            int labelW = 0;
            for (ReportSheet.Info i : s.info()) {
                labelW = Math.max(labelW, fm(label).stringWidth(i.label() + ":"));
            }
            labelW += 18;
            int valueW = contentW - labelW;
            for (ReportSheet.Info i : s.info()) {
                List<String> lines = wrap(i.value(), fm(value), valueW);
                int yy = y;
                text(i.label() + ":", MARGIN, yy + 20, label, INK);
                for (String l : lines) {
                    text(l, MARGIN + labelW, yy + 20, value, INK);
                    yy += 26;
                }
                y = yy + 2;
            }
            y += 14;
        }

        // ---- thẻ số liệu ----

        void statCards() {
            if (s.stats().isEmpty()) {
                return;
            }
            int n = s.stats().size();
            int gap = 18;
            int w = (contentW - gap * (n - 1)) / n;
            int h = 92;
            if (y + h > bottom) {
                newPage(false);
            }
            for (int i = 0; i < n; i++) {
                Stat st = s.stats().get(i);
                int x = MARGIN + i * (w + gap);
                int top = y;
                Color accent = tone(st.tone()).fg();
                page.add(g -> {
                    g.setColor(Color.WHITE);
                    g.fillRoundRect(x, top, w, h, 10, 10);
                    g.setColor(BORDER);
                    g.setStroke(new BasicStroke(1.2f));
                    g.drawRoundRect(x, top, w, h, 10, 10);
                    g.setColor(accent);
                    g.fillRoundRect(x, top, 7, h, 6, 6);
                });
                Font lf = f.plain(16);
                List<String> lab = wrap(st.label(), fm(lf), w - 36);
                text(lab.get(0), x + 20, top + 28, lf, MUTED);
                text(st.value(), x + 20, top + 74, f.strong(34), accent);
            }
            y += h + 22;
        }

        // ---- bảng ----

        void computeColumns() {
            int sum = s.widths().stream().mapToInt(Integer::intValue).sum();
            colW = new int[s.columns().size()];
            colX = new int[colW.length];
            int used = 0;
            for (int i = 0; i < colW.length; i++) {
                colW[i] = i == colW.length - 1 ? contentW - used : contentW * s.widths().get(i) / sum;
                colX[i] = MARGIN + used;
                used += colW[i];
            }
        }

        void table() {
            Font hf = f.strong(16);
            Font bf = f.plain(16);
            int pad = 10;
            int lineH = 22;
            List<String> head = s.columns();
            int headH = 14 + lineH * headerLines(hf, pad);
            if (y + headH + 60 > bottom) {
                newPage(false);
            }
            drawTableHead(head, hf, headH, pad);
            for (int r = 0; r < s.rows().size(); r++) {
                List<String> row = s.rows().get(r);
                List<List<String>> wrapped = new ArrayList<>();
                int lines = 1;
                for (int c = 0; c < row.size(); c++) {
                    List<String> cellLines = new ArrayList<>();
                    for (String part : row.get(c).split("\n", -1)) {
                        cellLines.addAll(wrap(part, fm(bf), colW[c] - 2 * pad));
                    }
                    wrapped.add(cellLines);
                    lines = Math.max(lines, cellLines.size());
                }
                int rowH = 16 + lineH * lines;
                if (y + rowH > bottom) {
                    newPage(false);
                    drawTableHead(head, hf, headH, pad);
                }
                drawRow(r, wrapped, rowH, bf, pad, lineH);
                y += rowH;
            }
            if (s.rows().isEmpty()) {
                int top = y;
                page.add(g -> {
                    g.setColor(Color.WHITE);
                    g.fillRect(MARGIN, top, contentW, 56);
                    g.setColor(BORDER);
                    g.drawRect(MARGIN, top, contentW, 56);
                });
                centered("Không có dữ liệu trong phạm vi báo cáo.", MARGIN, contentW, top + 35, f.plain(17).deriveFont(Font.ITALIC), MUTED);
                y += 56;
            }
            y += 24;
        }

        int headerLines(Font hf, int pad) {
            int max = 1;
            for (int c = 0; c < colW.length; c++) {
                max = Math.max(max, wrap(s.columns().get(c), fm(hf), colW[c] - 2 * pad).size());
            }
            return max;
        }

        void drawTableHead(List<String> head, Font hf, int headH, int pad) {
            int top = y;
            page.add(g -> {
                g.setColor(HEAD_BG);
                g.fillRect(MARGIN, top, contentW, headH);
            });
            for (int c = 0; c < head.size(); c++) {
                List<String> lines = wrap(head.get(c), fm(hf), colW[c] - 2 * pad);
                int yy = top + 7 + 17;
                for (String l : lines) {
                    centered(l, colX[c], colW[c], yy, hf, Color.WHITE);
                    yy += 22;
                }
            }
            y += headH;
        }

        void drawRow(int r, List<List<String>> wrapped, int rowH, Font bf, int pad, int lineH) {
            int top = y;
            boolean zebra = r % 2 == 1;
            page.add(g -> {
                g.setColor(zebra ? ZEBRA : Color.WHITE);
                g.fillRect(MARGIN, top, contentW, rowH);
                g.setColor(BORDER);
                g.setStroke(new BasicStroke(1f));
                g.drawLine(MARGIN, top + rowH, MARGIN + contentW, top + rowH);
                g.drawLine(MARGIN, top, MARGIN, top + rowH);
                g.drawLine(MARGIN + contentW, top, MARGIN + contentW, top + rowH);
                for (int c = 1; c < colW.length; c++) {
                    g.drawLine(colX[c], top, colX[c], top + rowH);
                }
            });
            for (int c = 0; c < wrapped.size(); c++) {
                if (c == s.badgeColumn()) {
                    badge(c, wrapped.get(c), s.badgeTones().get(r), top, rowH, pad);
                    continue;
                }
                int yy = top + 8 + 17;
                for (String l : wrapped.get(c)) {
                    switch (s.aligns().get(c)) {
                        case CENTER -> centered(l, colX[c], colW[c], yy, bf, INK);
                        case RIGHT -> text(l, colX[c] + colW[c] - pad - fm(bf).stringWidth(l), yy, bf, INK);
                        default -> text(l, colX[c] + pad, yy, bf, INK);
                    }
                    yy += lineH;
                }
            }
        }

        void badge(int c, List<String> lines, Tone tone, int top, int rowH, int pad) {
            String label = String.join(" ", lines);
            Font bf = f.strong(15);
            int w = Math.min(colW[c] - 12, fm(bf).stringWidth(label) + 26);
            int h = 30;
            int x = colX[c] + (colW[c] - w) / 2;
            int yy = top + (rowH - h) / 2;
            ToneColors tc = tone(tone);
            page.add(g -> {
                g.setColor(tc.bg());
                g.fillRoundRect(x, yy, w, h, 16, 16);
            });
            String shown = label;
            while (fm(bf).stringWidth(shown) > w - 10 && shown.length() > 1) {
                shown = shown.substring(0, shown.length() - 1);
            }
            centered(shown, x, w, yy + 21, bf, tc.fg());
        }

        // ---- tổng kết và chữ ký ----

        void summary() {
            if (s.summaryLines().isEmpty()) {
                return;
            }
            Font tf = f.strong(18);
            Font lf = f.plain(17);
            List<String> all = new ArrayList<>();
            for (String line : s.summaryLines()) {
                all.addAll(wrap("•  " + line, fm(lf), contentW - 40));
            }
            int h = 24 + 34 + all.size() * 26;
            if (y + h > bottom) {
                newPage(false);
            }
            int top = y;
            page.add(g -> {
                g.setColor(BOX_BG);
                g.fillRoundRect(MARGIN, top, contentW, h, 10, 10);
                g.setColor(BORDER);
                g.setStroke(new BasicStroke(1.2f));
                g.drawRoundRect(MARGIN, top, contentW, h, 10, 10);
            });
            text(s.summaryTitle().toUpperCase(java.util.Locale.forLanguageTag("vi-VN")), MARGIN + 20, top + 30, tf, PRIMARY);
            int yy = top + 62;
            for (String l : all) {
                text(l, MARGIN + 20, yy, lf, INK);
                yy += 26;
            }
            y = top + h + 24;
        }

        void signature() {
            int h = 190;
            if (y + h > bottom) {
                newPage(false);
            }
            int colW2 = contentW / 2;
            int top = y;
            Font tf = f.strong(18);
            Font nf = f.plain(16).deriveFont(Font.ITALIC);
            Font nameFont = f.strong(18);
            centered(ngayThang(s.issueDate()), MARGIN + colW2, colW2, top + 20, f.plain(17).deriveFont(Font.ITALIC), INK);
            centered(s.signLeftTitle(), MARGIN, colW2, top + 54, tf, INK);
            centered(s.signRightTitle(), MARGIN + colW2, colW2, top + 54, tf, INK);
            centered(s.signLeftNote(), MARGIN, colW2, top + 78, nf, MUTED);
            centered(s.signRightNote(), MARGIN + colW2, colW2, top + 78, nf, MUTED);
            if (s.signRightName() != null && !s.signRightName().isBlank()) {
                centered(s.signRightName(), MARGIN + colW2, colW2, top + 170, nameFont, INK);
            }
            y = top + h;
        }

        // ---- tiện ích vẽ ----

        void text(String t, int x, int baseline, Font font, Color color) {
            page.add(g -> {
                g.setFont(font);
                g.setColor(color);
                g.drawString(t, x, baseline);
            });
        }

        void centered(String t, int x, int width, int baseline, Font font, Color color) {
            int w = fm(font).stringWidth(t);
            text(t, x + Math.max(0, (width - w) / 2), baseline, font, color);
        }

        void line(int x1, int y1, int x2, int y2, Color color, float width) {
            page.add(g -> {
                g.setColor(color);
                g.setStroke(new BasicStroke(width));
                g.drawLine(x1, y1, x2, y2);
            });
        }
    }

    private record ToneColors(Color bg, Color fg) {
    }

    private static ToneColors tone(Tone t) {
        return switch (t) {
            case GOOD -> new ToneColors(new Color(219, 244, 228), new Color(21, 115, 71));
            case WARN -> new ToneColors(new Color(255, 243, 205), new Color(153, 101, 0));
            case BAD -> new ToneColors(new Color(248, 215, 218), new Color(176, 42, 55));
            case INFO, NONE -> new ToneColors(new Color(222, 236, 247), new Color(24, 78, 119));
        };
    }

    static String ngayThang(LocalDate d) {
        LocalDate date = d == null ? LocalDate.now() : d;
        return String.format("%s, ngày %02d tháng %02d năm %d", ThongTinClb.DIA_DANH, date.getDayOfMonth(),
                date.getMonthValue(), date.getYear());
    }

    /** Xuống dòng theo chiều rộng; từ dài hơn ô thì cắt theo ký tự. */
    static List<String> wrap(String text, FontMetrics fm, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String t = text == null ? "" : text.trim();
        if (t.isEmpty()) {
            lines.add("");
            return lines;
        }
        StringBuilder line = new StringBuilder();
        for (String word : t.split("\\s+")) {
            String candidate = line.length() == 0 ? word : line + " " + word;
            if (fm.stringWidth(candidate) <= maxWidth) {
                line = new StringBuilder(candidate);
                continue;
            }
            if (line.length() > 0) {
                lines.add(line.toString());
                line = new StringBuilder();
            }
            String rest = word;
            while (fm.stringWidth(rest) > maxWidth && rest.length() > 1) {
                int cut = rest.length() - 1;
                while (cut > 1 && fm.stringWidth(rest.substring(0, cut)) > maxWidth) {
                    cut--;
                }
                lines.add(rest.substring(0, cut));
                rest = rest.substring(cut);
            }
            line = new StringBuilder(rest);
        }
        if (line.length() > 0) {
            lines.add(line.toString());
        }
        return lines;
    }

    // ---- Ghi PDF 1.4: mỗi trang là một ảnh RGB nén Flate phủ kín trang A4 ngang ----

    private static void writePdf(List<BufferedImage> pages, String title, String text, OutputStream out) throws IOException {
        ByteArrayOutputStream body = new ByteArrayOutputStream();
        List<Integer> offsets = new ArrayList<>();
        write(body, "%PDF-1.4\n%âãÏÓ\n");
        int count = pages.size();
        offsets.add(body.size());
        write(body, "1 0 obj\n<< /Type /Catalog /Pages 2 0 R >>\nendobj\n");
        offsets.add(body.size());
        StringBuilder kids = new StringBuilder();
        for (int i = 0; i < count; i++) {
            kids.append(4 + i * 3).append(" 0 R ");
        }
        write(body, "2 0 obj\n<< /Type /Pages /Count " + count + " /Kids [" + kids + "] >>\nendobj\n");
        offsets.add(body.size());
        write(body, "3 0 obj\n<< /Title <" + utf16Hex(title) + "> /Subject <" + utf16Hex(text) + "> /Producer (Quan ly CLB) >>\nendobj\n");
        for (int i = 0; i < count; i++) {
            BufferedImage image = pages.get(i);
            int w = image.getWidth();
            int h = image.getHeight();
            double wPt = w * 72.0 / DPI;
            double hPt = h * 72.0 / DPI;
            int pageId = 4 + i * 3;
            int contentId = pageId + 1;
            int imageId = pageId + 2;
            offsets.add(body.size());
            write(body, pageId + " 0 obj\n<< /Type /Page /Parent 2 0 R /MediaBox [0 0 " + fmt(wPt) + " " + fmt(hPt)
                    + "] /Resources << /XObject << /Im0 " + imageId + " 0 R >> >> /Contents " + contentId + " 0 R >>\nendobj\n");
            String content = "q " + fmt(wPt) + " 0 0 " + fmt(hPt) + " 0 0 cm /Im0 Do Q";
            offsets.add(body.size());
            write(body, contentId + " 0 obj\n<< /Length " + content.length() + " >>\nstream\n" + content + "\nendstream\nendobj\n");
            byte[] pixels = deflate(rgb(image));
            offsets.add(body.size());
            write(body, imageId + " 0 obj\n<< /Type /XObject /Subtype /Image /Width " + w + " /Height " + h
                    + " /ColorSpace /DeviceRGB /BitsPerComponent 8 /Filter /FlateDecode /Length " + pixels.length
                    + " >>\nstream\n");
            body.write(pixels);
            write(body, "\nendstream\nendobj\n");
        }
        int xref = body.size();
        StringBuilder table = new StringBuilder("xref\n0 " + (offsets.size() + 1) + "\n0000000000 65535 f \n");
        for (int offset : offsets) {
            table.append(String.format("%010d 00000 n \n", offset));
        }
        table.append("trailer\n<< /Size ").append(offsets.size() + 1).append(" /Root 1 0 R /Info 3 0 R >>\nstartxref\n")
                .append(xref).append("\n%%EOF\n");
        write(body, table.toString());
        out.write(body.toByteArray());
        out.flush();
    }

    private static byte[] rgb(BufferedImage image) {
        int w = image.getWidth();
        int h = image.getHeight();
        byte[] data = new byte[w * h * 3];
        int[] row = new int[w];
        int p = 0;
        for (int y = 0; y < h; y++) {
            image.getRGB(0, y, w, 1, row, 0, w);
            for (int x = 0; x < w; x++) {
                int argb = row[x];
                data[p++] = (byte) (argb >> 16);
                data[p++] = (byte) (argb >> 8);
                data[p++] = (byte) argb;
            }
        }
        return data;
    }

    private static byte[] deflate(byte[] data) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (DeflaterOutputStream out = new DeflaterOutputStream(bytes, new Deflater(6))) {
            out.write(data);
        }
        return bytes.toByteArray();
    }

    private static String utf16Hex(String s) {
        StringBuilder sb = new StringBuilder("FEFF");
        for (char c : (s == null ? "" : s).toCharArray()) {
            sb.append(String.format("%04X", (int) c));
        }
        return sb.toString();
    }

    private static String fmt(double v) {
        return String.format(java.util.Locale.ROOT, "%.2f", v);
    }

    private static void write(ByteArrayOutputStream out, String text) {
        out.writeBytes(text.getBytes(StandardCharsets.ISO_8859_1));
    }
}
