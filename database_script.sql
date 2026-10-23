-- ============================================================================
-- DATABASE SCRIPT: MaiHaThanhVy_AttendanceDB
-- Hệ thống Điểm danh Sinh viên (Backend ASP.NET Core & Frontend Flutter)
-- Server: localhost\SQLEXPRESS
-- ============================================================================

USE master;
GO

IF NOT EXISTS (SELECT name FROM sys.databases WHERE name = N'MaiHaThanhVy_AttendanceDB')
BEGIN
    CREATE DATABASE [MaiHaThanhVy_AttendanceDB];
END
GO

USE [MaiHaThanhVy_AttendanceDB];
GO

SET ANSI_NULLS ON;
SET QUOTED_IDENTIFIER ON;
GO

-- ============================================================================
-- 0. BẢNG LỊCH SỬ MIGRATION CỦA ENTITY FRAMEWORK CORE (__EFMigrationsHistory)
-- Đảm bảo Backend chạy context.Database.Migrate() không bị xung đột bảng
-- ============================================================================
IF OBJECT_ID(N'[__EFMigrationsHistory]', N'U') IS NULL
BEGIN
    CREATE TABLE [__EFMigrationsHistory] (
        [MigrationId] nvarchar(150) NOT NULL,
        [ProductVersion] nvarchar(32) NOT NULL,
        CONSTRAINT [PK___EFMigrationsHistory] PRIMARY KEY ([MigrationId])
    );
END
GO

IF NOT EXISTS (SELECT 1 FROM [__EFMigrationsHistory] WHERE [MigrationId] = N'20261022043835_InitialCreate')
BEGIN
    INSERT INTO [__EFMigrationsHistory] ([MigrationId], [ProductVersion])
    VALUES (N'20261022043835_InitialCreate', N'9.0.0');
END
GO

-- ============================================================================
-- 1. TABLE: OtpCodes (Mã xác thực OTP 2 bước qua Email)
-- ============================================================================
IF OBJECT_ID(N'[OtpCodes]', N'U') IS NULL
BEGIN
    CREATE TABLE [OtpCodes] (
        [Id] int NOT NULL IDENTITY(1,1),
        [Email] nvarchar(150) NOT NULL,
        [Code] nvarchar(10) NOT NULL,
        [Type] nvarchar(30) NOT NULL, -- LOGIN, REGISTER, RESET_PASSWORD
        [ExpiresAt] datetime2 NOT NULL,
        [IsUsed] bit NOT NULL DEFAULT 0,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT [PK_OtpCodes] PRIMARY KEY ([Id])
    );
END
GO

-- ============================================================================
-- 2. TABLE: Subjects (Môn học)
-- ============================================================================
IF OBJECT_ID(N'[Subjects]', N'U') IS NULL
BEGIN
    CREATE TABLE [Subjects] (
        [Id] int NOT NULL IDENTITY(1,1),
        [SubjectCode] nvarchar(50) NOT NULL,
        [SubjectName] nvarchar(200) NOT NULL,
        [Credits] int NOT NULL DEFAULT 3,
        [Description] nvarchar(1000) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_Subjects] PRIMARY KEY ([Id])
    );

    CREATE UNIQUE INDEX [IX_Subjects_SubjectCode] ON [Subjects] ([SubjectCode]);
END
GO

-- ============================================================================
-- 3. TABLE: Users (Tài khoản người dùng: ADMIN, TEACHER, STUDENT)
-- ============================================================================
IF OBJECT_ID(N'[Users]', N'U') IS NULL
BEGIN
    CREATE TABLE [Users] (
        [Id] int NOT NULL IDENTITY(1,1),
        [Email] nvarchar(150) NOT NULL,
        [PasswordHash] nvarchar(255) NULL,
        [FullName] nvarchar(100) NOT NULL,
        [Role] nvarchar(20) NOT NULL DEFAULT N'STUDENT', -- ADMIN, TEACHER, STUDENT
        [PhoneNumber] nvarchar(20) NULL,
        [Avatar] nvarchar(500) NULL,
        [IsEmailVerified] bit NOT NULL DEFAULT 0,
        [FaceData] nvarchar(max) NULL,
        [GoogleId] nvarchar(100) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_Users] PRIMARY KEY ([Id])
    );

    CREATE UNIQUE INDEX [IX_Users_Email] ON [Users] ([Email]);
