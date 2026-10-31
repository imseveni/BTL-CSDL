# Module 10 – Mượn – Trả

Người phụ trách: **Trường**. Đã triển khai SQL (trigger, thủ tục, view), Java và nối vào menu chính qua `MuonTraModule`.

## 1. Bảng và các cột

**PhieuMuon**

| Cột | Kiểu | Ý nghĩa/ràng buộc schema |
|---|---|---|
| `MaPhieuMuon` | VARCHAR(20) | PK |
| `NgayMuon` | DATE | NOT NULL |
| `NgayHenTra` | DATE | NOT NULL, CHECK `NgayHenTra >= NgayMuon` |
| `MucDichMuon` | TEXT | Có thể NULL |
| `TrangThaiDuyet` | VARCHAR(20) | NOT NULL, mặc định `ChoDuyet`, CHECK thuộc {`ChoDuyet`, `DaDuyet`, `TuChoi`} |
| `MSVNguoiMuon` | VARCHAR(20) | NOT NULL, FK → ThanhVien.MSV, UPDATE CASCADE, DELETE RESTRICT |

**ChiTietMuon** (PK ghép `MaPhieuMuon` + `MaThietBi`)

| Cột | Kiểu | Ý nghĩa/ràng buộc schema |
|---|---|---|
| `MaPhieuMuon` | VARCHAR(20) | FK → PhieuMuon, DELETE CASCADE |
| `MaThietBi` | VARCHAR(20) | FK → CoSoVatChat, DELETE RESTRICT |
| `SoLuongMuon` | INT | NOT NULL, CHECK `> 0` |
| `NgayTraThucTe` | DATE | NULL = chưa trả |

Quan hệ "Lập": 1 thành viên lập nhiều phiếu. Quan hệ "Mượn": phiếu – thiết bị nhiều-nhiều, mỗi dòng có số lượng và ngày trả thực tế. Một thiết bị chỉ xuất hiện một lần trong một phiếu (muốn mượn thêm thì sửa số lượng khi phiếu còn chờ duyệt).

## 2. Ràng buộc và quyết định thiết kế

**Trừ kho lúc nào?** Theo đề xuất trong hướng dẫn (B5): trừ `SoLuongKhaDung` lúc phiếu được **duyệt**, cộng lại khi ghi `NgayTraThucTe`. Thêm thiết bị vào phiếu chờ chỉ **kiểm tra** đủ số lượng, chưa trừ. Vì chưa trừ nên hai phiếu chờ có thể cùng "vừa đủ" lúc tạo; khi duyệt, DB kiểm tra lại và chặn phiếu sau nếu đã hết. Trigger duyệt phiếu khóa các dòng thiết bị (`FOR UPDATE`) trước khi kiểm tra nên hai người duyệt cùng lúc không lấy trùng số lượng.

**Vòng đời:** `ChoDuyet` → `DaDuyet` (trừ kho) hoặc `TuChoi` (kho không đổi). Phiếu đã xử lý không đổi trạng thái lại. Chỉ phiếu `ChoDuyet` mới thêm/sửa/xóa thiết bị; chỉ phiếu `DaDuyet` mới ghi nhận trả.

| Ràng buộc | Nơi thực hiện |
|---|---|
| `NgayHenTra >= NgayMuon` | `trg_PhieuMuon_BI`, `trg_PhieuMuon_BU`, `sp_TaoPhieuMuon`, CHECK schema |
| Phiếu mới luôn `ChoDuyet` | `trg_PhieuMuon_BI` |
| `SoLuongMuon > 0` | `trg_ChiTietMuon_BI`, `trg_ChiTietMuon_BU`, CHECK schema |
| `SoLuongMuon <= SoLuongKhaDung` khi thêm/sửa dòng | `trg_ChiTietMuon_BI`, `trg_ChiTietMuon_BU` |
| `SoLuongMuon <= SoLuongKhaDung` khi duyệt (mọi dòng của phiếu); phiếu rỗng không duyệt được | `trg_PhieuMuon_BU` |
| Trừ `SoLuongKhaDung` khi duyệt | `trg_PhieuMuon_AU` |
| Cộng `SoLuongKhaDung` khi ghi ngày trả | `trg_ChiTietMuon_AU` |
| `NgayTraThucTe >= NgayMuon`; không trả hai lần; không trả phiếu chưa duyệt; không đổi khóa dòng | `trg_ChiTietMuon_BU` |
| Không xóa phiếu hoặc dòng đã duyệt mà còn chưa trả (xóa dây chuyền qua FK không kích hoạt trigger bảng con nên chặn thêm ở phiếu) | `trg_PhieuMuon_BD`, `trg_ChiTietMuon_BD` |

