import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DecoderTest {

    private Encoder encoder;
    private Decoder decoder;

    @BeforeEach
    void setUp() {
        MessageCipher cipher = new MessageCipher("MySecretKey12345".getBytes());
        encoder = new Encoder(cipher);
        decoder = new Decoder(cipher);
    }

    @Test
    void testDecode() throws Exception {
        Package original = new Package((byte) 1, 2, new Message(3, 4, "test"));
        Package decoded = decoder.decode(encoder.encode(original));

        assertEquals((byte) 1, decoded.getbSrc());
        assertEquals(2, decoded.getbPktId());
        assertEquals(3, decoded.getMessage().getcType());
        assertEquals(4, decoded.getMessage().getbUserId());
        assertEquals("test", decoded.getMessage().getMessage());
    }

    @Test
    void testDecodeInvalidMagicByte() throws Exception {
        byte[] encoded = encoder.encode(new Package((byte) 1, 2, new Message(3, 4, "test")));
        encoded[0] = 0x00;

        assertThrows(RuntimeException.class, () -> decoder.decode(encoded));
    }

    @Test
    void testDecodeInvalidHeaderCrc() throws Exception {
        byte[] encoded = encoder.encode(new Package((byte) 1, 2, new Message(3, 4, "test")));
        encoded[14] ^= 0xFF;

        assertThrows(RuntimeException.class, () -> decoder.decode(encoded));
    }

    @Test
    void testDecodeInvalidMessageCrc() throws Exception {
        byte[] encoded = encoder.encode(new Package((byte) 1, 2, new Message(3, 4, "test")));
        encoded[encoded.length - 1] ^= 0xFF;

        assertThrows(RuntimeException.class, () -> decoder.decode(encoded));
    }
}