END
GO

-- ============================================================================
-- 4. TABLE: Students (Hồ sơ Sinh viên)
-- ============================================================================
IF OBJECT_ID(N'[Students]', N'U') IS NULL
BEGIN
    CREATE TABLE [Students] (
        [Id] int NOT NULL IDENTITY(1,1),
        [StudentCode] nvarchar(50) NOT NULL,
        [FullName] nvarchar(100) NOT NULL,
        [Email] nvarchar(150) NOT NULL,
        [DateOfBirth] datetime2 NULL,
        [Gender] nvarchar(20) NULL,
        [PhoneNumber] nvarchar(20) NULL,
        [Address] nvarchar(255) NULL,
        [Avatar] nvarchar(500) NULL,
        [Major] nvarchar(100) NULL,
        [UserId] int NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_Students] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_Students_Users_UserId] FOREIGN KEY ([UserId]) REFERENCES [Users] ([Id]) ON DELETE SET NULL
    );

    CREATE UNIQUE INDEX [IX_Students_Email] ON [Students] ([Email]);
    CREATE UNIQUE INDEX [IX_Students_StudentCode] ON [Students] ([StudentCode]);
    CREATE UNIQUE INDEX [IX_Students_UserId] ON [Students] ([UserId]) WHERE [UserId] IS NOT NULL;
END
GO

-- ============================================================================
-- 5. TABLE: Teachers (Hồ sơ Giảng viên)
-- ============================================================================
IF OBJECT_ID(N'[Teachers]', N'U') IS NULL
BEGIN
    CREATE TABLE [Teachers] (
        [Id] int NOT NULL IDENTITY(1,1),
        [TeacherCode] nvarchar(50) NOT NULL,
        [FullName] nvarchar(100) NOT NULL,
        [Email] nvarchar(150) NOT NULL,
        [Department] nvarchar(100) NULL,
        [UserId] int NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_Teachers] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_Teachers_Users_UserId] FOREIGN KEY ([UserId]) REFERENCES [Users] ([Id]) ON DELETE SET NULL
    );

    CREATE UNIQUE INDEX [IX_Teachers_Email] ON [Teachers] ([Email]);
    CREATE UNIQUE INDEX [IX_Teachers_TeacherCode] ON [Teachers] ([TeacherCode]);
    CREATE UNIQUE INDEX [IX_Teachers_UserId] ON [Teachers] ([UserId]) WHERE [UserId] IS NOT NULL;
END
GO

-- ============================================================================
-- 6. TABLE: ClassRooms (Lớp học phần)
-- ============================================================================
IF OBJECT_ID(N'[ClassRooms]', N'U') IS NULL
BEGIN
    CREATE TABLE [ClassRooms] (
        [Id] int NOT NULL IDENTITY(1,1),
        [ClassName] nvarchar(100) NOT NULL,
        [SubjectId] int NOT NULL,
        [TeacherId] int NOT NULL,
        [Room] nvarchar(50) NULL,
        [Semester] nvarchar(50) NULL,
        [Schedule] nvarchar(200) NULL,
        [MaxStudents] int NOT NULL DEFAULT 50,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_ClassRooms] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_ClassRooms_Subjects_SubjectId] FOREIGN KEY ([SubjectId]) REFERENCES [Subjects] ([Id]) ON DELETE NO ACTION,
        CONSTRAINT [FK_ClassRooms_Teachers_TeacherId] FOREIGN KEY ([TeacherId]) REFERENCES [Teachers] ([Id]) ON DELETE NO ACTION
    );

    CREATE INDEX [IX_ClassRooms_SubjectId] ON [ClassRooms] ([SubjectId]);
    CREATE INDEX [IX_ClassRooms_TeacherId] ON [ClassRooms] ([TeacherId]);
