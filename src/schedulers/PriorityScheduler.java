package schedulers;

import java.util.List;
import java.util.Comparator;
import java.util.ArrayList;
import models.Process;
import models.SchedulingResult;
import models.ProcessState;

public class PriorityScheduler extends Scheduler {

    private final List<Process> priorityQueue;
    private metrics.GanttChart gantt;

    public PriorityScheduler() {
        this(  0);
    }

    public PriorityScheduler(int contextSwitchTime) {
        super(contextSwitchTime);
        this.priorityQueue = new ArrayList<>();
    }

    @Override 
    public SchedulingResult schedule(List<Process> processes) {
        if (processes ==null) {
            throw new IllegalArgumentException("Process list cannot be null.");
        }

        currentTime = 0;
        gantt = new metrics.GanttChart();
        priorityQueue.clear();

        List<Process> allProcesses = new ArrayList<>(processes);
        allProcesses.sort(Comparator.comparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));

        int nextArrival = 0;
        int completed = 0;

        while (completed < allProcesses.size()) {
            while (nextArrival < allProcesses.size() && allProcesses.get(nextArrival).getArrivalTime() <= currentTime) {
                Process arrived = allProcesses.get(nextArrival++);
                if(arrived.getRemainingTime() <=0){
                    arrived.setCompletionTime(arrived.getArrivalTime());
                    arrived.setTurnaroundTime(0);
                    arrived.setWaitingTime(0);
                    arrived.setResponseTime(0);
                    arrived.setState(ProcessState.COMPLETED);
                completed++;
                }else{
                    addProcess(arrived);
                }
            }
            if(!hasProcesses()){
                if(nextArrival < allProcesses.size()){
                    int idleStart = currentTime;
                    currentTime= Math.max(currentTime, allProcesses.get(nextArrival).getArrivalTime());
                    if(idleStart < currentTime){
                        gantt.addEntry("IDLE", idleStart, currentTime);
                    }
                }
                continue;
            }

            Process selected = removeProcess();
            selected.setState(ProcessState.RUNNING);
            selected.setStartTime(currentTime);
            selected.setResponseTime(currentTime - selected.getArrivalTime());

            int runStart = currentTime;
            int duration = selected.getRemainingTime();
            currentTime += duration;
            selected.setRemainingTime(0);
            selected.setCompletionTime(currentTime);
            selected.setTurnaroundTime(currentTime - selected.getArrivalTime());
            selected.setWaitingTime(selected.getTurnaroundTime() - selected.getBurstTime());
            selected.setState(ProcessState.COMPLETED);
            gantt.addEntry("P" + selected.getPid(), runStart, currentTime);
            completed++;
        }
        return createResult("Non-Preemptive Priority", allProcesses);
    }

    public void addProcess(Process process) {
        if (process == null) {
            throw new IllegalArgumentException("Process cannot be null.");
        }
        if (process.getRemainingTime() >0 && !priorityQueue.contains(process)) {
            process.setState(ProcessState.READY);
            priorityQueue.add(process);
        }
    }

    public Process selectHighestPriority(){
        if(priorityQueue.isEmpty()){
            return null;
        }
        priorityQueue.sort(Comparator.comparingInt(Process::getPriority).thenComparingInt(Process::getArrivalTime).thenComparingInt(Process::getPid));
        return priorityQueue.get(0);
    }

    public boolean hasProcesses() {
        return !priorityQueue.isEmpty();
    }

    public Process removeProcess() {
        Process selected = selectHighestPriority();
        if (selected != null) {
            priorityQueue.remove(selected);
        }
        return selected;
    }

    public List<Process> getQueue() {
        return new ArrayList<>(priorityQueue);
    }

    private SchedulingResult createResult(String algorithmName, List<Process> processes) {
        SchedulingResult result = new SchedulingResult();
        result.setAlgorithmName(algorithmName);
        result.setProcesses(processes);
        result.setGanttChart(gantt);
        
        double totalTurnaroundTime = 0;
        double totalWaiting = 0;
        for(Process process : processes){
            totalTurnaroundTime += process.getTurnaroundTime();
            totalWaiting += process.getWaitingTime();
        }
        if(!processes.isEmpty()){
            result.setAverageTurnaroundTime(totalTurnaroundTime / processes.size());
            result.setAverageWaitingTime(totalWaiting / processes.size());
        } 
        return result;
    }
}

