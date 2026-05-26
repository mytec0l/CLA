import org.junit.jupiter.api.Test;

class PipelineTest {

    @Test
    void testMultiThreadedMessages() throws InterruptedException {
        MessageCipher cipher = new MessageCipher("mySuperKey123456".getBytes());

        Encoder encoder = new Encoder(cipher);
        Decoder decoder = new Decoder(cipher);

        Buffer<byte[]> buf1 = new Buffer<>();
        Buffer<Package> buf2 = new Buffer<>();
        Buffer<Package> buf3 = new Buffer<>();
        Buffer<byte[]> buf4 = new Buffer<>();


        Decryptor decryptor = new Decryptor(buf1, buf2, decoder);
        Processor processor = new Processor(buf2, buf3);
        Encryptor encryptor = new Encryptor(buf3, buf4, encoder);
        FakeSender sender = new FakeSender(buf4);

        new Thread(decryptor).start();
        new Thread(processor).start();
        new Thread(encryptor).start();
        new Thread(sender).start();


        for (int i = 0; i < 5; i++) {
            final int id = i;

            new Thread(() -> {

                for (int j = 0; j < 10; j++){
                    try {
                        Package pack = new Package((byte) 1, id * 10L+ j, new Message(1, id, "Рис"));
                        buf1.put(encoder.encode(pack));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        Thread.sleep(3000);
        decryptor.stop();
        processor.stop();
        encryptor.stop();
        sender.stop();
    }
}