# Schedule — CPU Scheduling Simulator

A modular Java & JavaFX desktop application for simulating, visualizing, and comparing CPU scheduling algorithms—specifically pitting **Preemptive Shortest Job First (SJF)** against a two-tier **Adaptive / Aging CPU Scheduler (ACS)** with full step-by-step state tracking[cite: 1, 2, 5, 6].

---

## Key Features

* **Dual-Algorithm Side-by-Side Comparison:** Runs both **Preemptive SJF** and **ACS** simultaneously on the exact same process workload and displays their average **Waiting**, **Turnaround**, and **Response** times.
* **Two-Tier ACS Engine:**
  * **Tier 1 (Round Robin):** Newly arrived processes enter Tier 1 and execute up to a configurable time quantum (`Q`). If a process does not finish within its slice, it is demoted to Tier 2.
  * **Tier 2 (Priority with Dynamic Aging):** Demoted processes are scheduled non-preemptively by priority (lower integer = higher priority), with ties broken by arrival time and PID. Starvation is prevented via dynamic priority aging based on configurable `Aging Interval` and `Aging Step` values (set `Aging Interval = 0` to disable aging).
* **Context Switch Overhead Modeling:** Supports configurable context switch delays (`CS`) inserted whenever the CPU switches between distinct processes.
* **Interactive Gantt Charts:** Color-coded, scrollable timeline bars displaying process execution slices, context switches (`CS`), and idle CPU cycles (`IDLE`).
* **Step-by-Step Demotion Matrix:** Logs the exact state of the system at every time tick—showing the currently running process, Tier 1 queue, Tier 2 queue (with remaining burst and effective priority), and completed processes.
* **Input Validation:** Enforces valid parameters (`PID > 0`, `Arrival >= 0`, `Burst > 0`, `Priority >= 0`, `Q > 0`, `CS >= 0`, and unique PIDs) with clear error alerts[cite: 1, 2, 3, 4, 5].

---

## Project Architecture

```text
src/
├── Main.java                     # JavaFX application entry point (1000x620 window)
├── gui/
│   └── SimulatorView.java        # Main UI layout, process input panel, Gantt renderer, and tables
├── models/
│   ├── Process.java              # Process entity (PID, arrival, burst, priority, tier, metrics)
│   └── SchedulingResult.java     # Stores Gantt slices, step snapshots, and average metrics
│   └── TestCases.java            # Make test cases for (Starvation & Demotion)
└── schedulers/
    ├── ACSScheduler.java         # Two-tier RR + Aging Priority scheduler implementation
    └── SJFScheduler.java         # Preemptive Shortest Remaining Time First (SJF) implementation
```

[cite: 1, 2, 3, 4, 5, 6]

---

## Prerequisites & Running the Project

### Requirements

* **Java Development Kit (JDK):** JDK 17 or higher
* **JavaFX SDK:** Compatible JavaFX libraries configured in your project's module/classpath[cite: 5, 6]

### Compile & Run (CLI)

Replace `/path/to/javafx/lib` with the actual path to your JavaFX `lib` folder:

```bash
# Compile all source files
javac --module-path /path/to/javafx/lib --add-modules javafx.controls \
      -d out src/models/*.java src/schedulers/*.java src/gui/*.java src/Main.java

# Launch the simulator
java --module-path /path/to/javafx/lib --add-modules javafx.controls \
     -cp out Main
```

[cite: 1, 2, 3, 4, 5, 6]

---

## Usage Guide

1. **Configure Parameters (Top Bar):** Set the Round Robin **Quantum (`Q`)**, **Context Switch (`CS`)** cost, **Aging Interval**, and **Aging Step**.
2. **Add Processes (Left Panel):** Enter `PID`, `Arrival`, `Burst`, and `Priority` (lower number = higher priority) and click **Add**. Use **Remove** to delete a selected row.
3. **Run Simulation:** Click **Run** to generate results.
4. **Analyze Output:**
   * **Timelines & Metrics Tab:** Compare Gantt charts, average metrics, and per-process `Wait`, `TAT` (Turnaround Time), and `Resp` (Response Time) across both schedulers.
   * **Demotion Matrix Tab:** Inspect the tick-by-tick execution trace and queue transitions for the ACS algorithm[cite: 1, 5].

---

## Team Members

* **Jana Yehia**
* **Malak Alaa Eldeen**
* **Bilal Mohamed**
* **Hassan Sameh**
* **Demiana Farid**
