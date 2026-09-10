package com.logistica;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/logistica/views/AppView.fxml"));
        Parent root = loader.load();

        Scene scene = new Scene(root, 1000, 700);
        primaryStage.setTitle("Optimizador de Carga 3D - Furgón");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}