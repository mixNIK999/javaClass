package me.nikolyukin;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import me.nikolyukin.Parking;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ParkingTest {

    private Parking parking;
    private final int max = 20;
    private ExecutorService pool;

    @BeforeEach
    void init() {
        parking = new Parking(max);
        pool = Executors.newFixedThreadPool(4);
    }

    @AfterEach
    void shutdown() {
        pool.shutdown();
    }

    @Test
    void enterOne() {
        assertTrue(parking.enter());
    }

    @Test
    void enterMoreThanMax() {
        for (int i = 0; i < max; i++) {
            assertTrue(parking.enter());
        }
        assertFalse(parking.enter());
    }

    @Test
    void enterMoreThanMaxExitAndEnter() {
        for (int i = 0; i < max; i++) {
            assertTrue(parking.enter());
        }
        assertFalse(parking.enter());
        assertTrue(parking.exit());
        assertTrue(parking.enter());
    }

    @Test
    void ExitMoreThenEnter() {
        for (int i = 0; i < max; i++) {
            assertTrue(parking.enter());
        }
        assertFalse(parking.enter());
        for (int i = 0; i < max; i++) {
            assertTrue(parking.exit());
        }
        assertFalse(parking.exit());
    }

    @Test
    void multiTreadEnter() {
        var car = new Callable<Boolean> (){

            @Override
            public Boolean call() {
                return parking.enter();
            }
        };

        var futures = new ArrayList<Future<Boolean>>();
        int cntFalse = max + 1;
        for (int i = 0; i < max + cntFalse; i++) {
            futures.add(pool.submit(car));
        }

        assertEquals(cntFalse, futures.stream().filter(f -> {
            try {
                return !f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).count());
    }

    @Test
    void multiTreadEnterAndExit() {
        var car = new Callable<Boolean> (){

            @Override
            public Boolean call() {
                parking.enter();
                return parking.exit();
            }
        };

        var futures = new ArrayList<Future<Boolean>>();

        for (int i = 0; i < max; i++) {
            futures.add(pool.submit(car));
        }

        assertEquals(max, futures.stream().filter(f -> {
            try {
                return f.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }).count());
    }
}