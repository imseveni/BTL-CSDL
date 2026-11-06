# Đánh giá theo tiêu chí chấm và kế hoạch hoàn thiện

Căn cứ: tiêu chí chấm BTL do cô thông báo (8 mục, tổng 10 điểm). Đánh giá dựa trên kho mã nhánh `develop` (commit `f9a239c`) và bản mô tả đề tài gốc. Tỷ lệ "hoàn thiện" là **ước lượng của người soạn để sắp xếp công việc, không phải điểm dự đoán**.

## 1. Kết luận ngắn

- Nhóm đã làm **nhiều hơn mức cần** cho phần mềm (mục 5, 6) nhưng **thiếu phần thiết kế dạng văn bản** (mục 1, 4), **chưa có gì cho báo cáo xuất file** (mục 7) và **báo cáo BTL chưa gộp** (mục 8).
- Mục 1 đến 4 chiếm **6,5 / 10 điểm**; phần mềm (5, 6, 7) chỉ chiếm **2,5 / 10**. Vì vậy **kế hoạch "chuyển toàn bộ 10 module sang Swing" cần thu hẹp**: tiêu chí chỉ đòi 2 màn hình nhập, 1 màn hình tìm kiếm, 2 báo cáo. Hiện đã có 3 màn hình Swing (Thành viên, Khóa học, Dự án). Các màn hình Swing còn lại chỉ nên làm khi đã xong mục 1 đến 7.
- Ước lượng độ hoàn thiện theo trọng số: khoảng **một nửa** (xem bảng).

## 2. Hiện trạng từng tiêu chí

