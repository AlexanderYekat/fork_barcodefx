package site.barsukov.barcodefx.controllers;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.ListFiller;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.controllers.elements.MyLabel;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.services.BaseDocService;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.StatisticService;
import site.barsukov.barcodefx.utils.ScannerUtils;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.props.JsonLastStateProps.LastStateProp.*;
import static site.barsukov.barcodefx.props.JsonLastStateProps.LastStateProp.SCANNER_CONTROLLER_PORT;
import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.CHECK_UPDATES;

@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/scanner_interface.fxml")
public class ScannerInterfaceController implements Initializable {
    private static SerialPort serialPort;
    private static PortReader portReader;
    private final PropService propService;
    private final ConfigurableApplicationContext applicationContext;
    private final StatisticService statisticService;
    public ComboBox<GoodCategory> category;
    @FXML
    private Button saveFileButton;
    @FXML
    private ListView<MyLabel> textList;
    @FXML
    private TextArea textInputField;
    @FXML
    private TextField alternativeSeparator;
    @FXML
    private ToggleButton soundButton;
    @FXML
    private Label scannedNumberCountLabel;
    @FXML
    private Label scannedErrorsNumber;
    @FXML
    private CheckBox alternativeSeparatorCheckBox;
    @FXML
    private ComboBox<String> port;
    @FXML
    private ComboBox<String> bit;
    @FXML
    private ComboBox<String> stopBit;
    @FXML
    private ComboBox<String> speed;
    @FXML
    private ToggleButton comPotConnectionButton;

    private String buffer = "";

    public void savePickingOrderButtonAction(ActionEvent actionEvent) {
        try {
            File tempFile = File.createTempFile("scanned_", ".txt");
            tempFile.deleteOnExit();
            saveTextToFile(tempFile);
            File resultFile = openAddPickingOrderDialog(tempFile);
            if (resultFile == null) {
                return;
            }
            saveFileButton.getScene().setUserData(resultFile);
            GoodCategory goodCategory = category.getSelectionModel().getSelectedItem();
            statisticService.logAction("SCAN_ITEMS_IN_PICKING_ORDER", textList.getItems().size(), goodCategory);
            clearData();
        } catch (Exception e) {
            log.error("Error while saving codes to file ", e);
        }
    }

    private File openAddPickingOrderDialog(File file) throws IOException {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(PickingOrderAddController.class);

        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Добавление пикинг ордера");
        Scene scene = new Scene(root);
        PickingOrderAddController controller = fxWeaver.getBean(PickingOrderAddController.class);
        controller.setFile(file);
        controller.setGoodCategory(category.getSelectionModel().getSelectedItem());
        stage.setScene(scene);
        stage.showAndWait();
        return (File) scene.getUserData();
    }

    public void onEnter() {
        if (!StringUtils.isBlank(textInputField.getText())) {
            String text = textInputField.getText().trim();
            if (alternativeSeparatorCheckBox.isSelected()) {
                text = text.replace(alternativeSeparator.getText(), String.valueOf('\u001D'));
            }
            addText(text);
            textInputField.setText("");
        }
    }

    private void addLine(String text) {
        GoodCategory goodCategory = category.getSelectionModel().getSelectedItem();
        text = ScannerUtils.fixLine(text, goodCategory);
        MyLabel label = new MyLabel(text);
        try {
            if (!Validator.isKMValid(BaseDocService.createKM(label.getText(), goodCategory), goodCategory)
                    || !Validator.isKMAIValid(label.getText(), goodCategory)
                    || !Validator.isKMTotalLengthValid(BaseDocService.createKM(label.getText(), goodCategory), goodCategory)
            ) {
                label.setTextFill(Color.rgb(255, 0, 0));
                label.setErrorCode(true);
                playSound();
                label.setTooltip(new Tooltip("Ошибка разбора КМ. Подробности в файле логов."));
            }
        } catch (Exception e) {
            playSound();
            throw e;
        }
        textList.getItems().add(label);
    }

    private void addText(String text) {
        String[] lines = text.split("\r\n|\r|\n");
        for (String curString : lines) {
            addLine(curString);
        }
    }

