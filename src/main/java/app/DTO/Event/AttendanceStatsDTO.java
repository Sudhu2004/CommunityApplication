package app.DTO.Event;

public class AttendanceStatsDTO {
    private int totalAttendance;
    private int presentCount;
    private int absentCount;
    private int pendingCount;
    private double presentPercentage;

    // Constructors
    public AttendanceStatsDTO() {}

    public AttendanceStatsDTO(int totalAttendance, int presentCount, int absentCount, int pendingCount) {
        this.totalAttendance = totalAttendance;
        this.presentCount = presentCount;
        this.absentCount = absentCount;
        this.pendingCount = pendingCount;
        this.presentPercentage = totalAttendance > 0 ? (presentCount * 100.0 / totalAttendance) : 0.0;
    }

    // Getters and Setters
    public int getTotalAttendance() { return totalAttendance; }
    public void setTotalAttendance(int totalAttendance) { this.totalAttendance = totalAttendance; }

    public int getPresentCount() { return presentCount; }
    public void setPresentCount(int presentCount) { this.presentCount = presentCount; }

    public int getAbsentCount() { return absentCount; }
    public void setAbsentCount(int absentCount) { this.absentCount = absentCount; }

    public int getPendingCount() { return pendingCount; }
    public void setPendingCount(int pendingCount) { this.pendingCount = pendingCount; }

    public double getPresentPercentage() { return presentPercentage; }
    public void setPresentPercentage(double presentPercentage) { this.presentPercentage = presentPercentage; }
}
