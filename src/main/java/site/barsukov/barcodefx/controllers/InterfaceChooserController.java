package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.services.PropService;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

import static site.barsukov.barcodefx.utils.Utils.DEFAULT_CLASSIC_INTERFACE;
import static site.barsukov.barcodefx.utils.Utils.DEFAULT_SCANNER_INTERFACE;
import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.DEFAULT_INTERFACE;

@Component
@FxmlView("/fxml/interface_chooser.fxml")
public class InterfaceChooserController implements Initializable {
    public Button openClassicInterfaceButton;
    public Button openScannerInterfaceButton;
    public CheckBox useAsDefault;
    @Autowired
    private ConfigurableApplicationContext applicationContext;
    @Autowired
    private PropService propService;

    final static Logger logger = Logger.getLogger(InterfaceChooserController.class);


    public void openHomePage(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/barcodesfx/"));

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void openClassicInterfaceButtonAction(ActionEvent actionEvent) {
        if (useAsDefault.isSelected()) {
            propService.saveProp(DEFAULT_INTERFACE, DEFAULT_CLASSIC_INTERFACE);
        }
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(Controller.class);
        Stage stage = new Stage();
        stage.setTitle("BarCodesFX-" + getClass().getPackage().getImplementationVersion());
        stage.setScene(new Scene(root));
        Controller controller = fxWeaver.getBean(Controller.class);
        stage.setOnCloseRequest(
                event -> controller.onCloseActions()
        );
        stage.show();
        closeThisStage(actionEvent);
    }


    public void openScannerInterfaceButtonAction(ActionEvent actionEvent) {
        if (useAsDefault.isSelected()) {
            propService.saveProp(DEFAULT_INTERFACE, DEFAULT_SCANNER_INTERFACE);
        }
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(ScannerInterfaceController.class);
        Stage stage = new Stage();
        stage.setTitle("BarCodesFX-" + getClass().getPackage().getImplementationVersion());
        stage.setScene(new Scene(root));
        stage.show();
        closeThisStage(actionEvent);
    }

    private void closeThisStage(ActionEvent actionEvent) {
        Node source = (Node) actionEvent.getSource();
        if (source.getScene() != null) {
            Stage thisStage = (Stage) source.getScene().getWindow();
            thisStage.close();
        }
    }
}
