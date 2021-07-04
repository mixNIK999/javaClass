package me.nikolyukin;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

public class FirstApp extends Application {
    private int n = 3;
    private Controller controller = new Controller(n);

    @Override
    public void start(Stage primaryStage) {

        var pane = new GridPane();
        pane.setGridLinesVisible(true);

        for (int i = 0; i < n; i++) {
            var column = new ColumnConstraints();
            column.setPercentWidth(100.0/n);
            pane.getColumnConstraints().add(column);

            var row = new RowConstraints();
            row.setPercentHeight(100.0/n);
            pane.getRowConstraints().add(row);

            for (int j = 0; j < n; j++) {
                pane.add(new XOButton(i, j), i, j);
            }
        }

        var scene = new Scene(pane, 300, 300);
        primaryStage.setTitle("X vs O");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }

    private class XOButton extends Button {
        private final String[] text = {"X", "0"};
        private final int i, j;

        private XOButton(int i, int j) {
            this.i = i;
            this.j = j;
            setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            setOnAction(value -> {
                int player = controller.open(i, j);
                if (player != -1) {
                    setText(text[player]);
                }
            });
        }

    }

}