END
GO

-- ============================================================================
-- 7. TABLE: ClassEnrollments (Danh sách Sinh viên đăng ký lớp học)
-- ============================================================================
IF OBJECT_ID(N'[ClassEnrollments]', N'U') IS NULL
BEGIN
    CREATE TABLE [ClassEnrollments] (
        [Id] int NOT NULL IDENTITY(1,1),
        [ClassRoomId] int NOT NULL,
        [StudentId] int NOT NULL,
        [EnrolledAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [IsActive] bit NOT NULL DEFAULT 1,
        CONSTRAINT [PK_ClassEnrollments] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_ClassEnrollments_ClassRooms_ClassRoomId] FOREIGN KEY ([ClassRoomId]) REFERENCES [ClassRooms] ([Id]) ON DELETE CASCADE,
        CONSTRAINT [FK_ClassEnrollments_Students_StudentId] FOREIGN KEY ([StudentId]) REFERENCES [Students] ([Id]) ON DELETE NO ACTION
    );

    CREATE UNIQUE INDEX [IX_ClassEnrollments_ClassRoomId_StudentId] ON [ClassEnrollments] ([ClassRoomId], [StudentId]);
    CREATE INDEX [IX_ClassEnrollments_StudentId] ON [ClassEnrollments] ([StudentId]);
END
GO

-- ============================================================================
-- 8. TABLE: AttendanceSessions (Phiên điểm danh do Giảng viên tạo)
-- ============================================================================
IF OBJECT_ID(N'[AttendanceSessions]', N'U') IS NULL
BEGIN
    CREATE TABLE [AttendanceSessions] (
        [Id] int NOT NULL IDENTITY(1,1),
        [ClassRoomId] int NOT NULL,
        [TeacherId] int NOT NULL,
        [SessionDate] datetime2 NOT NULL,
        [StartTime] time NOT NULL,
        [EndTime] time NOT NULL,
        [QRCode] nvarchar(500) NULL,
        [Latitude] float NULL,
        [Longitude] float NULL,
        [WifiSSID] nvarchar(100) NULL,
        [Note] nvarchar(500) NULL,
        [IsActive] bit NOT NULL DEFAULT 1,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT [PK_AttendanceSessions] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_AttendanceSessions_ClassRooms_ClassRoomId] FOREIGN KEY ([ClassRoomId]) REFERENCES [ClassRooms] ([Id]) ON DELETE CASCADE,
        CONSTRAINT [FK_AttendanceSessions_Teachers_TeacherId] FOREIGN KEY ([TeacherId]) REFERENCES [Teachers] ([Id]) ON DELETE NO ACTION
    );

    CREATE INDEX [IX_AttendanceSessions_ClassRoomId] ON [AttendanceSessions] ([ClassRoomId]);
    CREATE INDEX [IX_AttendanceSessions_TeacherId] ON [AttendanceSessions] ([TeacherId]);
END
GO

-- ============================================================================
-- 9. TABLE: AttendanceRecords (Bản ghi kết quả điểm danh của Sinh viên)
-- ============================================================================
IF OBJECT_ID(N'[AttendanceRecords]', N'U') IS NULL
BEGIN
    CREATE TABLE [AttendanceRecords] (
        [Id] int NOT NULL IDENTITY(1,1),
        [SessionId] int NOT NULL,
        [StudentId] int NOT NULL,
        [CheckInTime] datetime2 NOT NULL,
        [Method] nvarchar(20) NOT NULL, -- MANUAL, QR, WIFI, FACE, NFC
        [Status] nvarchar(20) NOT NULL, -- PRESENT, LATE, ABSENT, EXCUSED
        [Latitude] float NULL,
        [Longitude] float NULL,
        [Note] nvarchar(500) NULL,
        [CreatedAt] datetime2 NOT NULL DEFAULT SYSUTCDATETIME(),
        [UpdatedAt] datetime2 NULL,
        CONSTRAINT [PK_AttendanceRecords] PRIMARY KEY ([Id]),
        CONSTRAINT [FK_AttendanceRecords_AttendanceSessions_SessionId] FOREIGN KEY ([SessionId]) REFERENCES [AttendanceSessions] ([Id]) ON DELETE CASCADE,
        CONSTRAINT [FK_AttendanceRecords_Students_StudentId] FOREIGN KEY ([StudentId]) REFERENCES [Students] ([Id]) ON DELETE NO ACTION
    );

    CREATE UNIQUE INDEX [IX_AttendanceRecords_SessionId_StudentId] ON [AttendanceRecords] ([SessionId], [StudentId]);
    CREATE INDEX [IX_AttendanceRecords_StudentId] ON [AttendanceRecords] ([StudentId]);
