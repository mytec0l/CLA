import java.net.*;
import java.util.Random;

public class StoreClientUDP implements Runnable {
    private static final int PORT = StoreServerUDP.PORT;
    private static final int TIMEOUT_MS = 2000;
    private static final int MAX_RETRIES = 3;
    private static final String[] PRODUCTS = {"Рис", "Куриця", "Макарони", "Сирники", "Кока кола"};
    private static final String[] GROUPS = {"Крупи", "Мясо", "Молочне", "Напої"};

    private final int clientId;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Random random = new Random();
    private long packetId = 1;

    public StoreClientUDP(int clientId, Encoder encoder, Decoder decoder) {
        this.clientId = clientId;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public void run() {
        try {
            InetAddress serverAddr = InetAddress.getByName("localhost");

            try (DatagramSocket socket = new DatagramSocket()) {
                socket.setSoTimeout(TIMEOUT_MS);

                for (int i = 0; i < 5; i++) {
                    int cType = random.nextInt(6) + 1;
                    Package pack = new Package((byte) clientId, packetId++,
                            new Message(cType, clientId, buildPayload(cType)));

                    byte[] data = encoder.encode(pack);
                    DatagramPacket sendPacket = new DatagramPacket(data, data.length, serverAddr, PORT);

                    boolean acked = false;
                    for (int attempt = 1; attempt <= MAX_RETRIES && !acked; attempt++) {
                        socket.send(sendPacket);
                        System.out.println("Client " + clientId + ": sent pkt#" + pack.getbPktId()
                                + " attempt=" + attempt + " cmd=" + cType + " msg=" + pack.getMessage().getMessage());

                        try {
                            byte[] buf = new byte[4096];
                            DatagramPacket resp = new DatagramPacket(buf, buf.length);
                            socket.receive(resp);

                            byte[] respData = new byte[resp.getLength()];
                            System.arraycopy(resp.getData(), 0, respData, 0, resp.getLength());

                            Package response = decoder.decode(respData);
                            if (response.getbPktId() == pack.getbPktId()) {
                                System.out.println("Client " + clientId + ": received pkt#"
                                        + response.getbPktId() + " response=" + response.getMessage().getMessage());
                                acked = true;
                            }
                        } catch (SocketTimeoutException e) {
                            System.out.println("Client " + clientId + ": timeout pkt#" + pack.getbPktId()
                                    + " retry " + attempt + "/" + MAX_RETRIES);
                        }
                    }

                    if (!acked) {
                        System.out.println("Client " + clientId + ": pkt#" + pack.getbPktId() + " lost");
                    }

                    Thread.sleep(1000);
                }
            }
        } catch (Exception e) {
            System.out.println("Client " + clientId + ": error " + e.getMessage());
        }
        System.out.println("Client " + clientId + ": done");
    }

    private String buildPayload(int cType) {
        String product = PRODUCTS[random.nextInt(PRODUCTS.length)];
        switch (cType) {
            case 1: return product;
            case 2: return product + "," + (random.nextInt(50) + 1);
            case 3: return product + "," + (random.nextInt(50) + 1);
            case 4: return GROUPS[random.nextInt(GROUPS.length)];
            case 5: return product + "," + GROUPS[random.nextInt(GROUPS.length)];
            case 6: return product + "," + (random.nextInt(100) + 5);
            default: return product;
        }
    }


    public static void main(String[] args) throws InterruptedException {
        MessageCipher cipher = new MessageCipher("mySuperKey123456".getBytes());
        Encoder encoder = new Encoder(cipher);
        Decoder decoder = new Decoder(cipher);

        Thread t1 = new Thread(new StoreClientUDP(1, encoder, decoder));
        Thread t2 = new Thread(new StoreClientUDP(2, encoder, decoder));
        Thread t3 = new Thread(new StoreClientUDP(3, encoder, decoder));

        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();
    }
}