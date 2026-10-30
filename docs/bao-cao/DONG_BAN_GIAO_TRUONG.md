# Bàn giao phần việc của Trường

Phạm vi: **module 09 (Cơ sở vật chất)** và **module 10 (Mượn – Trả)**, đối chiếu với README, PHAN_CONG, HUONG_DAN_THUC_HIEN, CONTRIBUTING, DECISIONS và schema hiện có. Các tệp schema, seed_master, run_all, pom.xml và các package app/config/common được giữ nguyên.

## Chức năng đã làm

- [x] 09: thêm, sửa thiết bị; xem danh sách và số lượng khả dụng; tra cứu theo mã/tên; gán/đổi/bỏ người quản lý; kiểm tra số lượng khả dụng trước khi cho mượn.
- [x] 10: tạo phiếu; thêm nhiều thiết bị vào phiếu; duyệt/từ chối; ghi nhận trả (một thiết bị hoặc tất cả); xem thiết bị chưa trả/quá hạn và trả muộn (view); xem lịch sử mượn của một thành viên.
- [x] Tự động trừ/cộng `SoLuongKhaDung` khi duyệt/trả bằng trigger (DECISIONS: "khi mượn/trả – module 10").
- [x] Hai bộ seed/queries/tests SQL; các lớp thực thể/Dao/Service/Ui và `CoSoVatChatModule`, `MuonTraModule`.
- [x] Báo cáo [09](09_cosovatchat.md), [10](10_muontra.md).
- [ ] Bổ sung mô hình quan hệ cho CoSoVatChat, PhieuMuon, ChiTietMuon (DECISIONS #10) — **chưa làm**, xem "Việc còn lại".

## Đã kiểm tra trong lúc viết (không cần MySQL)

| Kiểm tra | Kết quả |
|---|---|
| Biên dịch toàn bộ Java (main + test, cả ca tích hợp) với `-source 17` | Đạt, không lỗi |
| 18 ca Java không cần DB (4 + 6 + 8) chạy với Dao giả | Đạt, 0 thất bại |
| Cân bằng `BEGIN/END`, `IF/END IF` và `CREATE/DROP IF EXISTS` trong SQL | Đạt |

**Chưa chạy trên MySQL thật**: toàn bộ SQL (seed, queries, tests) và 2 ca tích hợp. Cần chạy theo mục sau và dán output vào hai báo cáo.

## Biên bản kiểm thử cần điền (chạy trên DB thử riêng)

Môi trường: MySQL ______ , Connector/J ______ , Java ______ , ngày chạy ______ .

| Kiểm tra | Kết quả |
|---|---|
| `run_all.sql` từ DB trống (sau `reset_db.sql`), exit code | ______ |
| Chạy lại `seed.sql` và `queries.sql` của 09 và 10 (không trùng, không lỗi) | ______ |
| Chạy lại seed 09/10 khi `seed_master` còn trống (0 dòng, không lỗi) | ______ |
| `tests.sql` module 09 | ______ (mong đợi dòng `PASS module 09: dữ liệu thử đã rollback`) |
| `tests.sql` module 10 | ______ (mong đợi dòng `PASS module 10: dữ liệu thử đã rollback`) |
| Chạy lại cả hai `tests.sql` | ______ |
| Dữ liệu `T_TRUONG_*` sau kiểm thử | ______ (mong đợi 0 dòng ở mọi bảng) |
| `mvn test` | ______ (mong đợi 0 lỗi, ca MySQL bị bỏ qua) |
| `mvn "-Dclb.mysql.tests=true" test` | ______ |

Câu lệnh kiểm tra dữ liệu thử còn sót:

```sql
SELECT (SELECT COUNT(*) FROM ThanhVien WHERE MSV LIKE 'T\_TRUONG\_%')      AS ThanhVienThu,
       (SELECT COUNT(*) FROM CoSoVatChat WHERE MaThietBi LIKE 'T\_TRUONG\_%') AS ThietBiThu,
       (SELECT COUNT(*) FROM PhieuMuon WHERE MaPhieuMuon LIKE 'T\_TRUONG\_%') AS PhieuThu;
```

Kiểm tra kho của seed khớp phiếu mượn (mong đợi 0 dòng):

```sql
SELECT t.MaThietBi, t.SoLuongKhaDung,
       t.TongSoLuong - COALESCE(SUM(CASE WHEN p.TrangThaiDuyet = 'DaDuyet' AND c.NgayTraThucTe IS NULL
                                         THEN c.SoLuongMuon END), 0) AS KhaDungTinhLai
FROM CoSoVatChat t
LEFT JOIN ChiTietMuon c ON c.MaThietBi = t.MaThietBi
LEFT JOIN PhieuMuon p ON p.MaPhieuMuon = c.MaPhieuMuon
GROUP BY t.MaThietBi, t.TongSoLuong, t.SoLuongKhaDung
HAVING SoLuongKhaDung <> KhaDungTinhLai;
```

## Cách chạy lại

1. Dựng MySQL, copy `db.properties.example` thành `db.properties` theo README (không đưa mật khẩu vào Git). Dùng **DB thử riêng** vì ca tích hợp chèn rồi xóa dữ liệu.
2. Từ thư mục `sql`: `mysql -u root -p < reset_db.sql` rồi `mysql -u root -p < run_all.sql`.
3. Chạy test SQL (trong mysql client, từ thư mục `sql`):

```sql
SOURCE modules/09_cosovatchat/tests.sql;
SOURCE modules/10_muontra/tests.sql;
```

4. Trong thư mục dự án: `mvn test`, `mvn "-Dclb.mysql.tests=true" test`, `mvn compile exec:java` (menu mục 9 và 10; nhập 0 để quay lại).

## Điểm cần Tú tích hợp / xác nhận

- **Tên lớp Module:** hai file `CoSoVatChatModule.java` và `MuonTraModule.java` theo quy ước đặt tên của các module khác. Nếu khung có sẵn trong repo đặt tên khác thì giữ tên của khung, chỉ chép phần thân (`getName`, `run`). Menu chính của Tú đã đăng ký sẵn module nào thì giữ nguyên, không sửa package `app`.
- **`seed_master.sql` còn TODO:** seed 09/10 chọn thành viên có thật theo thứ tự MSV và không tạo MSV giả. Khi seed nền chưa có thành viên, thiết bị vẫn được thêm (người quản lý NULL) nhưng phiếu mượn mẫu chèn 0 dòng. Sau khi Tú điền seed nền, chạy lại `seed.sql` của 09 rồi 10 để có ví dụ đầy đủ. Cần seed nền có **ít nhất 1 thành viên** (nên ≥ 4 để người quản lý/người mượn khác nhau).
- **Chọn thời điểm trừ kho:** làm theo đề xuất trong hướng dẫn (trừ lúc duyệt). Nếu nhóm muốn trừ ngay lúc tạo chi tiết mượn thì phải đổi trigger; xin Tú xác nhận.
- **Schema:** `TongSoLuong` cho phép 0 nhưng ứng dụng yêu cầu > 0 khi thêm/sửa thiết bị; không cần sửa schema.

## Việc còn lại

- [ ] Chạy toàn bộ mục "Biên bản kiểm thử" trên MySQL thật, điền kết quả vào đây và dán output vào mục 5 của hai báo cáo.
- [ ] Vẽ bổ sung mô hình quan hệ CoSoVatChat, PhieuMuon, ChiTietMuon (cùng khuôn với `docs/erd/Mo_hinh_quan_he_nhan_su.*` của Đông), đặt trong `docs/erd/`.
- [ ] Chụp màn hình menu mục 9 và 10 chạy thật nếu báo cáo cuối cần ảnh.
- [ ] Mở PR vào `develop` theo CONTRIBUTING (cần Tú tạo nhánh `develop` và thêm quyền Collaborator cho tài khoản của Trường).
