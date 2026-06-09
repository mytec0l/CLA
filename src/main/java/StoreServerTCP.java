import java.io.*;
import java.net.*;

public class StoreServerTCP {
    public static final int PORT = 8080;

    private static final MessageCipher cipher = new MessageCipher("mySuperKey123456".getBytes());
    private static final Encoder encoder = new Encoder(cipher);
    private static final Decoder decoder = new Decoder(cipher);

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("TCP Server started on port " + PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected: " + socket.getRemoteSocketAddress());
                new ClientHandler(socket).start();
            }
        }
    }

    static class ClientHandler extends Thread {
        private final Socket socket;

        ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try (socket;
                 DataInputStream in = new DataInputStream(socket.getInputStream());
                 DataOutputStream out = new DataOutputStream(socket.getOutputStream())) {

                while (true) {
                    int length;
                    try {
                        length = in.readInt();
                    } catch (EOFException e) {
                        System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
                        break;
                    }

                    byte[] data = new byte[length];
                    in.readFully(data);

                    Package pack = decoder.decode(data);
                    System.out.println("Received pkt#" + pack.getbPktId()
                            + " cmd=" + pack.getMessage().getcType()
                            + " msg=" + pack.getMessage().getMessage());

                    Package response = new Package(pack.getbSrc(), pack.getbPktId(),
                            new Message(pack.getMessage().getcType(), pack.getMessage().getbUserId(), "Ok"));

                    byte[] respData = encoder.encode(response);
                    out.writeInt(respData.length);
                    out.write(respData);
                    out.flush();
                }
            } catch (Exception e) {
                System.out.println("Connection error: " + e.getMessage());
            }
        }
    }
}