package me.nikolyukin;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

/**
 * lock-free парковка.
 * Позволяет пускать машины, если есть место, и регестрировать уезжающие машины.
 */
public class Parking {
    private final AtomicInteger counter;
    private final int max;
    private final int min;

    private final IntBinaryOperator addWithMax;
    private final IntBinaryOperator subWithMin;

    /**
     * Конструктор создает парковку с заданным максимальным количеством мест.
     * @param max максимальное количество мест >= 0.
     * @throws IllegalArgumentException если max < 0.
     */
    public Parking(int max) {
        if (max < 0) {
            throw new IllegalArgumentException("max < 0");
        }
        this.max = max;
        min = 0;
        addWithMax = ((a, b) -> Integer.min(a + b, max));
        subWithMin = ((a, b) -> Integer.max(a - b, min));
        counter = new AtomicInteger(min);
    }

    /**
     * Метод сообщает машине при въезде, есть ли свободные места.
     *
     * @return true, если количество машин на парковке увеличилось.
     */
    public boolean enter() {
        return counter.getAndAccumulate(1, addWithMax) != max;
    }

    /**
     * Метод регистрирует уезжающие машины.
     *
     * @return true, если количество машин на парковке уменьшилось.
     */
    public boolean exit() {
        return counter.getAndAccumulate(1, subWithMin) != min;
    }
}
