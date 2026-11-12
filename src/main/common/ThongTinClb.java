package common;

/**
 * Thông tin chung của Câu lạc bộ dùng để in ở đầu mọi báo cáo, để các báo cáo (thành viên, mượn – trả...) có
 * cùng một tiêu đề đơn vị. Đổi tên CLB bằng biến môi trường {@code CLB_TEN_CLB} hoặc {@code -Dclb.ten=...}.
 */
public final class ThongTinClb {

    /** In hoa ở đầu báo cáo, ví dụ "CÂU LẠC BỘ SINH VIÊN". */
    public static final String TEN_MAC_DINH = "CÂU LẠC BỘ SINH VIÊN";
    /** Đơn vị lập báo cáo: chỉ Ban Chủ nhiệm lập báo cáo (xem docs/PHAN_QUYEN.md). */
    public static final String DON_VI_LAP = "BAN CHỦ NHIỆM";
    /** Địa danh in trước ngày tháng ở phần ký. */
    public static final String DIA_DANH = "Hà Nội";

    private ThongTinClb() {
    }

    public static String ten() {
        String env = System.getenv("CLB_TEN_CLB");
        if (env != null && !env.isBlank()) {
            return env.strip().toUpperCase(java.util.Locale.forLanguageTag("vi-VN"));
        }
        String prop = System.getProperty("clb.ten");
        return prop != null && !prop.isBlank()
                ? prop.strip().toUpperCase(java.util.Locale.forLanguageTag("vi-VN")) : TEN_MAC_DINH;
    }
}
