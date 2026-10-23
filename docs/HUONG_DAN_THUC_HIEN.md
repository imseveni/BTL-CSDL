# Hướng dẫn thực hiện phần việc của từng thành viên

Đọc **Phần A** (chung cho cả nhóm), sau đó đọc **phần riêng của mình** ở Phần B.
Việc nào chưa rõ thì tạo Issue hỏi Tú, đừng tự sửa file của người khác.

---

## Phần A. Quy trình chung

### A1. Đọc theo thứ tự này
1. `README.md`: cách dựng DB và chạy dự án.
2. `docs/erd/ERD_trang2_mo_hinh_ER.png`: ERD. Tìm khung của module mình, xem thuộc tính và các đường nối (1, N, M).
3. `docs/erd/ERD_trang3_mo_hinh_quan_he.png`: mô hình quan hệ (gạch chân = khóa chính, gạch đứt = khóa ngoại).
4. `sql/00_schema/schema.sql`: định nghĩa bảng thật. **Nếu hình và schema khác nhau, báo Tú**, đừng tự sửa.
5. `docs/DECISIONS.md`: các điểm lệch giữa tài liệu đã xử lý thế nào.

> Cách đọc ERD: oval gạch chân là khóa chính; oval đôi (đỏ) là thuộc tính đa trị nên tách thành bảng riêng; oval nét đứt là thuộc tính suy diễn (tính từ dữ liệu khác).

### A2. Quy trình mỗi lần làm việc
1. `git checkout develop && git pull`
2. `git checkout -b feature/<module>-<mô-tả>` (ví dụ `feature/khoahoc-tra-cuu`)
3. Dựng DB sạch: `cd sql && mysql -u root -p < reset_db.sql && mysql -u root -p < run_all.sql`
4. Làm việc theo A3 và A4 bên dưới.
5. Chạy lại bước 3 từ DB trống, không được có lỗi.
6. `git add`, `git commit -m "[module] mô tả"`, `git pull --rebase origin develop`, `git push`
7. Mở Pull Request vào `develop`, điền checklist trong template, chờ Tú review.

### A3. Thư mục của bạn: viết SQL
Mỗi module có 3 file trong `sql/modules/NN_<ten>/`:

| File | Nội dung | Có chạy trong `run_all.sql` |
|---|---|---|
| `seed.sql` | Dữ liệu mẫu (INSERT) cho bảng của module | Có |
| `queries.sql` | Truy vấn, view, stored procedure, trigger | Có |
| `tests.sql` | Ca thử đúng và sai | Không (tự chạy khi cần) |

Quy tắc:
- **Thứ tự chạy:** schema, `seed_master` (của Tú), seed các module theo số thứ tự 01 đến 10, rồi `queries` các module. Vì vậy `seed.sql` chỉ được tham chiếu mã thành viên và mã ban có trong `seed_master.sql`, và tham chiếu bảng của module khác chỉ khi module đó có số thứ tự nhỏ hơn.
- **Dải mã riêng:** dùng tiền tố của module (`KH01`, `DA01`, `SK01`, `GD01`, `TB01`, `PM01`...) để không trùng nhau.
- **Viết lại được nhiều lần:** trước mỗi `CREATE` thủ tục, trigger, view đều có `DROP ... IF EXISTS`.
- **Không sửa:** `schema.sql`, `seed_master.sql`, `run_all.sql`. Cần đổi thì tạo Issue cho Tú.
- **Cần thêm file SQL mới:** báo Tú thêm một dòng `SOURCE` vào `run_all.sql`.

Mẫu trigger và thủ tục:
```sql
USE QuanLyCauLacBo;

DROP TRIGGER IF EXISTS trg_TenTrigger;
DELIMITER $$
CREATE TRIGGER trg_TenTrigger BEFORE INSERT ON TenBang
FOR EACH ROW
BEGIN
    IF NEW.SoLuong <= 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Số lượng phải lớn hơn 0';
    END IF;
END$$
DELIMITER ;

DROP PROCEDURE IF EXISTS sp_TenThuTuc;
DELIMITER $$
CREATE PROCEDURE sp_TenThuTuc(IN p_Ma VARCHAR(20))
BEGIN
    SELECT * FROM TenBang WHERE Ma = p_Ma;
END$$
DELIMITER ;
```

### A4. Thư mục của bạn: viết Java
Code nằm trong `src/main/<module>/`. Mỗi chức năng đi qua 4 lớp, tách bằng hậu tố tên file:

