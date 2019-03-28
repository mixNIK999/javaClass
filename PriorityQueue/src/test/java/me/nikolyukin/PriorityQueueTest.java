package me.nikolyukin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class PriorityQueueTest {
    private PriorityQueue<Integer> emptyIntegerPriorityQueue;
    private PriorityQueue<String> stringsPriorityQueue;
    private PriorityQueue<Map.Entry<Integer, Integer>>  pairPriorityQueue;

    @BeforeEach
    void init() {
        emptyIntegerPriorityQueue = new PriorityQueue<>();
        stringsPriorityQueue = new PriorityQueue<>();
        stringsPriorityQueue.addAll(Arrays.asList("a", "c", "b", "b"));
        pairPriorityQueue = new PriorityQueue<>(Comparator.comparingInt(Map.Entry::getValue));
        var tmpMap = new HashMap<Integer, Integer>();
        tmpMap.put(0, 2);
        tmpMap.put(1, 1);
        tmpMap.put(2, 1);
        tmpMap.put(3, 1);
        tmpMap.put(-1, 2);
        pairPriorityQueue.addAll(tmpMap.entrySet());
    }

    @Test
    void iteratorFromEmpty() {
        Iterator iterator = emptyIntegerPriorityQueue.iterator();
        assertFalse(iterator.hasNext());
    }

    @Test
    void iteratorFromNotEmptyNatural() {
        Iterator iterator = stringsPriorityQueue.iterator();
        assertEquals("a", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("b", iterator.next());
        assertEquals("c", iterator.next());
    }

    @Test
    void iteratorFromNotEmptyNotNatural() {
        Iterator<Map.Entry<Integer, Integer>> iterator = pairPriorityQueue.iterator();
        assertEquals(Integer.valueOf(1), iterator.next().getKey());
        assertEquals(Integer.valueOf(2), iterator.next().getKey());
        assertEquals(Integer.valueOf(3), iterator.next().getKey());
        assertEquals(Integer.valueOf(0), iterator.next().getKey());
        assertEquals(Integer.valueOf(-1), iterator.next().getKey());
    }


    @Test
    void sizeFromEmpty() {
        assertEquals(0, emptyIntegerPriorityQueue.size());
    }

    @Test
    void sizeFromNotEmpty() {
        assertEquals(5, pairPriorityQueue.size());
    }

    @Test
    void offerTrue() {
        assertTrue(emptyIntegerPriorityQueue.offer(0));
        assertEquals(1, emptyIntegerPriorityQueue.size());
    }

    @Test
    void offerFalse() {
        assertFalse(emptyIntegerPriorityQueue.offer(null));
        assertEquals(0, emptyIntegerPriorityQueue.size());
    }

    @Test
    void pollFromEmpty() {
        assertNull(emptyIntegerPriorityQueue.poll());
        assertEquals(0, emptyIntegerPriorityQueue.size());
    }

    @Test
    void pollFromNotEmpty() {
        assertEquals("a",stringsPriorityQueue.poll());
        assertEquals(3, stringsPriorityQueue.size());
    }

    @Test
    void peekFromEmpty() {
        assertNull(emptyIntegerPriorityQueue.peek());
    }

    @Test
    void peekFromNotEmpty() {
        assertEquals("a",stringsPriorityQueue.peek());
        assertEquals(4, stringsPriorityQueue.size());
    }
}