END
GO

-- ============================================================================
-- 10. SEED DATA (DỮ LIỆU MẪU ĐẦY ĐỦ CHO FRONTEND & BACKEND)
-- Mật khẩu mặc định:
--   + Admin:      AdminPassword123@  (Hash: $2a$11$9P0AgyhDMCHlIWiGnpYBi.P2QnNeI.TL7Jo/wAiX3/OVfk7eUU7.K)
--   + Giảng viên: Password123@       (Hash: $2a$11$Mz2VdIbe5hblShVdjIT6tuVi5wv3EXsast3FoZmWriS.QUxS00QYC)
--   + Sinh viên:  Password123@       (Hash: $2a$11$qANcI5UZZV4IghALjtsNhOb3QBkxllNKKs.N4dfAjJEZOJH1X3enu)
-- ============================================================================

-- 10.1. Tạo tài khoản Users mẫu (khớp nút chọn nhanh trên màn hình Login Flutter & email thực)
IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'admin@example.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'admin@example.com', N'$2a$11$9P0AgyhDMCHlIWiGnpYBi.P2QnNeI.TL7Jo/wAiX3/OVfk7eUU7.K', N'Quản trị viên Demo', N'ADMIN', N'0901234567', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'teacher1@example.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'teacher1@example.com', N'$2a$11$Mz2VdIbe5hblShVdjIT6tuVi5wv3EXsast3FoZmWriS.QUxS00QYC', N'Giảng viên Demo', N'TEACHER', N'0902345678', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'student1@example.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'student1@example.com', N'$2a$11$qANcI5UZZV4IghALjtsNhOb3QBkxllNKKs.N4dfAjJEZOJH1X3enu', N'Sinh viên Demo', N'STUDENT', N'0903456789', 1, 1, SYSUTCDATETIME());
END

-- Tài khoản Gmail thực hỗ trợ nhận OTP trực tiếp qua Email
IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'maivy0822@gmail.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'maivy0822@gmail.com', N'$2a$11$9P0AgyhDMCHlIWiGnpYBi.P2QnNeI.TL7Jo/wAiX3/OVfk7eUU7.K', N'Mai Hà Thanh Vy (Admin)', N'ADMIN', N'0911223344', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'mkgamez22@gmail.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'mkgamez22@gmail.com', N'$2a$11$Mz2VdIbe5hblShVdjIT6tuVi5wv3EXsast3FoZmWriS.QUxS00QYC', N'Nguyễn Văn Giảng', N'TEACHER', N'0922334455', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'mkwindy22@gmail.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'mkwindy22@gmail.com', N'$2a$11$qANcI5UZZV4IghALjtsNhOb3QBkxllNKKs.N4dfAjJEZOJH1X3enu', N'Lê Văn Tè', N'STUDENT', N'0933445566', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'student2@example.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'student2@example.com', N'$2a$11$qANcI5UZZV4IghALjtsNhOb3QBkxllNKKs.N4dfAjJEZOJH1X3enu', N'Phạm Quốc Khánh', N'STUDENT', N'0944556677', 1, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Users] WHERE [Email] = N'student3@example.com')
BEGIN
    INSERT INTO [Users] ([Email], [PasswordHash], [FullName], [Role], [PhoneNumber], [IsEmailVerified], [IsActive], [CreatedAt])
    VALUES (N'student3@example.com', N'$2a$11$qANcI5UZZV4IghALjtsNhOb3QBkxllNKKs.N4dfAjJEZOJH1X3enu', N'Võ Thảo Nguyên', N'STUDENT', N'0955667788', 1, 1, SYSUTCDATETIME());
