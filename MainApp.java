import application.port.InmuebleUseCase;
import infrastructure.config.AppConfig;
import infrastructure.ui.InmuebleController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de entrada de la aplicación JavaFX.
 * <p>Ensambla el servicio mediante {@link infrastructure.config.AppConfig}, carga la vista
 * FXML y se la inyecta al controlador.</p>
 */
public class MainApp extends Application {

    /**
     * Carga la vista y muestra la ventana principal.
     *
     * @param primaryStage ventana principal
     * @throws Exception si falla la carga del FXML o la conexión a la base de datos
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        InmuebleUseCase inmuebleUseCase = AppConfig.getInmuebleService();

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/inmueble-view.fxml"));
        Parent root = loader.load();

        InmuebleController controller = loader.getController();
        controller.setInmuebleUseCase(inmuebleUseCase);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Gestión de Inmuebles - Arquitectura Hexagonal");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(950);
        primaryStage.setMinHeight(620);
        primaryStage.show();
    }

    /**
     * @param args argumentos de línea de comandos (no se usan)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
