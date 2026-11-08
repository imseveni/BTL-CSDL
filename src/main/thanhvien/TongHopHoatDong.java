package thanhvien;

/** Một dòng của báo cáo "Tổng hợp hoạt động thành viên trong kỳ": số lần tham gia ở từng mảng. */
public record TongHopHoatDong(String msv, String hoVaTen, String tenBan, String chucVu, String trangThaiHD,
                              long soKhoaHoc, long soDuAn, long soSuKien, long soDanhGia, long soPhieuMuon,
                              long soGiaoDich, long tong) {
}
