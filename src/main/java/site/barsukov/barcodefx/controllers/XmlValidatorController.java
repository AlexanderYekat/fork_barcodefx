package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.StatisticService;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/validator.fxml")
public class XmlValidatorController implements Initializable {

    private final PropService propService;
    private final StatisticService statisticService;

    public TextArea texAreaAbout;
    public Label fileNameLabel;
    public Button validateFileButton;

    public void openHomePage(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/barcodesfx/"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        validateFileButton.setDisable(true);
    }


    public void validateFileButtonAction(ActionEvent actionEvent) {
        texAreaAbout.clear();
        texAreaAbout.setText(Validator.validateXml(new File(fileNameLabel.getText())));
        statisticService.logAction("XML_VALIDATION", 0, null);
    }

    public void chooseFileButtonAction(ActionEvent actionEvent) {
        File csvFile;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл для валидации");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("xml", "*.xml"));
        csvFile = fileChooser.showOpenDialog(new Stage());


        if (csvFile != null) {
            fileNameLabel.setText(csvFile.getAbsolutePath());
            validateFileButton.setDisable(false);
        }
    }
}
