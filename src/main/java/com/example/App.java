package com.example;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("JavaFX App");
        stage.setScene(new Scene(new Label("Hello JavaFX"), 320, 200));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
