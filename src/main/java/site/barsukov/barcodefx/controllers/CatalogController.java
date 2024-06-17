package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.props.JsonAppProps;
import site.barsukov.barcodefx.props.JsonCatalogProps;
import site.barsukov.barcodefx.services.CatalogService;
import site.barsukov.barcodefx.services.PropService;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import static site.barsukov.barcodefx.props.JsonCatalogProps.CatalogProp.CATALOG_ENABLED;
import static site.barsukov.barcodefx.props.JsonCatalogProps.CatalogProp.FILE_PATH;

@Component
@RequiredArgsConstructor
@FxmlView("/fxml/catalog_properties.fxml")
public class CatalogController implements Initializable {

    private final PropService propService;
    private final CatalogService catalogService;

    public Label fileLabel;
    public CheckBox useCatalog;

    public void chooseFileButtonAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл выгрузки ГС1");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Файл выгрузки", "*.xlsx"));
        File csvFile = fileChooser.showOpenDialog(new Stage());
        fileLabel.setText(csvFile.getAbsolutePath());
    }

    public void okButtonAction(ActionEvent actionEvent) {
        propService.saveProp(FILE_PATH, fileLabel.getText());
        propService.saveProp(CATALOG_ENABLED, useCatalog.isSelected());
        new Thread(catalogService::reloadData).start();
        Stage stage = (Stage) fileLabel.getScene().getWindow();
        stage.close();
    }

    public void cancelButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) fileLabel.getScene().getWindow();
        stage.close();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        useCatalog.setSelected(propService.getBooleanProp(CATALOG_ENABLED));
        fileLabel.setText(propService.getProp(FILE_PATH));
    }
}
