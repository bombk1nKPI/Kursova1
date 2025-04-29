package com.bombk1n.coursework1;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class MainApplication extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/bombk1n/coursework1/MainView.fxml"));
        AnchorPane root = loader.load();
        primaryStage.setScene(new Scene(root));
        primaryStage.setTitle("Vertex Cover Solver");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}