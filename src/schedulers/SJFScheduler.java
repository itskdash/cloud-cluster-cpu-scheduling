package schedulers;

import java.util.List;
import models.Process;
import models.SchedulingResult;

public class SJFScheduler {
    private final int cs;

    public SJFScheduler(int cs) {
        if (cs < 0) throw new IllegalArgumentException("Context switch time cannot be negative");
        this.cs = cs;
    }

    public SchedulingResult schedule(List<Process> procs) {
        SchedulingResult result = new SchedulingResult("Preemptive SJF", procs);
        int time = 0, done = 0;
        Process last = null;
        while (done < procs.size()) {
            Process p = shortest(procs, time, last);
            if (p == null) {
                result.addSlice(0, time, ++time);
                last = null;
                continue;
            }
            if (last != null && last != p) {
                result.addSlice(-1, time, time + cs);
                time += cs;
            }
            if (p.startTime < 0) p.startTime = time;
            result.addSlice(p.pid, time, ++time);
            if (--p.remaining == 0) {
                p.completionTime = time;
                done++;
            }
            last = p;
        }
        return result;
    }

    private Process shortest(List<Process> procs, int time, Process last) {
        Process best = null;
        for (Process p : procs) {
            if (p.arrival > time || p.remaining == 0) continue;
            if (best == null || p.remaining < best.remaining
                    || (p.remaining == best.remaining
                        && (p == last || (best != last && p.arrival < best.arrival)))) best = p;
        }
        return best;
    }
}