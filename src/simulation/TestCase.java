import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Input configuration for one scheduling experiment.
 * The process list is copied on construction. Use copyProcesses() to obtain
 * fresh process objects for each algorithm run.
 */
public class TestCase {
    private final String testCaseName;
    private final List<Process> processes;
    private final int quantum;
    private final int contextSwitch;

    public TestCase(String testCaseName, List<Process> processes,
                    int quantum, int contextSwitch) {
        if (testCaseName == null || testCaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("Test case name cannot be null or empty.");
        }
        if (processes == null || processes.isEmpty()) {
            throw new IllegalArgumentException("A test case must contain at least one process.");
        }
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than zero.");
        }
        if (contextSwitch < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }

        this.testCaseName = testCaseName.trim();
        this.processes = new ArrayList<Process>();
        Set<String> ids = new HashSet<String>();

        for (Process process : processes) {
            Objects.requireNonNull(process, "Process list cannot contain null.");
            if (!ids.add(process.getProcessId())) {
                throw new IllegalArgumentException("Duplicate process ID: " + process.getProcessId());
            }
            this.processes.add(process.copy());
        }

        this.quantum = quantum;
        this.contextSwitch = contextSwitch;
    }

    public String getTestCaseName() { return testCaseName; }
    public String getName() { return testCaseName; }
    public List<Process> getProcesses() {
        return Collections.unmodifiableList(processes);
    }

    /** Returns fresh, unexecuted process objects. Call once per algorithm run. */
    public List<Process> copyProcesses() {
        List<Process> copies = new ArrayList<Process>();
        for (Process process : processes) {
            copies.add(process.copy());
        }
        return copies;
    }

    public int getQuantum() { return quantum; }
    public int getContextSwitch() { return contextSwitch; }
    public int getContextSwitchTime() { return contextSwitch; }

    @Override
    public String toString() {
        return "TestCase{" +
                "name='" + testCaseName + '\'' +
                ", processCount=" + processes.size() +
                ", quantum=" + quantum +
                ", contextSwitch=" + contextSwitch +
                '}';
    }
}
