# Kịch bản demo module Thành viên (8 đến 10 phút)

Dành cho buổi demo với cô. Làm theo thứ tự; mỗi bước ghi rõ **việc làm**, **điều cần nói** và **kết quả mong đợi**. Phần cuối là phương án dự phòng và các câu cô có thể hỏi.

## A. Chuẩn bị (làm trước buổi demo, khoảng 15 phút)

1. Máy có **Java 17**, **Maven 3.8+**, **MySQL 8.0.16+**, và cửa sổ màn hình (không dùng máy chủ không có màn hình).
2. Dựng CSDL từ đầu, trong thư mục `sql/`:
   ```bash
   mysql -u root -p < reset_db.sql
   mysql -u root -p < run_all.sql          # phải chạy xong không báo lỗi
   mysql -u root -p < modules/01_thanhvien/tests.sql   # phải thấy dòng: PASS module 01
   ```
3. Cấu hình kết nối: sao chép `src/main/resources/db.properties.example` thành `db.properties`, sửa tài khoản và mật khẩu MySQL.
4. Thử chạy: `mvn compile exec:java`. Cửa sổ mở ra, tab **Thành viên** hiện 20 thành viên, chọn một dòng thì phần dưới có lịch sử hoạt động.
5. Chuẩn bị sẵn: ảnh màn hình trong `anh/` và hai file Excel mẫu trong `mau-xuat/` (phương án dự phòng).
6. Xóa dữ liệu demo cũ nếu có: chạy lại `reset_db.sql` và `run_all.sql` ngay trước buổi demo để MSV `B21DCCN901` chưa tồn tại.

## B. Trình tự demo

### Bước 1. Giới thiệu dữ liệu (1 phút)
- **Làm:** mở tab Thành viên; chỉ vào bảng thành viên, các ô tìm kiếm.
- **Nói:** "Module quản lý hồ sơ thành viên, gồm 4 bảng: ThanhVien, BanChuyenMon, QuyenHan, ThanhVien_QuyenHan. Mỗi thành viên thuộc đúng một ban; thành viên và quyền hạn là quan hệ nhiều nhiều nên có bảng liên kết." (chỉ vào hình `mo_hinh_quan_he_thanh_vien.png`)

### Bước 2. Màn hình nhập 1: Thêm thành viên (3 phút) — tiêu chí 5
1. Bấm **Thêm thành viên**. Nhập dữ liệu **sai** lần lượt, mỗi lần bấm Lưu để thấy thông báo (chọn 4 đến 5 ca trong bảng ca kiểm tra, mục 5.1 của báo cáo):

   | Nhập | Thông báo mong đợi |
   |---|---|
   | MSV để trống | Mã sinh viên không được để trống. |
   | Email `khong-hop-le` | Email không hợp lệ. |
   | SĐT `12ab` | Số điện thoại không hợp lệ (9 đến 15 chữ số, có thể bắt đầu bằng +). |
   | Ngày gia nhập trước ngày sinh | Ngày gia nhập không được trước ngày sinh. |
   | MSV `B21DCCN001` (đã có) | Mã sinh viên đã tồn tại |

2. Nhập **đúng**: MSV `B21DCCN901`, họ tên `Nguyễn Văn Mới`, ngày sinh `2004-03-01`, giới tính Nam, email `moi@example.com`, SĐT `0912345678`, ngày gia nhập hôm nay, trạng thái Đang hoạt động, ban Ban Chuyên môn. Bấm Lưu.
3. **Nói:** "Dữ liệu được kiểm tra ở ba lớp: giao diện, tầng Java và cơ sở dữ liệu (thủ tục, khóa chính, khóa ngoại, CHECK). Lớp CSDL là hàng rào cuối cùng nên dù đi đường khác dữ liệu sai cũng không vào được."
4. **Kết quả:** thành viên mới hiện trong bảng, được chọn sẵn.

### Bước 3. Màn hình nhập 2: Cấp quyền hạn (2 phút) — tiêu chí 5
1. Chọn thành viên vừa thêm; mở tab **Quyền hạn** (nửa dưới). Bảng quyền đang trống.
2. Trong ô **Cấp thêm quyền** chọn `QH02 – Duyệt phiếu mượn`, bấm **Cấp quyền**. Quyền xuất hiện trong bảng và biến mất khỏi ô chọn.
3. **Nói:** "Đây là bảng liên kết ThanhVien_QuyenHan. Khóa chính ghép (MSV, MaQuyenHan) chặn cấp trùng; ô chọn chỉ liệt kê quyền chưa có."
4. Chọn dòng QH02, bấm **Thu hồi quyền đang chọn**, bấm Đồng ý. Quyền bị xóa.

