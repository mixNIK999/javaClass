package me.nikolyukin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class LinkedHashMapTest {

    private LinkedHashMap<String, String> hashMap;
    private LinkedHashMap<String, String> smallHashMapWithHugeCollection;

    @BeforeEach
    void initialHashMap() {
        hashMap = new LinkedHashMap<>(5);
        smallHashMapWithHugeCollection = new LinkedHashMap<>(1);
        for (char c = 'a'; c <= 'z'; c++) {
            smallHashMapWithHugeCollection.put(Character.toString(c), Character.toString(c));
        }
    }

    @Test
    void entrySetWhenOne() {
        hashMap.put("a", "a");
        assertEquals(1, hashMap.entrySet().size());
    }

    @Test
    void entrySetWhenMany() {
        assertEquals('z' - 'a' + 1, smallHashMapWithHugeCollection.entrySet().size());
    }

    @Test
    void sizeWhenEmpty() {
        assertEquals(0, hashMap.size());
    }

    @Test
    void sizeWhenAdd1Element() {
        hashMap.put("a", "b");
        assertEquals(1, hashMap.size());
    }

    @Test
    void sizeWhenAdd2Remove1Elements() {
        hashMap.put("a", "b");
        hashMap.put("c", "d");
        hashMap.remove("c");
        assertEquals(1, hashMap.size());
    }

    @Test
    void getWhenEmpty() {
        assertNull(hashMap.get("a"));
    }

    @Test
    void getWhenHasNot() {
        hashMap.put("a", "b");
        assertNull(hashMap.get("c"));
    }

    @Test
    void getWhenHas() {
        hashMap.put("a", "b");
        assertEquals("b",hashMap.get("a"));
    }

    @Test
    void putWhenEmpty() {
        assertNull(hashMap.put("a", "b"));
    }

    @Test
    void putWhenHas() {
        hashMap.put("a", "b");
        assertEquals("b", hashMap.put("a", "c"));
    }

    @Test
    void putWhenHasNot() {
        hashMap.put("a", "b");
        assertNull(hashMap.put("c", "d"));
    }

    @Test
    void putWhenManyCollisions() {
        for (char c = 'a'; c <= 'z'; c++) {
            assertEquals(Character.toString(c), smallHashMapWithHugeCollection.put(Character.toString(c), "newValue"));
        }
    }

    @Test
    void removeWhenEmpty() {
        assertNull(hashMap.remove("a"));
    }

    @Test
    void removeWhenHas() {
        hashMap.put("a", "b");
        assertEquals("b", hashMap.remove("a"));
    }

    @Test
    void removeWhenHasNot() {
        hashMap.put("a", "b");
        assertNull(hashMap.remove("c"));
    }

    @Test
    void removeWhenManyCollisions() {
        for (char c = 'a'; c <= 'z'; c++) {
            assertEquals(Character.toString(c), smallHashMapWithHugeCollection.remove(Character.toString(c)));
        }
    }

    @Test
    void clearWhenEmpty() {
        hashMap.clear();
        assertEquals(0, hashMap.size());
    }

}