| File | Việc | Ví dụ |
|---|---|---|
| `X.java` | Thực thể, các trường giống cột của bảng | `KhoaHoc` |
| `XDao.java` | **Chỉ chỗ này** được chạm vào DB (JDBC) | `KhoaHocDao` |
| `XService.java` | Kiểm tra nghiệp vụ, gọi Dao | `KhoaHocService` |
| `XUi.java` | Nhập, xuất trên console | `KhoaHocUi` |

Cuối cùng gọi `XUi` trong `<Ten>Module.run(...)` (đã có sẵn file khung).
Cứ **xem module `thanhvien` do Tú làm xong đầu tiên** để theo cùng một kiểu.

Mẫu Dao (luôn dùng `PreparedStatement` và try-with-resources):
```java
package khoahoc;

import common.DataAccessException;
import config.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class KhoaHocDao {

    public List<String> timTheoTen(String tuKhoa) {
        String sql = "SELECT TenKhoaHoc FROM KhoaHoc WHERE TenKhoaHoc LIKE ?";
        List<String> ketQua = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, "%" + tuKhoa + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(rs.getString("TenKhoaHoc"));
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException("Lỗi tìm khóa học", e);
        }
        return ketQua;
    }
}
```
Khi một thao tác ghi nhiều bảng, dùng transaction: `c.setAutoCommit(false)`, cuối cùng `c.commit()`; có lỗi thì `c.rollback()`.

### A5. Hoàn thành một chức năng nghĩa là
- [ ] Có SQL (truy vấn hoặc thủ tục) trong `queries.sql` chạy đúng.
- [ ] Có code Java gọi được từ menu của module.
- [ ] Có ít nhất 1 ca đúng và 1 ca sai trong `tests.sql`.
- [ ] Đã ghi vào `docs/bao-cao/NN_<module>.md` (mô tả bảng, ràng buộc, ví dụ chạy).
- [ ] Chạy từ DB trống không lỗi, rồi mới mở Pull Request.

### A6. Lỗi hay gặp
- Quên `USE QuanLyCauLacBo;` ở đầu file SQL.
- Quên `DELIMITER` khi viết trigger, thủ tục.
- INSERT sai thứ tự (bảng con trước bảng cha) sẽ lỗi khóa ngoại.
- Nối chuỗi SQL trong Java thay vì dùng `?`.
- Commit nhầm `db.properties` (chứa mật khẩu); file này đã bị `.gitignore`, nếu `git status` hiện ra thì đừng add.
- Sửa file của người khác, dẫn đến conflict khi merge.

---

## Phần B. Việc của từng người

Thứ tự phụ thuộc: **Tú (schema, seed nền, Module 01) xong trước**, sau đó 4 người còn lại làm song song.
Trong lúc chờ Tú, hãy đọc ERD và viết trước các truy vấn, thủ tục; chưa cần chạy thật.

### B1. Tú: khung dự án, Module 01, tích hợp

**Bảng:** `ThanhVien` (MSV, HoVaTen, GioiTinh, NgaySinh, NgayGiaNhap, TrangThaiHD, ChucVu, MaBan), `ThanhVien_SDT` (MSV, SDT), `ThanhVien_Email` (MSV, Email).
SĐT và Email là thuộc tính đa trị nên mỗi thành viên có thể có nhiều dòng.

**Việc, theo thứ tự:**
1. Cả nhóm xác nhận các dòng trong `docs/DECISIONS.md`.
2. Viết `sql/01_seed_master/seed_master.sql`: vài ban chuyên môn, 15 đến 20 thành viên (kèm SĐT, email), vài quyền hạn.
3. Làm Module 01 hoàn chỉnh (SQL + Java) làm mẫu: thêm, sửa, tìm theo MSV hoặc tên, xem chi tiết, đổi trạng thái hoạt động, thêm/xóa SĐT và email.
4. Review và merge Pull Request của 4 bạn. Mỗi lần merge, chạy `run_all.sql` từ DB trống.
5. Làm truy vấn liên module (lịch sử hoạt động của một thành viên: khóa học, dự án, sự kiện, đánh giá).
6. Giai đoạn cuối: phân quyền (user/role MySQL hoặc view giới hạn cho thành viên thường so với Ban Chủ nhiệm) và tổng hợp báo cáo.

**Lưu ý:** nên đổi trạng thái hoạt động thay vì xóa cứng thành viên, vì nhiều bảng khác đang tham chiếu đến `MSV`.
**Test gợi ý:** `NgayGiaNhap` trước `NgaySinh` bị chặn; `MaBan` không tồn tại bị chặn; thêm SĐT trùng cho cùng một MSV bị chặn.

