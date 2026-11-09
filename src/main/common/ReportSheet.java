package common;

import java.util.List;
import java.util.Set;

/**
 * Nội dung một báo cáo dạng bảng theo mẫu văn bản hành chính: tên đơn vị, tiêu đề, kỳ báo cáo,
 * thông tin người lập và nơi nhận, bảng dữ liệu, dòng tổng hợp và khối chữ ký.
 * Dùng chung cho xem trước trên màn hình và xuất file Excel ({@link XlsxWriter}).
 *
 * @param sheetName      tên trang tính (tối đa 31 ký tự)
 * @param organization   tên đơn vị lập báo cáo, in góc trên bên trái
 * @param title          tiêu đề báo cáo, in giữa trang
 * @param subtitle       dòng phụ dưới tiêu đề (kỳ báo cáo, bộ lọc)
 * @param infoLines      các dòng thông tin trước bảng (người lập, nơi nhận, mục đích, ngày lập)
 * @param columns        tên cột
 * @param widths         độ rộng gợi ý của từng cột, tính theo số ký tự
 * @param numericColumns chỉ số các cột số (căn phải, ghi dạng số trong Excel)
 * @param rows           dữ liệu, mỗi dòng có đủ số ô bằng số cột
 * @param summaryLines   các dòng tổng hợp ngay dưới bảng
 * @param signDate       dòng ngày tháng phía trên khối chữ ký, ví dụ "Ngày 06 tháng 11 năm 2026"
 * @param signLeft       chức danh ký bên trái, ví dụ "NGƯỜI LẬP BÁO CÁO"
 * @param signRight      chức danh ký bên phải, ví dụ "CHỦ NHIỆM CÂU LẠC BỘ"
 */
public record ReportSheet(String sheetName, String organization, String title, String subtitle,
                          List<String> infoLines, List<String> columns, List<Integer> widths,
                          Set<Integer> numericColumns, List<List<String>> rows, List<String> summaryLines,
                          String signDate, String signLeft, String signRight) {

    public ReportSheet {
        if (columns.size() != widths.size()) {
            throw new IllegalArgumentException("Số độ rộng cột phải bằng số cột.");
        }
        for (List<String> row : rows) {
            if (row.size() != columns.size()) {
                throw new IllegalArgumentException("Mỗi dòng dữ liệu phải có đủ " + columns.size() + " ô.");
            }
        }
    }
}
