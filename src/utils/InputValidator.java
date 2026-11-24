import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class InputValidator {
    private InputValidator() { }

    public static boolean isValidProcessId(String id) {
        return id != null && !id.trim().isEmpty();
    }

    public static boolean isValidArrivalTime(int arrivalTime) {
        return arrivalTime >= 0;
    }

    public static boolean isValidBurstTime(int burstTime) {
        return burstTime > 0;
    }

    public static boolean isValidPriority(int priority) {
        return priority > 0;
    }

    public static boolean isValidQuantum(int quantum) {
        return quantum > 0;
    }

    public static boolean isValidContextSwitch(int contextSwitch) {
        return contextSwitch >= 0;
    }


    public static List<String> validateProcesses(List<Process> processes) {
        java.util.ArrayList<String> errors = new java.util.ArrayList<String>();
        if (processes == null || processes.isEmpty()) {
            errors.add("At least one process is required.");
            return errors;
        }

        Set<String> ids = new HashSet<String>();
        for (int i = 0; i < processes.size(); i++) {
            Process process = processes.get(i);
            if (process == null) {
                errors.add("Process at index " + i + " is null.");
                continue;
            }
            if (!isValidProcessId(process.getProcessId())) {
                errors.add("Process at index " + i + " has an invalid ID.");
            } else if (!ids.add(process.getProcessId())) {
                errors.add("Duplicate process ID: " + process.getProcessId());
            }
            if (!isValidArrivalTime(process.getArrivalTime())) {
                errors.add(process.getProcessId() + ": arrival time cannot be negative.");
            }
            if (!isValidBurstTime(process.getBurstTime())) {
                errors.add(process.getProcessId() + ": burst time must be positive.");
            }
            if (!isValidPriority(process.getPriority())) {
                errors.add(process.getProcessId() + ": priority must be positive.");
            }
        }
        return errors;
    }

    public static List<String> validateTestCase(TestCase testCase) {
        java.util.ArrayList<String> errors = new java.util.ArrayList<String>();
        if (testCase == null) {
            errors.add("Test case cannot be null.");
            return errors;
        }
        errors.addAll(validateProcesses(testCase.getProcesses()));
        if (!isValidQuantum(testCase.getQuantum())) {
            errors.add("Quantum must be greater than zero.");
        }
        if (!isValidContextSwitch(testCase.getContextSwitch())) {
            errors.add("Context switch time cannot be negative.");
        }
        return errors;
    }

    public static void validateTestCaseOrThrow(TestCase testCase) {
        List<String> errors = validateTestCase(testCase);
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException(String.join("\\n", errors));
        }
    }
}
