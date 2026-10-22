using Microsoft.EntityFrameworkCore;
using MaiHaThanhVy_Backend.Models;

namespace MaiHaThanhVy_Backend.Data;

public class AppDbContext : DbContext
{
    public AppDbContext(DbContextOptions<AppDbContext> options) : base(options)
    {
    }

    public DbSet<User> Users { get; set; } = null!;
    public DbSet<OtpCode> OtpCodes { get; set; } = null!;
    public DbSet<Subject> Subjects { get; set; } = null!;
    public DbSet<Teacher> Teachers { get; set; } = null!;
    public DbSet<ClassRoom> ClassRooms { get; set; } = null!;
    public DbSet<Student> Students { get; set; } = null!;
    public DbSet<ClassEnrollment> ClassEnrollments { get; set; } = null!;
    public DbSet<AttendanceSession> AttendanceSessions { get; set; } = null!;
    public DbSet<AttendanceRecord> AttendanceRecords { get; set; } = null!;

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        base.OnModelCreating(modelBuilder);

        // User indexes
        modelBuilder.Entity<User>(entity =>
        {
            entity.HasIndex(u => u.Email).IsUnique();
        });

        // Subject indexes
        modelBuilder.Entity<Subject>(entity =>
        {
            entity.HasIndex(s => s.SubjectCode).IsUnique();
        });

        // Teacher indexes and relationships
        modelBuilder.Entity<Teacher>(entity =>
        {
            entity.HasIndex(t => t.TeacherCode).IsUnique();
            entity.HasIndex(t => t.Email).IsUnique();

            entity.HasOne(t => t.User)
                  .WithOne(u => u.Teacher)
                  .HasForeignKey<Teacher>(t => t.UserId)
                  .OnDelete(DeleteBehavior.SetNull);
        });

        // Student indexes and relationships
        modelBuilder.Entity<Student>(entity =>
        {
            entity.HasIndex(s => s.StudentCode).IsUnique();
            entity.HasIndex(s => s.Email).IsUnique();

            entity.HasOne(s => s.User)
                  .WithOne(u => u.Student)
                  .HasForeignKey<Student>(s => s.UserId)
                  .OnDelete(DeleteBehavior.SetNull);
        });

        // ClassRoom relationships
        modelBuilder.Entity<ClassRoom>(entity =>
        {
            entity.HasOne(c => c.Subject)
                  .WithMany(s => s.ClassRooms)
                  .HasForeignKey(c => c.SubjectId)
                  .OnDelete(DeleteBehavior.Restrict);

            entity.HasOne(c => c.Teacher)
                  .WithMany(t => t.ClassRooms)
                  .HasForeignKey(c => c.TeacherId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        // ClassEnrollment indexes and relationships
        modelBuilder.Entity<ClassEnrollment>(entity =>
        {
            entity.HasIndex(e => new { e.ClassRoomId, e.StudentId }).IsUnique();

            entity.HasOne(e => e.ClassRoom)
                  .WithMany(c => c.ClassEnrollments)
                  .HasForeignKey(e => e.ClassRoomId)
                  .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(e => e.Student)
                  .WithMany(s => s.ClassEnrollments)
                  .HasForeignKey(e => e.StudentId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        // AttendanceSession relationships
        modelBuilder.Entity<AttendanceSession>(entity =>
        {
            entity.HasOne(s => s.ClassRoom)
                  .WithMany(c => c.AttendanceSessions)
                  .HasForeignKey(s => s.ClassRoomId)
                  .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(s => s.Teacher)
                  .WithMany(t => t.AttendanceSessions)
                  .HasForeignKey(s => s.TeacherId)
                  .OnDelete(DeleteBehavior.Restrict);
        });

        // AttendanceRecord indexes and relationships
        modelBuilder.Entity<AttendanceRecord>(entity =>
        {
            entity.HasIndex(r => new { r.SessionId, r.StudentId }).IsUnique();

            entity.HasOne(r => r.Session)
                  .WithMany(s => s.AttendanceRecords)
                  .HasForeignKey(r => r.SessionId)
                  .OnDelete(DeleteBehavior.Cascade);

            entity.HasOne(r => r.Student)
                  .WithMany(s => s.AttendanceRecords)
                  .HasForeignKey(r => r.StudentId)
                  .OnDelete(DeleteBehavior.Restrict);
        });
    }
}
