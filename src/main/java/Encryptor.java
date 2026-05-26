public class Encryptor implements Runnable {
    private final Buffer<Package> inputBuffer;
    private final Buffer<byte[]> outputBuffer;

    private final Encoder encoder;
    private volatile boolean running = true;

    public Encryptor(Buffer<Package> inputBuffer, Buffer<byte[]> outputBuffer, Encoder encoder) {
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.encoder = encoder;
    }



    @Override
    public void run() {
        while (running) {
            try {
                Package pack = inputBuffer.take();
                byte[] encoded = encoder.encode(pack);
                outputBuffer.put(encoded);
            } catch (Exception e) {
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }

}