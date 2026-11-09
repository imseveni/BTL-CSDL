package baocaomuontra;

/**
 * Thông tin thành viên trong Câu lạc bộ lập báo cáo mượn – trả cơ sở vật chất.
 */
public record NguoiLapBaoCao(
        String msv,
        String hoVaTen,
        String chucVu,
        String tenBan) {

    public static final NguoiLapBaoCao MAC_DINH = new NguoiLapBaoCao(
            "TV_CLB",
            "Thành viên Câu lạc bộ",
            "Thành viên",
            "Ban Cơ sở vật chất – Hậu cần");

    public String hoTenHienThi() {
        if (hoVaTen == null || hoVaTen.isBlank()) {
            return msvHienThi();
        }
        return hoVaTen.trim();
    }

    public String msvHienThi() {
        if (msv == null || msv.isBlank() || "TV_CLB".equals(msv)) {
            return "";
        }
        return msv.trim();
    }

    public String hoTenKemMsv() {
        if (hoVaTen == null || hoVaTen.isBlank()) {
            return msv == null ? "" : msv;
        }
        if (msv == null || msv.isBlank() || "TV_CLB".equals(msv)) {
            return hoVaTen;
        }
        return hoVaTen + " (" + msv + ")";
    }

    public String chucVuHienThi() {
        if (chucVu == null || chucVu.isBlank()) {
            return "Thành viên";
        }
        String ketQua = chucVu.trim().replaceAll("(?i)\\s+(CLB|Câu lạc bộ)$", "").trim();
        return ketQua.isEmpty() ? "Thành viên" : ketQua;
    }

    public String boPhanHienThi() {
        return (tenBan == null || tenBan.isBlank()) ? "Câu lạc bộ Sinh viên" : tenBan;
    }

    @Override
    public String toString() {
        if (msv == null || msv.isBlank() || "TV_CLB".equals(msv)) {
            return hoVaTen + " - " + chucVuHienThi();
        }
        return hoVaTen + " (" + msv + ") - " + chucVuHienThi() + " [" + boPhanHienThi() + "]";
    }
}
