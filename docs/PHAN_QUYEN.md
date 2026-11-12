# Phân quyền theo tác nhân (Thành viên / Ban Chủ nhiệm)

Theo bản mô tả đề tài, hệ thống có hai nhóm người dùng. Khi mở ứng dụng, màn hình đầu tiên cho chọn vai trò; mỗi vai trò chỉ thấy và chỉ làm được các chức năng phù hợp.

## 1. Cách vào hệ thống

| Vai trò | Nhập | Ghi chú |
|---|---|---|
| Thành viên Câu lạc bộ | Mã sinh viên | Phải là thành viên chưa rời CLB |
| Ban Chủ nhiệm / Người quản lý | Mã sinh viên + **mã xác nhận** | Mã xác nhận mặc định `BCN2026`; đổi bằng biến môi trường `CLB_ADMIN_CODE` hoặc `-Dclb.admin.code=...` |

Đây là cơ chế đơn giản cho bài tập lớn, **không phải mật khẩu thật**: mã nằm trong cấu hình, không lưu trong CSDL. Người đăng nhập Ban Chủ nhiệm tự động là **người lập báo cáo** ở tab Báo cáo Mượn – Trả. Bấm **Đăng xuất** để quay về màn hình chọn vai trò (phiên làm việc được xóa).

## 2. Ma trận quyền

| Chức năng | Thành viên | Ban Chủ nhiệm |
|---|---|---|
| Hồ sơ cá nhân | Xem; sửa họ tên, ngày sinh, giới tính, email, SĐT **của mình** | Xem, thêm, sửa, đổi trạng thái, xóa mọi thành viên |
| Ban chuyên môn, chức vụ, trạng thái, quyền hạn | Chỉ xem của mình | Quản lý, cấp và thu hồi quyền |
| Lịch sử hoạt động | Của mình | Của mọi thành viên |
| Thiết bị (Cơ sở vật chất) | Tra cứu, xem số lượng khả dụng | Thêm, sửa, gán người quản lý |
| Phiếu mượn | Lập phiếu, thêm thiết bị, theo dõi **phiếu của mình** | Xem mọi phiếu, duyệt, từ chối, ghi nhận trả |
| Báo cáo thành viên, báo cáo mượn – trả | Không | Xem trên màn hình và xuất file |

## 3. Cách thực hiện

- `common.PhienLamViec` giữ người đang đăng nhập; `app.DangNhapService` kiểm tra khi vào.
- **Quyền được kiểm tra ở tầng Service** (`yeuCauBanChuNhiem`, `yeuCauChinhMinhHoacBanChuNhiem`) nên Thành viên không gọi được thao tác quản lý dù có mở được màn hình. Vi phạm ném `QuyenException`, giao diện hiện thành thông báo.
- Giao diện ẩn thêm các tab và nút không dùng đến (`SwingModules.choVaiTro`), để người dùng không thấy chức năng mình không có.
- Ngoài ra `sql/02_security/phan_quyen.sql` định nghĩa vai trò MySQL (tùy chọn, chạy riêng) cho lớp bảo vệ ở mức CSDL.
- Menu dòng lệnh `--console` chạy với quyền quản trị, chỉ dùng khi phát triển.
- Kiểm thử: `app/PhanQuyenTest` (chặn quyền ở từng Service), `DangNhapServiceTest`, `DangNhapPanelTest`.

## 4. Phạm vi bản tích hợp

Giao diện Swing chỉ còn bốn phần: **Thành viên, Cơ sở vật chất, Mượn – Trả, Báo cáo Mượn – Trả**. Các module Ban chuyên môn, Quyền hạn, Khóa học, Dự án, Sự kiện, Thu – Chi, Đánh giá **vẫn còn đủ trong CSDL (schema, ERD, SQL, seed) và mã Dao/Service/console**, chỉ gỡ phần giao diện Swing. Nhờ vậy ERD, mô hình quan hệ và 3NF vẫn đầy đủ, còn các tác vụ khóa học, dự án, đánh giá trong mô tả tác nhân được ghi nhận là thuộc phạm vi mở rộng, chưa có màn hình. Bản đầy đủ giao diện cũ có thể khôi phục bằng tag git `archive/truoc-khi-gon-giao-dien`.

## 5. Ảnh minh họa

- Màn hình chọn vai trò: `docs/images/dangnhap_chon_vai_tro.png`
- Thành viên: `docs/images/vai_tro_thanh_vien_ho_so.png`, `docs/images/vai_tro_thanh_vien_muon_tra.png` (chỉ thấy phiếu của mình, không có nút duyệt/trả)
- Ban Chủ nhiệm: `docs/images/vai_tro_ban_chu_nhiem_bao_cao_muon_tra.png`