END
GO

-- 10.2. Tạo hồ sơ Giảng viên (Teachers)
DECLARE @TeacherDemoUserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'teacher1@example.com');
IF NOT EXISTS (SELECT 1 FROM [Teachers] WHERE [Email] = N'teacher1@example.com')
BEGIN
    INSERT INTO [Teachers] ([TeacherCode], [FullName], [Email], [Department], [UserId], [IsActive], [CreatedAt])
    VALUES (N'GVDEMO001', N'Giảng viên Demo', N'teacher1@example.com', N'Công nghệ thông tin', @TeacherDemoUserId, 1, SYSUTCDATETIME());
END

DECLARE @TeacherRealUserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'mkgamez22@gmail.com');
IF NOT EXISTS (SELECT 1 FROM [Teachers] WHERE [Email] = N'mkgamez22@gmail.com')
BEGIN
    INSERT INTO [Teachers] ([TeacherCode], [FullName], [Email], [Department], [UserId], [IsActive], [CreatedAt])
    VALUES (N'GV001', N'Nguyễn Văn Giảng', N'mkgamez22@gmail.com', N'Công nghệ phần mềm', @TeacherRealUserId, 1, SYSUTCDATETIME());
END
GO

-- 10.3. Tạo hồ sơ Sinh viên (Students)
DECLARE @StudentDemoUserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'student1@example.com');
IF NOT EXISTS (SELECT 1 FROM [Students] WHERE [Email] = N'student1@example.com')
BEGIN
    INSERT INTO [Students] ([StudentCode], [FullName], [Email], [DateOfBirth], [Gender], [PhoneNumber], [Address], [Major], [UserId], [IsActive], [CreatedAt])
    VALUES (N'SVDEMO001', N'Sinh viên Demo', N'student1@example.com', '2004-05-15', N'Nam', N'0903456789', N'TP. Hồ Chí Minh', N'Công nghệ thông tin', @StudentDemoUserId, 1, SYSUTCDATETIME());
END

DECLARE @StudentRealUserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'mkwindy22@gmail.com');
IF NOT EXISTS (SELECT 1 FROM [Students] WHERE [Email] = N'mkwindy22@gmail.com')
BEGIN
    INSERT INTO [Students] ([StudentCode], [FullName], [Email], [DateOfBirth], [Gender], [PhoneNumber], [Address], [Major], [UserId], [IsActive], [CreatedAt])
    VALUES (N'SV001', N'Lê Văn Tè', N'mkwindy22@gmail.com', '2004-08-22', N'Nam', N'0933445566', N'Quận Bình Thạnh, TP.HCM', N'Công nghệ thông tin', @StudentRealUserId, 1, SYSUTCDATETIME());
END

DECLARE @Student2UserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'student2@example.com');
IF NOT EXISTS (SELECT 1 FROM [Students] WHERE [Email] = N'student2@example.com')
BEGIN
    INSERT INTO [Students] ([StudentCode], [FullName], [Email], [DateOfBirth], [Gender], [PhoneNumber], [Address], [Major], [UserId], [IsActive], [CreatedAt])
    VALUES (N'SV002', N'Phạm Quốc Khánh', N'student2@example.com', '2004-02-10', N'Nam', N'0944556677', N'TP. Thủ Đức, TP.HCM', N'Kỹ thuật phần mềm', @Student2UserId, 1, SYSUTCDATETIME());
END

