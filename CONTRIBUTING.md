# Quy ước làm việc nhóm

## Git
- `main`: bản ổn định, chạy được. Chỉ Tú merge vào `main`.
- `develop`: nhánh tích hợp. Mọi PR đều trỏ vào `develop`.
- Mỗi người làm trên nhánh riêng: `feature/<module>-<mô-tả>`, ví dụ `feature/khoahoc-tra-cuu`.
- **Không push trực tiếp** lên `main` hoặc `develop`.
- Trước khi push: `git pull --rebase origin develop`.
- Commit nhỏ, mỗi commit một việc, theo chuẩn Conventional Commits (xem mục dưới).

## Quy ước commit (Conventional Commits)
Dạng: `<loại>(<phạm vi>): <mô tả>`

- **Loại:** `feat` (chức năng mới), `fix` (sửa lỗi), `docs`, `test`, `refactor`, `chore`, `build`.
- **Phạm vi:** tên module viết thường, không dấu: `thanhvien`, `banchuyenmon`, `quyenhan`, `khoahoc`, `duan`, `sukien`, `thuchi`, `danhgia`, `cosovatchat`, `muontra`; việc chung dùng `db`, `core`, `erd`.
- **Mô tả:** bắt đầu bằng động từ, tối đa 72 ký tự, không có dấu chấm cuối.
- Ví dụ: `feat(khoahoc): thêm thủ tục đăng ký khóa học`, `fix(muontra): sửa lỗi không cộng lại số lượng khi trả`, `test(duan): thêm ca thử tiến độ vượt 100%`.
- Khi merge Pull Request chọn *Squash and merge* và đặt tiêu đề theo đúng dạng trên.

## Chống conflict
1. Mỗi người chỉ sửa file trong module của mình (`sql/modules/NN_*` và `src/main/<module>/`).
2. **Chỉ Tú** sửa: `sql/00_schema`, `sql/01_seed_master`, `sql/run_all.sql`, `config/`, `app/`, `common/`, `pom.xml`.
3. Cần đổi schema hoặc code chung → tạo Issue mô tả rõ, Tú xử lý rồi thông báo cả nhóm.
4. Dữ liệu mẫu dùng dải mã riêng của module (vd `KH01..`, `DA01..`, `SK01..`).

## Quy ước đặt tên
- Bảng/cột: đúng như trong `schema.sql` (PascalCase, tiếng Việt không dấu).
- SQL: view `vw_...`, stored procedure `sp_...`, trigger `trg_...`, function `fn_...`.
- Java: mỗi module một thư mục/package phẳng `src/main/<module>/`; đặt tên file theo hậu tố: `ThanhVien` (thực thể), `ThanhVienDao`, `ThanhVienService`, `ThanhVienUi`.
- Mỗi module có class `<Ten>Module implements AppModule`.

## Quy trình một Pull Request
1. Tạo nhánh từ `develop` mới nhất.
2. Làm việc, tự chạy `sql/run_all.sql` từ DB trống.
3. Mở PR vào `develop`, điền checklist trong template.
4. Tú review (yêu cầu sửa nếu cần) rồi merge.

## Quy ước code Java
- Java 17, UTF-8, thụt lề 4 dấu cách.
- Truy cập DB chỉ ở các class `*Dao`; dùng `PreparedStatement` (không nối chuỗi SQL) và try-with-resources.
- Bọc `SQLException` bằng `DataAccessException`.
- Nghiệp vụ ở `*Service`; `*Ui` chỉ nhập/xuất.