    public void soundButtonAction(ActionEvent actionEvent) {
        if (soundButton.isSelected()) {
            soundButton.setText("Звук ВЫКЛ");
        } else {
            soundButton.setText("Звук ВКЛ");
        }
    }


    public void deleteDoubles(ActionEvent actionEvent) {
        List<MyLabel> distinctList = textList.getItems()
                .stream()
                .distinct()
                .collect(Collectors.toList());
        textList.getItems().setAll(distinctList);
        recount();
    }

    public void deleteErrors(ActionEvent actionEvent) {
        List<MyLabel> distinctList = textList.getItems()
                .stream()
                .filter(s -> !s.isErrorCode())
                .collect(Collectors.toList());
        textList.getItems().setAll(distinctList);
        recount();
    }


    public void deleteSelectedItem(ActionEvent actionEvent) {
        List<Integer> selected = textList.getSelectionModel().getSelectedIndices();
        for (Integer curInt : selected) {
            textList.getItems().remove((int) curInt);
        }
        recount();
    }

    private void playSound() {
        if (!soundButton.isSelected()) {
            Toolkit.getDefaultToolkit().beep();
        }
    }

    private void clearData() {
        textList.getItems().clear();
        textList.refresh();
        recount();
    }

    private void recount() {
        textList.refresh();
        scannedNumberCountLabel.setText(Integer.toString(textList.getItems().size()));
        scannedErrorsNumber.setText(Long.toString(textList.getItems().stream().filter(MyLabel::isErrorCode).count()));
    }

    private void saveTextToFile(File file) throws IOException {
        FileUtils.writeLines(file,
                "UTF-8",
                textList.getItems()
                        .stream()
                        .map(Labeled::getText)
                        .collect(Collectors.toList()));
    }

    public void alternativeSeparatorAction(ActionEvent actionEvent) {
        if (alternativeSeparatorCheckBox.isSelected()) {
            alternativeSeparator.setDisable(false);
        } else {
            alternativeSeparator.setDisable(true);
        }

    }

    public void comPortConnectAction(ActionEvent actionEvent) {
        try {
            saveDefaults();
            comPortConnect();
            new Alert(Alert.AlertType.INFORMATION, "OK").showAndWait();
        } catch (IllegalArgumentException ex) {
            log.error("Error", ex);
            new Alert(Alert.AlertType.ERROR, ex.getMessage()).showAndWait();
        } catch (Exception e) {
            log.error("Error", e);
            new Alert(Alert.AlertType.ERROR, "Unexpected exception: " + e.getMessage()).showAndWait();
        }
    }

