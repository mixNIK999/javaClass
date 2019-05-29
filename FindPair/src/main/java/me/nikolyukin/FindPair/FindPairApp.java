package me.nikolyukin.FindPair;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class FindPairApp  extends Application {
    private NumberButton prevButton;
    private volatile boolean pause = false;

    private int score = 0;
    private int scoreToWin;
    private Stage stage;

    @Override
    public void start(Stage primaryStage) {

        this.stage = primaryStage;
        var pane = new GridPane();
        pane.setGridLinesVisible(true);

//        var
        int n = 2;
        scoreToWin = n * n / 2;
        List<Integer> numbers = new ArrayList<>(n * n);

        for (int i = 0; i < n * n; i++) {
            numbers.add(i/2);
        }

        Collections.shuffle(numbers);

        for (int i = 0; i < n; i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(100.0/n);
            pane.getColumnConstraints().add(column);

            var row = new RowConstraints();
            row.setPercentHeight(100.0/n);
            pane.getRowConstraints().add(row);

            for (int j = 0; j < n; j++) {
                pane.add(new NumberButton(numbers.get(i * n + j)), i, j);
            }
        }

        var scene = new Scene(pane, 500, 500);
        primaryStage.setTitle("Find Pair");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }

    private void incrementScore() {
        ++score;
        if (score == scoreToWin) {
            stage.setTitle("You win!!!!");
        }
    }
    private class NumberButton extends Button {
        private boolean isActive = false;
        private int number;
        private int pauseSeconds = 1;

        private NumberButton(int number) {
            this.number = number;
            hide();
            setText(String.valueOf(number)) ;
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setOnAction(value -> {
                if(!pause && !isActive) {
                    isActive = true;
                    if (prevButton == null) {
                        choose();
                        prevButton = this;
                        return;
                    }

                    NumberButton pairButton = prevButton;
                    if (pairButton.number == number) {
                        accept();
                        pairButton.accept();
                        FindPairApp.this.incrementScore();
                    } else {
                        reject();
                        pairButton.reject();
                        pause = true;

                        var timer = new Timer();
                        var task = new TimerTask() {
                            @Override
                            public void run() {
                                NumberButton.this.hide();
                                pairButton.hide();
                                pause = false;
                                timer.cancel();
                            }
                        };
                        timer.schedule(task, TimeUnit.SECONDS.toMillis(pauseSeconds));
                    }
                    prevButton = null;
                }
            });
        }

        void hide() {
            setStyle("-fx-text-fill: transparent");
            isActive = false;
        }

        void accept() {
            setStyle("-fx-text-fill: green");
            isActive = true;
        }

        void reject() {
            setStyle("-fx-text-fill: red");
            isActive = true;
        }

        void choose() {
            setStyle("-fx-text-fill: blue");
            isActive = true;
        }
    }
}

