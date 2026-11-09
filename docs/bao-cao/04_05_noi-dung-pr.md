## Module / nội dung

04_khoahoc, 05_duan – bổ sung SQL, dữ liệu mẫu, kiểm thử và báo cáo; đồng bộ quy ước tiến độ Java/SQL.

## Đã làm

- [x] Thêm seed KH01/KH02 và DA01/DA02; chỉ tham chiếu thành viên đã tồn tại.
- [x] Thêm view chi tiết, thống kê số thành viên, thủ tục tra cứu khóa học/dự án.
- [x] Thêm truy vấn lịch sử tham gia của thành viên qua các view.
- [x] Thêm ba trigger tính lại tiến độ khi INSERT/UPDATE/DELETE lượt tham gia dự án, gồm chuyển giữa hai dự án.
- [x] Đồng bộ tiến độ cho seed nạp trước trigger; thống nhất Java/SQL: AVG bỏ NULL, chưa có tiến độ thì bằng 0.
- [x] Bổ sung tests.sql hai module: ca hợp lệ, CHECK, PK, FK; kiểm tra tính tiến độ, làm tròn và xóa.
- [x] Test tự tạo dữ liệu riêng và ROLLBACK; đã chạy lại thành công.
- [x] Chạy sql/run_all.sql từ DB trống không lỗi trên MySQL 8.0.46 riêng biệt.
- [x] Viết báo cáo bảng, ràng buộc, truy vấn, cách chạy và kết quả test trong 04_khoahoc.md, 05_duan.md.

## Checklist trước khi xin merge

- [ ] Chỉ sửa file trong module của mình (cần kiểm tra toàn bộ diff của PR; nhánh Chien đã chứa thay đổi app/common từ commit trước).
- [x] Chạy sql/run_all.sql từ DB trống không lỗi.
- [x] Có test/ca thử đúng + sai trong tests.sql của cả hai module; cả hai báo PASS.
- [ ] Đã git pull --rebase origin develop trước khi push (chưa có nhánh develop trên origin tại thời điểm kiểm tra 30/10/2026).
- [x] Mô tả bảng/ràng buộc của module đã cập nhật trong docs/bao-cao/.

## Cần Tú lưu ý

- Origin hiện chưa có nhánh develop. Cần tạo/xác nhận nhánh tích hợp, sau đó Chiến commit, rebase, chạy lại kiểm tra rồi push.
- Nhánh Chien đã có thay đổi file chung từ commit trước: src/main/app/MainMenu.java, src/main/common/*, ClubWorkspace.java, SwingApp.java (so với origin/main lưu trên máy). Cần rà diff theo base thật trước khi tick mục chỉ sửa module; có thể tách PR phần khung Swing với Tú.
- Đã chạy mvn test: Java biên dịch được, nhưng MainMenuTest.menuHas10Modules thất bại (expected 10, actual 8). Hai module khóa học/dự án đang bị comment trong MainMenu; các lớp Module hiện cung cấp createSwingUi(), chưa implements AppModule. Tú cần chốt cách tích hợp console/Swing, không chỉ bỏ comment.
- seed_master.sql đang là TODO. Vì vậy DB sạch có khóa học/dự án mẫu nhưng chưa có thành viên/lượt tham gia mẫu; test đã tự tạo fixture và ROLLBACK. Cần Tú bổ sung seed nền.
- Chốt quy ước tiến độ: bỏ qua NULL khi AVG, không có tiến độ thì 0; lưu hai chữ số thập phân.
- MySQL không kích hoạt trigger con khi xóa/đổi mã thành viên bằng FK CASCADE. Cần thống nhất luồng quản lý thành viên để tránh cột TienDoChung cũ; view và Java tính AVG trực tiếp vẫn phản ánh dữ liệu hiện tại. Chưa kiểm thử ghi đồng thời nhiều phiên.
- run_all.sql không tự chạy tests.sql. Sau khi dựng DB, chạy riêng SOURCE modules/04_khoahoc/tests.sql; và SOURCE modules/05_duan/tests.sql; từ thư mục sql trong MySQL client.
- Chưa xác nhận thao tác UI bằng tay; chưa commit, rebase hoặc push các thay đổi lần này.

Sau khi cập nhật develop hoặc sửa phần tích hợp, chạy lại kiểm tra và cập nhật nội dung/checklist này trước khi gửi review.
