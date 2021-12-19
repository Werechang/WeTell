package com.gebb.wetell;

import com.gebb.wetell.client.IGUICallable;
import com.gebb.wetell.client.WeTellClient;
import com.gebb.wetell.client.gui.SceneManager;
import com.gebb.wetell.client.gui.SceneType;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.Objects;

public class GUITest extends Application implements IGUICallable {

    private SceneManager sceneManager;

    @Override
    public void start(Stage stage) {
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        sceneManager = new SceneManager(stage, this, true);
    }

    public static void initialize() {
        launch();
    }

    @Override
    public void prepareClose() {
        // Do nothing
    }

    @Override
    public void onLoginPress(String username, String password) {
        sceneManager.setScene(SceneType.MESSAGE);
    }

    @Override
    public void onSignInPress(String username, String password) {
        sceneManager.setScene(SceneType.MESSAGE);
    }

    @Override
    public void onLogoutPress() {
        sceneManager.setScene(SceneType.LOGIN);
    }

    @Override
    public void onSelectChat(int chatId) {

    }

    @Override
    public void onSendMessage(String content) {

    }

    @Override
    public void onAddChat(String chatName) {

    }

    @Override
    public void onAddUserToChat(int chatId, int userId) {

    }

    @Override
    public void backtoMessagePane() {

    }
}
