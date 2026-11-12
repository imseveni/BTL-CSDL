package common;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Nội dung một báo cáo theo mẫu văn bản hành chính, dùng chung cho mọi báo cáo của hệ thống để các báo cáo
 * có cùng bố cục: tên đơn vị và quốc hiệu, tiêu đề, khối thông tin (kính gửi, người lập, mục đích), thẻ số liệu
 * tổng quan, bảng chi tiết, phần tổng kết và khối chữ ký. Việc vẽ nằm ở {@link BaoCaoPdf}.
 */
public record ReportSheet(String organization, String unit, String title, String subtitle, List<Info> info,
                          List<Stat> stats, List<String> columns, List<Integer> widths, List<Align> aligns,
                          List<List<String>> rows, int badgeColumn, List<Tone> badgeTones, String summaryTitle,
                          List<String> summaryLines, LocalDate issueDate, String signLeftTitle, String signLeftNote,
                          String signRightTitle, String signRightNote, String signRightName, String footerLabel) {

    public enum Align { LEFT, CENTER, RIGHT }

    /** Sắc thái màu của thẻ số liệu hoặc nhãn trạng thái. */
    public enum Tone { NONE, INFO, GOOD, WARN, BAD }

    /** Một dòng thông tin trước bảng, ví dụ "Người lập" – "Nguyễn Văn An – Chủ nhiệm". */
    public record Info(String label, String value) {
    }

    /** Một thẻ số liệu tổng quan. */
    public record Stat(String label, String value, Tone tone) {
    }

    public ReportSheet {
        if (columns.size() != widths.size() || columns.size() != aligns.size()) {
            throw new IllegalArgumentException("Độ rộng và căn lề phải có đủ cho " + columns.size() + " cột.");
        }
        for (List<String> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException("Mỗi dòng dữ liệu phải có đủ " + columns.size() + " ô.");
            }
        }
        if (badgeColumn >= columns.size() || (badgeColumn >= 0 && badgeTones.size() != rows.size())) {
            throw new IllegalArgumentException("Cột nhãn trạng thái không hợp lệ.");
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    /** Dựng báo cáo từng bước; đơn vị và địa danh lấy từ {@link ThongTinClb}. */
    public static final class Builder {
        private String title = "";
        private String subtitle;
        private final List<Info> info = new ArrayList<>();
        private final List<Stat> stats = new ArrayList<>();
        private final List<String> columns = new ArrayList<>();
        private final List<Integer> widths = new ArrayList<>();
        private final List<Align> aligns = new ArrayList<>();
        private List<List<String>> rows = new ArrayList<>();
        private int badgeColumn = -1;
        private List<Tone> badgeTones = List.of();
        private String summaryTitle = "Tổng kết";
        private List<String> summaryLines = List.of();
        private LocalDate issueDate = LocalDate.now();
        private String signLeftTitle = "CHỦ NHIỆM CÂU LẠC BỘ";
        private String signLeftNote = "(Ký, ghi rõ họ tên)";
        private String signRightTitle = "NGƯỜI LẬP BÁO CÁO";
        private String signRightNote = "(Ký, ghi rõ họ tên)";
        private String signRightName = "";
        private String footerLabel;

        public Builder title(String v) { title = v; return this; }
        public Builder subtitle(String v) { subtitle = v; return this; }
        public Builder info(String label, String value) { info.add(new Info(label, value)); return this; }
        public Builder stat(String label, Object value, Tone tone) { stats.add(new Stat(label, String.valueOf(value), tone)); return this; }
        public Builder column(String name, int width, Align align) { columns.add(name); widths.add(width); aligns.add(align); return this; }
        public Builder rows(List<List<String>> v) { rows = v; return this; }
        public Builder badge(int column, List<Tone> tones) { badgeColumn = column; badgeTones = tones; return this; }
        public Builder summary(String heading, List<String> lines) { summaryTitle = heading; summaryLines = lines; return this; }
        public Builder issueDate(LocalDate v) { issueDate = v; return this; }
        public Builder signLeft(String titleText, String note) { signLeftTitle = titleText; signLeftNote = note; return this; }
        public Builder signRight(String titleText, String note, String name) { signRightTitle = titleText; signRightNote = note; signRightName = name; return this; }
        public Builder footerLabel(String v) { footerLabel = v; return this; }

        public ReportSheet build() {
            return new ReportSheet(ThongTinClb.ten(), ThongTinClb.DON_VI_LAP, title, subtitle, List.copyOf(info),
                    List.copyOf(stats), List.copyOf(columns), List.copyOf(widths), List.copyOf(aligns), rows, badgeColumn,
                    badgeTones, summaryTitle, summaryLines, issueDate, signLeftTitle, signLeftNote, signRightTitle,
                    signRightNote, signRightName, footerLabel == null ? title : footerLabel);
        }
    }
}
