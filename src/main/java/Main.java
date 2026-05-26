public class Main {
    public static void main(String[] args) throws InterruptedException {
        MessageCipher cipher = new MessageCipher("mySuperKey123456".getBytes());

        Encoder encoder = new Encoder(cipher);
        Decoder decoder = new Decoder(cipher);


        Buffer<byte[]> receiverToDecryptor = new Buffer<>();
        Buffer<Package> decryptorToProcessor = new Buffer<>();
        Buffer<Package> processorToEncryptor = new Buffer<>();
        Buffer<byte[]> encryptorToSender = new Buffer<>();

        FakeReceiver receiver = new FakeReceiver(receiverToDecryptor, encoder);
        Decryptor decryptor = new Decryptor(receiverToDecryptor, decryptorToProcessor, decoder);
        Processor processor = new Processor(decryptorToProcessor, processorToEncryptor);
        Encryptor encryptor = new Encryptor(processorToEncryptor, encryptorToSender, encoder);
        FakeSender sender = new FakeSender(encryptorToSender);

        Thread tReceiver  = new Thread(receiver);
        Thread tDecryptor = new Thread(decryptor);
        Thread tProcessor = new Thread(processor);
        Thread tEncryptor = new Thread(encryptor);
        Thread tSender    = new Thread(sender);

        tReceiver.start();
        tDecryptor.start();
        tProcessor.start();
        tEncryptor.start();
        tSender.start();

        Thread.sleep(5000);

        receiver.stop();
        decryptor.stop();
        processor.stop();
        encryptor.stop();
        sender.stop();

        tReceiver.join();
        tDecryptor.join();
        tProcessor.join();
        tEncryptor.join();
        tSender.join();
    }
}