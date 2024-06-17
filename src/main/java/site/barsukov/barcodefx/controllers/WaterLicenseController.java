package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.codehaus.plexus.util.StringUtils;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.production.WaterLicense;

import java.net.URL;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Set;

import static site.barsukov.barcodefx.utils.Utils.getLocalDate;

@Component
@RequiredArgsConstructor
@FxmlView("/fxml/water_licenses.fxml")
public class WaterLicenseController implements Initializable {

    public TextField licenseNumber;
    public DatePicker licenseDate;
    public TableView<WaterLicense> licensesList;
    private List<WaterLicense> waterLicenses;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        licensesList.getColumns().get(0).setCellValueFactory(new PropertyValueFactory<>("number"));
        licensesList.getColumns().get(1).setCellValueFactory(new PropertyValueFactory<>("date"));
    }

    public void setWaterLicenses(List<WaterLicense> waterLicenses) {
        this.waterLicenses = waterLicenses;
        updateLicensesView();
    }

    public void addLicenseButtonAction(ActionEvent actionEvent) {
        var number = licenseNumber.getText();
        LocalDate date = getLocalDate(licenseDate);
        if (StringUtils.isBlank(number) || date == null) {
            new Alert(Alert.AlertType.ERROR, "Не заполнен номер лицензии или дата выдачи").showAndWait();
            return;
        }
        var license = new WaterLicense(number, date);
        if (!waterLicenses.contains(license)) {
            waterLicenses.add(license);
        }
        updateLicensesView();
    }

    public void deleteLicenseButtonAction(ActionEvent actionEvent) {
        var selected = licensesList.getSelectionModel().getSelectedItems();
        if (selected == null || selected.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Не выбраны лицензии для удаления").showAndWait();
            return;
        }
        waterLicenses.removeAll(selected);
        updateLicensesView();
    }

    private void updateLicensesView() {
        licensesList.getItems().clear();
        licensesList.getItems().addAll(waterLicenses);
    }
}
