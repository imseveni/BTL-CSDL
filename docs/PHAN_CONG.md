# Phân công công việc

> Cách làm chi tiết cho từng người: xem [`HUONG_DAN_THUC_HIEN.md`](HUONG_DAN_THUC_HIEN.md).

Quy ước: mỗi người **chỉ sửa trong module của mình**. Cần đổi schema → tạo Issue gửi Tú.
Mỗi module gồm: SQL (`sql/modules/NN_ten/{seed,queries,tests}.sql`) và Java (`src/main/<ten>/`).

## Bảng tổng quan

| Module | Chức năng | Bảng liên quan | Người phụ trách |
|---|---|---|---|
| 01 thanhvien | Quản lý Thành viên | ThanhVien | **Tú** |
| 02 banchuyenmon | Ban chuyên môn & Chức vụ | BanChuyenMon (+ChucVu trong ThanhVien) | **Đông** |
| 03 quyenhan | Quyền hạn | QuyenHan, ThanhVien_QuyenHan | **Đông** |
| 08 danhgia | Đánh giá thành viên | DanhGia | **Đông** |
| 04 khoahoc | Khóa học | KhoaHoc, ThamGiaKhoaHoc | **Chiến** |
| 05 duan | Dự án | DuAn, ThamGiaDuAn | **Chiến** |
| 06 sukien | Sự kiện | SuKien, ThamGiaSuKien | **Minh** |
| 07 thuchi | Thu – Chi | ThuChi, ThuChi_PhuongThucThanhToan | **Minh** |
| 09 cosovatchat | Cơ sở vật chất | CoSoVatChat | **Trường** |
| 10 muontra | Mượn – Trả | PhieuMuon, ChiTietMuon | **Trường** |

Mỗi người còn viết **phần báo cáo của module mình** (mô tả bảng, ràng buộc, kết quả test) trong `docs/bao-cao/`.

## Tú – khung dự án, quản lý code, Module 01
**Giai đoạn 0–1 (làm trước, chặn cả nhóm):**
- [ ] Họp nhóm chốt các điểm trong `docs/DECISIONS.md`
- [ ] Tạo repo, nhánh `main`/`develop`, bảo vệ nhánh (bắt buộc PR + review)
- [ ] Chạy thử `sql/run_all.sql`, sửa lỗi schema
- [ ] Viết `sql/01_seed_master/seed_master.sql` (ban, ~15–20 thành viên, quyền hạn)
- [ ] Hoàn thành Module 01 làm **mẫu** cho cả nhóm (thực thể/Dao/Service/Ui + SQL + test)
- [ ] Chốt định dạng chuẩn: đặt tên, xử lý lỗi, cách viết test

**Module 01 – Quản lý Thành viên:**
- [ ] Thêm mới thành viên
- [ ] Cập nhật thông tin cá nhân
- [ ] Xóa hoặc đổi trạng thái hoạt động
- [ ] Tìm kiếm theo MSV hoặc họ tên
- [ ] Xem chi tiết một thành viên
- [ ] Theo dõi ngày gia nhập và trạng thái hoạt động

**Giai đoạn 3–4:**
- [ ] Review PR, merge vào `develop`, xử lý conflict
- [ ] Tích hợp: chạy `run_all.sql` từ DB trống
- [ ] Truy vấn tổng hợp liên module (lịch sử hoạt động của một thành viên, thống kê)
- [ ] Phân quyền: user/role MySQL hoặc view giới hạn cho Thành viên thường vs Ban Chủ nhiệm
- [ ] Tổng hợp báo cáo cuối, rà soát trước khi nộp

## Đông – Module 02, 03, 08 (cụm nhân sự)
**Module 02 – Ban chuyên môn & Chức vụ:**
- [ ] Thêm, sửa, xóa ban chuyên môn
- [ ] Tra cứu danh sách các ban
- [ ] Phân thành viên vào ban
- [ ] Ghi nhận chức vụ trong ban
- [ ] Đổi ban/chức vụ khi điều chỉnh cơ cấu
- [ ] Xem danh sách thành viên theo từng ban

**Module 03 – Quyền hạn:**
- [ ] Thêm, sửa, xóa quyền hạn
- [ ] Xem danh sách quyền hạn
- [ ] Phân quyền cho thành viên
- [ ] Thu hồi quyền của thành viên
- [ ] Xem các quyền một thành viên đang có
- [ ] Xem danh sách thành viên được cấp một quyền

