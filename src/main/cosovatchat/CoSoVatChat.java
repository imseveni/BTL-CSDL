package cosovatchat;

/** Dữ liệu bất biến của một thiết bị (kèm tên người quản lý lấy từ view). */
public record CoSoVatChat(String maThietBi, String tenThietBi, int tongSoLuong, int soLuongKhaDung,
                          String msvQuanLy, String tenNguoiQuanLy) {

    /** Số lượng đang được mượn = tổng - khả dụng. */
    public int soLuongDangMuon() {
        return tongSoLuong - soLuongKhaDung;
    }
}
