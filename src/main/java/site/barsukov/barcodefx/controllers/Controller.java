package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.ContextCreator;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.model.AboutResource;
import site.barsukov.barcodefx.model.AboutTextEnum;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.model.production.WaterLicense;
import site.barsukov.barcodefx.props.TabProps;
import site.barsukov.barcodefx.services.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Calendar.DECEMBER;
import static java.util.Calendar.JANUARY;
import static site.barsukov.barcodefx.ContextCreator.*;
import static site.barsukov.barcodefx.ListFiller.*;
import static site.barsukov.barcodefx.props.JsonCatalogProps.CatalogProp.CATALOG_ENABLED;
import static site.barsukov.barcodefx.props.JsonLastStateProps.LastStateProp.*;
import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.USE_PACKAGE_WORK_TYPE;
import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.XML_VALIDATION_ENABLED;
import static site.barsukov.barcodefx.props.JsonSystemProps.SystemProp.*;
import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.CHECK_UPDATES;

@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/sample.fxml")
public class Controller implements Initializable {

    public ListView crossborderList;
    public MenuButton chooseFileButton;
    public Label chooseFileLabel;
    public Button scanFileButton;
    public Button ssccButton;
    public ComboBox<String> templatesList;
    public CheckMenuItem updatesMenuItem;
    public ComboBox<GoodCategory> category;
    public TextField vOborotImportFtsINN;
    public TextField vOborotImportFtsDtNum;
    public DatePicker vOborotImportFtsDtDate;
    public ListView withdrawList;
    public TextField markNumberStart;
    public ListView shipmentList;
    public ListView cancellationList;
    public Label comparedFileLabel;
    public TextArea comparedTextArea;
    public CheckBox ignoreCryptoTail;
    public TextField aggregationDocNum;
    public TextField aggregationOrgName;
    public TextField aggregationSccCodeText;
    public TextField aggregationINN;
    public Label fileNameLabel;
    public Button generateXMLButton;
    public Button generateXMLAcceptanceButton;
    public Button generateXMLShipmentButton;
    public TextField height;
    public TextField width;
    public RadioButton isVertical;
    public RadioButton isHorizontal;
    public RadioButton sizeA4;
    public RadioButton sizeTermoPrinter;
    public TextArea userLabelText;
    public CheckBox rangeCheckBox;
    public TextField rangeFrom;
    public TextField rangeTo;
    public TextField userINNText;
    public TextField acceptanceReceiverINN;
    public TextField acceptanceSenderINN;
    public TextField acceptanceDocNum;
    public DatePicker acceptanceDocDate;
    public DatePicker acceptanceDate;
    public ComboBox acceptanceType;
    public TabPane tabPaneElement;
    public CheckBox printMarkNumber;
    public ListView productionList;
    public List<WaterLicense> waterLicenses = new ArrayList<>();
    public TextField shipmentId;
    public ImageView logoImage;
    public CheckBox aggregationIpCB;
    public ListView remarkList;

    private final ConfigurableApplicationContext applicationContext;
    private final PropService propService;
    private final ContextCreator contextCreator;
    private final UpdateService updateService;
    private final CatalogService catalogService;
    private final StatisticService statisticService;
    private final AboutService aboutService;

    private boolean isPackageType = false;
    private String resultFolder = null;
    private java.util.List<Tab> allTabs;