Trigger đảm bảo quy tắc dù ai gọi thẳng SQL; thủ tục thêm kiểm tra dễ hiểu. Thủ tục không mở transaction riêng: mỗi lệnh ghi (kể cả trigger kéo theo) là một câu lệnh nguyên tử, lỗi thì rollback cả câu; nhờ vậy `tests.sql` chạy trong transaction rồi rollback được.

## 3. Thủ tục, view và ví dụ

| Đối tượng SQL | Chức năng |
|---|---|
| `sp_TaoPhieuMuon(Ma, NgayMuon, NgayHenTra, MucDich, MSV)` | Tạo phiếu `ChoDuyet` |
| `sp_ThemChiTietMuon(MaPhieu, MaThietBi, SoLuong)` | Thêm một thiết bị vào phiếu (gọi nhiều lần cho nhiều thiết bị) |
| `sp_DuyetPhieuMuon(MaPhieu)` | Duyệt phiếu, trigger kiểm tra lại và trừ kho |
| `sp_TuChoiPhieuMuon(MaPhieu)` | Từ chối phiếu |
| `sp_TraThietBi(MaPhieu, MaThietBi, NgayTra)` | Ghi nhận trả một thiết bị; `NgayTra` NULL = hôm nay |
| `sp_TraTatCaThietBi(MaPhieu, NgayTra)` | Trả mọi thiết bị còn lại bằng một câu UPDATE nguyên tử |
| `sp_LichSuMuon(MSV)` | Lịch sử mượn của thành viên (mỗi dòng một thiết bị), mới nhất trước |
| `vw_ThietBiChuaTra` | Đã duyệt chưa trả; `TinhTrang` = `QuaHan` nếu quá ngày hẹn, kèm số ngày trễ |
| `vw_ThietBiTraMuon` | Đã trả nhưng muộn hơn ngày hẹn, kèm số ngày trễ |

```sql
USE QuanLyCauLacBo;
-- Thay MSV_THUC_TE bằng mã thành viên có sẵn, TB09_01 là thiết bị của seed module 09:
CALL sp_TaoPhieuMuon('PM10_DEMO', '2026-11-01', '2026-11-05', 'Họp ban', 'MSV_THUC_TE');
CALL sp_ThemChiTietMuon('PM10_DEMO', 'TB09_01', 1);
CALL sp_ThemChiTietMuon('PM10_DEMO', 'TB09_03', 2);
CALL sp_DuyetPhieuMuon('PM10_DEMO');             -- trừ khả dụng của 2 thiết bị
SELECT MaThietBi, SoLuongKhaDung FROM CoSoVatChat WHERE MaThietBi IN ('TB09_01', 'TB09_03');
CALL sp_TraThietBi('PM10_DEMO', 'TB09_01', '2026-11-07');   -- trả muộn 2 ngày
CALL sp_TraTatCaThietBi('PM10_DEMO', '2026-11-07');
SELECT * FROM vw_ThietBiTraMuon;
SELECT * FROM vw_ThietBiChuaTra;
CALL sp_LichSuMuon('MSV_THUC_TE');
```

## 4. Java và seed

Thực thể/bản ghi: `PhieuMuon`, `ChiTietMuon`, `DongTheoDoi` (dòng của 2 view theo dõi), `LichSuMuon`. `PhieuMuonDao` chỉ truy cập JDBC (gọi thủ tục/view), `PhieuMuonService` kiểm tra dữ liệu nhập, `PhieuMuonUi` là menu 11 chức năng (nhập 0 để quay lại), `MuonTraModule` nối vào menu chính. Lỗi nghiệp vụ từ DB giữ nguyên thông báo tiếng Việt để hiển thị. Ngày nhập theo `yyyy-MM-dd`, phân tích nghiêm ngặt nên `2026-02-30` bị chặn trước khi ghi DB; duyệt/từ chối có hỏi xác nhận; thêm nhiều thiết bị vào một phiếu, lỗi một dòng thì báo rồi cho nhập tiếp.

