import java.util.Objects;
public class Process {
    private final String processId;
    private final int arrivalTime;
    private final int burstTime;

    private int remainingTime;
    private final int priority;
    private int effectivePriority;

    private int completionTime;
    private int firstStartTime;
    private ProcessState state;

    public Process(String processId, int arrivalTime, int burstTime, int priority) {
        if (processId == null || processId.trim().isEmpty()) {
            throw new IllegalArgumentException("Process ID cannot be null or empty.");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time cannot be negative.");
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be greater than zero.");
        }
        if (priority <= 0) {
            throw new IllegalArgumentException("Priority must be greater than zero.");
        }

        this.processId = processId.trim();
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
        this.priority = priority;
        this.effectivePriority = priority;
        this.completionTime = -1;
        this.firstStartTime = -1;
        this.state = ProcessState.NEW;
    }

    /** Copy constructor: creates a fresh process with its original input values. */
    public Process(Process other) {
        Objects.requireNonNull(other, "Process to copy cannot be null.");
        this.processId = other.processId;
        this.arrivalTime = other.arrivalTime;
        this.burstTime = other.burstTime;
        this.remainingTime = other.burstTime;
        this.priority = other.priority;
        this.effectivePriority = other.priority;
        this.completionTime = -1;
        this.firstStartTime = -1;
        this.state = ProcessState.NEW;
    }

    /**
     * Records the first time this process receives the CPU.
     * Calling it again does not overwrite the original response-time timestamp.
     */
    public void markStarted(int currentTime) {
        if (currentTime < arrivalTime) {
            throw new IllegalArgumentException("A process cannot start before its arrival time.");
        }
        if (firstStartTime == -1) {
            firstStartTime = currentTime;
        }
        state = ProcessState.RUNNING;
    }

    /** Reduces the remaining burst by the supplied positive execution duration. */
    public void execute(int duration) {
        if (duration <= 0) {
            throw new IllegalArgumentException("Execution duration must be positive.");
        }
        if (state == ProcessState.TERMINATED) {
            throw new IllegalStateException("A terminated process cannot execute.");
        }
        if (duration > remainingTime) {
            throw new IllegalArgumentException("Execution duration exceeds remaining time.");
        }
        remainingTime -= duration;
    }

    /** Marks the process as completed at the supplied absolute simulation time. */
    public void markCompleted(int currentTime) {
        if (remainingTime != 0) {
            throw new IllegalStateException("A process cannot terminate while work remains.");
        }
        if (currentTime < arrivalTime) {
            throw new IllegalArgumentException("Completion time cannot be before arrival time.");
        }
        completionTime = currentTime;
        state = ProcessState.TERMINATED;
    }

    /** Returns a fresh, unexecuted copy for running another scheduling algorithm. */
    public Process copy() {
        return new Process(this);
    }

    public String getProcessId() { return processId; }
    public String getId() { return processId; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getRemainingTime() { return remainingTime; }
    public int getPriority() { return priority; }
    public int getEffectivePriority() { return effectivePriority; }
    public int getCompletionTime() { return completionTime; }
    public int getFirstStartTime() { return firstStartTime; }
    public ProcessState getState() { return state; }

    public void setRemainingTime(int remainingTime) {
        if (remainingTime < 0 || remainingTime > burstTime) {
            throw new IllegalArgumentException("Remaining time must be between 0 and burst time.");
        }
        this.remainingTime = remainingTime;
        if (remainingTime == 0) {
            state = ProcessState.TERMINATED;
        }
    }

    public void setEffectivePriority(int effectivePriority) {
        if (effectivePriority <= 0) {
            throw new IllegalArgumentException("Effective priority must be greater than zero.");
        }
        this.effectivePriority = effectivePriority;
    }

    public void setCompletionTime(int completionTime) {
        if (completionTime < arrivalTime) {
            throw new IllegalArgumentException("Completion time cannot be before arrival time.");
        }
        this.completionTime = completionTime;
        if (remainingTime == 0) {
            state = ProcessState.TERMINATED;
        }
    }

    public void setFirstStartTime(int firstStartTime) {
        if (firstStartTime < arrivalTime) {
            throw new IllegalArgumentException("First start time cannot be before arrival time.");
        }
        this.firstStartTime = firstStartTime;
    }

    public void setState(ProcessState state) {
        this.state = Objects.requireNonNull(state, "Process state cannot be null.");
    }

    public boolean isCompleted() {
        return remainingTime == 0 || state == ProcessState.TERMINATED;
    }

    public int getTurnaroundTime() {
        if (completionTime < 0) {
            throw new IllegalStateException("Turnaround time is unavailable before completion.");
        }
        return completionTime - arrivalTime;
    }

    public int getWaitingTime() {
        if (completionTime < 0) {
            throw new IllegalStateException("Waiting time is unavailable before completion.");
        }
        return getTurnaroundTime() - burstTime;
    }

    public int getResponseTime() {
        if (firstStartTime < 0) {
            throw new IllegalStateException("Response time is unavailable before first CPU start.");
        }
        return firstStartTime - arrivalTime;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id='" + processId + '\'' +
                ", arrival=" + arrivalTime +
                ", burst=" + burstTime +
                ", remaining=" + remainingTime +
                ", priority=" + priority +
                ", effectivePriority=" + effectivePriority +
                ", state=" + state +
                '}';
    }
}
