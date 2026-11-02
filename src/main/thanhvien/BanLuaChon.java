package thanhvien;

/** Một ban chuyên môn để chọn trong danh sách thả xuống (hiển thị "BAN01 – Ban Chủ nhiệm"). */
public record BanLuaChon(String maBan, String tenBan) {

    @Override
    public String toString() {
        return maBan + " – " + tenBan;
    }
}