DECLARE @Student3UserId INT = (SELECT TOP 1 [Id] FROM [Users] WHERE [Email] = N'student3@example.com');
IF NOT EXISTS (SELECT 1 FROM [Students] WHERE [Email] = N'student3@example.com')
BEGIN
    INSERT INTO [Students] ([StudentCode], [FullName], [Email], [DateOfBirth], [Gender], [PhoneNumber], [Address], [Major], [UserId], [IsActive], [CreatedAt])
    VALUES (N'SV003', N'Võ Thảo Nguyên', N'student3@example.com', '2004-11-20', N'Nữ', N'0955667788', N'Quận 1, TP.HCM', N'Hệ thống thông tin', @Student3UserId, 1, SYSUTCDATETIME());
END
GO

-- 10.4. Tạo Môn học (Subjects)
IF NOT EXISTS (SELECT 1 FROM [Subjects] WHERE [SubjectCode] = N'DEMO101')
BEGIN
    INSERT INTO [Subjects] ([SubjectCode], [SubjectName], [Credits], [Description], [IsActive], [CreatedAt])
    VALUES (N'DEMO101', N'Lập trình di động', 3, N'Môn học phát triển ứng dụng di động đa nền tảng với Flutter và ASP.NET Core', 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Subjects] WHERE [SubjectCode] = N'CMP301')
BEGIN
    INSERT INTO [Subjects] ([SubjectCode], [SubjectName], [Credits], [Description], [IsActive], [CreatedAt])
    VALUES (N'CMP301', N'Phát triển ứng dụng Web & API', 3, N'Xây dựng RESTful API với ASP.NET Core và SQL Server', 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Subjects] WHERE [SubjectCode] = N'CMP302')
BEGIN
    INSERT INTO [Subjects] ([SubjectCode], [SubjectName], [Credits], [Description], [IsActive], [CreatedAt])
    VALUES (N'CMP302', N'Cơ sở dữ liệu nâng cao', 3, N'Thiết kế, tối ưu hóa và quản trị hệ cơ sở dữ liệu SQL Server', 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [Subjects] WHERE [SubjectCode] = N'CMP303')
BEGIN
    INSERT INTO [Subjects] ([SubjectCode], [SubjectName], [Credits], [Description], [IsActive], [CreatedAt])
    VALUES (N'CMP303', N'An toàn và Bảo mật thông tin', 3, N'Xác thực đa nhân tố OTP, JWT, OAuth2 và bảo mật ứng dụng', 1, SYSUTCDATETIME());
END
GO

-- 10.5. Tạo Lớp học phần (ClassRooms)
DECLARE @SubDemoId INT = (SELECT TOP 1 [Id] FROM [Subjects] WHERE [SubjectCode] = N'DEMO101');
DECLARE @SubWebId  INT = (SELECT TOP 1 [Id] FROM [Subjects] WHERE [SubjectCode] = N'CMP301');
DECLARE @TeacherDemoId INT = (SELECT TOP 1 [Id] FROM [Teachers] WHERE [TeacherCode] = N'GVDEMO001');
DECLARE @TeacherGv1Id  INT = (SELECT TOP 1 [Id] FROM [Teachers] WHERE [TeacherCode] = N'GV001');

IF NOT EXISTS (SELECT 1 FROM [ClassRooms] WHERE [ClassName] = N'LTDD-DEMO-01')
BEGIN
    INSERT INTO [ClassRooms] ([ClassName], [SubjectId], [TeacherId], [Room], [Semester], [Schedule], [MaxStudents], [IsActive], [CreatedAt])
    VALUES (N'LTDD-DEMO-01', @SubDemoId, @TeacherDemoId, N'A101', N'2026-2027-1', N'Thứ 2 08:00-11:30', 40, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [ClassRooms] WHERE [ClassName] = N'LTDD-NMT-02')
BEGIN
    INSERT INTO [ClassRooms] ([ClassName], [SubjectId], [TeacherId], [Room], [Semester], [Schedule], [MaxStudents], [IsActive], [CreatedAt])
    VALUES (N'LTDD-NMT-02', @SubDemoId, @TeacherGv1Id, N'E1.04.02', N'2026-2027-1', N'Thứ 6 07:30-11:00', 45, 1, SYSUTCDATETIME());
END

