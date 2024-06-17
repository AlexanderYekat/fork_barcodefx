package site.barsukov.barcodefx.controllers;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.services.AggregationService;
import site.barsukov.barcodefx.services.PickingOrderService;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.dto.AggregateMultiCreateDto;
import site.barsukov.barcodefx.services.dto.AggregateMultiCreateValidationResult;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.props.JsonLastStateProps.LastStateProp.*;

@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/aggregate_multi_create.fxml")
public class AggregateMultiCreateController implements Initializable {

    public static final String INVALID_INN = "Некорректный ИНН";
    private final PropService propService;
    private final AggregationService aggregationService;

    @FXML
    private CheckBox ip;
    @FXML
    private CheckBox changeStatusCB;
    @FXML
    private TextField innTF;
    @FXML
    private TextField orgTF;
    @FXML
    private TextField newStatusTF;
    @FXML
    private Label aggregateCount;
    @FXML
    private Label innValidation;

    private List<PickingOrder> pickingOrders;
    private String resultPath;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        innTF.setText(propService.getProp(AGG_MULTI_CREATE_INN));
        orgTF.setText(propService.getProp(AGG_MULTI_CREATE_ORG_NAME));
        newStatusTF.setText(propService.getProp(AGG_MULTI_CREATE_NEW_STATUS));
        changeStatusCB.setSelected(propService.getBooleanProp(AGG_MULTI_CREATE_CHANGE_STATUS));
        newStatusTF.disableProperty().bind(changeStatusCB.selectedProperty().not());

        if (!Validator.isInnValid(innTF.getText())) {
            innValidation.setText(INVALID_INN);
        }

        innTF.focusedProperty().addListener((arg0, oldValue, newValue) -> {
            if (!newValue) {
                if (!Validator.isInnValid(innTF.getText())) {
                    innValidation.setText(INVALID_INN);
                } else {
                    innValidation.setText("");
                }
            }

        });
    }

    public void okButtonAction() {
        if (!innValidation.getText().isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Введен некорректный ИНН").showAndWait();
            return;
        }

        this.resultPath = chooseDirectory();

        if (Objects.isNull(resultPath)) {
            return;
        }

        propService.saveProp(AGG_MULTI_CREATE_INN, innTF.getText());
        propService.saveProp(AGG_MULTI_CREATE_ORG_NAME, orgTF.getText());
        propService.saveProp(AGG_MULTI_CREATE_NEW_STATUS, newStatusTF.getText());
        propService.saveProp(AGG_MULTI_CREATE_CHANGE_STATUS, changeStatusCB.isSelected());
        propService.saveProp(AGG_MULTI_SAVE_DIRECTORY, resultPath);

        var errors = pickingOrders.stream()
            .map(this::createDto)
            .map(aggregationService::createAggregate)
            .filter(AggregateMultiCreateValidationResult::isError)
            .collect(Collectors.toSet());

        if (!errors.isEmpty()) {
            aggregationService.writeErrors(errors, resultPath);
        }

        var message = new StringBuilder()
            .append(String.format("Корректно обработано агрегатов: %s\nОшибок: %s", pickingOrders.size() - errors.size(), errors.size()));

        if (errors.size() > 0) {
            message.append(String.format("\nДетали в %s", resultPath + File.separatorChar + "errors.json"));
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Результат агрегации");
        alert.setHeaderText(message.toString());
        alert.showAndWait();

        var stage = (Stage) changeStatusCB.getScene().getWindow();
        stage.close();
    }

    public void cancelButtonAction() {
        var stage = (Stage) changeStatusCB.getScene().getWindow();
        stage.close();
    }

    public void setPickingOrders(List<PickingOrder> pickingOrders) {
        this.pickingOrders = pickingOrders;
        aggregateCount.setText(String.valueOf(pickingOrders.size()));
    }

    public String chooseDirectory() {
        var directoryChooser = new DirectoryChooser();
        var initialDirectory = new File(propService.getProp(AGG_MULTI_SAVE_DIRECTORY));
        directoryChooser.setInitialDirectory(initialDirectory);
        var stage = (Stage) changeStatusCB.getScene().getWindow();
        var chosenDirectory = directoryChooser.showDialog(stage);

        return Optional.ofNullable(chosenDirectory)
            .map(dir -> dir.getAbsolutePath().toLowerCase(Locale.ROOT))
            .orElse(null);
    }

    private AggregateMultiCreateDto createDto(PickingOrder order) {
        return AggregateMultiCreateDto.builder()
            .fileName(order.getFileName())
            .goodCategory(PickingOrderService.convertTg(order.getProductGroup()))
            .ip(ip.isSelected())
            .inn(innTF.getText())
            .orgName(orgTF.getText())
            .statusChanged(changeStatusCB.isSelected())
            .status(order.getAggregationCode())
            .newStatus(newStatusTF.getText())
            .resultPath(resultPath)
            .build();
    }
}
