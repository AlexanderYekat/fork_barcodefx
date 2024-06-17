package site.barsukov.barcodefx;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import org.apache.commons.io.FileUtils;
import site.barsukov.barcodefx.model.ArchivedPickingOrder;
import site.barsukov.barcodefx.model.crpt.withdraw.WithdrawalPrimaryDocumentType;
import site.barsukov.barcodefx.model.crpt.withdraw.WithdrawalType;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.model.enums.RemarkCause;
import site.barsukov.barcodefx.model.production.WaterLicense;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.ContextCreator.*;
import static site.barsukov.barcodefx.utils.Utils.TEMPLATES_FOLDER_PATH;

public class ListFiller {
    public static void fillWithdrawList(ListView withdrawList) {
        TextField withdrawKktNumber = new TextField();
        withdrawKktNumber.setPromptText("Номер ККТ");
        withdrawKktNumber.setId("withdrawKktNumber");

        TextField withdrawPrimaryDocumentCustomName = new TextField();
        withdrawPrimaryDocumentCustomName.setPromptText("Наименование первичного документа");
        withdrawPrimaryDocumentCustomName.setId("withdrawPrimaryDocumentCustomName");

        TextField withdrawPrimaryDocumentNumber = new TextField();
        withdrawPrimaryDocumentNumber.setPromptText("Номер первичного документа");
        withdrawPrimaryDocumentNumber.setId("withdrawPrimaryDocumentNumber");

        DatePicker withdrawPrimaryDocumentDate = new DatePicker();
        withdrawPrimaryDocumentDate.setPromptText("Дата первичного документа");
        withdrawPrimaryDocumentDate.setId("withdrawPrimaryDocumentDate");

        DatePicker withdrawDate = new DatePicker();
        withdrawDate.setPromptText("Дата вывода из оборота");
        withdrawDate.setId("withdrawDate");

        ComboBox withdrawPrimaryDocumentType = new ComboBox();
        withdrawPrimaryDocumentType.setPromptText("Тип первичного документа");
        withdrawPrimaryDocumentType.setItems(getWithdrawDocumentTypeOptions());
        withdrawPrimaryDocumentType.setId("withdrawPrimaryDocumentType");

        TextField withdrawTradeParticipantInn = new TextField();
        withdrawTradeParticipantInn.setPromptText("ИНН участника оборота");
        withdrawTradeParticipantInn.setId("withdrawTradeParticipantInn");

        ComboBox withdrawType = new ComboBox();
        withdrawType.setPromptText("Причина вывода из оборота");
        withdrawType.setItems(getWithdrawalTypeOptions());
        withdrawType.setId("withdrawType");

        withdrawList.getItems().add(withdrawTradeParticipantInn);
        withdrawList.getItems().add(withdrawKktNumber);
        withdrawList.getItems().add(withdrawPrimaryDocumentType);
        withdrawList.getItems().add(withdrawPrimaryDocumentNumber);
        withdrawList.getItems().add(withdrawPrimaryDocumentCustomName);
        withdrawList.getItems().add(withdrawPrimaryDocumentDate);
        withdrawList.getItems().add(withdrawType);
        withdrawList.getItems().add(withdrawDate);
    }

    public static void fillCategories(ComboBox<GoodCategory> category) {
        ObservableList templatesObservableList = FXCollections.observableArrayList(
//            Stream.of(GoodCategory.values()).map(GoodCategory::getShortName).collect(Collectors.toList())
            GoodCategory.values()
        );
        category.setItems(templatesObservableList);
        category.getSelectionModel().select(0);
    }

    public static void fillTemplates(ComboBox<String> templatesList) {
        ObservableList templatesObservableList = FXCollections.observableArrayList(
            "DEFAULT"
        );

        File templatesDir = new File(TEMPLATES_FOLDER_PATH);
        templatesDir.mkdir();
        Collection<File> templates = FileUtils.listFiles(templatesDir, new String[]{"html"}, false);
        for (File curFile : templates) {
            templatesObservableList.add(curFile.getName());
        }
        templatesList.setItems(templatesObservableList);
        templatesList.getSelectionModel().select(0);

    }