IF NOT EXISTS (SELECT 1 FROM [ClassRooms] WHERE [ClassName] = N'WEBAPI-01')
BEGIN
    INSERT INTO [ClassRooms] ([ClassName], [SubjectId], [TeacherId], [Room], [Semester], [Schedule], [MaxStudents], [IsActive], [CreatedAt])
    VALUES (N'WEBAPI-01', @SubWebId, @TeacherDemoId, N'B302', N'2026-2027-1', N'Thứ 4 13:00-16:30', 40, 1, SYSUTCDATETIME());
END
GO

-- 10.6. Ghi danh Sinh viên vào các Lớp học (ClassEnrollments)
DECLARE @Class1Id INT = (SELECT TOP 1 [Id] FROM [ClassRooms] WHERE [ClassName] = N'LTDD-DEMO-01');
DECLARE @Class2Id INT = (SELECT TOP 1 [Id] FROM [ClassRooms] WHERE [ClassName] = N'LTDD-NMT-02');
DECLARE @Class3Id INT = (SELECT TOP 1 [Id] FROM [ClassRooms] WHERE [ClassName] = N'WEBAPI-01');

INSERT INTO [ClassEnrollments] ([ClassRoomId], [StudentId], [EnrolledAt], [IsActive])
SELECT @Class1Id, s.[Id], SYSUTCDATETIME(), 1
FROM [Students] s
WHERE NOT EXISTS (
    SELECT 1 FROM [ClassEnrollments] ce WHERE ce.[ClassRoomId] = @Class1Id AND ce.[StudentId] = s.[Id]
);

INSERT INTO [ClassEnrollments] ([ClassRoomId], [StudentId], [EnrolledAt], [IsActive])
SELECT @Class2Id, s.[Id], SYSUTCDATETIME(), 1
FROM [Students] s
WHERE NOT EXISTS (
    SELECT 1 FROM [ClassEnrollments] ce WHERE ce.[ClassRoomId] = @Class2Id AND ce.[StudentId] = s.[Id]
);

INSERT INTO [ClassEnrollments] ([ClassRoomId], [StudentId], [EnrolledAt], [IsActive])
SELECT @Class3Id, s.[Id], SYSUTCDATETIME(), 1
FROM [Students] s
WHERE s.[StudentCode] IN (N'SVDEMO001', N'SV001')
  AND NOT EXISTS (
    SELECT 1 FROM [ClassEnrollments] ce WHERE ce.[ClassRoomId] = @Class3Id AND ce.[StudentId] = s.[Id]
);
GO

-- 10.7. Tạo Phiên điểm danh mẫu (AttendanceSessions)
DECLARE @Class1Id INT = (SELECT TOP 1 [Id] FROM [ClassRooms] WHERE [ClassName] = N'LTDD-DEMO-01');
DECLARE @Class2Id INT = (SELECT TOP 1 [Id] FROM [ClassRooms] WHERE [ClassName] = N'LTDD-NMT-02');
DECLARE @TeacherDemoId INT = (SELECT TOP 1 [Id] FROM [Teachers] WHERE [TeacherCode] = N'GVDEMO001');
DECLARE @TeacherGv1Id  INT = (SELECT TOP 1 [Id] FROM [Teachers] WHERE [TeacherCode] = N'GV001');

IF NOT EXISTS (SELECT 1 FROM [AttendanceSessions] WHERE [QRCode] = N'ATT-DEMO-SESSION-01')
BEGIN
    INSERT INTO [AttendanceSessions] (
        [ClassRoomId], [TeacherId], [SessionDate], [StartTime], [EndTime],
        [QRCode], [Latitude], [Longitude], [WifiSSID], [Note], [IsActive], [CreatedAt]
    )
    VALUES (
        @Class1Id, @TeacherDemoId, CAST(SYSUTCDATETIME() AS DATE), '07:30:00', '11:30:00',
        N'ATT-DEMO-SESSION-01', 10.8018, 106.7145, N'HUTECH_CAMPUS_WIFI',
        N'Buổi học thực hành điểm danh QR & WiFi', 1, SYSUTCDATETIME()
    );
