package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.model.printing.ProfileType;
import site.barsukov.barcodefx.model.yo.pickingorder.CodesTypeType;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.repository.PrintingProfileRepo;
import site.barsukov.barcodefx.services.PickingOrderService;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.SsccService;
import site.barsukov.barcodefx.services.StatisticService;

import java.io.File;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.stream.Stream;

import static site.barsukov.barcodefx.model.yo.pickingorder.CodesTypeType.*;
import static site.barsukov.barcodefx.props.JsonLastStateProps.LastStateProp.*;
import static site.barsukov.barcodefx.services.PickingOrderService.convertTg;
import static site.barsukov.barcodefx.utils.Utils.getXMLGregorianCalendarNow;

@Slf4j
@RequiredArgsConstructor
@Component
@FxmlView("/fxml/picking_order_add.fxml")
public class PickingOrderAddController implements Initializable {
    private final PropService propService;
    private final SsccService ssccService;
    private final PrintingProfileRepo printingProfileRepo;
    private final StatisticService statisticService;
    private final ToggleGroup rbGroup = new ToggleGroup();
    public TextField fileNameTf;
    public TextField statusTf;
    public TextField commentTf;
    public ComboBox<GoodCategory> goodCategoryCb;
    public RadioButton kmRb;
    public RadioButton kiRb;
    public RadioButton aggregateRb;
    public TextField aggregationCodeTextField;
    public ComboBox<String> printingProfileComboBox;
    public CheckBox printComboBox;
    private File sourceFile;
    private PickingOrder pickingOrder;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        goodCategoryCb.getItems().addAll(GoodCategory.values());
        goodCategoryCb.setConverter(new StringConverter<>() {
            @Override
            public String toString(GoodCategory goodCategory) {
                return goodCategory.getShortName();
            }

            @Override
            public GoodCategory fromString(String s) {
                return GoodCategory.getByShortName(s);
            }
        });

        kmRb.setToggleGroup(rbGroup);
        kiRb.setToggleGroup(rbGroup);
        aggregateRb.setToggleGroup(rbGroup);

        rbGroup.selectedToggleProperty().addListener((ov, old_toggle, new_toggle) ->
                aggregationCodeTextField.setDisable(new_toggle == null || new_toggle != aggregateRb)
        );

        printingProfileRepo.findAll().forEach(s -> {
            if (s.getType() == ProfileType.ZPL) {
                printingProfileComboBox.getItems().add(s.getName());
            }
        });

        printComboBox.selectedProperty().addListener(s ->
                printingProfileComboBox.setDisable(!printComboBox.isSelected())
        );

