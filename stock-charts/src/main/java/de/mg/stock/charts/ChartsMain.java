package de.mg.stock.charts;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

public class ChartsMain extends Application {

    static final String BASIC_AUTH_LOGIN = "stock";
    static final String BASIC_AUTH_PASSWORD = "janzJeheim";

    public static String serverUrl;

    public static void main(String[] args) {

        if (args.length == 0) {
            usage();
            serverUrl = "https://localhost:8181/stock-server-1.0-SNAPSHOT/rest/";
        } else if (args.length == 1) {
            serverUrl = args[0];
        } else {
            usage();
            System.exit(-1);
        }
        System.out.println("serverUrl= " + serverUrl);

        launch(args);
    }

    private static void usage() {
        System.out.println("Usage: java -jar stock.jar http://host...");
    }

    @Override
    public void start(Stage stage) {

        Thread.setDefaultUncaughtExceptionHandler(ChartsMain::showError);

        String fxmlFile = "/fxml/charts.fxml";
        FXMLLoader loader = new FXMLLoader();
        Parent rootNode = null;
        try {
            rootNode = loader.load(getClass().getResourceAsStream(fxmlFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        Scene scene = new Scene(rootNode);

        stage.setTitle("Stock Charts");
        stage.setResizable(true);
        stage.setScene(scene);

        // does not work:
        //VBox chartsContainer = (VBox) rootNode.lookup("#chartsContainer");
        VBox chartsContainer = (VBox) ((ScrollPane) rootNode.getChildrenUnmodifiable().get(1)).getContent();

        Label statusLeft = (Label) scene.lookup("#statusLeft");
        Label statusRight = (Label) scene.lookup("#statusRight");
        ChartsUpdater.INSTANCE.initialize(chartsContainer, statusLeft, statusRight);

        stage.show();

        WorkerThread worker = new WorkerThread();
        worker.start();
    }

    private static void showError(Thread thread, Throwable catched) {
        catched.printStackTrace(System.err);
        if (Platform.isFxApplicationThread()) {
            showErrorDialog(catched);
        }
    }

    private static void showErrorDialog(Throwable catched) {
        StringWriter errorMsg = new StringWriter();
        catched.printStackTrace(new PrintWriter(errorMsg));

        String fxmlFile = "/fxml/error.fxml";
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setTitle("exception occured");
        FXMLLoader loader = new FXMLLoader();
        try {
            Parent root = loader.load(ChartsMain.class.getResourceAsStream(fxmlFile));
            ((ErrorController) loader.getController()).setErrorText(errorMsg.toString());
            dialog.setScene(new Scene(root, 500, 400));
            dialog.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