### B2. Đông: Module 02, 03, 08

**Module 02, Ban chuyên môn và Chức vụ**
- Bảng: `BanChuyenMon` (MaBan, TenBan). Quan hệ "Thuộc về": 1 ban có nhiều thành viên, mỗi thành viên phải thuộc đúng 1 ban (`ThanhVien.MaBan` bắt buộc).
- Chức vụ là cột `ChucVu` của `ThanhVien`, nên module 02 cập nhật cột này bằng thủ tục của mình (ví dụ `sp_DoiChucVu`, `sp_ChuyenBan`), **không sửa code module 01**.
- Việc: thêm, sửa, xóa ban; xem danh sách ban; xem thành viên theo ban (có đếm số lượng); chuyển ban; gán chức vụ.
- Lưu ý: xóa ban còn thành viên sẽ bị chặn (khóa ngoại), nên viết thủ tục kiểm tra trước và báo lỗi dễ hiểu.

**Module 03, Quyền hạn**
- Bảng: `QuyenHan` (MaQuyenHan, TenQuyenHan, MoTaChiTiet), `ThanhVien_QuyenHan` (MSV, MaQuyenHan). Quan hệ "Có": nhiều-nhiều.
- Việc: thêm, sửa, xóa quyền hạn; cấp quyền cho thành viên; thu hồi quyền; xem quyền của một thành viên; xem thành viên có một quyền.
- Test gợi ý: cấp trùng một quyền cho cùng một thành viên bị chặn bởi khóa chính.

**Module 08, Đánh giá**
- Bảng: `DanhGia` (MaGhiNhan, LoaiDanhGia, HinhThuc, LyDo, NgayQuyetDinh, MSV). Quan hệ "Nhận": 1 thành viên có nhiều đánh giá.
- Việc: tạo, cập nhật đánh giá; tra cứu lịch sử đánh giá của một thành viên; thống kê số đánh giá theo loại.
- Lưu ý: `LoaiDanhGia` (khen thưởng, kỷ luật...) nên thống nhất danh sách giá trị với Tú để thống kê đúng.

**Báo cáo:** bổ sung vào mô hình quan hệ các bảng Quyền hạn, TV–Quyền hạn, Đánh giá (xem `DECISIONS.md` mục 10).

### B3. Chiến: Module 04, 05

**Module 04, Khóa học**
- Bảng: `KhoaHoc` (MaKhoaHoc, TenKhoaHoc, NgayKhaiGiang, NgayBeGiang), `ThamGiaKhoaHoc` (MSV, MaKhoaHoc, VaiTro, KetQuaDanhGia). Quan hệ "Tham gia": nhiều-nhiều.
- Việc: thêm, sửa, xóa khóa học; thêm thành viên vào khóa kèm vai trò; cập nhật kết quả đánh giá; xem thành viên của khóa; xem khóa học của một thành viên.
- Test gợi ý: ngày bế giảng nhỏ hơn ngày khai giảng bị chặn.

**Module 05, Dự án**
- Bảng: `DuAn` (MaDuAn, TenDuAn, MucTieu, NgayBatDau, HanHoanThanh, TienDoChung), `ThamGiaDuAn` (MSV, MaDuAn, VaiTro, TienDoRieng).
- **Điểm khó:** `TienDoChung` là thuộc tính suy diễn (oval nét đứt trong ERD), bằng trung bình `TienDoRieng` của các thành viên. Viết trigger trên `ThamGiaDuAn` (sau INSERT, UPDATE, DELETE; MySQL cần 3 trigger riêng) để tự cập nhật `DuAn.TienDoChung`.
- Việc: thêm, sửa, xóa dự án; thêm hoặc loại thành viên; phân vai trò; cập nhật tiến độ riêng; xem thành viên của dự án; xem dự án của một thành viên.
- Test gợi ý: nhập `TienDoRieng` 150 bị chặn; sau khi cập nhật tiến độ riêng, `TienDoChung` phải đổi đúng.

**Báo cáo:** rà lại mô hình quan hệ phần khóa học và dự án.

### B4. Minh: Module 06, 07

**Module 06, Sự kiện**
- Bảng: `SuKien` (MaSK, TenSK, NgayBatDau, NgayKetThuc), `ThamGiaSuKien` (MSV, MaSK, VaiTro). Quan hệ "Tham gia": nhiều-nhiều.
- Việc: thêm, sửa, xóa sự kiện; thêm thành viên kèm vai trò; xem thành viên của sự kiện; xem sự kiện của một thành viên.

