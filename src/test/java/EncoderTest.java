import org.apache.commons.codec.binary.Hex;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EncoderTest {

    private Encoder encoder;
    private Decoder decoder;

    @BeforeEach
    void setUp() {
        MessageCipher cipher = new MessageCipher("MySecretKey12345".getBytes());
        encoder = new Encoder(cipher);
        decoder = new Decoder(cipher);
    }

    @Test
    void testEncode() throws Exception {
        Package original = new Package((byte) 1, 2, new Message(3, 4, "test"));
        Package decoded = decoder.decode(encoder.encode(original));

        assertEquals((byte) 1, decoded.getbSrc());
        assertEquals(2, decoded.getbPktId());
        assertEquals(3, decoded.getMessage().getcType());
        assertEquals(4, decoded.getMessage().getbUserId());
        assertEquals("test", decoded.getMessage().getMessage());
    }
}