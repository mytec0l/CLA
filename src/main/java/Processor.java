public class Processor implements Runnable {
    private final Buffer<Package> inputBuffer;
    private final Buffer<Package> outputBuffer;
    private volatile boolean running = true;


    public Processor(Buffer<Package> inputBuffer, Buffer<Package> outputBuffer){
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
    }


    @Override
    public void run() {
        while (running) {
            try {
                Package pack = inputBuffer.take();
                System.out.println(getCommandName(pack.getMessage().getcType()) + " " + pack.getMessage().getMessage());
                Package response = process(pack);
                outputBuffer.put(response);
            } catch (Exception e) {
                break;
            }
        }
    }

    private Package process(Package pack) {
        Message response = new Message(pack.getMessage().getcType(), pack.getMessage().getbUserId(), "Ok");
        return new Package(pack.getbSrc(), pack.getbPktId(), response);
    }

    private String getCommandName(int cType){
        switch (cType) {
            case 1: return "Дізнатись кількість";
            case 2: return "Списати";
            case 3: return "Зарахувати";
            case 4: return "Додати групу";
            case 5: return "Додати товар до групи";
            case 6: return "Встановити ціну";
            default: return "Невідома команда";
        }
    }

    public void stop() {
        running = false;
    }


}