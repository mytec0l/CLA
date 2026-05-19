import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class Decoder {
    private final MessageCipher cipher;

    public Decoder(MessageCipher cipher) {
        this.cipher = cipher;
    }
    public Package decode(byte[] data) throws GeneralSecurityException {
        Package pack = new Package();

        ByteBuffer bytes = ByteBuffer.wrap(data);

        byte magic = bytes.get();
        if (magic != 0x13) {
            throw new RuntimeException("Invalid magic byt");
        }

        pack.setbSrc(bytes.get());
        pack.setbPktId(bytes.getLong());

        int messageLength = bytes.getInt();

        if (Crc16.calculateCrc(data, 0, 14) != bytes.getShort()) {
            throw new RuntimeException("header CRc16 error");
        }

        byte[] encryptedMsg = new byte[messageLength];
        bytes.get(encryptedMsg);

        if (Crc16.calculateCrc(data, 16, messageLength) != bytes.getShort()) {
            throw new RuntimeException("message CRC16 error");
        }

        byte[] decrypted = cipher.decrypt(encryptedMsg);
        ByteBuffer msgBuffer = ByteBuffer.wrap(decrypted);
        pack.setMessage(new Message(msgBuffer.getInt(), msgBuffer.getInt(), new String(decrypted, 8, decrypted.length - 8)));

        return pack;
    }
}
