package common;

/** Người lập báo cáo: in ở khối thông tin ("Họ tên – Chức vụ") và dưới chữ ký (chỉ họ tên). */
public record NguoiLap(String hoVaTen, String chucVu) {

    public NguoiLap {
        hoVaTen = hoVaTen == null ? "" : hoVaTen.strip();
        chucVu = chucVu == null ? "" : chucVu.strip();
    }

    public String hienThi() {
        return chucVu.isEmpty() ? hoVaTen : hoVaTen + " – " + chucVu;
    }
}
