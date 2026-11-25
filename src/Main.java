import gui.SimulatorView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        SimulatorView root = new SimulatorView();
        Scene scene = new Scene(root, 1000, 620);
        primaryStage.setTitle("Schedulify");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}