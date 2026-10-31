package cosovatchat;

import java.util.List;
import java.util.Objects;

/** Kiểm tra dữ liệu nhập; quy tắc số lượng và tham chiếu do thủ tục/MySQL đảm bảo lại. */
public class CoSoVatChatService {
    private final CoSoVatChatDao dao;

    public CoSoVatChatService() {
        this(new CoSoVatChatDao());
    }

    public CoSoVatChatService(CoSoVatChatDao dao) {
        this.dao = Objects.requireNonNull(dao);
    }

    public List<CoSoVatChat> danhSach() {
        return dao.danhSach();
    }

    public List<CoSoVatChat> timKiem(String tuKhoa) {
        return dao.timKiem(tuKhoa == null ? "" : tuKhoa.strip());
    }

    public CoSoVatChat xemChiTiet(String ma) {
        CoSoVatChat tb = dao.timTheoMa(required(ma, "Mã thiết bị", 20));
        if (tb == null) {
            throw new IllegalArgumentException("Thiết bị không tồn tại.");
        }
        return tb;
    }

    /** soLuongKhaDung null -> bằng tổng; msvQuanLy null hoặc trống -> chưa có người quản lý. */
    public void themMoi(String ma, String ten, int tong, Integer soLuongKhaDung, String msvQuanLy) {
        String maSach = required(ma, "Mã thiết bị", 20);
        String tenSach = required(ten, "Tên thiết bị", 150);
        if (tong <= 0) {
            throw new IllegalArgumentException("Tổng số lượng phải lớn hơn 0.");
        }
        if (soLuongKhaDung != null && (soLuongKhaDung < 0 || soLuongKhaDung > tong)) {
            throw new IllegalArgumentException("Số lượng khả dụng phải từ 0 đến tổng số lượng.");
        }
        dao.themMoi(maSach, tenSach, tong, soLuongKhaDung, optional(msvQuanLy, "MSV người quản lý", 20));
    }

    public void capNhat(String ma, String ten, int tong) {
        String maSach = required(ma, "Mã thiết bị", 20);
        String tenSach = required(ten, "Tên thiết bị", 150);
        if (tong <= 0) {
            throw new IllegalArgumentException("Tổng số lượng phải lớn hơn 0.");
        }
        dao.capNhat(maSach, tenSach, tong);
    }

    /** msv null hoặc trống -> bỏ người quản lý. */
    public void ganNguoiQuanLy(String ma, String msv) {
        dao.ganNguoiQuanLy(required(ma, "Mã thiết bị", 20), optional(msv, "MSV người quản lý", 20));
    }

    public boolean kiemTraKhaDung(String ma, int soLuong) {
        String maSach = required(ma, "Mã thiết bị", 20);
        if (soLuong <= 0) {
            throw new IllegalArgumentException("Số lượng cần kiểm tra phải lớn hơn 0.");
        }
        return dao.kiemTraKhaDung(maSach, soLuong);
    }

    private String required(String value, String label, int max) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " không được để trống.");
        }
        String result = value.strip();
        if (result.codePointCount(0, result.length()) > max) {
            throw new IllegalArgumentException(label + " tối đa " + max + " ký tự.");
        }
        return result;
    }

    private String optional(String value, String label, int max) {
        return value == null || value.isBlank() ? null : required(value, label, max);
    }
}
