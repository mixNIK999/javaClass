

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

public class Parking {
    private final AtomicInteger counter;
    private int max;

    private final IntBinaryOperator addWithMax = ((a, b) -> Integer.min(a + b, max));
    private final IntBinaryOperator subWithMin = ((a, b) -> Integer.max(a - b, 0));

    public Parking(int max) {
        this.max = max;
        counter = new AtomicInteger(0);
    }

    public boolean enter() {
        return counter.getAndAccumulate(1, addWithMax) != max;

    }

    public boolean exit() {
        return counter.getAndAccumulate(1, subWithMin) != 0;
    }
}
