package com.prac.calc;

import java.util.*;

public class Calc {
    private Stack<int> stack;

    public Cacl(Stack<Character> stack) {
        this.stack = stack;
    }

    public int calculate(String s) {
        int currentNum = 0;
        for (String c : s.split(" ")) {
            if (c.length() == 1) {
                char op = c.charAt(0);
                if (op == '+') {
                    int first = stack.pop();
                    int second = stack.pop();
                    stack.push(first + second);
                } else if (op == '-') {
                    int
                }
            } else {
                currentNum = Integer.parseInt(c);
                stack.push(currentNum);
            }
        }
    }
}