        loadParams();
    }

    public void setFile(File file) {
        this.sourceFile = file;
        fileNameTf.setText(FilenameUtils.getName(sourceFile.getAbsolutePath()));
    }

    public void setGoodCategory(GoodCategory goodCategory) {
        goodCategoryCb.getSelectionModel().select(goodCategory);
    }

    public void okButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) fileNameTf.getScene().getWindow();
        try {
            var pickingOrder = getPickingOrder();
            if (Objects.isNull(pickingOrder)) {
                return;
            }
            var destFile = PickingOrderService.savePickingOrder(sourceFile, pickingOrder);
            statusTf.getScene().setUserData(destFile);
            statisticService.logAction("ADD_PICKING_ORDER", 0, goodCategoryCb.getSelectionModel().getSelectedItem());
            saveParams();
            stage.close();
        } catch (Exception e) {
            log.error("Error while adding picking order ", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка при добавлении пикинг ордера " + e.getMessage()).showAndWait();
        }
    }

    public void cancelButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) fileNameTf.getScene().getWindow();
        stage.close();
    }

    public void generateSsccButtonAction(ActionEvent actionEvent) {
        if (aggregateRb.isSelected()) {
            aggregationCodeTextField.setText(ssccService.getNextCode());
        }
    }

    private PickingOrder getPickingOrder() {
        if (pickingOrder == null) {
            pickingOrder = new PickingOrder();
            pickingOrder.setFileName(fileNameTf.getText());
            pickingOrder.setState(statusTf.getText());
            pickingOrder.setComment(commentTf.getText());
            if (aggregateRb.isSelected()) {
                pickingOrder.setAggregationCode(aggregationCodeTextField.getText().trim());
            }
            pickingOrder.setCodesType(getCodeType());
            pickingOrder.setCreationDate(getXMLGregorianCalendarNow());
            pickingOrder.setModificationDate(getXMLGregorianCalendarNow());
//        pickingOrder.setSecurity();
            if (Objects.isNull(goodCategoryCb.getSelectionModel().getSelectedItem())) {
                var alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Ошибка");
                alert.setHeaderText("Не указана товарная группа");
                alert.showAndWait();
                return null;
            }
            pickingOrder.setProductGroup(convertTg(goodCategoryCb.getSelectionModel().getSelectedItem()));
            pickingOrder.setVersion(BigDecimal.ONE);
        } else {
            pickingOrder.setState(statusTf.getText());
            pickingOrder.setCodesType(getCodeType());
            if (aggregateRb.isSelected()) {
                pickingOrder.setAggregationCode(aggregationCodeTextField.getText().trim());
            }
            pickingOrder.setComment(commentTf.getText());
            pickingOrder.setProductGroup(convertTg(goodCategoryCb.getSelectionModel().getSelectedItem()));
            pickingOrder.setModificationDate(getXMLGregorianCalendarNow());
        }

        return pickingOrder;
    }

    public void setPickingOrder(PickingOrder pickingOrder) {
        this.pickingOrder = pickingOrder;
        if (pickingOrder == null) {
            return;
        }
        commentTf.setText(pickingOrder.getComment());
        statusTf.setText(pickingOrder.getState());
        statusTf.setText(pickingOrder.getState());
        aggregationCodeTextField.setText(pickingOrder.getAggregationCode());

        rbGroup.selectToggle(selectToggle(pickingOrder));
        goodCategoryCb.getSelectionModel().select(PickingOrderService.convertTg(pickingOrder.getProductGroup()));
    }

    private void saveParams() {
        propService.saveProp(PICKING_ORDER_LAST_COMMENT, commentTf.getText());
        propService.saveProp(PICKING_ORDER_LAST_STATUS, statusTf.getText());
        propService.saveProp(PICKING_ORDER_LAST_TYPE, getOrderType());
        propService.saveProp(PICKING_ORDER_LAST_ENABLE_PRINTING, printComboBox.isSelected());
        propService.saveProp(PICKING_ORDER_LAST_PRINT_PROFILE, printingProfileComboBox.getSelectionModel().getSelectedItem());
    }

    private void loadParams() {
        commentTf.setText(propService.getProp(PICKING_ORDER_LAST_COMMENT));
        statusTf.setText(propService.getProp(PICKING_ORDER_LAST_STATUS));
        getLastSelectedOrderTypeButton().setSelected(true);
        printComboBox.setSelected(propService.getBooleanProp(PICKING_ORDER_LAST_ENABLE_PRINTING));
        printingProfileComboBox.setValue(propService.getProp(PICKING_ORDER_LAST_PRINT_PROFILE));
    }

    private String getOrderType() {
        return Stream.of(kmRb, kiRb, aggregateRb)
                .filter(ToggleButton::isSelected)
                .map(Labeled::getText)
                .findFirst().orElse("КМ");
    }

    private RadioButton getLastSelectedOrderTypeButton() {
        var buttonText = propService.getProp(PICKING_ORDER_LAST_TYPE);
        return Stream.of(kmRb, kiRb, aggregateRb).filter(button -> button.getText().equals(buttonText)).findFirst().orElse(kmRb);
    }

    private CodesTypeType getCodeType() {
        if (kmRb.isSelected()) {
            return KM;
        }
        if (kiRb.isSelected()) {
            return KI;
        }
        if (aggregateRb.isSelected()) {
            return AGGREGATE;
        }
        return null;
    }

    private Toggle selectToggle(PickingOrder pickingOrder) {
        if (pickingOrder.getCodesType() == AGGREGATE) {
            return aggregateRb;
        } else if (pickingOrder.getCodesType() == KI) {
            return kiRb;
        } else {
            return kmRb;
        }
    }
}
