package org.manul.context;

import junit.framework.TestCase;
import org.manul.schedule.Scheduler;

public class SchedulingTest extends TestCase {

    public void test() {
        ApplicationContext.start(MyWorker.class, MyScheduler.class, MyTask.class);
        assertEquals(1, MyTask.runs);
    }

}


@Component
class MyTask {

    static int runs = 0;

    void run() {
        runs++;
    }

}

@Component
class MyWorker {

    private MyTask task;

    @Scheduled(fixedDelay = 100)
    public void run() {
        task.run();
    }

    @Autowired
    public void setTask(MyTask task) {
        this.task = task;
    }
}

@Component
@Primary
class MyScheduler implements Scheduler {

    @Override
    public void schedule(Runnable run, int delay) {
        run.run();
    }
}



