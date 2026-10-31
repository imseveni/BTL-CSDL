-- =====================================================================
-- SCHEMA – Quản lý Câu lạc bộ (MySQL 8.0.16+)
-- CHỈ Tú được sửa file này. Muốn đổi schema: tạo Issue đề xuất.
-- Schema đã chỉnh theo ERD (docs/erd/ERD_trang2...) và mô hình quan hệ (docs/erd/ERD_trang3...).
-- Các chỗ đánh dấu [FIX #n] là điểm lệch so với bản mô tả gốc, xem docs/DECISIONS.md.
-- =====================================================================

CREATE DATABASE IF NOT EXISTS QuanLyCauLacBo
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QuanLyCauLacBo;

-- 01. BanChuyenMon
CREATE TABLE BanChuyenMon (
    MaBan  VARCHAR(20) PRIMARY KEY,
    TenBan VARCHAR(100) NOT NULL
);

-- 02. ThanhVien
CREATE TABLE ThanhVien (
    MSV         VARCHAR(20) PRIMARY KEY,
    HoVaTen     VARCHAR(100) NOT NULL,
    NgaySinh    DATE,
    GioiTinh    VARCHAR(20),
    Email       VARCHAR(100),
    SDT         VARCHAR(20),
    NgayGiaNhap DATE NOT NULL,
    TrangThaiHD VARCHAR(50),
    MaBan       VARCHAR(20) NOT NULL,
    ChucVu      VARCHAR(100),
    CONSTRAINT FK_ThanhVien_BanChuyenMon
        FOREIGN KEY (MaBan) REFERENCES BanChuyenMon(MaBan)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT CK_ThanhVien_NgayGiaNhap
        CHECK (NgaySinh IS NULL OR NgayGiaNhap >= NgaySinh)
);

-- 03. QuyenHan
CREATE TABLE QuyenHan (
    MaQuyenHan  VARCHAR(20) PRIMARY KEY,
    TenQuyenHan VARCHAR(100) NOT NULL,
    MoTaChiTiet TEXT
);

-- 04. ThanhVien_QuyenHan (M-N)
CREATE TABLE ThanhVien_QuyenHan (
    MSV        VARCHAR(20),
    MaQuyenHan VARCHAR(20),
    PRIMARY KEY (MSV, MaQuyenHan),
    CONSTRAINT FK_TVQH_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_TVQH_QuyenHan
        FOREIGN KEY (MaQuyenHan) REFERENCES QuyenHan(MaQuyenHan)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- 05. KhoaHoc
CREATE TABLE KhoaHoc (
    MaKhoaHoc     VARCHAR(20) PRIMARY KEY,
    TenKhoaHoc    VARCHAR(150) NOT NULL,
    NgayKhaiGiang DATE NOT NULL,
    NgayBeGiang   DATE,
    CONSTRAINT CK_KhoaHoc_ThoiGian
        CHECK (NgayBeGiang IS NULL OR NgayBeGiang >= NgayKhaiGiang)
);

-- 06. ThamGiaKhoaHoc  [FIX #1] theo ERD: VaiTro, KetQuaDanhGia
CREATE TABLE ThamGiaKhoaHoc (
    MSV           VARCHAR(20),
    MaKhoaHoc     VARCHAR(20),
    VaiTro        VARCHAR(100),
    KetQuaDanhGia VARCHAR(255),
    PRIMARY KEY (MSV, MaKhoaHoc),
    CONSTRAINT FK_TGKH_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_TGKH_KhoaHoc
        FOREIGN KEY (MaKhoaHoc) REFERENCES KhoaHoc(MaKhoaHoc)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- 07. DuAn  [FIX #2] TienDoChung là thuộc tính suy diễn (nét đứt trong ERD) nhưng có cột trong mô hình
--     quan hệ -> giữ cột, cập nhật tự động bằng trigger từ ThamGiaDuAn.TienDoRieng (module 05)
CREATE TABLE DuAn (
    MaDuAn       VARCHAR(20) PRIMARY KEY,
    TenDuAn      VARCHAR(150) NOT NULL,
    MucTieu      TEXT,
    NgayBatDau   DATE NOT NULL,
    HanHoanThanh DATE,
    TienDoChung  DECIMAL(5,2) DEFAULT 0,
    CONSTRAINT CK_DuAn_ThoiGian
        CHECK (HanHoanThanh IS NULL OR HanHoanThanh >= NgayBatDau),
    CONSTRAINT CK_DuAn_TienDoChung
        CHECK (TienDoChung >= 0 AND TienDoChung <= 100)
);

-- 08. ThamGiaDuAn
CREATE TABLE ThamGiaDuAn (
    MSV         VARCHAR(20),
    MaDuAn      VARCHAR(20),
    VaiTro      VARCHAR(100),
    TienDoRieng DECIMAL(5,2) DEFAULT 0,
    PRIMARY KEY (MSV, MaDuAn),
    CONSTRAINT FK_TGDA_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_TGDA_DuAn
        FOREIGN KEY (MaDuAn) REFERENCES DuAn(MaDuAn)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT CK_TGDA_TienDoRieng
        CHECK (TienDoRieng >= 0 AND TienDoRieng <= 100)
);

-- 09. SuKien
CREATE TABLE SuKien (
    MaSK        VARCHAR(20) PRIMARY KEY,
    TenSK       VARCHAR(150) NOT NULL,
    NgayBatDau  DATE NOT NULL,
    NgayKetThuc DATE,
    CONSTRAINT CK_SuKien_ThoiGian
        CHECK (NgayKetThuc IS NULL OR NgayKetThuc >= NgayBatDau)
);

-- 10. ThamGiaSuKien
CREATE TABLE ThamGiaSuKien (
    MSV    VARCHAR(20),
    MaSK   VARCHAR(20),
    VaiTro VARCHAR(100),
    PRIMARY KEY (MSV, MaSK),
    CONSTRAINT FK_TGSK_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_TGSK_SuKien
        FOREIGN KEY (MaSK) REFERENCES SuKien(MaSK)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- 11. ThuChi  [FIX #5] 'Phát sinh' 1 SK – N ThuChi, nét đơn (không bắt buộc) -> MaSK cho phép NULL
CREATE TABLE ThuChi (
    MaGD     VARCHAR(20) PRIMARY KEY,
    LoaiGD   VARCHAR(20) NOT NULL,
    MucDich  TEXT,
    SoTien   DECIMAL(15,2) NOT NULL,
    ThoiGian DATETIME NOT NULL,
    MinhChung TEXT,
    MSV      VARCHAR(20),
    MaSK     VARCHAR(20),
    CONSTRAINT CK_ThuChi_LoaiGD CHECK (LoaiGD IN ('Thu', 'Chi')),
    CONSTRAINT CK_ThuChi_SoTien CHECK (SoTien > 0),
    CONSTRAINT FK_ThuChi_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FK_ThuChi_SuKien
        FOREIGN KEY (MaSK) REFERENCES SuKien(MaSK)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 12. ThuChi_PhuongThucThanhToan (thuộc tính đa trị, oval đôi trong ERD)  [FIX #6]
CREATE TABLE ThuChi_PhuongThucThanhToan (
    MaGD                VARCHAR(20),
    PhuongThucThanhToan VARCHAR(100),
    PRIMARY KEY (MaGD, PhuongThucThanhToan),
    CONSTRAINT FK_PTTT_ThuChi
        FOREIGN KEY (MaGD) REFERENCES ThuChi(MaGD)
        ON UPDATE CASCADE ON DELETE CASCADE
);

-- 13. DanhGia
CREATE TABLE DanhGia (
    MaGhiNhan     VARCHAR(20) PRIMARY KEY,
    LoaiDanhGia   VARCHAR(100),
    HinhThuc      VARCHAR(100),
    LyDo          TEXT,
    NgayQuyetDinh DATE NOT NULL,
    MSV           VARCHAR(20) NOT NULL,
    CONSTRAINT FK_DanhGia_ThanhVien
        FOREIGN KEY (MSV) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 14. PhieuMuon  [FIX #7] TrangThaiDuyet có trong ERD
CREATE TABLE PhieuMuon (
    MaPhieuMuon    VARCHAR(20) PRIMARY KEY,
    NgayMuon       DATE NOT NULL,
    NgayHenTra     DATE NOT NULL,
    MucDichMuon    TEXT,
    TrangThaiDuyet VARCHAR(20) NOT NULL DEFAULT 'ChoDuyet',
    MSVNguoiMuon   VARCHAR(20) NOT NULL,
    CONSTRAINT CK_PhieuMuon_ThoiGian CHECK (NgayHenTra >= NgayMuon),
    CONSTRAINT CK_PhieuMuon_TrangThaiDuyet
        CHECK (TrangThaiDuyet IN ('ChoDuyet', 'DaDuyet', 'TuChoi')),
    CONSTRAINT FK_PhieuMuon_ThanhVien
        FOREIGN KEY (MSVNguoiMuon) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE RESTRICT
);

-- 15. CoSoVatChat  [FIX #3] ERD không có TinhTrang
CREATE TABLE CoSoVatChat (
    MaThietBi      VARCHAR(20) PRIMARY KEY,
    TenThietBi     VARCHAR(150) NOT NULL,
    TongSoLuong    INT NOT NULL,
    SoLuongKhaDung INT NOT NULL,
    CONSTRAINT CK_CSVT_TongSoLuong CHECK (TongSoLuong >= 0),
    CONSTRAINT CK_CSVT_SoLuongKhaDung
        CHECK (SoLuongKhaDung >= 0 AND SoLuongKhaDung <= TongSoLuong)
);

-- 16. ChiTietMuon (quan hệ 'Mượn' giữa PhieuMuon và CoSoVatChat)
--     [FIX #4] 'Quản lý' nối ThanhVien (1) – N vào khung kết tập [PhieuMuon – Muon – CoSoVatChat]
--     -> mỗi dòng mượn có 1 thành viên quản lý (MSVQuanLy)
CREATE TABLE ChiTietMuon (
    MaPhieuMuon   VARCHAR(20),
    MaThietBi     VARCHAR(20),
    SoLuongMuon   INT NOT NULL,
    NgayTraThucTe DATE,
    MSVQuanLy     VARCHAR(20) NOT NULL,
    PRIMARY KEY (MaPhieuMuon, MaThietBi),
    CONSTRAINT CK_ChiTietMuon_SoLuong CHECK (SoLuongMuon > 0),
    CONSTRAINT FK_ChiTietMuon_PhieuMuon
        FOREIGN KEY (MaPhieuMuon) REFERENCES PhieuMuon(MaPhieuMuon)
        ON UPDATE CASCADE ON DELETE CASCADE,
    CONSTRAINT FK_ChiTietMuon_CSVT
        FOREIGN KEY (MaThietBi) REFERENCES CoSoVatChat(MaThietBi)
        ON UPDATE CASCADE ON DELETE RESTRICT,
    CONSTRAINT FK_ChiTietMuon_NguoiQuanLy
        FOREIGN KEY (MSVQuanLy) REFERENCES ThanhVien(MSV)
        ON UPDATE CASCADE ON DELETE RESTRICT
);
