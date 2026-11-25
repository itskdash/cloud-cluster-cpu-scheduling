package models;

public class Process {
    public final int pid, arrival, burst;
    public int priority;
    public int remaining;
    public int startTime = -1;
    public int completionTime;
    public int tier = 1;
    public int waitingSince = -1;

    public Process(int pid, int arrival, int burst, int priority) {
        if (pid <= 0 || arrival < 0 || burst <= 0 || priority < 0)
            throw new IllegalArgumentException("Invalid process P" + pid
                    + " (PID > 0, Arrival >= 0, Burst > 0, Priority >= 0)");
        this.pid = pid;
        this.arrival = arrival;
        this.burst = burst;
        this.priority = priority;
        this.remaining = burst;
    }

    public int turnaround() {
        return completionTime - arrival;
    }

    public int waiting() {
        return turnaround() - burst;
    }

    public int response() {
        return startTime - arrival;
    }
}