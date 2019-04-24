package me.nikolyukin;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
public class FirstApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var button1 = new Button("Click me");
        button1.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button1.setOnAction(value -> Platform.exit());

        var button2 = new Button("Or me");
        button2.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        button2.setOnAction(value -> Platform.exit());

        var pane = new GridPane();
        pane.setGridLinesVisible(true);

        var column1 = new ColumnConstraints();

        column1.setPercentWidth(50);
        var column2 = new ColumnConstraints();
        column2.setPercentWidth(50);

        var row = new RowConstraints();
        row.setPercentHeight(100);
        row.setFillHeight(true);
        pane.getColumnConstraints().addAll(column1, column2);
        pane.getRowConstraints().add(row);
        pane.add(button1, 0, 0);
        pane.add(button2, 1, 0);

        var scene = new Scene(pane, 500, 500);
        primaryStage.setTitle("lol");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    public static void main(String[] args) {
        Application.launch(args);
    }
}