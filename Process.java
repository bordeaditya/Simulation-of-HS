/*
* Aditya Borde 	  (asb140930)
* Bharat Bhavsar (bmb140330)
* Braden Herndon (bph091020)
*/

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Process implements Runnable {

    int pid;
    MessageQueue inboundLeft, outboundRight, inboundRight, outboundLeft;
    Synchronizer sync;
    int phase;
    boolean running = true;
    boolean active = true;
    boolean processFinished = false;
    boolean myIdReturnedFromLeft = false;
    boolean myIdReturnedFromRight = false;
    CyclicBarrier cb;

    public Process(int pid, MessageQueue inboundL, MessageQueue outboundL,
                   MessageQueue inboundR, MessageQueue outboundR, CyclicBarrier cb, Synchronizer sync) {
        this.pid = pid;
        this.phase = 0;
        this.inboundLeft = inboundL;
        this.outboundLeft = outboundL;
        this.inboundRight = inboundR;
        this.outboundRight = outboundR;
        this.sync = sync;
        this.cb = cb;
    }

    public void run() {
        System.out.println("Thread for " + this.pid + " is created.");
        while (!sync.isFinished()) {
            try {
                if (processFinished) {
                    cb.await();
                } else {
                    cb.await();
                    hs();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
        }
    }

    public void hs() {
        if (active) {
                outboundRight.send(new Message(pid, "out", (int) Math.pow(2, phase), -1));
                outboundLeft.send(new Message(pid, "out", (int) Math.pow(2, phase), -1));
                active = false;
        }

        Message messageFromLeft, messageFromRight;
        messageFromLeft = inboundLeft.receive();
        messageFromRight = inboundRight.receive();

        // First, check if the leader has been found by checking if the leader field
        // of the message is still -1. If the leader field is not -1, the leader has been
        // found already and this process can stop running.

        if (messageFromLeft != null && messageFromLeft.getLeader() > -1) {
            System.out.println(this.pid + " says: " + messageFromLeft.getLeader() + " is the leader.");
            outboundRight.send(messageFromLeft);
            this.processFinished = true;
            sync.incrementCounter();
            return;
        }

        if (messageFromRight != null && messageFromRight.getLeader() > -1) {
            System.out.println(this.pid + " says: " + messageFromRight.getLeader() + " is the leader.");
            outboundLeft.send(messageFromRight);
            this.processFinished = true;
            sync.incrementCounter();
            return;
        }

        // Then, check if process phase is done. If the process has received its own ID,
        // coming inward, from both sides, then the process can move on to its phase 2.

        if (myIdReturnedFromLeft && myIdReturnedFromRight) {
            active = true;
            myIdReturnedFromLeft= false;
            myIdReturnedFromRight = false;
            phase++;
            return;
        }

        // Finally, start processing messages in accordance with HS algorithm from book.

        if (messageFromLeft != null) {

            // If message is outbound...
            if(messageFromLeft.getDirection() == "out") {

                // if it has hops left, decrement hops and pass it on.
                if (messageFromLeft.getOriginId() > this.pid && messageFromLeft.getHops() > 1) {
                    messageFromLeft.setHops(messageFromLeft.getHops() - 1);
                    outboundRight.send(messageFromLeft);

                //  if it doesn't have hops left, turn it around and send it back.
                } else if (messageFromLeft.getOriginId() > this.pid && messageFromLeft.getHops() == 1) {
                    messageFromLeft.setHops(1);
                    messageFromLeft.setDirection("in");
                    outboundLeft.send(messageFromLeft);

                // if the message id equals process id, process is the leader and the thread can terminate.
                } else if (messageFromLeft.getOriginId() == this.pid) {
                    System.out.println(this.pid + " says: " + this.pid + " is the leader. Completed at phase " + this.phase);
                    outboundLeft.send(new Message(this.pid, "out", 1, this.pid));
                    outboundRight.send(new Message(this.pid, "out", 1, this.pid));
                    this.processFinished = true;
                    sync.incrementCounter();
                    return;
                }

            // If message is inbound...
            } else if (messageFromLeft.getDirection() == "in") {// && messageFromLeft.getOriginId() != this.pid) {

                // If the message ID equals the process ID, it can confirm it has received its id back from that side.
                if(messageFromLeft.getOriginId() == this.pid) {
                    myIdReturnedFromLeft = true;

                // If it is a different ID, just pass it on in.
                } else {
                    outboundRight.send(messageFromLeft);
                }
            }
        }

        if (messageFromRight != null) {

            // If message is outbound...
            if(messageFromRight.getDirection() == "out") {

                // if it has hops left, decrement hops and pass it on.
                if (messageFromRight.getOriginId() > this.pid && messageFromRight.getHops() > 1) {
                    messageFromRight.setHops(messageFromRight.getHops() - 1);
                    outboundLeft.send(messageFromRight);

                //  if it doesn't have hops left, turn it around and send it back.
                } else if (messageFromRight.getOriginId() > this.pid && messageFromRight.getHops() == 1) {
                    messageFromRight.setHops(1);
                    messageFromRight.setDirection("in");
                    outboundRight.send(messageFromRight);

                // if the message id equals process id, process is the leader and the thread can terminate.
                } else if (messageFromRight.getOriginId() == this.pid) {
                    System.out.println(this.pid + " says: " + this.pid + " is the leader. Completed at phase " + this.phase);
                    outboundLeft.send(new Message(this.pid, "out", 1, this.pid));
                    outboundRight.send(new Message(this.pid, "out", 1, this.pid));
                    this.processFinished = true;
                    sync.incrementCounter();
                    return;
                }

            }

            // If message is inbound...
            else if (messageFromRight.getDirection() == "in") {

                // If the message ID equals the process ID, it can confirm it has received its id back from that side.
                if (messageFromRight.getOriginId() == this.pid) {
                    myIdReturnedFromRight = true;

                // If it is a different ID, just pass it on in.
                } else {
                    outboundLeft.send(messageFromRight);
                }
            }
        }
    }
}

