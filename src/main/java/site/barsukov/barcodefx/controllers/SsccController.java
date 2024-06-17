package site.barsukov.barcodefx.controllers;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.SsccService;
import site.barsukov.barcodefx.services.StatisticService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;


@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/sscc.fxml")
public class SsccController implements Initializable {
    private final PropService propService;
    private final SsccService ssccService;
    private final StatisticService statisticService;
    public TextField prefix;
    public TextField serialStart;
    public Label lastGeneratedSerialLabel;
    public TextField numberOfLabels;
    public ComboBox<Integer> extension;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        prefix.setText(ssccService.getPrefix());
        var lastSerialString = ssccService.getLastSerial();
        lastGeneratedSerialLabel.setText(lastSerialString);
        int lastSerial = Integer.parseInt(lastSerialString);
        serialStart.setText(Integer.toString(lastSerial + 1));
        numberOfLabels.setText("1");
        ArrayList<Integer> optionsList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            optionsList.add(i);
        }
        ObservableList<Integer> options = FXCollections.observableArrayList(optionsList);

        extension.setItems(options);
        extension.getSelectionModel().select(ssccService.getLastExtension());
    }


    public void generateButtonAction(ActionEvent actionEvent) throws IOException {
        try {
            generateSscc();
            statisticService.logAction("SSCC_GENERATED", Long.parseLong(numberOfLabels.getText()), null);
        } catch (IllegalArgumentException ex) {
            log.error("Error", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        } catch (Exception e) {
            log.error("Error", e);
            new Alert(Alert.AlertType.ERROR, "Unexpected exception: " + e.getMessage()).showAndWait();
        }
    }

    private void generateSscc() throws IOException {

        // get a handle to the stage
        Stage stage = (Stage) lastGeneratedSerialLabel.getScene().getWindow();
        // do what you have to do
        FileChooser fileChooser = new FileChooser();

        //Set extension filter for text files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("TXT files (*.txt)", "*.txt");
        fileChooser.getExtensionFilters().add(extFilter);

        //Show save file dialog
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            FileUtils.writeLines(file, ssccService.generateCodes(
                    Integer.parseInt(serialStart.getText()),
                    Integer.parseInt(numberOfLabels.getText()),
                    Integer.toString(extension.getSelectionModel().getSelectedItem()),
                    prefix.getText()

            ));
            updateProps();
            lastGeneratedSerialLabel.getScene().setUserData(file);
            stage.close();
        }
    }

    private void updateProps() {
        ssccService.saveProps(Integer.toString(extension.getSelectionModel().getSelectedIndex()),
                prefix.getText(),
                Integer.toString(Integer.parseInt(serialStart.getText()) + Integer.parseInt(numberOfLabels.getText()) - 1));
    }


}
