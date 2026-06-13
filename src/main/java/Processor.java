public class Processor implements Runnable {
    private final Buffer<Package> inputBuffer;
    private final Buffer<Package> outputBuffer;
    private final ProductDb db;
    private volatile boolean running = true;

    public Processor(Buffer<Package> inputBuffer, Buffer<Package> outputBuffer, ProductDb db) {
        this.inputBuffer = inputBuffer;
        this.outputBuffer = outputBuffer;
        this.db = db;
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
        Message msg = pack.getMessage();
        String payload = msg.getMessage();
        String result;

        switch (msg.getcType()) {
            case 1: {
                result = db.getByName(payload)
                        .map(p -> "Кількість: " + p.getQuantity())
                        .orElse("Товар не знайдено: " + payload);
                break;
            }
            case 2: {
                String[] parts = payload.split(",", 2);
                String name = parts[0].trim();
                int amount = Integer.parseInt(parts[1].trim());
                result = db.getByName(name).map(p -> {
                    p.setQuantity(Math.max(0, p.getQuantity() - amount));
                    db.update(p);
                    return "Списано. Залишок: " + p.getQuantity();
                }).orElse("Товар не знайдено: " + name);
                break;
            }
            case 3: {
                String[] parts = payload.split(",", 2);
                String name = parts[0].trim();
                int amount = Integer.parseInt(parts[1].trim());
                result = db.getByName(name).map(p -> {
                    p.setQuantity(p.getQuantity() + amount);
                    db.update(p);
                    return "Зараховано. Кількість: " + p.getQuantity();
                }).orElse("Товар не знайдено: " + name);
                break;
            }
            case 4: {
                result = "Групу додано: " + payload;
                break;
            }
            case 5: {
                String[] parts = payload.split(",", 2);
                String name = parts[0].trim();
                String category = parts[1].trim();
                result = db.getByName(name).map(p -> {
                    p.setCategory(category);
                    db.update(p);
                    return "Категорію оновлено для: " + name;
                }).orElseGet(() -> {
                    int id = db.insert(new Product(name, category, 0, 0.0));
                    return "Товар створено з id=" + id;
                });
                break;
            }
            case 6: {
                String[] parts = payload.split(",", 2);
                String name = parts[0].trim();
                double price = Double.parseDouble(parts[1].trim());
                result = db.getByName(name).map(p -> {
                    p.setPrice(price);
                    db.update(p);
                    return "Ціну встановлено: " + price;
                }).orElse("Товар не знайдено: " + name);
                break;
            }
            default:
                result = "Невідома команда";
        }

        return new Package(pack.getbSrc(), pack.getbPktId(), new Message(msg.getcType(), msg.getbUserId(), result));
    }

    private String getCommandName(int cType) {
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