**Module 08 – Đánh giá thành viên:**
- [ ] Tạo mới bản ghi đánh giá
- [ ] Ghi nhận loại, hình thức đánh giá
- [ ] Ghi nhận lý do và ngày quyết định
- [ ] Cập nhật đánh giá
- [ ] Tra cứu lịch sử đánh giá của một thành viên
- [ ] Tổng hợp đánh giá phục vụ theo dõi hoạt động

**Hỗ trợ Giai đoạn 4:** bổ sung vào mô hình quan hệ các bảng Quyền hạn, TV–Quyền hạn, Đánh giá (xem DECISIONS #10).

## Chiến – Module 04, 05 (khóa học & dự án)
**Module 04 – Khóa học:**
- [ ] Thêm, cập nhật, xóa khóa học
- [ ] Tra cứu thông tin và thời gian khóa học
- [ ] Ghi nhận thành viên tham gia khóa học + vai trò
- [ ] Cập nhật kết quả đánh giá của thành viên
- [ ] Xem danh sách thành viên của khóa học
- [ ] Xem lịch sử khóa học của một thành viên

**Module 05 – Dự án:**
- [ ] Thêm mới dự án; cập nhật tên, mục tiêu, ngày bắt đầu, hạn hoàn thành
- [ ] Theo dõi tiến độ chung
- [ ] Thêm/loại bỏ thành viên khỏi dự án
- [ ] Phân vai trò trong dự án
- [ ] Theo dõi và cập nhật tiến độ riêng của từng thành viên
- [ ] Xem thành viên của một dự án
- [ ] Xem các dự án của một thành viên

**Hỗ trợ Giai đoạn 4:** rà lại mô hình quan hệ phần khóa học, dự án.

## Minh – Module 06, 07 (sự kiện & thu–chi)
**Module 06 – Sự kiện:**
- [ ] Thêm, cập nhật, xóa sự kiện
- [ ] Tra cứu thời gian diễn ra
- [ ] Thêm thành viên tham gia + ghi nhận vai trò
- [ ] Xem thành viên của từng sự kiện
- [ ] Xem lịch sử sự kiện của một thành viên

**Module 07 – Thu – Chi:**
- [ ] Thêm, cập nhật, xóa giao dịch
- [ ] Phân loại Thu/Chi
- [ ] Ghi mục đích, số tiền, thời gian, phương thức thanh toán
- [ ] Lưu minh chứng
- [ ] Ghi nhận thành viên thực hiện; liên kết giao dịch với sự kiện
- [ ] Tra cứu giao dịch theo thành viên hoặc sự kiện
- [ ] Thống kê thu – chi theo từng sự kiện

## Trường – Module 09, 10 (cơ sở vật chất & mượn–trả, phần khó nhất)
**Module 09 – Cơ sở vật chất:**
- [ ] Thêm, cập nhật thiết bị
- [ ] Theo dõi tổng số lượng và số lượng khả dụng
- [ ] Xem thiết bị đang cho mượn và thành viên quản lý từng dòng mượn (đọc từ `ChiTietMuon`, phối hợp module 10)
- [ ] Tra cứu danh sách thiết bị
- [ ] Kiểm tra số lượng khả dụng trước khi cho mượn

**Module 10 – Mượn – Trả:**
- [ ] Tạo phiếu mượn (ngày mượn, ngày hẹn trả, mục đích)
- [ ] Thêm một hoặc nhiều thiết bị vào phiếu + số lượng, chỉ định thành viên quản lý (`MSVQuanLy`) cho từng dòng
- [ ] Kiểm tra số lượng khả dụng trước khi xác nhận (trigger/thủ tục)
- [ ] Ghi nhận ngày trả thực tế
- [ ] Tự cập nhật `SoLuongKhaDung` khi mượn/trả (trigger)
- [ ] Theo dõi thiết bị chưa trả hoặc trả muộn
- [ ] Xem lịch sử mượn của một thành viên
- [ ] Xem/duyệt tình trạng phiếu mượn (ChoDuyet/DaDuyet/TuChoi)

## Mốc thời gian (điền khi biết deadline)
| Giai đoạn | Nội dung | Hạn |
|---|---|---|
| 0 | Chốt DECISIONS, dựng repo, seed nền | ____ |
| 1 | Module 01 mẫu (Tú) | ____ |
| 2 | Các module song song (Đông, Chiến, Minh, Trường) | ____ |
| 3 | Tích hợp, merge | ____ |
| 4 | Phân quyền, ERD, báo cáo, rà soát | ____ |
