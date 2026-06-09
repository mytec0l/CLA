import java.io.*;
import java.net.*;
import java.util.Random;

public class StoreClientTCP implements Runnable {
    private static final int PORT = StoreServerTCP.PORT;
    private static final String[] PRODUCTS = {"Рис", "Куриця", "Макарони", "Сирники", "Кока кола"};
    private static final String[] GROUPS = {"Крупи", "Мясо", "Молочне", "Напої"};
    private static final int TOTAL_PACKETS = 5;

    private final int clientId;
    private final Encoder encoder;
    private final Decoder decoder;
    private final Random random = new Random();
    private long packetId = 1;

    public StoreClientTCP(int clientId, Encoder encoder, Decoder decoder) {
        this.clientId = clientId;
        this.encoder = encoder;
        this.decoder = decoder;
    }

    @Override
    public void run() {
        int sent = 0;

        while (sent < TOTAL_PACKETS) {
            try (Socket socket = new Socket("localhost", PORT);
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream());
                 DataInputStream in = new DataInputStream(socket.getInputStream())) {

                System.out.println("Client " + clientId + ": connected");

                while (sent < TOTAL_PACKETS) {
                    int cType = random.nextInt(6) + 1;
                    Package pack = new Package((byte) clientId, packetId,
                            new Message(cType, clientId, buildPayload(cType)));

                    byte[] data = encoder.encode(pack);
                    out.writeInt(data.length);
                    out.write(data);
                    out.flush();
                    System.out.println("Client " + clientId + ": sent pkt#" + pack.getbPktId()
                            + " cmd=" + cType + " msg=" + pack.getMessage().getMessage());

                    int respLen = in.readInt();
                    byte[] respData = new byte[respLen];
                    in.readFully(respData);
                    Package resp = decoder.decode(respData);
                    System.out.println("Client " + clientId + ": received pkt#" + resp.getbPktId()
                            + " response=" + resp.getMessage().getMessage());

                    sent++;
                    packetId++;
                    Thread.sleep(1000);
                }

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                System.out.println("Client " + clientId + ": server unavailable, reconnecting...");
                try { Thread.sleep(2000); } catch (InterruptedException ie) { break; }
            } catch (Exception e) {
                System.out.println("Client " + clientId + ": error " + e.getMessage());
                try { Thread.sleep(2000); } catch (InterruptedException ie) { break; }
            }
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

        Thread t1 = new Thread(new StoreClientTCP(1, encoder, decoder));
        Thread t2 = new Thread(new StoreClientTCP(2, encoder, decoder));
        Thread t3 = new Thread(new StoreClientTCP(3, encoder, decoder));

        t1.start(); t2.start(); t3.start();
        t1.join();  t2.join();  t3.join();
    }
}