### Bước 4. Tìm kiếm (1 phút) — tiêu chí 6
- **Làm:** gõ `Văn` vào ô từ khóa, bấm Tìm kiếm; chọn thêm ban Ban Chuyên môn và trạng thái Đang hoạt động, tìm lại.
- **Nói:** "Tìm theo một phần MSV hoặc họ tên, kết hợp ban và trạng thái bằng phép và. Chọn một dòng thì xem được lịch sử hoạt động của người đó, lấy từ nhiều bảng."
- **Làm thêm (tùy chọn):** chọn `B21DCCN001`, mở tab Lịch sử hoạt động để thấy khóa học, dự án, sự kiện, đánh giá.

### Bước 5. Hai báo cáo (2 đến 3 phút) — tiêu chí 7
1. Mở tab **Báo cáo**. Báo cáo 1: giữ "Tất cả ban", nhập **Người lập** (ví dụ tên bạn thư ký), bấm **Xem báo cáo**.
2. **Nói rõ thực tế sử dụng:** "Báo cáo này do **thư ký CLB** lập, **gửi chủ nhiệm, giảng viên phụ trách và Đoàn trường** để đăng ký danh sách sinh hoạt đầu kỳ." (chỉ vào dòng Người lập, Nơi nhận, Mục đích trên màn hình)
3. Bấm **Xuất Excel (.xlsx)**, lưu file, **mở file bằng Excel** cho cô xem: tiêu đề, người lập, nơi nhận, bảng, tổng, khối chữ ký.
4. Báo cáo 2: chọn "Tổng hợp hoạt động thành viên trong kỳ", đặt từ ngày và đến ngày, Xem báo cáo.
5. **Nói:** "Báo cáo này do **Ban Chủ nhiệm** lập, **gửi chủ nhiệm và giảng viên phụ trách** để xét khen thưởng cuối kỳ. Số liệu lấy từ 6 loại hoạt động ở các bảng khác nhau." Xuất Excel lần nữa.

### Bước 6. Thiết kế và chuẩn hóa (1 đến 2 phút) — tiêu chí 1, 3, 4
- Mở báo cáo `BAO_CAO_THANH_VIEN`, mục 4.3 (bảng quá trình đưa về 3NF).
- **Nói:** "Thiết kế ban đầu gộp ban và quyền vào một bảng, có nhóm lặp và phụ thuộc bắc cầu MSV → MaBan → TenBan. Tách ra thành 4 bảng thì cả 4 đều đạt 3NF."

## C. Phương án dự phòng
| Sự cố | Cách xử lý |
|---|---|
| Không kết nối được MySQL | Bấm **Kết nối MySQL** ở góc trên bên phải, nhập lại địa chỉ, tài khoản, mật khẩu; nếu vẫn lỗi, dùng ảnh trong `anh/` và file Excel trong `mau-xuat/` để trình bày |
| Máy không mở được cửa sổ | Dùng `mvn compile exec:java -Dexec.args="--console"` (menu chữ) và các ảnh |
| Thông báo lỗi khác bảng ca kiểm tra | Đọc thông báo; mọi thông báo đều do mã dự án viết, nội dung tiếng Việt; chỉ ra ca đó bị chặn là đủ |
| Nhập sai làm lệch dữ liệu demo | Chạy lại `reset_db.sql` rồi `run_all.sql` |

## D. Câu cô có thể hỏi
| Câu hỏi | Gợi ý trả lời |
|---|---|
| Vì sao `ChucVu` nằm trong bảng ThanhVien mà không ở BanChuyenMon? | Chức vụ là của **từng thành viên** (cùng một ban có trưởng ban, phó ban, thành viên), phụ thuộc MSV chứ không phụ thuộc mã ban |
| Vì sao Email, SĐT không tách bảng? | Theo ERD đó là thuộc tính đơn trị: mỗi thành viên một email và một số liên hệ chính. Nếu cần nhiều giá trị thì tách bảng để giữ 1NF |
| Vì sao không xóa thành viên? | Thành viên có lịch sử hoạt động (khóa học, dự án...), xóa sẽ mất lịch sử; hệ thống chỉ cho xóa khi chưa có lịch sử, còn lại đổi trạng thái Đã rời CLB |
| Khóa chính ghép của ThanhVien_QuyenHan để làm gì? | Chặn cấp trùng một quyền cho cùng thành viên ngay ở cơ sở dữ liệu |
| Dữ liệu sai có thể lọt qua giao diện không? | Không: thủ tục và các ràng buộc (khóa chính, khóa ngoại, CHECK) ở MySQL vẫn kiểm tra lại |
| Báo cáo xuất file được dùng làm gì? | Xem mục 7: ai lập, gửi ai, dùng để làm gì (ghi ngay trên báo cáo) |
| Có xuất PDF không? | Hiện xuất Excel; Excel in ra khổ A4 nằm ngang, có thể lưu thành PDF từ Excel |