END

IF NOT EXISTS (SELECT 1 FROM [AttendanceSessions] WHERE [QRCode] = N'ATT-DEMO-SESSION-02')
BEGIN
    INSERT INTO [AttendanceSessions] (
        [ClassRoomId], [TeacherId], [SessionDate], [StartTime], [EndTime],
        [QRCode], [Latitude], [Longitude], [WifiSSID], [Note], [IsActive], [CreatedAt]
    )
    VALUES (
        @Class2Id, @TeacherGv1Id, CAST(SYSUTCDATETIME() AS DATE), '07:30:00', '11:30:00',
        N'ATT-DEMO-SESSION-02', 10.8018, 106.7145, N'HUTECH_CAMPUS_WIFI',
        N'Buổi kiểm tra đồ án Lập trình di động', 1, SYSUTCDATETIME()
    );
END
GO

-- 10.8. Tạo Bản ghi điểm danh mẫu (AttendanceRecords)
DECLARE @Session1Id INT = (SELECT TOP 1 [Id] FROM [AttendanceSessions] WHERE [QRCode] = N'ATT-DEMO-SESSION-01');
DECLARE @SvDemoId   INT = (SELECT TOP 1 [Id] FROM [Students] WHERE [StudentCode] = N'SVDEMO001');
DECLARE @Sv001Id    INT = (SELECT TOP 1 [Id] FROM [Students] WHERE [StudentCode] = N'SV001');
DECLARE @Sv002Id    INT = (SELECT TOP 1 [Id] FROM [Students] WHERE [StudentCode] = N'SV002');

IF @Session1Id IS NOT NULL AND @SvDemoId IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM [AttendanceRecords] WHERE [SessionId] = @Session1Id AND [StudentId] = @SvDemoId)
BEGIN
    INSERT INTO [AttendanceRecords] ([SessionId], [StudentId], [CheckInTime], [Method], [Status], [Latitude], [Longitude], [Note], [CreatedAt])
    VALUES (@Session1Id, @SvDemoId, SYSUTCDATETIME(), N'QR', N'PRESENT', 10.8018, 106.7145, N'Điểm danh qua mã QR', SYSUTCDATETIME());
END

IF @Session1Id IS NOT NULL AND @Sv001Id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM [AttendanceRecords] WHERE [SessionId] = @Session1Id AND [StudentId] = @Sv001Id)
BEGIN
    INSERT INTO [AttendanceRecords] ([SessionId], [StudentId], [CheckInTime], [Method], [Status], [Latitude], [Longitude], [Note], [CreatedAt])
    VALUES (@Session1Id, @Sv001Id, SYSUTCDATETIME(), N'WIFI', N'PRESENT', 10.8018, 106.7145, N'Điểm danh qua mạng WiFi: HUTECH_CAMPUS_WIFI', SYSUTCDATETIME());
END

IF @Session1Id IS NOT NULL AND @Sv002Id IS NOT NULL
   AND NOT EXISTS (SELECT 1 FROM [AttendanceRecords] WHERE [SessionId] = @Session1Id AND [StudentId] = @Sv002Id)
BEGIN
    INSERT INTO [AttendanceRecords] ([SessionId], [StudentId], [CheckInTime], [Method], [Status], [Latitude], [Longitude], [Note], [CreatedAt])
    VALUES (@Session1Id, @Sv002Id, SYSUTCDATETIME(), N'MANUAL', N'LATE', NULL, NULL, N'Giảng viên điểm danh bổ sung', SYSUTCDATETIME());
END
GO

PRINT N'====================================================================';
PRINT N'KHỞI TẠO DATABASE MaiHaThanhVy_AttendanceDB THÀNH CÔNG!';
PRINT N'- Admin:      admin@example.com     / AdminPassword123@';
PRINT N'- Giảng viên: teacher1@example.com  / Password123@';
PRINT N'- Sinh viên:  student1@example.com  / Password123@';
PRINT N'====================================================================';
GO