    private void comPortConnect() {
        Stage stage = (Stage) saveFileButton.getScene().getWindow();
        stage.setOnCloseRequest(event -> closeComPort());
        if (comPotConnectionButton.isSelected()) {
            isDisabledComElements(true);
            comPotConnectionButton.setText("Отключить");
            //Передаём в конструктор имя порта
            serialPort = new SerialPort(port.getValue());
            try {
                //Открываем порт
                serialPort.openPort();
                //Выставляем параметры
                serialPort.setParams(Integer.parseInt(speed.getValue()),
                        Integer.parseInt(bit.getValue()),
                        Integer.parseInt(stopBit.getValue()),
                        SerialPort.PARITY_NONE);
                //Включаем аппаратное управление потоком
                serialPort.setFlowControlMode(SerialPort.FLOWCONTROL_RTSCTS_IN |
                        SerialPort.FLOWCONTROL_RTSCTS_OUT);
                //Устанавливаем ивент лисенер и маску
                serialPort.addEventListener(portReader, SerialPort.MASK_RXCHAR);
            } catch (SerialPortException ex) {
                log.error("Error", ex);
                throw new IllegalArgumentException("Ошибка подключения сканера");
            }
        } else {
            isDisabledComElements(false);
            comPotConnectionButton.setText("Подключить");
            try {
                serialPort.closePort();
            } catch (SerialPortException e) {
                log.error("Error", e);
                throw new IllegalArgumentException("Ошибка отключения сканера");
            }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        textList.getItems().addListener(new ListChangeListener() {
            @Override
            public void onChanged(ListChangeListener.Change change) {
                Platform.runLater(() -> {
                    recount();
                });

            }
        });
        textInputField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent keyEvent) {
                if (keyEvent.getCode() == KeyCode.ENTER) {
                    onEnter();
                    // clear text
                    textInputField.setText("");
                }
            }
        });

        List<String> ports = new ArrayList<>();
        for (int i = 1; i <= 100; i++) {
            ports.add("COM" + i);
        }
        ObservableList<String> options =
                FXCollections.observableArrayList(
                        ports
                );
        port.setItems(options);
        port.getSelectionModel().select(0);

        ObservableList<String> bitOptions =
                FXCollections.observableArrayList(
                        "5",
                        "6",
                        "7",
                        "8"
                );
        bit.setItems(bitOptions);
        bit.getSelectionModel().select(0);
        ObservableList<String> speedOptions =
                FXCollections.observableArrayList(
                        "1200",
                        "4800",
                        "9600",
                        "14400",
                        "19200",
                        "38400",
                        "57600",
                        "115200",
                        "128000",
                        "256000"
                );
        speed.setItems(speedOptions);
        speed.getSelectionModel().select(0);
        ObservableList<String> stopbitOptions =
                FXCollections.observableArrayList(
                        "1",
                        "2",
                        "3"
                );
        stopBit.setItems(stopbitOptions);
        stopBit.getSelectionModel().select(0);

        portReader = new PortReader();

        ListFiller.fillCategories(category);
        category.getSelectionModel().select(0);
        setDefaults();
    }

    private void setDefaults() {
        speed.getSelectionModel().select(propService.getProp(INTERFACE_SCANNER_CONTROLLER_SPEED));
        stopBit.getSelectionModel().select(propService.getProp(INTERFACE_SCANNER_CONTROLLER_STOP_BIT));
        bit.getSelectionModel().select(propService.getProp(INTERFACE_SCANNER_CONTROLLER_BIT));
        port.getSelectionModel().select(propService.getProp(INTERFACE_SCANNER_CONTROLLER_PORT));

    }

    private void saveDefaults() {
        propService.saveProp(INTERFACE_SCANNER_CONTROLLER_SPEED, speed.getSelectionModel().getSelectedItem());
        propService.saveProp(INTERFACE_SCANNER_CONTROLLER_STOP_BIT, stopBit.getSelectionModel().getSelectedItem());
        propService.saveProp(INTERFACE_SCANNER_CONTROLLER_BIT, bit.getSelectionModel().getSelectedItem());
        propService.saveProp(INTERFACE_SCANNER_CONTROLLER_PORT, port.getSelectionModel().getSelectedItem());
    }

    private void isDisabledComElements(boolean source) {
        port.setDisable(source);
        bit.setDisable(source);
        stopBit.setDisable(source);
        speed.setDisable(source);

    }

    public void openSystemPropsButtonAction(ActionEvent actionEvent) throws IOException {
        FxWeaver fxWeaver = applicationContext.getBean(FxWeaver.class);
        Parent root = fxWeaver.loadView(SystemPropertiesController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Системные настройки");
        stage.setScene(new Scene(root));
        fxWeaver.getBean(SystemPropertiesController.class).fillData();
        stage.showAndWait();
    }

    public void openPickingOrdersButtonAction(ActionEvent actionEvent) {
        try {
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
                FileUtils.readLines(fileWithCodes, "UTF-8").forEach(this::addLine);
            }

        } catch (Exception e) {
            log.error("Error opening picking order list", e);
        }
    }

    private void closeComPort() {
        try {
            if (serialPort != null) serialPort.closePort();
        } catch (Exception e) {
            log.error("Error", e);
        }
    }

    private class PortReader implements SerialPortEventListener {

        public void serialEvent(SerialPortEvent event) {
            if (event.isRXCHAR() && event.getEventValue() > 0) {
                try {
                    //Получаем ответ от устройства, обрабатываем данные и т.д.
                    int bufferMax = category.getSelectionModel().getSelectedItem().getTotalLength();
                    String data = serialPort.readString(event.getEventValue())
                            .replace("\r", "").replace("\n", "");

                    buffer = buffer + data;
                    if (buffer.length() >= bufferMax) {
                        addText(buffer);
                        buffer = "";
                    }

                } catch (Exception ex) {
                    log.error("Error", ex);
                }
            }
        }
    }
}
