package schedulers;

import java.util.List;
import models.Process;
import models.SchedulingResult;
import metrics.GanttChart;

public abstract class Scheduler {

    protected int contextSwitchTime;
    protected int currentTime;
    protected GanttChart ganttChart;

    protected Scheduler(){
        this(contextSwitchTime : 0);
    }

    protected Scheduler(int contextSwitchTime) {
        if (contextSwitchTime < 0) {
            throw new IllegalArgumentException("Context switch time cannot be negative.");
        }
        this.contextSwitchTime = contextSwitchTime;
        this.currentTime = 0;
        this.ganttChart = new GanttChart();
    }

    public abstract SchedulingResult schedule(List<Process> processes);

    protected void applyContextSwitch() {
        currentTime += contextSwitchTime;
    }

    public int getCurrentTime() {
        return currentTime;
    }

    public GanttChart getGanttChart() {
        return ganttChart;
    }
}