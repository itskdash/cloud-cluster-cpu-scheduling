package schedulers;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import metrics.GanttChart;
import models.Process;
import models.SchedulingResult;
import models.ProcessState;
public class SJFScheduler extends Scheduler {

    private final List<Process> readyQueue;

    public SJFScheduler() {
        this(0);
    }

    public SJFScheduler(int contextSwitchTime) {
        super(contextSwitchTime);
        this.readyQueue = new ArrayList<>();
    }

    @Override 
    public SchedulingResult schedule(List<Process> processes) {
        if (processes == null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }

        currentTime = 0;
        ganttChart = new GanttChart();
        readyQueue.clear();

        List<Process> allProcesses = new ArrayList<>(processes);
        allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));
        int nextArrival = 0;
        int completed = 0;
        Process previousProcess = null;
        Set<Process> startedProcesses = new HashSet<>();

        while (completed < allProcesses.size()) {
            while (nextArrival < allProcesses.size() && allProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                Process arrived = allProcesses.get(nextArrival++);
                if (arrived.getRemainingTime() > 0) {
                    arrived.setState(ProcessState.READY);
                    readyQueue.add(arrived);
                } else {
                    arrived.setCompletionTime(arrived.getArrivalTime());
                    arrived.setTurnaroundTime(0);
                    arrived.setWaitingTime(0);
                    arrived.setResponseTime(0);
                    arrived.setState(ProcessState.COMPLETED);
                    completed++;
                }
            }

            if (!readyQueue.isEmpty()) {
                if (nextArrival < allProcesses.size()){
                    int idleStart = currentTime;
                    currentTime = Math.max(currentTime, allProcesses.get(nextArrival).getArrivalTime());
                    if (idleStart < currentTime) {
                        ganttChart.addEntry("IDLE", idleStart, currentTime);
                    }
                    previousProcess = null;
                } 
                continue;
            }

            Process selected = selectShortestRemaining();

            if(previousProcess != null && previousProcess != selected && contextSwitchTime > 0){
                int switchStart = currentTime;
                applyContextSwitch();
                gantt.addEntry("CS" ,switchStart, currentTime);

                while (nextArrival < allProcesses.size() && allProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                    Process arrived = allProcesses.get(nextArrival++);
                    if (arrived.getRemainingTime() > 0) {
                        arrived.setState(ProcessState.READY);
                        readyQueue.add(arrived);
                    } else {
                        arrived.setCompletionTime(arrived.getArrivalTime());
                        arrived.setTurnaroundTime(0);
                        arrived.setWaitingTime(0);
                        arrived.setResponseTime(0);
                        arrived.setState(ProcessState.COMPLETED);
                        completed++;
                    }
                } 
                if (readyQueue.isEmpty()) {
                    previousProcess = null;
                    continue;
                }
                selected = selectShortestRemaining();
            }

            int runStart = currentTime;
            selected.setState(ProcessState.RUNNING);
            if (startedProcesses.add(selected)) {
                selected.setStartTime(currentTime);
                selected.setResponseTime(currentTime - selected.getArrivalTime());
            }

            selected.setRemainingTime(selected.getRemainingTime() - 1);
            currentTime++;
            gantt.addEntry("P" + selected.getPid(), runStart, currentTime);

            if (selected.getRemainingTime() == 0) {
                selected.setCompletionTime(currentTime);
                selected.setTurnaroundTime(currentTime - selected.getArrivalTime());
                selected.setWaitingTime(selected.getTurnaroundTime() - selected.getBurstTime());
                selected.setState(ProcessState.COMPLETED);
                readyQueue.remove(selected);
                completed++;
            } else{
                selected.setState(ProcessState.READY);
            }

            previousProcess = selected;
        }
        return createResult("Preemptive SJF", allProcesses);
    }
    
    public Process selectShortestRemaining() {
        if (readyQueue.isEmpty()) {
            return null;
        }
        readyQueue.sort(Comparator.comparingInt(Process::getRemainingTime).thenComparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));
        return readyQueue.get(0);
    }
    private SchedulingResult createResult(String algorithmName, List<Process> processes) {
        SchedulingResult result = new SchedulingResult();
        result.setAlgorithmName(algorithmName);
        result.setProcesses(processes);
        result.setGanttChart(gantt);

        double totalTurnaroundTime = 0;
        double totalWaitingTime = 0;
        for (Process process : processes) {
            totalTurnaroundTime += process.getTurnaroundTime();
            totalWaitingTime += process.getWaitingTime();
        }
        if(!processes.isEmpty()){
            result.setAverageTurnaroundTime(totalTurnaroundTime / processes.size());
            result.setAverageWaitingTime(totalWaitingTime / processes.size());
        } 
        return result;
        }
    }
        