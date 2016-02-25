/*
* Aditya Borde 	  (asb140930)
* Bharat Bhavsar (bmb140330)
* Braden Herndon (bph091020)
*/

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Synchronizer {

    int numThreads;
    AtomicInteger counter;
    public AtomicBoolean finished;

    public Synchronizer(int numThreads) {
        finished = new AtomicBoolean(false);
        counter = new AtomicInteger(0);
        this.numThreads = numThreads;
    }

    public synchronized boolean isFinished() {
        return finished.get();
    }

    public void incrementCounter() {
        counter.set(counter.incrementAndGet());
        if (counter.intValue() == numThreads) {
            finished.set(true);
        }
    }
}
