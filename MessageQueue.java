/*
* Aditya Borde 	  (asb140930)
* Bharat Bhavsar (bmb140330)
* Braden Herndon (bph091020)
*/

import java.util.LinkedList;
import java.util.Queue;

public class MessageQueue {

    private Queue<Message> queue;

    public MessageQueue() {
        this.queue = new LinkedList<>();
    }

    public synchronized void send(Message t) {
        queue.add(t);
    }

    public synchronized Message receive() {
        return queue.poll();
    }

    public synchronized boolean isEmpty() {
        return queue.isEmpty();
    }
}

