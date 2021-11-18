package com.gebb.wetell.client;

import com.gebb.wetell.client.gui.LoginScene;
import javafx.application.Application;
import javafx.stage.Stage;

public class WeTellClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        LoginScene login = new LoginScene();
        login.setAsScene(stage);
        stage.show();
    }
}
