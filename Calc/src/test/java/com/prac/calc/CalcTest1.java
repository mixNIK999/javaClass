package com.prac.calc;

import org.junit.jupiter.api.*;

import java.security.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CalcTest {

    @Test
    void calculateSummTest() {
        Stack<Integer> k = mock(Stack.class);
        var kek = new Calc(k);
        when(k.pop()).thenReturn(-1).thenReturn(2).thenReturn(-2);
        when(k.size()).thenReturn(1);
        assertEquals(-2, kek.calculate("-1 2 *"));
    }

    @Test
    void calculateSimpleTest() {
        Stack<Integer> k = mock(Stack.class);
        var kek = new Calc(k);
        when(k.pop()).thenReturn(0);
        when(k.size()).thenReturn(1);
        assertEquals(0, kek.calculate("1 1 *"));
    }

    @Test
    void calculateThrowsSizeTest() {
        Stack<Integer> k = mock(Stack.class);
        var kek = new Calc(k);
        when(k.pop()).thenReturn(-1).thenReturn(2).thenReturn(-2);
        when(k.size()).thenReturn(10);
        assertThrows(InvalidParameterException.class, () -> {kek.calculate("-1 2 *");});
    }

    @Test
    void calculateThrowsOperationTest() {
        Stack<Integer> k = mock(Stack.class);
        var kek = new Calc(k);
        when(k.pop()).thenReturn(-1).thenReturn(2).thenReturn(-2);
        when(k.size()).thenReturn(10);
        assertThrows(NumberFormatException.class, () -> {kek.calculate("-1 2 !");});
    }

    @Test
    void calculateSimpleOrderTest() {
        Stack<Integer> k = mock(Stack.class);
        var kek = new Calc(k);
        when(k.pop()).thenReturn(2).thenReturn(1);
        verify(k).push(eq(0));
        when(k.size()).thenReturn(1);
        assertEquals(0, kek.calculate("1 2 /"));
    }

}