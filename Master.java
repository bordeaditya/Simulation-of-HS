
/*
* Aditya Borde 	  (asb140930)
* Bharat Bhavsar (bmb140330)
* Braden Herndon (bph091020)
*/


import java.io.*;
import java.util.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

/**
 * Created by braden on 1/29/16.
 */
public class Master {

    static Synchronizer sync;
    static CyclicBarrier cb;
    private static ArrayList<Process> processes;

    public static void main(String[] args) throws InterruptedException, BrokenBarrierException {

        processes = new ArrayList<>();

        Scanner in = null;
        int numberOfThreads = 0;

        try {
            in = new Scanner(new File("input.dat"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        numberOfThreads = Integer.parseInt(in.nextLine());
        System.out.println("Master: number of processes = " + numberOfThreads);

        cb = new CyclicBarrier(numberOfThreads + 1);
        sync = new Synchronizer(numberOfThreads);

        MessageQueue firstInboundLeft = new MessageQueue();
        MessageQueue firstOutboundLeft = new MessageQueue();
        MessageQueue lastInboundRight = null;
        MessageQueue lastOutboundRight = null;

        // Create two inbound thread to save for later.
        // For the number of threads in the input file, loop through, getting each id.
        // For each thread, create two new outbound queues and save a reference to them.
        // For the inbound queues, get the last two outbound queues created.
        // Finally, for the final process's outbound threads, grab the first
        // two inbound threads we made, completing the ring.

        for (int i = 0; i < numberOfThreads; i++) {
            int pid = Integer.parseInt(in.nextLine());
            MessageQueue inboundR = new MessageQueue();
            MessageQueue outboundR = new MessageQueue();
            Process p;
            if (i == 0) {
                p = new Process(pid, firstInboundLeft, firstOutboundLeft,
                        inboundR, outboundR, cb, sync);
            } else if (i == numberOfThreads - 1) {
                p = new Process(pid, lastOutboundRight, lastInboundRight, firstOutboundLeft,
                        firstInboundLeft, cb, sync);
            } else {
                p = new Process(pid, lastOutboundRight, lastInboundRight, inboundR, outboundR, cb, sync);
            }
            lastInboundRight = inboundR;
            lastOutboundRight = outboundR;
            new Thread(p).start();
            processes.add(p);
        }


        // While leader is not found, execute.
        while (!sync.isFinished()) {
            // Master waits for everyone to finish round, then gives the go-ahead.
            if (cb.getNumberWaiting() == numberOfThreads) {
                cb.await();
            }
        }
        System.out.println("All threads completed. Shutting down.");
        System.exit(0);
    }

}

