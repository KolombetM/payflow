package playground;

import org.springframework.stereotype.Component;

@Component
public class SlowService {

    @Timed
    public void doWork() {
        System.out.println("doWork running");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
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
