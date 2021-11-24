package com.gebb.wetell.client;

import com.gebb.wetell.Datapacket;
import com.gebb.wetell.KeyPairManager;
import com.gebb.wetell.PacketData;
import com.gebb.wetell.PacketType;
import com.gebb.wetell.client.gui.SceneManager;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.PublicKey;
import java.util.Objects;

public class WeTellClient extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        /* Example code for encryption */
        KeyPair kp = KeyPairManager.generateRSAKeyPair();
        Datapacket datapacket = new Datapacket(kp.getPublic(), PacketType.KEY, KeyPairManager.RSAPublicKeyToByteStream(kp.getPublic()));
        PacketData pd = datapacket.getPacketData(kp.getPrivate());
        if (pd.getType() == PacketType.KEY) {
            PublicKey k = KeyPairManager.byteStreamToRSAPublicKey(pd.getData());
            Datapacket p = new Datapacket(k, PacketType.LOGIN, "NiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNiceNice".getBytes(StandardCharsets.UTF_8));
            System.out.println(new String(p.getPacketData(kp.getPrivate()).getData(), StandardCharsets.UTF_8));
        }
        // Window preparations
        stage.setTitle("WeTell");
        stage.getIcons().add(new Image(Objects.requireNonNull(WeTellClient.class.getResource("gui/icons/wetell.png")).toExternalForm()));

        SceneManager sceneManager = new SceneManager(stage, this);
    }
}
