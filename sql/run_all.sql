-- Chạy từ thư mục sql/:  mysql -u root -p < run_all.sql
-- (hoặc trong mysql client:  SOURCE run_all.sql;)
-- Thứ tự: schema -> seed nền -> seed từng module -> truy vấn từng module

SOURCE 00_schema/schema.sql;
SOURCE 01_seed_master/seed_master.sql;

SOURCE modules/01_thanhvien/seed.sql;
SOURCE modules/02_banchuyenmon/seed.sql;
SOURCE modules/03_quyenhan/seed.sql;
SOURCE modules/04_khoahoc/seed.sql;
SOURCE modules/05_duan/seed.sql;
SOURCE modules/06_sukien/seed.sql;
SOURCE modules/07_thuchi/seed.sql;
SOURCE modules/08_danhgia/seed.sql;
SOURCE modules/09_cosovatchat/seed.sql;
SOURCE modules/10_muontra/seed.sql;

SOURCE modules/01_thanhvien/queries.sql;
SOURCE modules/02_banchuyenmon/queries.sql;
SOURCE modules/03_quyenhan/queries.sql;
SOURCE modules/04_khoahoc/queries.sql;
SOURCE modules/05_duan/queries.sql;
SOURCE modules/06_sukien/queries.sql;
SOURCE modules/07_thuchi/queries.sql;
SOURCE modules/08_danhgia/queries.sql;
SOURCE modules/09_cosovatchat/queries.sql;
SOURCE modules/10_muontra/queries.sql;
