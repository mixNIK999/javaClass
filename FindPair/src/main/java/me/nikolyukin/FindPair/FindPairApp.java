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

        List<String> args = getParameters().getUnnamed();

        int n = Integer.parseInt(args.get(0));
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
        if (isWin()) {
            primaryStage.setTitle("You win!!!!");
        } else {
            primaryStage.setTitle("Find Pair");
        }
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Needs one natural integer argument");
            return;
        }

        try {
            if (Integer.parseInt(args[0]) <= 0) {
                System.out.println("Argument is not natural integer");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Argument is not integer");
            return;
        }


        Application.launch(args);
    }

    private void incrementScore() {
        ++score;
        if (isWin()) {
            stage.setTitle("You win!!!!");
        }
    }

    private boolean isWin() {
        return score == scoreToWin;
    }
    private class NumberButton extends Button {
        private boolean isDisabled = false;
        private int number;
        private int pauseSeconds = 1;

        private NumberButton(int number) {
            this.number = number;
            hide();
            setText(String.valueOf(number)) ;
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setOnAction(value -> {
                if(!pause && !isDisabled) {
                    isDisabled = true;
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
            isDisabled = false;
        }

        void accept() {
            setStyle("-fx-text-fill: green");
            isDisabled = true;
            setDisabled(true);
        }

        void reject() {
            setStyle("-fx-text-fill: red");
            isDisabled = true;
        }

        void choose() {
            setStyle("-fx-background-color: gray; -fx-text-fill: transparent");
            isDisabled = true;
        }
    }
}

