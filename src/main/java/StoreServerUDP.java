import java.net.*;

public class StoreServerUDP {
    public static final int PORT = 4445;

    public static void main(String[] args) throws Exception {
        MessageCipher cipher = new MessageCipher("mySuperKey123456".getBytes());
        Encoder encoder = new Encoder(cipher);
        Decoder decoder = new Decoder(cipher);

        try (DatagramSocket serverSocket = new DatagramSocket(PORT)) {
            System.out.println("UDP Server started on port " + PORT);

            while (true) {
                byte[] buf = new byte[4096];
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                serverSocket.receive(packet);

                byte[] data = new byte[packet.getLength()];
                System.arraycopy(packet.getData(), 0, data, 0, packet.getLength());

                Package pack = decoder.decode(data);
                System.out.println("Received pkt#" + pack.getbPktId()
                        + " from " + packet.getAddress() + ":" + packet.getPort()
                        + " cmd=" + pack.getMessage().getcType()
                        + " msg=" + pack.getMessage().getMessage());

                Package response = new Package(pack.getbSrc(), pack.getbPktId(),
                        new Message(pack.getMessage().getcType(), pack.getMessage().getbUserId(), "Ok"));

                byte[] respData = encoder.encode(response);
                serverSocket.send(new DatagramPacket(respData, respData.length,
                        packet.getAddress(), packet.getPort()));
            }
        }
    }
}