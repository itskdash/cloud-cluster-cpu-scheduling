package schedulers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import models.Process;
import models.SchedulingResult;

public class ACSScheduler {
    private final int cs, quantum, agingInterval, agingStep;
    private final Deque<Process> tier1 = new ArrayDeque<>();
    private final List<Process> tier2 = new ArrayList<>();
    private List<Process> arrivals;
    private SchedulingResult result;
    private int time, next;

    public ACSScheduler(int cs, int quantum, int agingInterval, int agingStep) {
        if (cs < 0 || quantum <= 0 || agingInterval < 0 || agingStep < 0)
            throw new IllegalArgumentException("Invalid values: CS >= 0, Q > 0, aging interval/step >= 0");
        this.cs = cs;
        this.quantum = quantum;
        this.agingInterval = agingInterval;
        this.agingStep = agingStep;
    }

    public SchedulingResult schedule(List<Process> procs) {
        result = new SchedulingResult("ACS", procs);
        arrivals = new ArrayList<>(procs);
        arrivals.sort(Comparator.comparingInt((Process p) -> p.arrival).thenComparingInt(p -> p.pid));
        tier1.clear();
        tier2.clear();
        time = 0;
        next = 0;
        admit();

        int done = 0;
        Process last = null;
        while (done < procs.size()) {
            boolean fromTier1 = !tier1.isEmpty();
            if (!fromTier1 && tier2.isEmpty()) {
                tick(0, null);
                last = null;
                continue;
            }
            Process p = fromTier1 ? tier1.poll() : pickFromTier2();
            if (last != null && last != p)
                for (int i = 0; i < cs; i++) tick(-1, null);

            if (p.startTime < 0) p.startTime = time;
            int slice = fromTier1 ? Math.min(quantum, p.remaining) : p.remaining;
            for (int i = 0; i < slice; i++) tick(p.pid, p);
            last = p;

            if (p.remaining == 0) {
                p.completionTime = time;
                done++;
            } else {
                p.tier = 2;
                p.waitingSince = time;
                tier2.add(p);
            }
        }
        snapshot("-");
        return result;
    }

    private void tick(int label, Process running) {
        snapshot(SchedulingResult.label(label));
        result.addSlice(label, time, time + 1);
        if (running != null) running.remaining--;
        time++;
        admit();
    }

    private void admit() {
        while (next < arrivals.size() && arrivals.get(next).arrival <= time) tier1.add(arrivals.get(next++));
    }

    private Process pickFromTier2() {
        Process best = tier2.stream().min(Comparator.comparingInt(this::priority)
                .thenComparingInt(q -> q.arrival).thenComparingInt(q -> q.pid)).get();
        tier2.remove(best);
        best.priority = priority(best);
        best.waitingSince = -1;
        return best;
    }

    private int priority(Process p) {
        if (p.waitingSince < 0 || agingInterval == 0) return p.priority;
        return Math.max(0, p.priority - (time - p.waitingSince) / agingInterval * agingStep);
    }

    private void snapshot(String running) {
        List<String> t1 = new ArrayList<>(), t2 = new ArrayList<>(), finished = new ArrayList<>();
        for (Process p : result.processes) {
            if (p.arrival > time) continue;
            if (p.remaining == 0) finished.add("P" + p.pid);
            else (p.tier == 1 ? t1 : t2).add("P" + p.pid + " (rem " + p.remaining + ", prio " + priority(p) + ")");
        }
        result.steps.add(new String[]{String.valueOf(time), running, join(t1), join(t2), join(finished)});
    }

    private static String join(List<String> items) {
        return items.isEmpty() ? "-" : String.join(" | ", items);
    }
}