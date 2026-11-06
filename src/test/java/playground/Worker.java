package playground;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

@Component
public class Worker {

    @Timed
    public void doWork() {
        System.out.println("Worker.doWork running");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @PostConstruct
    public void postConstruct(){
        System.out.println("Worker @postConstruct");
        doWork();
    }
}
