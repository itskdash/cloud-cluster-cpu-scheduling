package metrics;

import java.util.ArrayList;
import java.util.List;

public class GanttChart {
    private final List<String> entries;

    public GanttChart() {
        entries = new ArrayList<>();
    }

    public void addEntry(String label, int start, int end) {
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Label cannot be null or empty.");
        }
        if (start < 0 || end < start) {
            throw new IllegalArgumentException("Invalid start or end time.");
        }
        if (start == end) {
            return; // Ignore zero-length entries
        }

        if (!entries.isEmpty()) {
            int lastIndex = entries.size() - 1;
            String last = entries.get(lastIndex);
            String[] parts = last.split("\\|",-1);
            if (parts.length == 3 && parts[0].equals(label) && Integer.parseInt(parts[2]) == start) {
                entries.set(lastIndex, label + "|" + parts[1] + "|" + end);
                return;
            }
        }
        entries.add(label + "|" + start + "|" + end);
    }

    public void print(){
        if (entries.isEmpty()) {
            System.out.println("Gantt Chart is empty.");
            return;
        }
        
        System.out.println("Gantt Chart:");
        for (String entry : entries) {
            String[] parts = entry.split("\\|",-1);
            System.out.printf("| %s (%s-%s) ", parts[0], parts[1], parts[2]);
    }
    System.out.println("|");
    }

    public List<String> getEntries() {
        return new ArrayList<>(entries);
    }

    @Override 
    public String toString() {
        return String.join(", ", entries);
    }
}