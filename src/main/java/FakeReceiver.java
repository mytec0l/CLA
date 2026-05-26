import java.util.Random;

public class FakeReceiver implements Receiver, Runnable {
    private static final String[] Products = {"Рис", "Куриця", "Макарони", "Сирники", "Кока кола"};
    private static final String[] Groups = {"Крупи", "Мясо", "Молочне", "Напої"};

    private final Buffer<byte[]> outputBuffer;
    private final Encoder encoder;
    private final Random random = new Random();

    private volatile boolean running = true;
    private long packetId = 1;

    public FakeReceiver(Buffer<byte[]> outputBuffer, Encoder encoder) {
        this.outputBuffer = outputBuffer;
        this.encoder = encoder;
    }

    @Override
    public void receiveMessage() {
        while (running) {
            try {
                int cType = random.nextInt(6)+ 1;
                Package pack = new Package((byte) 1, packetId++, new Message(cType, 1, buildPayload(cType)));
                outputBuffer.put(encoder.encode(pack));
                Thread.sleep(1000);
            } catch (Exception e) {
                break;
            }
        }
    }


    private String buildPayload(int cType) {
        String product = Products[random.nextInt(Products.length)];

        switch (cType) {
            case 1:
                return product;
            case 2:
                return product + "," + (random.nextInt(50) + 1);
            case 3:
                return product + "," + (random.nextInt(50) + 1);
            case 4:
                return Groups[random.nextInt(Groups.length)];
            case 5:
                return product + "," + Groups[random.nextInt(Groups.length)];
            case 6:
                return product + "," + (random.nextInt(100) + 5);
            default:
                return product;
        }
    }

    @Override
    public void stop() { running = false; }

    @Override
    public void run() { receiveMessage(); }
}
