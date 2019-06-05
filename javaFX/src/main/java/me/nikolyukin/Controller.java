package me.nikolyukin;

import java.util.Arrays;

public class Controller {
    private int size;
    private int[][] opened;
    private int turn = 0;
    private int winner = -1;

    public Controller(int size) {
        this.size = size;
        opened = new int[size][size];
        for (var row : opened) {
            Arrays.fill(row,-1);
        }
    }

    public boolean canOpen(int i, int j) {
        return opened[i][j] == -1;
    }

    public int open(int i, int j) {
        if (!canOpen(i, j)) {
            return -1;
        }
        int player = turn++ % 2;
        opened[i][j] = player;
        return player;
    }

    public boolean isEnd() {
        return winner != -1;
    }

    public int whoWin() {
        return winner;
    }

    private boolean checkEnd() {
        if (isEnd()) {
            return true;
        }

        return checkWinRows()|| checkWinColumns() ||
            checkWinDiagonalZeroSize() || checkWinDiagonalZeroZero();


    }

    private boolean checkWinRows() {
        for (int i = 0; i < size; i++) {
            int player = opened[i][0];
            boolean isWin = player != -1;
            for (int j = 0; j < size; j++) {
                isWin &= opened[i][j] == player;
            }
            if (isWin) {
                setWinner(player);
                return true;
            }
        }
        return false;
    }

    private boolean checkWinColumns() {
        for (int j = 0; j < size; j++) {
            int player = opened[0][j];
            boolean isWin = player != -1;
            for (int i = 0; i < size; i++) {
                isWin &= opened[i][j] == player;
            }
            if (isWin) {
                setWinner(player);
                return true;
            }
        }
        return false;
    }

    private boolean checkWinDiagonalZeroZero() {
        int player = opened[0][0];
        boolean isWin = player != -1;
        for (int i = 0; i < size; i++) {
            isWin &= opened[i][i] == player;
        }
        if (isWin) {
            setWinner(player);
            return true;
        }
        return false;
    }

    private boolean checkWinDiagonalZeroSize() {
        int player = opened[0][size - 1];
        boolean isWin = player != -1;
        for (int i = 0; i < size; i++) {
            isWin &= opened[i][size - i - 1] == player;
        }
        if (isWin) {
            setWinner(player);
            return true;
        }
        return false;
    }

    private void setWinner(int winner) {
        this.winner = winner;
    }
}
