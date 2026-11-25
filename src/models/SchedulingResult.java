package models;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SchedulingResult {
    public final String algorithm;
    public final List<Process> processes;
    public final List<int[]> gantt = new ArrayList<>();
    public final List<String[]> steps = new ArrayList<>();

    public SchedulingResult(String algorithm, List<Process> processes) {
        this.algorithm = algorithm;
        this.processes = new ArrayList<>(processes);
        this.processes.sort(Comparator.comparingInt((Process p) -> p.pid));
    }

    public static String label(int label) {
        return label > 0 ? "P" + label : label == 0 ? "IDLE" : "CS";
    }

    public void addSlice(int label, int start, int end) {
        if (end <= start)
            return;
        int[] last = gantt.isEmpty() ? null : gantt.get(gantt.size() - 1);
        if (last != null && last[0] == label && last[2] == start)
            last[2] = end;
        else
            gantt.add(new int[] { label, start, end });
    }

    public double avgWaiting() {
        return processes.stream().mapToInt(Process::waiting).average().orElse(0);
    }

    public double avgTurnaround() {
        return processes.stream().mapToInt(Process::turnaround).average().orElse(0);
    }

    public double avgResponse() {
        return processes.stream().mapToInt(Process::response).average().orElse(0);
    }
}