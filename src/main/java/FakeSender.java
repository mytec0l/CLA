public class FakeSender implements Runnable {
    private final Buffer<byte[]> inputBuffer;
    private volatile boolean running = true;

    public FakeSender(Buffer<byte[]> inputBuffer) {
        this.inputBuffer = inputBuffer;
    }


    @Override
    public void run() {
        while (running){
            try {
                byte[] data = inputBuffer.take();
            } catch (Exception e) {
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }
}