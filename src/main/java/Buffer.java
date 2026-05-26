import java.util.LinkedList;
import java.util.Queue;

public class Buffer<T> {
    private final Queue<T> queue = new LinkedList<>();

    public synchronized void put(T item) {
        queue.add(item);
        notifyAll();
    }



    public synchronized T take() throws InterruptedException {
        while (queue.isEmpty()) {
            wait();
        }
        return queue.poll();
    }
}