**Module 07, Thu – Chi**
- Bảng: `ThuChi` (MaGD, LoaiGD, MucDich, SoTien, ThoiGian, MinhChung, MSV, MaSK), `ThuChi_PhuongThucThanhToan` (MaGD, PhuongThucThanhToan).
- Quan hệ: "Thực hiện": 1 thành viên có nhiều giao dịch; "Phát sinh": 1 sự kiện có nhiều giao dịch (`MaSK` có thể để trống nếu khoản thu chi không thuộc sự kiện nào).
- **Điểm khó:** phương thức thanh toán là đa trị nên một giao dịch có thể có nhiều dòng. Thêm giao dịch mới phải ghi `ThuChi` và các dòng `ThuChi_PhuongThucThanhToan` trong **cùng một transaction**.
- Việc: thêm, sửa, xóa giao dịch; tra cứu theo thành viên hoặc sự kiện; thống kê tổng thu, tổng chi và chênh lệch theo từng sự kiện (nên làm view `vw_ThuChiTheoSuKien`).
- Test gợi ý: `LoaiGD` khác Thu hoặc Chi bị chặn; `SoTien` bằng 0 hoặc âm bị chặn; xóa sự kiện còn giao dịch bị chặn.

### B5. Trường: Module 09, 10

**Module 09, Cơ sở vật chất**
- Bảng: `CoSoVatChat` (MaThietBi, TenThietBi, TongSoLuong, SoLuongKhaDung, MSVQuanLy). Quan hệ "Quản lý": 1 thành viên quản lý nhiều thiết bị.
- Việc: thêm, sửa thiết bị; xem danh sách thiết bị và số lượng khả dụng; gán hoặc đổi thành viên quản lý; kiểm tra số lượng khả dụng.
- Test gợi ý: `SoLuongKhaDung` lớn hơn `TongSoLuong` bị chặn.

**Module 10, Mượn – Trả (phần khó nhất, nên bắt đầu sớm)**
- Bảng: `PhieuMuon` (MaPhieuMuon, NgayMuon, NgayHenTra, MucDichMuon, TrangThaiDuyet, MSVNguoiMuon), `ChiTietMuon` (MaPhieuMuon, MaThietBi, SoLuongMuon, NgayTraThucTe).
- Quan hệ: "Lập": 1 thành viên lập nhiều phiếu; "Mượn": phiếu và thiết bị nhiều-nhiều, mỗi dòng có số lượng mượn và ngày trả thực tế.
- **Các quy tắc phải do trigger hoặc thủ tục đảm bảo:**
  - Số lượng mượn không được vượt số lượng khả dụng.
  - Khi mượn thì trừ `SoLuongKhaDung`; khi ghi `NgayTraThucTe` thì cộng lại.
  - `NgayTraThucTe` không được trước `NgayMuon` của phiếu.
- **Cần chốt với Tú trước khi làm:** trừ số lượng khả dụng lúc tạo chi tiết mượn, hay lúc phiếu được duyệt. Đề xuất: thủ tục `sp_DuyetPhieuMuon` kiểm tra và trừ số lượng cho cả phiếu trong một transaction, còn `sp_TraThietBi` cộng lại.
- Việc: tạo phiếu; thêm nhiều thiết bị vào phiếu; duyệt hoặc từ chối phiếu; ghi nhận trả; xem thiết bị chưa trả hoặc trả muộn (nên làm view); xem lịch sử mượn của một thành viên.
- Test gợi ý: mượn vượt số lượng khả dụng bị chặn; trả xong thì số lượng khả dụng trở lại như cũ; phiếu có ngày hẹn trả nhỏ hơn ngày mượn bị chặn.

**Báo cáo:** bổ sung vào mô hình quan hệ các bảng Cơ sở vật chất, Phiếu mượn, Chi tiết mượn (xem `DECISIONS.md` mục 10).

---

## Phần C. Mốc kiểm tra gợi ý

| Thời điểm | Cần có |
|---|---|
| Sau khi Tú xong seed nền | Mọi người chạy được `run_all.sql` từ DB trống |
| Giữa giai đoạn làm song song | Mỗi người có ít nhất 1 chức năng chạy đủ SQL, Java và test, đã mở Pull Request đầu tiên |
| Trước khi tích hợp | `seed.sql` và `queries.sql` của cả 10 module chạy liền nhau không lỗi |
| Trước khi nộp | Báo cáo từng module đầy đủ, mô hình quan hệ hoàn chỉnh, phần phân quyền xong |