Seed dùng mã `PM10_01`…`PM10_05` và thiết bị `TB09_*`, chạy lại không chèn trùng, không duyệt/trả lần hai. Người mượn chọn từ ThanhVien **đã có thật**, xoay vòng theo thứ tự MSV; nếu seed nền còn trống thì các câu chèn 0 dòng (không lỗi), chạy lại sau khi Tú điền seed nền. Dữ liệu đi qua đúng luồng nghiệp vụ nên chạy được cả khi trigger đã có lẫn chưa có:

| Phiếu | Tình huống |
|---|---|
| `PM10_01` | Đã duyệt, đã trả đúng hạn |
| `PM10_02` | Đã duyệt, đã trả muộn 2 ngày |
| `PM10_03` | Đã duyệt, chưa trả, quá hạn (có trong `vw_ThietBiChuaTra`) |
| `PM10_04` | Đang chờ duyệt |
| `PM10_05` | Bị từ chối |

Cuối seed đồng bộ `SoLuongKhaDung` của thiết bị `TB09_*` = tổng − (đã duyệt, chưa trả), cần khi trigger chưa tồn tại lúc seed chạy lần đầu theo thứ tự của `run_all.sql`.

## 5. Kết quả kiểm thử

`tests.sql` tự dựng dữ liệu `T_TRUONG_*` (thành viên, thiết bị, phiếu) trong một transaction rồi rollback. Ca đúng: tạo phiếu và thêm thiết bị chưa trừ kho; duyệt trừ kho cho mọi thiết bị; hai phiếu chờ cùng mượn vừa đủ, duyệt phiếu đầu được; từ chối không đổi kho; sửa số lượng và xóa dòng khi phiếu còn chờ; trả một thiết bị cộng lại kho, trả muộn xuất hiện đúng số ngày trễ trong view; trả tất cả không ghi đè dòng đã trả; trả hết thì kho về như ban đầu; thiết bị quá hạn hiển thị `QuaHan`.

Ca sai (mỗi ca kiểm tra thêm dữ liệu không bị đổi): ngày hẹn trả trước ngày mượn; mã phiếu trùng/trống; người mượn không tồn tại; phiếu tạo sẵn ở trạng thái đã duyệt; thêm trùng thiết bị; mượn vượt khả dụng; số lượng 0 hoặc âm; duyệt phiếu rỗng; duyệt lại; duyệt phiếu thứ hai khi đã hết; duyệt hoặc từ chối phiếu đã xử lý; đổi trạng thái đã duyệt về chờ; thêm/sửa/xóa dòng của phiếu đã duyệt; trả với ngày trước ngày mượn; trả phiếu chưa duyệt hoặc thiết bị không có trong phiếu; trả hai lần; hoàn tác ngày trả; xóa phiếu hoặc dòng đang mượn; FK chặn xóa thành viên đã lập phiếu và thiết bị đã được mượn.

Kết quả chạy trên MySQL (điền sau khi chạy thật):

```text
(dán output của: mysql -u root -p < sql/modules/10_muontra/tests.sql)
```

Unit test Java (`mvn test`): `PhieuMuonServiceTest` 6 ca, `CoSoVatChatServiceTest` 4 ca, `MuonTraMenuTest` 8 ca, đều dùng Dao giả. Ca tích hợp MySQL `MuonTraDatabaseTest` (2 ca, đi qua Service → Dao → JDBC → thủ tục/trigger/view) bật bằng `mvn "-Dclb.mysql.tests=true" test` trên DB thử riêng.

## 6. Bổ sung mô hình quan hệ

Theo DECISIONS #10 cần bổ sung vào mô hình quan hệ ba bảng **CoSoVatChat**, **PhieuMuon**, **ChiTietMuon** cùng các liên kết: ThanhVien –(Lập)→ PhieuMuon (1–N), PhieuMuon –(Mượn)→ CoSoVatChat qua ChiTietMuon (N–N), ThanhVien –(Quản lý)→ CoSoVatChat (1–N, tùy chọn). Sơ đồ này chưa được vẽ trong bộ file này; xem mục "Việc còn lại" trong [DONG_BAN_GIAO_TRUONG.md](DONG_BAN_GIAO_TRUONG.md).