    public static void fillProductionList(ListView productionList, EventHandler<ActionEvent> openWaterLicenses) {

        TextField participantInn = new TextField();
        participantInn.setPromptText("ИНН участника оборота");
        participantInn.setId("participantInn");

        TextField producerInn = new TextField();
        producerInn.setPromptText("ИНН производителя");
        producerInn.setId("producerInn");

        TextField ownerInn = new TextField();
        ownerInn.setPromptText("ИНН собственника");
        ownerInn.setId("ownerInn");

        TextField certificateNumber = new TextField();
        certificateNumber.setPromptText("№ док., подтверждающего соответствие");
        certificateNumber.setId("certificateNumber");

        DatePicker productDate = new DatePicker();
        productDate.setPromptText("Дата производства");
        productDate.setId("productDate");

        DatePicker certificateDate = new DatePicker();
        certificateDate.setPromptText("Дата документа, подтверждающего соответствие");
        certificateDate.setId("certificateDate");
        certificateDate.setTooltip(new Tooltip("Дата документа, подтверждающего соответствие"));

        TextField tnvedCode = new TextField();
        tnvedCode.setPromptText("Код ТН ВЭД ЕАС товара");
        tnvedCode.setId("tnvedCode");

        TextField vsdNumber = new TextField();
        vsdNumber.setPromptText("Идентификатор ВСД. Только для ТГ Молоко");
        vsdNumber.setId(VSD_NUMBER);

        Button waterLicense = new Button();
        waterLicense.setText(WATER_LICENSE_BUTTON_TEXT);
        waterLicense.setId(WATER_LICENSE);
        waterLicense.setOnAction(openWaterLicenses);

        ObservableList<String> productionOrderList =
            FXCollections.observableArrayList(
                "OWN_PRODUCTION",
                "CONTRACT_PRODUCTION"
            );
        ComboBox productionOrder = new ComboBox();
        productionOrder.setPromptText("Тип производственного заказа");
        productionOrder.setItems(productionOrderList);
        productionOrder.setId("productionOrder");

        ObservableList<String> certificateTypeList =
            FXCollections.observableArrayList(
                "CONFORMITY_CERTIFICATE",
                "CONFORMITY_DECLARATION"
            );


        ComboBox certificateType = new ComboBox();
        certificateType.setPromptText("Вид документа, подтверждающего соответствие");
        certificateType.setItems(certificateTypeList);
        certificateType.setId("certificateType");

        productionList.getItems().add(participantInn);
        productionList.getItems().add(producerInn);
        productionList.getItems().add(ownerInn);
        productionList.getItems().add(productDate);
        productionList.getItems().add(certificateNumber);
        productionList.getItems().add(certificateDate);
        productionList.getItems().add(tnvedCode);
        productionList.getItems().add(productionOrder);
        productionList.getItems().add(certificateType);
        productionList.getItems().add(vsdNumber);
        productionList.getItems().add(waterLicense);
    }

    public static void fillCancellationList(ListView cancellationList) {
        TextField participantInn = new TextField();
        participantInn.setPromptText("ИНН участника оборота");
        participantInn.setId("participantInn");

        TextField cancellationDocNum = new TextField();
        cancellationDocNum.setPromptText("Документ списания N");
        cancellationDocNum.setId("cancellationDocNum");

        DatePicker cancellationDocDate = new DatePicker();
        cancellationDocDate.setPromptText("Дата документа списания");
        cancellationDocDate.setId("cancellationDocDate");

        ObservableList<String> options =
            FXCollections.observableArrayList(
                "KM_SPOILED",
                "KM_LOST",
                "KM_DESTROYED"
            );
        ComboBox cancellationReason = new ComboBox();
        cancellationReason.setPromptText("Причина списания");
        cancellationReason.setId("cancellationReason");
        cancellationReason.setItems(options);

        cancellationList.getItems().add(participantInn);
        cancellationList.getItems().add(cancellationDocNum);
        cancellationList.getItems().add(cancellationDocDate);
        cancellationList.getItems().add(cancellationReason);
    }

