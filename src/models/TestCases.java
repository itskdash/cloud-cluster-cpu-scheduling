package models;

import java.util.List;

public class TestCases {

    public static class TestCase {
        public final int quantum;
        public final int contextSwitch;
        public final int agingInterval;
        public final int agingStep;
        public final List<Process> processes;

        public TestCase(int quantum, int contextSwitch, int agingInterval, int agingStep, List<Process> processes) {
            this.quantum = quantum;
            this.contextSwitch = contextSwitch;
            this.agingInterval = agingInterval;
            this.agingStep = agingStep;
            this.processes = processes;
        }
    }

    public static TestCase starvationTestCase() {
        return new TestCase(3, 0, 4, 2, List.of(
                new Process(1, 0, 15, 5),
                new Process(2, 1, 2, 1),
                new Process(3, 2, 2, 1),
                new Process(4, 4, 2, 1),
                new Process(5, 6, 2, 1),
                new Process(6, 8, 2, 1)));
    }

    public static TestCase demotionAndContextSwitchTestCase() {
        return new TestCase(3, 1, 5, 1, List.of(
                new Process(1, 0, 9, 2),
                new Process(2, 1, 2, 3),
                new Process(3, 2, 8, 1),
                new Process(4, 4, 3, 4),
                new Process(5, 6, 2, 2)));
    }
}