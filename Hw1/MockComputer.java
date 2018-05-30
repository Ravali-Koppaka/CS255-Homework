import java.util.Random;

public class MockComputer {

    private int id;

    public int round(int roundNumber) {
        Random random_generator = new Random();
        int boundValue = (int)Math.pow(roundNumber,3);
        id = random_generator.nextInt(boundValue);
        return id;
    }

    public int getId() {
        return id;
    }
}
