# Module 05 – Quản lý dự án

Phụ trách: Chiến. Kết quả kiểm tra ngày 30/10/2026.

## Bảng và ràng buộc

| Bảng | Cột | Ý nghĩa/ràng buộc |
|---|---|---|
| DuAn | MaDuAn VARCHAR(20) | Khóa chính |
| DuAn | TenDuAn VARCHAR(150) | Bắt buộc |
| DuAn | MucTieu TEXT | Có thể NULL |
| DuAn | NgayBatDau DATE | Bắt buộc |
| DuAn | HanHoanThanh DATE | Có thể NULL; nếu có phải >= ngày bắt đầu |
| DuAn | TienDoChung DECIMAL(5,2) | Mặc định 0, CHECK 0–100; thuộc tính suy diễn |
| ThamGiaDuAn | MSV, MaDuAn | Khóa chính ghép; tham chiếu ThanhVien và DuAn |
| ThamGiaDuAn | VaiTro VARCHAR(100) | Có thể NULL |
| ThamGiaDuAn | TienDoRieng DECIMAL(5,2) | Mặc định 0, CHECK 0–100; schema cho phép NULL |

Hai FK của ThamGiaDuAn có ON UPDATE CASCADE và ON DELETE CASCADE.
Không sửa schema chung. DAO chặn xóa dự án còn người tham gia bằng DELETE có điều kiện.
Java kiểm tra mã/tên, độ dài, thứ tự ngày, tiến độ trước khi ghi; DAO dùng
PreparedStatement và try-with-resources.

## Tiến độ chung và trigger

Quy ước triển khai: AVG(TienDoRieng) bỏ qua NULL; không có tiến độ nào thì bằng 0.
Cột lưu trữ làm tròn theo DECIMAL(5,2). Java averageProgress dùng cùng quy ước.

- trg_DuAn_TienDo_Insert: tính lại sau INSERT lượt tham gia.
- trg_DuAn_TienDo_Update: tính lại sau UPDATE; nếu chuyển sang dự án khác thì cập nhật cả dự án cũ và mới.
- trg_DuAn_TienDo_Delete: tính lại sau DELETE lượt tham gia.
- queries.sql đồng bộ lại dữ liệu hiện có vì run_all.sql nạp seed trước khi tạo trigger.
- DROP IF EXISTS cho phép tạo lại view, thủ tục và trigger.

Lưu ý tích hợp: MySQL không kích hoạt trigger con khi FK CASCADE từ bảng cha.
Nếu xóa cứng ThanhVien hoặc đổi MSV gây cascade, cột DuAn.TienDoChung có thể cũ.
Tú cần thống nhất dùng đổi trạng thái thành viên hoặc luồng xóa chủ động cập nhật tiến độ.
View thống kê và Java tính AVG trực tiếp nên phản ánh dữ liệu tham gia hiện tại.
Việc ghi trực tiếp vào DuAn.TienDoChung cũng cần được giới hạn ở luồng dùng chung.
Chưa kiểm thử nhiều phiên ghi đồng thời.

## Chức năng và truy vấn

Java hiện có thực thể, DAO, Service, Swing UI: tìm theo mã/tên/mục tiêu;
thêm/sửa/xóa dự án; thêm/sửa/xóa lượt tham gia; cập nhật vai trò, tiến độ riêng;
xem danh sách thành viên và tiến độ trung bình.

- vw_DuAnThanhVien: thông tin thành viên, vai trò, tiến độ từng dự án.
- vw_DuAnThongKe: số thành viên và AVG hiện tại, bao gồm dự án trống.
- sp_DuAnTraCuu: tìm chuỗi nguyên văn trong mã/tên/mục tiêu; NULL hoặc rỗng trả tất cả.

```sql
CALL sp_DuAnTraCuu('CLB');
SELECT * FROM vw_DuAnThanhVien WHERE MaDuAn = 'DA01';
SELECT * FROM vw_DuAnThanhVien WHERE MSV = 'ma-thanh-vien-thuc-te';
SELECT * FROM vw_DuAnThongKe ORDER BY MaDuAn;
```

## Dữ liệu mẫu

seed.sql thêm DA01, DA02; gắn tối đa hai thành viên đã có vào DA01 với tiến độ 20.
Không phụ thuộc vào MSV giả định hoặc thêm thành viên vào seed chung.
Hiện seed_master.sql chưa có dữ liệu, nên trên DB trống có hai dự án, tiến độ 0,
chưa có lượt tham gia mẫu. seed.sql chỉ nạp trên DB sạch để không trùng mã.

## Chạy và kết quả test

Chạy MySQL client từ thư mục sql trên DB thử nghiệm có thể xóa:

```sql
SOURCE reset_db.sql;
SOURCE run_all.sql;
SOURCE modules/05_duan/tests.sql;
```

reset_db.sql xóa QuanLyCauLacBo; tests.sql không được run_all.sql gọi tự động.
Test tự tạo fixture DA_TEST_, ROLLBACK dữ liệu, phát lại lỗi bất ngờ và xóa
các thủ tục trợ giúp sau khi thành công. Chỉ bắt đúng mã lỗi ràng buộc mong đợi.

| Ca thử | Mong đợi | Kết quả |
|---|---|---|
| Dự án chưa có người | Tiến độ 0 | PASS |
| Thêm người tiến độ 20 rồi 80 | Tiến độ 20 rồi 50 | PASS |
| Cập nhật 20 thành 100 | Tiến độ 90 | PASS |
| Chuyển người tiến độ 80 sang dự án khác | Dự án cũ 100, mới 80 | PASS |
| NULL, chỉ còn NULL, tiến độ 0 | AVG bỏ NULL; không có giá trị thì 0 | PASS |
| Trung bình của 33.33 và 0 | Cột lưu 16.67 | PASS |
| Xóa từng người rồi xóa người cuối | Tính lại trung bình, cuối cùng 0 | PASS |
| INSERT tiến độ -1/150; UPDATE tiến độ 150 | CHECK 3819, tiến độ cũ giữ nguyên | PASS |
| Hạn hoàn thành trước ngày bắt đầu | CHECK 3819 | PASS |
| Tham gia trùng | PK 1062 | PASS |
| MSV hoặc dự án không tồn tại | FK 1452 | PASS |

Đã chạy bằng MySQL Community Server 8.0.46 trên instance riêng, từ DB trống.
queries.sql và tests.sql chạy lại thành công; không còn thành viên test sau ROLLBACK.

```text
PASS: DuAn - INSERT/UPDATE/DELETE/chuyen du an, NULL, lam tron, CHECK, PK, FK
```

Chưa kiểm thử thao tác Swing tự động. Maven biên dịch được nhưng test menu chung
MainMenuTest thất bại: expected 10, actual 8. Việc đăng ký lại menu thuộc phần Tú phụ trách.
