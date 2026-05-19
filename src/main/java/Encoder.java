import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class Encoder {
    private final MessageCipher cipher;


    public Encoder(MessageCipher cipher) {
        this.cipher = cipher;
    }


    public byte[] encode(Package pack) throws GeneralSecurityException {
        byte[] messageText = pack.getMessage().getMessage().getBytes();

        ByteBuffer msgBuffer = ByteBuffer.allocate(4+4+messageText.length);
        msgBuffer.putInt(pack.getMessage().getcType());
        msgBuffer.putInt(pack.getMessage().getbUserId());
        msgBuffer.put(messageText);


        byte[] encryptedMsg = cipher.encrypt(msgBuffer.array());
        int messageLength = encryptedMsg.length;


        ByteBuffer bytes = ByteBuffer.allocate(1+1+8+4+2+messageLength+2);
        bytes.put((byte)0x13);
        bytes.put(pack.getbSrc());
        bytes.putLong(pack.getbPktId());
        bytes.putInt(messageLength);
        bytes.putShort(Crc16.calculateCrc(bytes.array(), 0, bytes.position()));


        bytes.put(encryptedMsg);
        bytes.putShort(Crc16.calculateCrc(bytes.array(), 16, messageLength));
        return bytes.array();
    }
}