    public static void fillCrossborderList(ListView crossborderList) {
        TextField userInn = new TextField();
        userInn.setPromptText("ИНН участника оборота");
        userInn.setId(USER_INN);

        TextField senderInn = new TextField();
        senderInn.setPromptText("Номер налогоплательщика отправителя");
        senderInn.setId(SENDER_INN);

        TextField senderName = new TextField();
        senderName.setPromptText("Наименование экспортера");
        senderName.setId(EXPORTER_NAME);

        TextField countryOksm = new TextField();
        countryOksm.setPromptText("Код страны согласно ОКСМ");
        countryOksm.setId(COUNTRY_OKSM);

        DatePicker importDate = new DatePicker();
        importDate.setPromptText("Дата импорта");
        importDate.setId(IMPORT_DATE);

        TextField primaryDocNum = new TextField();
        primaryDocNum.setPromptText("Номер первичного документа");
        primaryDocNum.setId(PRIMARY_DOC_NUM);

        DatePicker primaryDocDate = new DatePicker();
        primaryDocDate.setPromptText("Дата первичного документа");
        primaryDocDate.setId(PRIMARY_DOC_DATE);

        TextField vsdNumber = new TextField();
        vsdNumber.setPromptText("Идентификатор ВСД. Только для ТГ Молоко");
        vsdNumber.setId(VSD_NUMBER);

        crossborderList.getItems().add(userInn);
        crossborderList.getItems().add(senderInn);
        crossborderList.getItems().add(senderName);
        crossborderList.getItems().add(countryOksm);
        crossborderList.getItems().add(importDate);
        crossborderList.getItems().add(primaryDocNum);
        crossborderList.getItems().add(primaryDocDate);
        crossborderList.getItems().add(vsdNumber);
    }

    public static void fillPickingOrdersTable(TableView<PickingOrder> ordersTableView) {
        TableColumn<PickingOrder, String> orderFileNameColumn = new TableColumn<>("Имя файла");
        orderFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        ordersTableView.getColumns().add(orderFileNameColumn);

        TableColumn<PickingOrder, Date> orderDateCreation = new TableColumn<>("Дата создания");
        orderDateCreation.setCellValueFactory(new PropertyValueFactory<>("creationDate"));
        ordersTableView.getColumns().add(orderDateCreation);

        TableColumn<PickingOrder, String> orderCategoryNameColumn = new TableColumn<>("ТГ");
        orderCategoryNameColumn.setCellValueFactory(new PropertyValueFactory<>("productGroup"));
        ordersTableView.getColumns().add(orderCategoryNameColumn);

        TableColumn<PickingOrder, Integer> orderCodesCount = new TableColumn<>("Количество кодов");
        orderCodesCount.setCellValueFactory(new PropertyValueFactory<>("cisCount"));
        ordersTableView.getColumns().add(orderCodesCount);

        TableColumn<PickingOrder, String> orderState = new TableColumn<>("Статус");
        orderState.setCellValueFactory(new PropertyValueFactory<>("state"));
        ordersTableView.getColumns().add(orderState);

        TableColumn<PickingOrder, String> orderType = new TableColumn<>("Тип ордера");
        orderType.setCellValueFactory(new PropertyValueFactory<>("codesType"));
        ordersTableView.getColumns().add(orderType);

        TableColumn<PickingOrder, String> orderComment = new TableColumn<>("Комментарий");
        orderComment.setCellValueFactory(new PropertyValueFactory<>("comment"));
        ordersTableView.getColumns().add(orderComment);
    }

    public static void fillArchivedPickingOrdersTable(TableView<ArchivedPickingOrder> ordersTableView) {
        TableColumn<ArchivedPickingOrder, String> orderFileNameColumn = new TableColumn<>("Имя файла");
        orderFileNameColumn.setCellValueFactory(new PropertyValueFactory<>("fileName"));
        ordersTableView.getColumns().add(orderFileNameColumn);

        TableColumn<ArchivedPickingOrder, Date> orderDateCreation = new TableColumn<>("Дата создания");
        orderDateCreation.setCellValueFactory(new PropertyValueFactory<>("createDate"));
        ordersTableView.getColumns().add(orderDateCreation);


    }

