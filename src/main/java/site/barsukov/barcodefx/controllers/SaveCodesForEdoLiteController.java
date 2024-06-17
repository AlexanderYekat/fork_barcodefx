package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.services.EdoLiteService;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

@Slf4j
@RequiredArgsConstructor
@Component
@FxmlView("/fxml/save_codes_for_edo_lite.fxml")
public class SaveCodesForEdoLiteController implements Initializable {
    private final EdoLiteService edoLiteService;
    private final ToggleGroup rbGroup = new ToggleGroup();
    public TextField fileNameTf;
    public RadioButton kmRb;
    public RadioButton aggregateRb;
    public TextField manualGroupingTf;
    public CheckBox groupByGtin;
    public TextField firstLineNumber;
    private List<PickingOrder> pickingOrders;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            pickingOrders = (List<PickingOrder>) resources.getObject(null);
        } catch (MissingResourceException e) {
            //ignore
        }
        kmRb.setToggleGroup(rbGroup);
        aggregateRb.setToggleGroup(rbGroup);
        kmRb.setSelected(true);
        manualGroupingTf.setDisable(true);
        rbGroup.getSelectedToggle().selectedProperty().addListener((observable, oldValue, newValue) -> {
            disableAll();
            if (kmRb.isSelected()) {
                groupByGtin.setDisable(false);
            } else if (aggregateRb.isSelected()) {
                manualGroupingTf.setDisable(false);
            }
        });
    }

    private void disableAll() {
        manualGroupingTf.setDisable(true);
        groupByGtin.setDisable(true);
    }

    public void okButtonAction(ActionEvent actionEvent) {
        if (StringUtils.isBlank(fileNameTf.getText())) {
            new Alert(Alert.AlertType.ERROR, "Не заполнено имя файла").showAndWait();
            return;
        }
        Stage stage = (Stage) fileNameTf.getScene().getWindow();
        try {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку для сохранения");
            File directory = directoryChooser.showDialog(new Stage());
            if (directory == null) {
                return;
            }

            if (kmRb.isSelected() && !groupByGtin.isSelected()) {
                edoLiteService.saveKmToFileNoGrouping(fileNameTf.getText(), directory, pickingOrders, getFirstLineNumber());
            } else if (kmRb.isSelected()) {
                edoLiteService.saveKmToFileGtinGrouping(fileNameTf.getText(), directory, pickingOrders, getFirstLineNumber());
            } else if (aggregateRb.isSelected()) {
                edoLiteService.saveAggregatesToFileWithGrouping(fileNameTf.getText(), directory, pickingOrders, manualGroupingTf.getText(), getFirstLineNumber());
            }
            new Alert(Alert.AlertType.INFORMATION, "Коды успешно выгружены").showAndWait();
            stage.close();
        } catch (Exception e) {
            log.error("Error while saving codes for edo lite ", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка при сохранении кодов для ЭДО лайт " + e.getMessage()).showAndWait();
        }
    }

    private int getFirstLineNumber() {
        try {
            return Integer.parseInt(firstLineNumber.getText());
        } catch (Exception e) {
            log.error("Error getFirstLineNumber {}", firstLineNumber.getText());
            return 1;
        }
    }

}
