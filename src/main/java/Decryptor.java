public class Decryptor implements Runnable {
    private final Buffer<byte[]> inputBuffer;
    private final Buffer<Package> outputBuffer;

    private final Decoder decoder;
    private volatile boolean running = true;

    public Decryptor(Buffer<byte[]> inputBuffer, Buffer<Package> outputBuffer,Decoder decoder){
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.decoder = decoder;
    }

    @Override
    public void run() {
        while (running) {
            try {
                byte[] data = inputBuffer.take();
                Package pack = decoder.decode(data);
                outputBuffer.put(pack);
            } catch (Exception e) {
                break;
            }
        }
    }

    public void stop() {
        running = false;
    }
}