    public static void fillRemarkList(ListView remarkList) {
        TextField participantINN = new TextField();
        participantINN.setPromptText("ИНН участника");
        participantINN.setId("participantInn");

        DatePicker remarkDate = new DatePicker();
        remarkDate.setPromptText("Дата перемаркировки");
        remarkDate.setId("remarkDate");

        TextField remarkTnvd = new TextField();
        remarkTnvd.setPromptText("ТН ВЭД (10 символов)");
        remarkTnvd.setId("remarkTnvd");

        ComboBox<RemarkCause> remarkCauseComboBox = new ComboBox();
        remarkCauseComboBox.setPromptText("Причина перемаркировки");
        remarkCauseComboBox.getItems().addAll(RemarkCause.values());
        remarkCauseComboBox.setId("remarkCause");

        TextField remarkColor = new TextField();
        remarkColor.setPromptText("Цвет");
        remarkColor.setId("remarkColor");

        TextField remarkSize = new TextField();
        remarkSize.setPromptText("Размер");
        remarkSize.setId("remarkSize");

        remarkList.getItems().add(participantINN);
        remarkList.getItems().add(remarkDate);
        remarkList.getItems().add(remarkTnvd);
        remarkList.getItems().add(remarkCauseComboBox);
        remarkList.getItems().add(remarkColor);
        remarkList.getItems().add(remarkSize);
    }

    public static void fillShipmentList(ListView shipmentList) {
        TextField shipmentReceiverINN = new TextField();
        shipmentReceiverINN.setPromptText("ИНН получателя");
        shipmentReceiverINN.setId("shipmentReceiverINN");

        TextField shipmentSenderINN = new TextField();
        shipmentSenderINN.setPromptText("ИНН отправителя");
        shipmentSenderINN.setId("shipmentSenderINN");

        DatePicker shipmentDate = new DatePicker();
        shipmentDate.setPromptText("Дата отгрузки");
        shipmentDate.setId("shipmentDate");

        TextField shipmentDocNum = new TextField();
        shipmentDocNum.setPromptText("Документ отгрузки N");
        shipmentDocNum.setId("shipmentDocNum");

        DatePicker shipmentDocDate = new DatePicker();
        shipmentDocDate.setPromptText("Дата документа");
        shipmentDocDate.setId("shipmentDocDate");

        ComboBox shipmentType = new ComboBox();
        shipmentType.setPromptText("Тип отгрузки");
        shipmentType.setItems(getChangeOptions());
        shipmentType.setId("shipmentType");

        CheckBox shipmentNotUOT = new CheckBox();
        shipmentNotUOT.setText("Отгрузка не УОТ-у");
        shipmentNotUOT.setId("shipmentNotUOT");

        ComboBox withdrawType = new ComboBox();
        withdrawType.setPromptText("Причина вывода");
        withdrawType.setItems(getWithdrawOptions());
        withdrawType.setId("withdrawType");
        withdrawType.setDisable(true);

        DatePicker withdrawDate = new DatePicker();
        withdrawDate.setPromptText("Дата вывода из оборота");
        withdrawDate.setId("withdrawDate");
        withdrawDate.setDisable(true);

        shipmentNotUOT.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                boolean isSelected = ((CheckBox) event.getSource()).isSelected();
                withdrawType.setDisable(!isSelected);
                withdrawDate.setDisable(!isSelected);
            }
        });

        shipmentList.getItems().add(shipmentReceiverINN);
        shipmentList.getItems().add(shipmentSenderINN);
        shipmentList.getItems().add(shipmentDate);
        shipmentList.getItems().add(shipmentDocNum);
        shipmentList.getItems().add(shipmentDocDate);
        shipmentList.getItems().add(shipmentType);
        shipmentList.getItems().add(shipmentNotUOT);
        shipmentList.getItems().add(withdrawType);
        shipmentList.getItems().add(withdrawDate);
    }


    private static ObservableList getWithdrawDocumentTypeOptions() {
        return FXCollections.observableArrayList(
            Arrays.stream(WithdrawalPrimaryDocumentType.values())
                .map(s -> s.name())
                .collect(Collectors.toList())
        );
    }


    private static ObservableList getWithdrawalTypeOptions() {
        return FXCollections.observableArrayList(
            Arrays.stream(WithdrawalType.values())
                .map(s -> s.name())
                .collect(Collectors.toList())
        );
    }

    public static ObservableList getChangeOptions() {
        return FXCollections.observableArrayList(
            "SELLING",
            "COMMISSION",
            "AGENT",
//            "COMMISSIONAIRE_SALE",
            "CONTRACT"
        );
    }

    private static ObservableList getWithdrawOptions() {
        return FXCollections.observableArrayList(
            "DONATION",
            "STATE_ENTERPRISE",
            "NO_RETAIL_USE"
        );
    }

}
