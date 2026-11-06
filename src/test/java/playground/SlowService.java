package playground;

import org.springframework.stereotype.Component;

@Component
public class SlowService {

    private final Worker worker;

    public SlowService(Worker worker) {
        this.worker = worker;
    }

    public void doWorkTwiceViaWorker(){
        worker.doWork();
        worker.doWork();
    }

    @Timed
    public void doWork() {
        System.out.println("doWork running");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
        }
    }

    public void fastWork(){
        System.out.println("fastWork running");
    }

    public void doWorkTwice() {
        doWork();
        doWork();
    }
}
