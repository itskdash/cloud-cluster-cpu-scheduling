import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SchedulingResult {
    public enum EntryType {
        PROCESS,
        CONTEXT_SWITCH,
        IDLE
    }

    public static class TimelineEntry {
        private final int startTime;
        private final int endTime;
        private final String processId;
        private final EntryType type;

        public TimelineEntry(int startTime, int endTime, String processId, EntryType type) {
            if (startTime < 0 || endTime <= startTime) {
                throw new IllegalArgumentException("Timeline entry must have a positive duration.");
            }
            this.startTime = startTime;
            this.endTime = endTime;
            this.processId = processId;
            this.type = Objects.requireNonNull(type, "Timeline entry type cannot be null.");

            if (type == EntryType.PROCESS && (processId == null || processId.trim().isEmpty())) {
                throw new IllegalArgumentException("A PROCESS timeline entry needs a process ID.");
            }
        }

        public int getStartTime() { return startTime; }
        public int getEndTime() { return endTime; }
        public int getDuration() { return endTime - startTime; }
        public String getProcessId() { return processId; }
        public EntryType getType() { return type; }

        @Override
        public String toString() {
            String label = type == EntryType.PROCESS ? processId : type.name();
            return "[" + startTime + "-" + endTime + ": " + label + "]";
        }
    }

    private final String algorithmName;
    private final List<Process> processes;
    private final List<TimelineEntry> timeline;
    private final Map<String, Integer> completionTimes;
    private int contextSwitchCount;
    private int totalContextSwitchTime;
    private int finishTime;

    public SchedulingResult(String algorithmName) {
        if (algorithmName == null || algorithmName.trim().isEmpty()) {
            throw new IllegalArgumentException("Algorithm name cannot be empty.");
        }
        this.algorithmName = algorithmName.trim();
        this.processes = new ArrayList<Process>();
        this.timeline = new ArrayList<TimelineEntry>();
        this.completionTimes = new LinkedHashMap<String, Integer>();
        this.contextSwitchCount = 0;
        this.totalContextSwitchTime = 0;
        this.finishTime = 0;
    }

    public SchedulingResult(String algorithmName, List<Process> completedProcesses) {
        this(algorithmName);
        setProcesses(completedProcesses);
    }

    public String getAlgorithmName() { return algorithmName; }

    /** Stores references to the final process objects so metrics can read their timestamps. */
    public void setProcesses(List<Process> completedProcesses) {
        processes.clear();
        completionTimes.clear();
        if (completedProcesses == null) {
            return;
        }
        for (Process process : completedProcesses) {
            Objects.requireNonNull(process, "Result process list cannot contain null.");
            processes.add(process);
            if (process.getCompletionTime() >= 0) {
                completionTimes.put(process.getProcessId(), process.getCompletionTime());
                finishTime = Math.max(finishTime, process.getCompletionTime());
            }
        }
    }

    public List<Process> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    public void addTimelineEntry(TimelineEntry entry) {
        Objects.requireNonNull(entry, "Timeline entry cannot be null.");
        if (!timeline.isEmpty()) {
            TimelineEntry previous = timeline.get(timeline.size() - 1);
            if (entry.getStartTime() < previous.getEndTime()) {
                throw new IllegalArgumentException("Timeline entries cannot overlap or be out of order.");
            }
        }
        timeline.add(entry);
        finishTime = Math.max(finishTime, entry.getEndTime());
        if (entry.getType() == EntryType.CONTEXT_SWITCH) {
            contextSwitchCount++;
            totalContextSwitchTime += entry.getDuration();
        }
    }

    public List<TimelineEntry> getTimeline() {
        return Collections.unmodifiableList(timeline);
    }

    public Map<String, Integer> getCompletionTimes() {
        return Collections.unmodifiableMap(completionTimes);
    }

    public void setContextSwitchCount(int count) {
        if (count < 0) throw new IllegalArgumentException("Context switch count cannot be negative.");
        contextSwitchCount = count;
    }

    public int getContextSwitchCount() { return contextSwitchCount; }

    public void setTotalContextSwitchTime(int time) {
        if (time < 0) throw new IllegalArgumentException("Context switch time cannot be negative.");
        totalContextSwitchTime = time;
    }

    public int getTotalContextSwitchTime() { return totalContextSwitchTime; }
    public int getFinishTime() { return finishTime; }

    public void setFinishTime(int finishTime) {
        if (finishTime < 0) throw new IllegalArgumentException("Finish time cannot be negative.");
        this.finishTime = finishTime;
    }

    @Override
    public String toString() {
        return "SchedulingResult{" +
                "algorithm='" + algorithmName + '\'' +
                ", processCount=" + processes.size() +
                ", timelineEntries=" + timeline.size() +
                ", contextSwitchCount=" + contextSwitchCount +
                ", finishTime=" + finishTime +
                '}';
    }
}
