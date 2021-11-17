module com.gebb.wetell {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.gebb.wetell.client to javafx.controls;
    exports com.gebb.wetell.client;
    exports com.gebb.wetell;
    exports com.gebb.wetell.server;
}