| # | Tiêu chí | Điểm | Hoàn thiện | Đã có | Còn thiếu |
|---|---|---|---|---|---|
| 1 | Mô tả tổng quan yêu cầu dữ liệu | 1 | ~40% | Nội dung nằm trong **bản mô tả gốc (file .txt, ngoài repo)**: mục đích, tác nhân, 7 nhóm dữ liệu, 10 nhóm chức năng | Chưa có trong repo; chưa cập nhật theo thiết kế cuối (Email/SĐT đơn trị, kết quả đánh giá khóa học, quản lý thiết bị...) |
| 2 | Vẽ và mô tả ERD | 2,5 | ~70% | Hình ERD đã vẽ: `docs/erd/ERD_trang2_mo_hinh_ER.png`; mô tả thực thể, thuộc tính, 11 quan hệ trong bản gốc | Mô tả chưa vào repo; hình và mô tả còn lệch thiết kế ở điểm "Quản lý" (điểm #4 của `DECISIONS.md`); chưa ghi rõ bản số và ràng buộc tham gia |
| 3 | Mô hình quan hệ và mô tả bảng | 2 | ~65% | `sql/00_schema/schema.sql` (16 bảng); hình đầy đủ `docs/erd/Mo_hinh_quan_he_day_du.png` (sinh từ schema); mô tả bảng rải rác trong 10 báo cáo module | Chưa có **từ điển dữ liệu tổng hợp** (cột, kiểu, khóa, ràng buộc, ý nghĩa); chưa có **quy tắc ánh xạ ER sang quan hệ**; hình trang 3 của draw.io còn cũ |
| 4 | Chuẩn hóa 3NF | 1 | ~15% | Chưa có văn bản nào. Phân tích sơ bộ ở mục 3 cho thấy **16 bảng đã ở 3NF** | Viết chính thức: tập phụ thuộc hàm từng bảng, kiểm tra 1NF, 2NF, 3NF, kết luận và ghi chú 2 cột suy diễn |
| 5 | 2 màn hình nhập dữ liệu, có demo kiểm tra tính đúng đắn | 1 | ~85% | 3 màn hình Swing có kiểm tra dữ liệu: Thành viên, Khóa học, Dự án; Service và thủ tục SQL chặn dữ liệu sai; test tự động | **Chưa chạy trên MySQL thật** (đặc biệt Thành viên); chưa có kịch bản demo, bộ ca đúng/sai và ảnh chụp |
| 6 | 1 màn hình tìm kiếm | 0,5 | ~90% | Thành viên: tìm theo MSV/họ tên, lọc ban và trạng thái, hiện kết quả ra bảng; Khóa học có tìm kiếm | Kiểm tra trên MySQL thật; ảnh chụp và câu SQL cho báo cáo |
| 7 | 2 báo cáo xem trên màn hình và xuất file | 1 | ~10% | Dữ liệu sẵn: `vw_ThuChiTheoSuKien`, `vw_ThietBiChuaTra`, `vw_ThietBiTraMuon`, `vw_BanChuyenMonThongKe`, `vw_DuAnThongKe`... | **Toàn bộ phần còn lại**: màn hình báo cáo, xuất file Excel hoặc PDF theo mẫu thực tế, ghi rõ người lập và người nhận; `pom.xml` chưa có thư viện xuất file |
| 8 | Báo cáo BTL đủ 7 mục + SQL của mục 5, 6, 7 | 1 | ~20% | 11 báo cáo theo module và `TONG_HOP.md`; SQL nằm trong `sql/modules/` | Chưa gộp thành một báo cáo; chưa có phụ lục SQL gắn với từng màn hình và báo cáo; làm sau cùng theo kế hoạch |

## 3. Kết quả kiểm tra 3NF sơ bộ (để người viết mục 4 kiểm chứng lại)

Giả định phụ thuộc hàm: mỗi thuộc tính không khóa phụ thuộc vào toàn bộ khóa chính của bảng; không có quy tắc nghiệp vụ nào khác. Nếu nghiệp vụ có thêm quy tắc, phải bổ sung.

| Bảng | Khóa chính | Nhận xét |
|---|---|---|
| BanChuyenMon, QuyenHan, KhoaHoc, SuKien | Mã tương ứng | Mọi thuộc tính phụ thuộc trực tiếp vào khóa. Đạt 3NF |
| ThanhVien | MSV | Tên ban không lưu ở đây (nằm ở BanChuyenMon). Email và SĐT là **đơn trị** theo ERD, nên đạt 1NF. Đạt 3NF |
| ThanhVien_QuyenHan | (MSV, MaQuyenHan) | Bảng chỉ có khóa. Đạt 3NF |
| ThamGiaKhoaHoc, ThamGiaDuAn, ThamGiaSuKien | (MSV, mã) | Vai trò, kết quả, tiến độ riêng phụ thuộc vào cả cặp. Đạt 3NF |
| ThuChi, ThuChi_PhuongThucThanhToan | MaGD; (MaGD, phương thức) | Phương thức thanh toán là **đa trị** nên đã tách bảng để đạt 1NF. Đạt 3NF |
| DanhGia | MaGhiNhan | Cần xác nhận: nếu nghiệp vụ quy định mỗi *loại đánh giá* có tập *hình thức* cố định (LoaiDanhGia xác định HinhThuc) thì phải tách bảng LoaiDanhGia. Nếu không có quy tắc đó thì đạt 3NF |
| PhieuMuon | MaPhieuMuon | Đạt 3NF |
| CoSoVatChat | MaThietBi | Đạt 3NF theo phụ thuộc hàm, nhưng `SoLuongKhaDung` là **giá trị suy diễn** (bằng tổng trừ số đang mượn) được trigger ở module 10 duy trì |
| ChiTietMuon | (MaPhieuMuon, MaThietBi) | Số lượng mượn và ngày trả thực tế phụ thuộc vào cả cặp. Đạt 3NF |

**Hai cột suy diễn cần ghi rõ trong báo cáo** (không vi phạm 3NF nhưng gây dư thừa dữ liệu): `DuAn.TienDoChung` (trung bình `TienDoRieng`, ERD vẽ nét đứt) và `CoSoVatChat.SoLuongKhaDung`. Nhóm chọn một trong hai: (a) **giữ**, giải thích là chủ ý để truy vấn nhanh và được trigger duy trì (đang làm vậy, không phải sửa code); (b) bỏ cột và tính bằng view (phải sửa module 05 và 10). Đề xuất: (a).

## 4. Kế hoạch tiếp theo

### 4.1 Thay đổi so với `PHAN_CONG_SWING.md`

- **Ưu tiên lại:** làm theo tiêu chí. Việc của mục 1 đến 4 và báo cáo mục 7 đi **trước**.
- **Màn hình Swing còn lại (02, 03, 06, 07, 08, 09, 10):** chuyển thành **tùy chọn**, chỉ làm khi xong các việc dưới đây. Menu console của các module này vẫn dùng được cho phần demo nếu cần.
- **Giữ nguyên** cửa sổ chính, màn hình mẫu Thành viên, Khóa học, Dự án.

### 4.2 Việc cần làm và người phụ trách

| Nhóm việc | Sản phẩm | Người | Tiêu chí |
|---|---|---|---|
| **A1. Mô tả yêu cầu dữ liệu** | `docs/bao-cao-btl/01_yeu_cau_du_lieu.md`: mục đích, tác nhân, 7 nhóm dữ liệu, chức năng chính, cập nhật theo thiết kế cuối | Đông | 1 |
| **A2. Mô tả ERD** | `02_erd.md` + hình ERD cập nhật: bảng thực thể, thuộc tính (khóa, đa trị, suy diễn), từng quan hệ kèm bản số và ràng buộc tham gia; thống nhất điểm "Quản lý" | Minh và Chiến | 2 |
| **A3. Mô hình quan hệ và từ điển dữ liệu** | `03_mo_hinh_quan_he.md`: quy tắc ánh xạ ER sang quan hệ, hình đầy đủ, từ điển dữ liệu 16 bảng (cột, kiểu, khóa, ràng buộc, ý nghĩa); mỗi người viết các bảng thuộc module của mình | Tú tổng hợp; Đông, Chiến, Minh, Trường mỗi người viết bảng của mình | 3 |
| **A4. Chuẩn hóa 3NF** | `04_chuan_hoa_3nf.md`: phụ thuộc hàm từng bảng, kiểm tra 1NF, 2NF, 3NF, kết luận, ghi chú hai cột suy diễn | Tú (Đông soát) | 4 |
| **B1. Chạy thật trên MySQL** | Chạy `run_all.sql` từ DB trống và `tests.sql` của 10 module; ghi kết quả vào `TONG_HOP.md` | Mỗi người chạy module mình; Tú chạy tổng | 5, 6, 8 |
| **B2. Gói demo mục 5 và 6** | Bộ ca đúng/sai cho 2 màn hình nhập (đề xuất **Thành viên** và **Dự án** hoặc **Khóa học**), kịch bản 5 phút, ảnh chụp, danh sách câu SQL dùng | Tú (Thành viên), Chiến (Dự án, Khóa học) | 5, 6 |
| **C1. Khung báo cáo xuất file** | Thêm thư viện xuất file vào `pom.xml`; tab **Báo cáo** trong cửa sổ chính (chọn tham số, xem kết quả trên bảng, nút xuất); lớp xuất dùng chung | Chiến (Tú thêm thư viện và duyệt) | 7 |
| **C2. Báo cáo 1: Thu – Chi** | Dữ liệu, mẫu trình bày và file xuất (xem mục 4.3) | Minh | 7 |
| **C3. Báo cáo 2: Mượn – Trả** | Dữ liệu, mẫu trình bày và file xuất (xem mục 4.3) | Trường | 7 |
| **D. Báo cáo BTL** | Gộp A1 đến A4, B2, C vào một báo cáo, thêm phụ lục SQL của mục 5, 6, 7 | Tú tổng hợp, làm sau cùng | 8 |

Mỗi người giữ file của mình trong `docs/bao-cao-btl/` (một file một mục) để không xung đột.

### 4.3 Hai báo cáo đề xuất (nói rõ thực tế sử dụng, đúng yêu cầu của cô)

| | Báo cáo 1: Thu – Chi theo sự kiện và kỳ | Báo cáo 2: Thiết bị chưa trả và trả muộn |
|---|---|---|
| **Ai lập** | Thủ quỹ của CLB (thành viên Ban Chủ nhiệm có quyền "Quản lý tài chính") | Thành viên quản lý cơ sở vật chất (Ban Hậu cần) |
| **Gửi cho ai** | Chủ nhiệm CLB và giảng viên hoặc đơn vị phụ trách CLB, để duyệt quyết toán sau mỗi sự kiện hoặc học kỳ | Chủ nhiệm CLB, để nhắc thành viên trả thiết bị và quyết định xử lý |
| **Nội dung** | Bảng thu, chi theo sự kiện: mã giao dịch, mục đích, số tiền, thời gian, phương thức, người thực hiện; tổng thu, tổng chi, chênh lệch | Danh sách phiếu: người mượn, thiết bị, số lượng, ngày hẹn trả, số ngày trễ |
| **Dữ liệu sẵn có** | `vw_ThuChiTheoSuKien`, `vw_ChiTietGiaoDich` (module 07) | `vw_ThietBiChuaTra`, `vw_ThietBiTraMuon` (module 10) |
| **Định dạng xuất đề xuất** | Excel (.xlsx), giống sổ quỹ: tiêu đề, kỳ báo cáo, bảng, dòng tổng, chỗ ký tên | PDF hoặc Excel, giống biên bản: tiêu đề, ngày lập, bảng, người lập |
| **Tham số** | Sự kiện hoặc khoảng ngày | Ngày tính trễ (mặc định hôm nay) |

**Phương án dự phòng** nếu một module chưa chạy ổn trên MySQL thật (module 06, 07, 09, 10 chưa có bằng chứng): thay bằng *Báo cáo nhân sự theo ban* (gửi Ban Chủ nhiệm và đơn vị quản lý) hoặc *Báo cáo tiến độ dự án* (gửi Chủ nhiệm và giảng viên cố vấn). Hai báo cáo này dựa trên module 01, 02, 05 là những phần đã được kiểm chứng nhiều hơn.

**Thư viện xuất file:** Apache POI cho Excel; trang tải chính thức của Apache POI hiện ghi bản ổn định mới nhất là 6.0.0 (22/10/2026) và các bản 5.4.x vẫn là dòng cũ phổ biến. **Cần kiểm tra yêu cầu phiên bản Java và cách khai báo dependency của bản đang chọn trước khi thêm vào `pom.xml`** (dự án đang dùng Java 17). PDF tiếng Việt cần nhúng font Unicode (ví dụ Noto Sans) trong `src/main/resources`. Nếu muốn ít rủi ro nhất: làm cả hai báo cáo bằng Excel, PDF làm sau.

### 4.4 Thứ tự và mốc (điền deadline)

| Mốc | Nội dung | Hạn |
|---|---|---|
| **M1** | B1 xong (mọi module chạy thật, có kết quả); A1, A2 bản nháp; Chiến xong khung báo cáo (C1) | ____ |
| **M2** | A1 đến A4 hoàn chỉnh; hai báo cáo xem được trên màn hình và xuất được file; B2 có kịch bản và ảnh | ____ |
| **M3** | Tích hợp, duyệt chéo, chụp ảnh cuối, đóng gói | ____ |
| **M4** | D: viết báo cáo BTL 8 mục, gắn phụ lục SQL | ____ |

### 4.5 Quy trình duyệt chéo

Mỗi file trong `docs/bao-cao-btl/` do một người viết và một người khác đọc đối chiếu với `schema.sql` và ERD (đề xuất: Đông đọc A4, Tú đọc A1 và A2, Chiến đọc A3, Trường đọc B2). Mọi mâu thuẫn phát hiện được ghi vào `DECISIONS.md`.

## 5. Điểm cần cả nhóm quyết định ngay

1. **Điểm "Quản lý" (#4):** thiết bị có một người quản lý, hay mỗi dòng mượn có một người quản lý? Cần chốt trước khi viết A2 và A3 vì ảnh hưởng đến ERD và từ điển dữ liệu.
2. **Hai cột suy diễn** (`TienDoChung`, `SoLuongKhaDung`): giữ hay bỏ (đề xuất giữ).
3. **Hai báo cáo** chọn ở mục 4.3 hay dùng phương án dự phòng.
4. **Hai màn hình nhập để demo** (đề xuất Thành viên và Dự án).
5. **Thư viện xuất file** và có làm PDF hay chỉ Excel.

## 6. Rủi ro

- **Chưa chạy thật trên MySQL** (nhất là module 01, 06, 07, 09, 10): báo cáo và demo dựa vào SQL chưa kiểm chứng. Làm B1 đầu tiên.
- **Mô tả, ERD và schema lệch nhau:** cô chấm mục 2 và 3 theo tính tương thích giữa ERD và mô hình quan hệ. Mọi chỗ lệch (xem `DECISIONS.md`) phải được giải quyết hoặc giải thích trong báo cáo.
- **Làm quá nhiều giao diện, thiếu văn bản:** 6,5 điểm nằm ở thiết kế và mô tả, không nằm ở số lượng màn hình.