    public void chooseFileButtonAction(ActionEvent actionEvent) {
        File csvFile;
        File folder = new File(FilenameUtils.getFullPath(fileNameLabel.getText()));
        if (isPackageType) {
            DirectoryChooser directoryChooser = new DirectoryChooser();
            directoryChooser.setTitle("Выберите папку с кодами маркировки");
            if (folder.exists()) {
                directoryChooser.setInitialDirectory(folder);
            }
            csvFile = directoryChooser.showDialog(new Stage());
        } else {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл с кодами маркировки");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv, txt", "*.csv", "*.txt", "*.zms"));
            if (folder.exists()) {
                fileChooser.setInitialDirectory(folder);
            }
            csvFile = fileChooser.showOpenDialog(new Stage());
        }
        if (csvFile != null) {
            fileNameLabel.setText(csvFile.getAbsolutePath());
            resultFolder = csvFile.getParent();
            enableGeneratorButtons();
        }
    }

    public void choosePickingOrderButtonAction(ActionEvent actionEvent) {
        try {
            if (isPackageType) {
                throw new IllegalArgumentException("Функционал не поддерживается в пакетном режиме");
            } else {
                FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
                Parent root = fxWeaver.loadView(PickingOrderListController.class);
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setTitle("Пикинг ордера");
                Scene scene = new Scene(root);
                stage.setScene(scene);
                stage.showAndWait();

                File fileWithCodes = (File) scene.getUserData();
                if (fileWithCodes != null) {
                    fileNameLabel.setText(fileWithCodes.getAbsolutePath());
                    resultFolder = fileWithCodes.getParent();
                    enableGeneratorButtons();
                }
            }
        } catch (Exception e) {
            log.error("Error opening picking order list", e);
        }
    }

    private void fillData() {
        acceptanceType.setItems(getChangeOptions());
        updatesMenuItem.setSelected(
                propService.getBooleanProp(CHECK_UPDATES));

        category.getSelectionModel().selectedItemProperty().addListener(
                (options, oldValue, newValue) -> {
                    hideTabs();
                }
        );
        if (isPackageType) {
            chooseFileButton.setText("Выбрать папку с кодами");
            chooseFileLabel.setText("Выбранная папка:");
            fileNameLabel.setText("Папка не выбрана");
            Alert disabledAlert = new Alert(Alert.AlertType.ERROR, "Функционал недоступен в режиме пакетной обработки");
            ssccButton.setOnAction(new EventHandler() {
                                       @Override
                                       public void handle(Event event) {
                                           disabledAlert.showAndWait();
                                       }
                                   }
            );
            scanFileButton.setOnAction(new EventHandler() {
                                           @Override
                                           public void handle(Event event) {
                                               disabledAlert.showAndWait();
                                           }
                                       }
            );
        }
        EventHandler<ActionEvent> openWaterLicenses = new EventHandler() {
            @Override
            public void handle(Event event) {
                openWaterLicenses();

            }
        };
        fillCategories(category);
        fillProductionList(productionList, openWaterLicenses);
        fillShipmentList(shipmentList);
        fillRemarkList(remarkList);
        fillCancellationList(cancellationList);
        fillWithdrawList(withdrawList);
        fillTemplates(templatesList);
        fillCrossborderList(crossborderList);
        updateWaterLicenseCounter(productionList);
    }

    private void updateWaterLicenseCounter(ListView productionList) {
        Button waterLicensesButton = (Button) productionList.getItems().stream().filter(this::isWaterButton).findFirst().get();
        waterLicensesButton.setText(WATER_LICENSE_BUTTON_TEXT + " (" + waterLicenses.size() + ")");
    }

    private boolean isWaterButton(Object s) {
        return s instanceof Button && WATER_LICENSE.equals(((Button) s).getId());
    }

    private void enableGeneratorButtons() {
        generateXMLButton.setDisable(false);
        generateXMLShipmentButton.setDisable(false);
        generateXMLAcceptanceButton.setDisable(false);
        tabPaneElement.setDisable(false);
    }

    private List<String> getCsvFileFromFolder(String text) {
        return FileUtils.listFiles(new File(text), new String[]{"csv", "txt"}, true)
                .stream()
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());
    }

    private BaseDocService getPdfCreateService(String csvFileName) {
        if (sizeTermoPrinter.isSelected() && !templatesList.getSelectionModel().isSelected(0)) {
            return new PDFTemplatesCreateService(contextCreator.createDataMatrixContext(this, csvFileName), catalogService);
        } else {
            return new PDFCreateService(contextCreator.createDataMatrixContext(this, csvFileName));
        }
    }

    public void printDatamatrixButtonAction(ActionEvent actionEvent) {
        if (isPackageType) {
            boolean noErrors = true;
            java.util.List<String> csvFiles = getCsvFileFromFolder(fileNameLabel.getText());
            int errorCounter = 0;
            for (String curFile : csvFiles) {
                boolean result = performPackageAction(getPdfCreateService(curFile));
                if (!result) {
                    noErrors = false;
                    errorCounter++;
                }
            }
            if (noErrors) {
                String message = String.format("Файлы обработаны без ошибок. Успешно обработано %d файлов", csvFiles.size());
                new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
            } else {
                String message = String.format("Файлы обработаны c ошибками. Успешно обработано %d файлов из %d. Подробная информация в логах.",
                        csvFiles.size() - errorCounter, csvFiles.size());
                new Alert(Alert.AlertType.ERROR, message).showAndWait();
            }
        } else {
            performAction(getPdfCreateService(fileNameLabel.getText()));
        }
    }

    public void printDatamatrixInPacksAction(ActionEvent actionEvent) {
        if (isPackageType) {
            new Alert(Alert.AlertType.ERROR, "Функционал недоступен в режиме пакетной обработки").showAndWait();
        } else {
            FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
            Parent root = fxWeaver.loadView(PrintPacksController.class);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Печать пачками");
            Scene scene = new Scene(root);
            stage.setScene(scene);
            PrintPacksController controller = fxWeaver.getBean(PrintPacksController.class);
            controller.setDataMatrixContext(contextCreator.createDataMatrixContext(this, fileNameLabel.getText()));
            controller.setUseTemplate(sizeTermoPrinter.isSelected() && !templatesList.getSelectionModel().isSelected(0));
            stage.showAndWait();

            Boolean success = (Boolean) scene.getUserData();
            if (success != null && success) {
                new Alert(Alert.AlertType.INFORMATION, "Файлы успешно созданы. Находятся в той же папке, что и файл с кодами.").showAndWait();
            } else if (success != null && !success) {
                new Alert(Alert.AlertType.ERROR, "При печати произошла ошибка, подробнее в файле логов.").showAndWait();
            }
        }
    }

    public void generateXMLAcceptance(ActionEvent actionEvent) {
        XMLAcceptanceService xmlCreateService = new XMLAcceptanceService(contextCreator.createAcceptanceContext(this));
        performAction(xmlCreateService);
    }

    public void remarkButtonAction(ActionEvent actionEvent) {
        XMLRemarkService xmlRemarkService = new XMLRemarkService(contextCreator.createRemarkContext(this));
        performAction(xmlRemarkService);
    }

    public void aggregationSaveButtonAction(ActionEvent actionEvent) {
        XMLAggregationService xmlAggregationService = new XMLAggregationService(contextCreator.createAggregationContext(this));
        performAction(xmlAggregationService);
    }

    public void vOborotImportFtsButtonAction(ActionEvent actionEvent) {
        XMLVvodVOborotImportFtsService xmlVvodVOborotImportFtsService = new XMLVvodVOborotImportFtsService(contextCreator.createVvodVOborotImportFtsContext(this), catalogService);
        performAction(xmlVvodVOborotImportFtsService);
    }

    public void generateXMLWithdrawButtonAction(ActionEvent actionEvent) {
        XMLWithdrawService xmlWithdrawService = new XMLWithdrawService(contextCreator.createWithdrawContext(this));
        performAction(xmlWithdrawService);
    }

    public void cancellationButtonAction(ActionEvent actionEvent) {
        XMLCancellationService xmlCancellationService = new XMLCancellationService(contextCreator.createCancellationContext(this));
        performAction(xmlCancellationService);
    }

    public void generateXML(ActionEvent actionEvent) {
        XMLVvodVOborotService xmlCreateService = new XMLVvodVOborotService(contextCreator.createVvodVOborotContext(this));
        performAction(xmlCreateService);
    }

    public void generateXMLShipment(ActionEvent actionEvent) {
        XMLShipmentService xmlCreateService = new XMLShipmentService(contextCreator.createShipmentContext(this));
        performAction(xmlCreateService);
    }

    public void generateXMLProduction(ActionEvent actionEvent) {
        XMLProductionService xmlProductionService = new XMLProductionService(contextCreator.createProductionRFContext(this));
        performAction(xmlProductionService);
    }

    public void vOborotImportCrossborderButtonAction(ActionEvent actionEvent) {
        if (!propService.getBooleanProp(CATALOG_ENABLED)) {
            new Alert(Alert.AlertType.ERROR, "Для формирования файл ввода в оборот при трансграничной торговле " +
                    "требуется в настройках подключить каталог товаров").showAndWait();
        } else {
            XMLVvodVOborotCrossborderService xmlCrossborderService = new XMLVvodVOborotCrossborderService(contextCreator.createVoborotCrossborderContext(this), catalogService);
            performAction(xmlCrossborderService);
        }
    }

    public void rangeCheckBoxAction(ActionEvent actionEvent) {
        if (rangeCheckBox.isSelected()) {
            rangeFrom.setDisable(false);
            rangeTo.setDisable(false);
        } else {
            rangeFrom.setDisable(true);
            rangeTo.setDisable(true);
        }
    }

    public void scanFileButtonAction(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(ScannerController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Scanner");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();

        File scanFile = (File) scene.getUserData();
        if (scanFile != null) {
            fileNameLabel.setText(scanFile.getAbsolutePath());
            resultFolder = scanFile.getParent();
            enableGeneratorButtons();
        }
    }

    public void termoprinterSelection(ActionEvent actionEvent) {
        if (sizeTermoPrinter.isSelected()) {
            height.setDisable(false);
            width.setDisable(false);
            isHorizontal.setSelected(true);
            isVertical.setSelected(false);
            isHorizontal.setDisable(true);
            isVertical.setDisable(true);
            templatesList.setDisable(false);
        }
    }

    public void sizeA4Selection(ActionEvent actionEvent) {
        if (sizeA4.isSelected()) {
            height.setDisable(true);
            width.setDisable(true);
            isHorizontal.setDisable(false);
            isVertical.setDisable(false);
            templatesList.setDisable(true);
        }
    }

    public void openFortaLink(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://fourta.org/?utm_source=barcodesfx"));
    }

    public void openTutorial(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://www.youtube.com/watch?v=sPWce9AgoMc?utm_source=barcodesfx"));
    }

    public void openAbout(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(AboutController.class, new AboutResource(AboutTextEnum.ABOUT));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("About");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        AboutController controller = fxWeaver.getBean(AboutController.class);
        stage.setOnCloseRequest(event -> controller.saveProps());

        stage.showAndWait();
    }

    public void openPrintingProfileButtonAction(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent printingProfileEditor = fxWeaver.loadView(PrintingProfileEditorController.class);
        Stage stage = new Stage();
        stage.setResizable(false);
        stage.setTitle("Printing profiles");
        stage.setScene(new Scene(printingProfileEditor));
        stage.showAndWait();
    }

    public void openPickingServersButtonAction(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(PickingServersController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Servers");
        Scene scene = new Scene(root);
        stage.setScene(scene);

        stage.showAndWait();
    }

    public void supportLinkAction(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://yoomoney.ru/fundraise/0UWgzAK4hNY.230328"));
    }

    public void comparedButtonAction(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл с кодами маркировки");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv, txt", "*.csv", "*.txt"));
            File csvFile = fileChooser.showOpenDialog(new Stage());
            comparedFileLabel.setText(csvFile.getAbsolutePath());
            ComparatorService comparatorService = new ComparatorService(contextCreator.createComparatorContext(this));
            comparedTextArea.setText(comparatorService.getReport(statisticService));
        } catch (IllegalArgumentException ex) {
            log.error("Ошибка обработки сравнения: ", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        } catch (Exception e) {
            log.error("Error comparedButtonAction: ", e);
            new Alert(Alert.AlertType.ERROR, "Unexpected exception: " + e.getMessage()).showAndWait();
        }
    }

    public void ssccButtonAction(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(SsccController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("SSCC");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        File scanFile = (File) scene.getUserData();
        if (scanFile != null) {
            fileNameLabel.setText(scanFile.getAbsolutePath());
            resultFolder = scanFile.getParent();
            enableGeneratorButtons();
        }
    }

    public void ssccAbout(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.SSCC_ABOUT);
    }

    public void openAboutCrossborderAction(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.CROSSBORDER);
    }

    public void nameAbout(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.AGGREGATION_NAME_ABOUT);
    }

    public void withdrawExplanation(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.WITHDRAW_EXPLANATION);
    }

    public void templatesExplanation(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.TEMPLATES_EXPLANATION);
    }

    public void aggregationDocNum(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.AGGREGATION_DOC_NUM);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            isPackageType = propService.getBooleanProp(USE_PACKAGE_WORK_TYPE);
            allTabs = new ArrayList<>(tabPaneElement.getTabs());
            printMarkNumber.selectedProperty().addListener(
                    (options, oldValue, newValue) -> {
                        markNumberStart.setDisable(!printMarkNumber.isSelected());
                    }
            );
            hideTabs();
            fillData();
            statisticService.logAction("START", 0, null);
            new Thread(catalogService::reloadData).start();

            fillDefaults();
            openNews();
            updateService.start();
            changeLogo();
        } catch (Exception e) {
            log.error("Ошибка при запуске приложения");
            log.error("Error: ", e);
        }
    }

    private void changeLogo() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        boolean holidayDay = (cal.get(Calendar.DAY_OF_MONTH) >= 25 && cal.get(Calendar.MONTH) == DECEMBER) ||
                (cal.get(Calendar.DAY_OF_MONTH) <= 11 && cal.get(Calendar.MONTH) == JANUARY);
        if (holidayDay) {
            InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("tree.png");
            Image image = new Image(inputStream);
            logoImage.setImage(image);
        }
    }

    private void fillDefaults() {
        fillPrintDefaults();
        fillAggregateDefaults();
        fillVOborotOstatkiDefaults();
        fillVOborotImportFtsDefaults();
        fillCrossborderDefault();
        fillProductionRFDefault();
    }

    private void fillProductionRFDefault() {

        for (var item : productionList.getItems()) {
            switch (((Control) item).getId()) {
                case "participantInn" ->
                        ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_PARTICIPANT_INN));
                case "producerInn" ->
                        ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_PRODUCER_INN));
                case "ownerInn" -> ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_OWNER_INN));
                case "certificateNumber" ->
                        ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_CERT_NUMBER));
                case "productDate" ->
                        ((DatePicker) item).getEditor().setText(propService.getProp(V_OBOROT_PRODUCTION_RF_PRODUCT_DATE));
                case "certificateDate" ->
                        ((DatePicker) item).getEditor().setText(propService.getProp(V_OBOROT_PRODUCTION_RF_CERTIFICATE_DATE));
                case "tnvedCode" -> ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_TNVED));
                case VSD_NUMBER -> ((TextField) item).setText(propService.getProp(V_OBOROT_PRODUCTION_RF_VSD));
                case "productionOrder" ->
                        ((ComboBox) item).getSelectionModel().select(propService.getProp(V_OBOROT_PRODUCTION_RF_PRODUCTION_ORDER));
                case "certificateType" ->
                        ((ComboBox) item).getSelectionModel().select(propService.getProp(V_OBOROT_PRODUCTION_RF_CERT_TYPE));
            }
        }
    }

    private void fillCrossborderDefault() {
        for (var item : crossborderList.getItems()) {
            switch (((Control) item).getId()) {
                case USER_INN -> ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_INN_UOT));
                case SENDER_INN -> ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_INN_SENDER));
                case EXPORTER_NAME -> ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_SENDER_NAME));
                case COUNTRY_OKSM -> ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_COUNTRY_OKSM));
                case IMPORT_DATE ->
                        ((DatePicker) item).getEditor().setText(propService.getProp(V_OBOROT_TRANSGRAN_IMPORT_DATE));
                case PRIMARY_DOC_NUM ->
                        ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_NUMBER));
                case PRIMARY_DOC_DATE ->
                        ((DatePicker) item).getEditor().setText(propService.getProp(V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_DATE));
                case VSD_NUMBER -> ((TextField) item).setText(propService.getProp(V_OBOROT_TRANSGRAN_VSD));
            }
        }

    }

    private void fillVOborotOstatkiDefaults() {
        userINNText.setText(propService.getProp(V_OBOROT_OSTATKI_INN));
    }

    private void fillVOborotImportFtsDefaults() {
        vOborotImportFtsINN.setText(propService.getProp(V_OBOROT_IMPORT_FTS_INN));
    }

    private void fillAggregateDefaults() {
        aggregationDocNum.setText(propService.getProp(AGGREGATION_DOC_NUM));
        aggregationINN.setText(propService.getProp(AGGREGATION_INN));
        aggregationOrgName.setText(propService.getProp(AGGREGATION_ORG_NAME));
        aggregationIpCB.setSelected(propService.getBooleanProp(AGGREGATION_IS_IP));
    }

    private void fillPrintDefaults() {
        GoodCategory goodCategory = GoodCategory.valueOf(propService.getProp(CATEGORY));
        category.getSelectionModel().select(goodCategory);
        height.setText(propService.getProp(LABEL_HEIGHT));
        width.setText(propService.getProp(LABEL_WIDTH));
        userLabelText.setText(propService.getProp(LABEL_TEXT));
        markNumberStart.setText(propService.getProp(LABEL_START_NUMBER));
        printMarkNumber.setSelected(propService.getBooleanProp(LABEL_NUMERATE));
        isVertical.setSelected(propService.getBooleanProp(LABEL_VERTICAL));
        isHorizontal.setSelected(propService.getBooleanProp(LABEL_HORIZONTAL));
        if (propService.getBooleanProp(LABEL_SIZE_A4)) {
            sizeA4.setSelected(true);
            sizeA4.getOnAction().handle(null);
        }
        if (propService.getBooleanProp(LABEL_SIZE_TERMOPRINTER)) {
            sizeTermoPrinter.setSelected(true);
            sizeTermoPrinter.getOnAction().handle(null);
        }
    }

    private void saveDefaults() {
        savePrintDefaults();
        saveAggregationDefaults();
        saveVvodVOborotOstatkiDefaults();
        saveVvodVOborotImportFtsDefaults();
        saveVOborotTransgranDefaults();
        saveVOborotProductionRFDefaults();
    }

    private void saveVOborotProductionRFDefaults() {
        for (var item : productionList.getItems()) {
            switch (((Control) item).getId()) {
                case "participantInn" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_PARTICIPANT_INN, ((TextField) item).getText());
                case "producerInn" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_PRODUCER_INN, ((TextField) item).getText());
                case "ownerInn" -> propService.saveProp(V_OBOROT_PRODUCTION_RF_OWNER_INN, ((TextField) item).getText());
                case "certificateNumber" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_CERT_NUMBER, ((TextField) item).getText());
                case "productDate" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_PRODUCT_DATE, ((DatePicker) item).getEditor().getText());
                case "certificateDate" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_CERTIFICATE_DATE, ((DatePicker) item).getEditor().getText());
                case "tnvedCode" -> propService.saveProp(V_OBOROT_PRODUCTION_RF_TNVED, ((TextField) item).getText());
                case VSD_NUMBER -> propService.saveProp(V_OBOROT_PRODUCTION_RF_VSD, ((TextField) item).getText());
                case "productionOrder" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_PRODUCTION_ORDER, (String) ((ComboBox) item).getSelectionModel().getSelectedItem());
                case "certificateType" ->
                        propService.saveProp(V_OBOROT_PRODUCTION_RF_CERT_TYPE, (String) ((ComboBox) item).getSelectionModel().getSelectedItem());
            }
        }
    }

    private void saveVOborotTransgranDefaults() {
        for (var item : crossborderList.getItems()) {
            switch (((Control) item).getId()) {
                case USER_INN -> propService.saveProp(V_OBOROT_TRANSGRAN_INN_UOT, ((TextField) item).getText());
                case SENDER_INN -> propService.saveProp(V_OBOROT_TRANSGRAN_INN_SENDER, ((TextField) item).getText());
                case EXPORTER_NAME ->
                        propService.saveProp(V_OBOROT_TRANSGRAN_SENDER_NAME, ((TextField) item).getText());
                case COUNTRY_OKSM ->
                        propService.saveProp(V_OBOROT_TRANSGRAN_COUNTRY_OKSM, ((TextField) item).getText());
                case IMPORT_DATE ->
                        propService.saveProp(V_OBOROT_TRANSGRAN_IMPORT_DATE, ((DatePicker) item).getEditor().getText());
                case PRIMARY_DOC_NUM ->
                        propService.saveProp(V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_NUMBER, ((TextField) item).getText());
                case PRIMARY_DOC_DATE ->
                        propService.saveProp(V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_DATE, ((DatePicker) item).getEditor().getText());
                case VSD_NUMBER -> propService.saveProp(V_OBOROT_TRANSGRAN_VSD, ((TextField) item).getText());
            }
        }
    }

    private void saveVvodVOborotImportFtsDefaults() {
        propService.saveProp(V_OBOROT_IMPORT_FTS_INN, vOborotImportFtsINN.getText());
    }

    private void saveVvodVOborotOstatkiDefaults() {
        propService.saveProp(V_OBOROT_OSTATKI_INN, userINNText.getText());
    }

    private void saveAggregationDefaults() {
        propService.saveProp(AGGREGATION_DOC_NUM, aggregationDocNum.getText());
        propService.saveProp(AGGREGATION_INN, aggregationINN.getText());
        propService.saveProp(AGGREGATION_ORG_NAME, aggregationOrgName.getText());
        propService.saveProp(AGGREGATION_IS_IP, aggregationIpCB.isSelected());
    }

    private void savePrintDefaults() {
        GoodCategory goodCategory = category.getSelectionModel().getSelectedItem();
        propService.saveProp(CATEGORY, goodCategory.name());

        propService.saveProp(LABEL_HEIGHT, height.getText());
        propService.saveProp(LABEL_WIDTH, width.getText());
        propService.saveProp(LABEL_TEXT, userLabelText.getText());
        propService.saveProp(LABEL_START_NUMBER, markNumberStart.getText());
        propService.saveProp(LABEL_NUMERATE, printMarkNumber.isSelected());
        propService.saveProp(LABEL_SIZE_A4, sizeA4.isSelected());
        propService.saveProp(LABEL_SIZE_TERMOPRINTER, sizeTermoPrinter.isSelected());
        propService.saveProp(LABEL_VERTICAL, isVertical.isSelected());
        propService.saveProp(LABEL_HORIZONTAL, isHorizontal.isSelected());
    }

    private void hideTabs() {
        TabProps.INSTANCE.updateProperties();
        java.util.List<Tab> visibleTabs = new ArrayList<>();
        for (Tab tab : allTabs) {
            if (TabProps.INSTANCE.isVisible(tab.getId(),
                    category.getSelectionModel().getSelectedItem(), isPackageType)) {
                visibleTabs.add(tab);
            }
        }
        tabPaneElement.getTabs().setAll(visibleTabs);
    }


    public void openTabProperties(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(TabPropertiesController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Настройка вкладок");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        TabPropertiesController controller = fxWeaver.getBean(TabPropertiesController.class);
        controller.setTabs(allTabs);
        controller.filData();
        stage.showAndWait();

        hideTabs();
    }

    public void openCatalogProperties(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(CatalogController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Настройка каталога");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void openWaterLicenses() {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(WaterLicenseController.class);
        Stage stage = new Stage();
        WaterLicenseController controller = fxWeaver.getBean(WaterLicenseController.class);
        controller.setWaterLicenses(waterLicenses);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Лицензии на добычу воды");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
        updateWaterLicenseCounter(productionList);
    }

    public void validationButtonAction(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(XmlValidatorController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Валидация документа");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void openPrintProperties(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(PrintPropertiesController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Настройка печати");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        fxWeaver.getBean(PrintPropertiesController.class).fillData();
        stage.showAndWait();
    }

    public void openSystemProperties(ActionEvent actionEvent) {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(SystemPropertiesController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Системные настройки");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        fxWeaver.getBean(SystemPropertiesController.class).fillData();
        stage.showAndWait();
    }

    public void openNews() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/fxml/news.fxml"));
        Parent root1 = (Parent) fxmlLoader.load();
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Новости");
        Scene scene = new Scene(root1);
        stage.setScene(scene);
        stage.setHeight(propService.getFloatProp(NEWS_WINDOW_HEIGHT));
        stage.setWidth(propService.getFloatProp(NEWS_WINDOW_WIDTH));
        stage.showAndWait();
    }

    public void exportPropertiesAction(ActionEvent actionEvent) {
        Stage stage = (Stage) fileNameLabel.getScene().getWindow();
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Сохранить настройки");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Properties (*.json)", "*.json"));
        fileChooser.setInitialFileName("*.json");
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            if (!file.getName().contains(".")) {
                file = new File(file.getAbsolutePath() + ".json");
            }
            propService.saveProps(propService.exportProps(), file);
        }
    }

    public void importPropertiesAction(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Выберите файл с сохраненными настройками");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("json", "*.json"));
        File file = fileChooser.showOpenDialog(new Stage());
        if (file != null) {
            propService.importProps(file);
        }
    }

    public void enableUpdateAction(ActionEvent actionEvent) {
        propService.saveProp(CHECK_UPDATES, updatesMenuItem.isSelected());
    }

    public void onCloseActions() {
        saveDefaults();
    }

    private void performAction(BaseDocService service) {
        try {
            if (isPickingOrder(service.getContext().getCsvFileName())
                    && !propService.getBooleanProp(USE_DEFAULT_SAVE_FOLDER)
            ) {
                DirectoryChooser directoryChooser = new DirectoryChooser();
                directoryChooser.setTitle("Выберите папку для сохранения");

                File folder = directoryChooser.showDialog(new Stage());
                if (folder != null) {
                    service.getContext().setResultFolder(folder.getAbsolutePath());
                } else {
                    return;
                }
            }
            boolean validationEnabled = propService.getBooleanProp(XML_VALIDATION_ENABLED);
            File xmlFile = service.generateResult();
            boolean fileIsValid = true;
            if (service.isXmlResult() && validationEnabled) {
                fileIsValid = Validator.isXmlValid(xmlFile);
            }
            if (fileIsValid) {
                System.out.println(" Finish Printing " + new Date().getTime());
                new Alert(Alert.AlertType.INFORMATION, "Файл сохранен в " + xmlFile.getAbsolutePath()).showAndWait();
            } else {
                String message = String.format("Файл сохранен в %s. %nСгенерированный файл не соответствует xsd схеме. %nВоспользуйтесь функцией валидации для получения дополнительной информации.",
                        xmlFile.getAbsolutePath());
                new Alert(Alert.AlertType.WARNING, message).showAndWait();
            }

        } catch (IllegalArgumentException ex) {
            log.error("Ошибка обработки документа: ", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        } catch (Exception e) {
            String message = "Error in : " + service.getServiceName();
            log.error(message, e);
            new Alert(Alert.AlertType.ERROR, "Unexpected exception: " + e.getMessage()).showAndWait();
        }
    }

    private boolean isPickingOrder(String csvFileName) {
        return PickingOrderService.isPickingOrder(csvFileName);
    }

    private boolean performPackageAction(BaseDocService service) {
        try {
            service.generateResult();
            return true;
        } catch (IllegalArgumentException ex) {
            log.error("Ошибка обработки документа: ", ex);
            return false;
        } catch (Exception e) {
            String message = "Error in : " + service.getServiceName();
            log.error(message, e);
            return false;
        }

    }

    public String getResultFolder() {
        return resultFolder;
    }

    public void setResultFolder(String resultFolder) {
        this.resultFolder = resultFolder;
    }

    public void aggregationClearSsccButtonAction(ActionEvent actionEvent) {
        aggregationSccCodeText.setText("");
        aggregationSccCodeText.requestFocus();
    }

    public List<WaterLicense> getWaterLicenses() {
        return waterLicenses;
    }
}
