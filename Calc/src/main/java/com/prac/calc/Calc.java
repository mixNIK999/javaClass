package com.prac.calc;

import java.util.*;

public class Calc {
    private Stack<Integer> stack;

    public Calc(Stack<Integer> stack) {
        this.stack = stack;
    }

    private boolean isOperation(String s) {
        if (s.length() != 1) {
            return false;
        }
        char op = s.charAt(0);
        return op == '+' || op == '-' || op == '*' || op == '/';
    }   

    public int calculate(String s) {
        for (String current : s.split(" ")) {
            if (isOperation(current)) {
                char op = current.charAt(0);
                int second = stack.pop();
                int first = stack.pop();
                switch (op) {
                    case '+':
                        stack.push(first + second);
                        break;
                    case '-':
                        stack.push(first - second);
                        break;
                    case '*':
                        stack.push(first * second);
                        break;
                    case '/':
                        stack.push(first / second);
                        break;
                        default:
                        throw new UnsupportedOperationException();
                }
            } else {
                stack.push(Integer.parseInt(current));
            }
        }
        return stack.pop();
    }
}
