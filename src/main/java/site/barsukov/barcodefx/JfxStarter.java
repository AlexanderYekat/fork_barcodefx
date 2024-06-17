package site.barsukov.barcodefx;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import site.barsukov.barcodefx.controllers.Controller;
import site.barsukov.barcodefx.controllers.InterfaceChooserController;
import site.barsukov.barcodefx.controllers.ScannerInterfaceController;
import site.barsukov.barcodefx.services.PropService;

import static site.barsukov.barcodefx.utils.Utils.DEFAULT_CLASSIC_INTERFACE;
import static site.barsukov.barcodefx.utils.Utils.DEFAULT_SCANNER_INTERFACE;
import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.DEFAULT_INTERFACE;

public class JfxStarter extends Application {
    private ConfigurableApplicationContext applicationContext;

    @Override
    public void init() {
        String[] args = getParameters().getRaw().toArray(new String[0]);
        this.applicationContext = new SpringApplicationBuilder()
            .sources(Main.class)
            .headless(false)
            .run(args);
    }

    @Override
    public void start(Stage primaryStage) {
        PropService propService = applicationContext.getBean(PropService.class);
        String defaultInterface = propService.getProp(DEFAULT_INTERFACE);
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        primaryStage.setTitle("BarCodesFX-" + getClass().getPackage().getImplementationVersion());
        switch (defaultInterface) {
            case DEFAULT_CLASSIC_INTERFACE:
                Parent rootClassic = fxWeaver.loadView(Controller.class);
                primaryStage.setScene(new Scene(rootClassic));
                Controller controller = fxWeaver.getBean(Controller.class);
                primaryStage.setOnCloseRequest(
                        event -> controller.onCloseActions()
                );
                break;
            case DEFAULT_SCANNER_INTERFACE:
                Parent rootScanner = fxWeaver.loadView(ScannerInterfaceController.class);
                primaryStage.setScene(new Scene(rootScanner));
                break;
            default:
                Parent rootChooser = fxWeaver.loadView(InterfaceChooserController.class);
                primaryStage.setScene(new Scene(rootChooser));
                primaryStage.setResizable(false);
                break;
        }
        primaryStage.show();
    }

    @Override
    public void stop() {
        this.applicationContext.close();
        Platform.exit();
        System.exit(0);
    }
}
