package site.barsukov.barcodefx.controllers;

import com.google.common.collect.ImmutableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.rgielen.fxweaver.core.FxWeaver;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.ArchivedPickingOrder;
import site.barsukov.barcodefx.model.PickingOrdersResource;
import site.barsukov.barcodefx.model.PickingServer;
import site.barsukov.barcodefx.model.yo.pickingorder.CodesTypeType;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.services.PickingOrderService;
import site.barsukov.barcodefx.services.PropService;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.ListFiller.fillArchivedPickingOrdersTable;
import static site.barsukov.barcodefx.ListFiller.fillPickingOrdersTable;

@Slf4j
@Component
@RequiredArgsConstructor
@FxmlView("/fxml/picking_order_list.fxml")
public class PickingOrderListController implements Initializable {
    private final PropService propService;
    private final FxWeaver fxWeaver;

    public TableView<PickingOrder> ordersTableView;
    public TableView<ArchivedPickingOrder> archiveTableView;
    public TableView<PickingOrder> remoteOrdersTableView;
    public ComboBox<PickingServer> remoteOrdersServersCB;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            fillPickingOrdersTable(ordersTableView);
            fillPickingOrdersTable(remoteOrdersTableView);
            fillArchivedPickingOrdersTable(archiveTableView);
            refreshLocalOrdersList();
            refreshLocalArchiveOrdersList();
            PickingOrderService.clearRemoteCache();
            refreshRemoteOrdersList();
            archiveTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            remoteOrdersServersCB.getItems().addAll(propService.getServers());
            if (!remoteOrdersServersCB.getItems().isEmpty()) {
                remoteOrdersServersCB.getSelectionModel().select(0);
            }
            remoteOrdersServersCB.getSelectionModel().selectedItemProperty().addListener(
                    (options, oldValue, newValue) -> {
                        try {
                            PickingOrderService.clearRemoteCache();
                            remoteOrdersTableView.getItems().clear();
                        } catch (Exception e) {
                            log.error("Error clearing remote cache: ", e);
                        }
                    }
            );

            ordersTableView.setRowFactory(tv -> {
                TableRow<PickingOrder> row = new TableRow<>();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        chooseOrder(row.getItem());
                    }
                });
                return row;
            });
            ordersTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        } catch (Exception e) {
            log.error("Error initializing picking order view", e);
        }
    }

    public void chooseOrderButtonAction(ActionEvent actionEvent) {
        PickingOrder selectedItem = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.INFORMATION, "Пикинг ордер не выбран").showAndWait();
        } else {
            chooseOrder(selectedItem);
        }
    }

    private void chooseOrder(PickingOrder selectedItem) {
        Stage stage = (Stage) ordersTableView.getScene().getWindow();
        File fileWithCodes = new File(PickingOrderService.LOCAL_PICKING_ORDERS_PATH + selectedItem.getFileName());
        ordersTableView.getScene().setUserData(fileWithCodes);
        stage.close();
    }

    public void addOrderFromFileButtonAction(ActionEvent actionEvent) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Выберите файл с кодами маркировки");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("csv, txt", "*.csv", "*.txt"));

            File csvFile = fileChooser.showOpenDialog(new Stage());
            if (csvFile != null) {
                if (PickingOrderService.isOrderExist(csvFile)) {
                    Alert confirmWindow = new Alert(
                            Alert.AlertType.CONFIRMATION,
                            "Пикинг ордер с таким файлом кодов уже существует. Заменить?");
//                   confirmWindow.setHeaderText(String.format("Доступна новая версия программы - %s.", answer.getVersion()));
//                   confirmWindow.setTitle("Проверка обновлений");
                    Optional<ButtonType> result = confirmWindow.showAndWait();
                    if (result.isPresent() && result.get() == ButtonType.OK) {
                        openAddPickingOrderDialog(csvFile);
                    }
                } else {
                    openAddPickingOrderDialog(csvFile);
                }
            }
            refreshLocalOrdersList();
        } catch (Exception e) {
            log.error("Error opening picking order list", e);
        }
    }

    public void sendToArchiveButtonAction(ActionEvent actionEvent) {
        try {
            for (PickingOrder pickingOrder : ordersTableView.getSelectionModel().getSelectedItems()) {
                PickingOrderService.archivePickingOrder(pickingOrder);
            }

            refreshLocalOrdersList();
            refreshLocalArchiveOrdersList();
        } catch (IOException e) {
            log.error("Error updating picking order meta info", e);
        }
    }

    public void editOrderButtonAction(ActionEvent actionEvent) {
        PickingOrder selectedItem = ordersTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            new Alert(Alert.AlertType.INFORMATION, "Пикинг ордер не выбран").showAndWait();
        } else {
            openAddPickingOrderDialog(
                    new File(PickingOrderService.LOCAL_PICKING_ORDERS_PATH + File.separatorChar + selectedItem.getFileName()),
                    selectedItem);
        }
        refreshLocalOrdersList();
    }

    private File openAddPickingOrderDialog(File file) {
        return openAddPickingOrderDialog(file, null);
    }

    private File openAddPickingOrderDialog(File file, PickingOrder pickingOrder) {
        Parent root = fxWeaver.loadView(PickingOrderAddController.class);
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Добавление пикинг ордера");
        Scene scene = new Scene(root);
        PickingOrderAddController controller = fxWeaver.getBean(PickingOrderAddController.class);
        controller.setFile(file);
        controller.setPickingOrder(pickingOrder);
        stage.setScene(scene);
        stage.showAndWait();
        return (File) scene.getUserData();
    }

    private void refreshLocalOrdersList() {
        ordersTableView.getItems().clear();
        ordersTableView.getItems().addAll(PickingOrderService.getLocalPickingOrders());
    }

    private void refreshLocalArchiveOrdersList() {
        archiveTableView.getItems().clear();
        try {
            archiveTableView.getItems().addAll(PickingOrderService.getLocalArchivePickingOrders());
        } catch (IOException e) {
            log.error("Error refreshing local archive list: ", e);
        }
    }

    private void refreshRemoteOrdersList() throws IOException {
        remoteOrdersTableView.getItems().clear();
        remoteOrdersTableView.getItems().addAll(PickingOrderService.getRemotePickingOrders());
    }

    public void sendToServerButtonAction(ActionEvent actionEvent) {
        try {
            Parent root = fxWeaver.loadView(PickingServersChooserController.class);
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Отправка пикинг ордеров");
            Scene scene = new Scene(root);

            stage.setResizable(false);
            PickingServersChooserController controller = fxWeaver.getBean(PickingServersChooserController.class);
            controller.setOrders(ordersTableView.getSelectionModel().getSelectedItems());
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            log.error("Error sending picking orders to server:", e);
        }
    }

    public void sendFromArchiveButtonAction(ActionEvent actionEvent) {
        try {
            List<ArchivedPickingOrder> selectedItems = archiveTableView.getSelectionModel().getSelectedItems();
            if (selectedItems != null
                    && !selectedItems.isEmpty()
                    && showConfirmationWindow(String.format("Количество записей для восстановления - %s. Восстановить?", selectedItems.size()))) {

                PickingOrderService.returnFromArchivePickingOrder(selectedItems);
                refreshLocalOrdersList();
                refreshLocalArchiveOrdersList();
            }
        } catch (IOException e) {
            log.error("Error sendFromArchiveButtonAction ", e);
        }
    }

    public void createXmlButtonAction() {
        var items = ordersTableView.getSelectionModel().getSelectedItems().stream()
                .filter(i -> i.getCodesType().equals(CodesTypeType.AGGREGATE))
                .collect(Collectors.toList());
        if (items.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Не выбрано пикинг ордеров с типом агрегат").showAndWait();
            return;
        }

        openAggregateMultiCreateDialog(items);
    }

    private void openAggregateMultiCreateDialog(List<PickingOrder> pickingOrders) {
        Parent root = fxWeaver.loadView(AggregateMultiCreateController.class);
        Stage stage = new Stage();
        stage.setOnHiding(event -> refreshLocalOrdersList());

        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Массовое создание агрегатов");
        Scene scene = new Scene(root);

        stage.setResizable(false);
        AggregateMultiCreateController controller = fxWeaver.getBean(AggregateMultiCreateController.class);
        controller.setPickingOrders(pickingOrders);
        stage.setScene(scene);
        stage.showAndWait();
    }

    public void deleteFromArchiveButtonAction() {
        List<ArchivedPickingOrder> selectedArchivedPickingOrders = archiveTableView.getSelectionModel().getSelectedItems();
        if (selectedArchivedPickingOrders != null
                && !selectedArchivedPickingOrders.isEmpty()
                && showConfirmationWindow(String.format("Количество записей для удаления - %s. Удалить?", selectedArchivedPickingOrders.size())
        )) {
            PickingOrderService.removeArchivedOrders(selectedArchivedPickingOrders);
        }
        refreshLocalArchiveOrdersList();
    }


    public void downloadRemoteOrderButtonAction(ActionEvent actionEvent) {
        List<PickingOrder> selectedRemotePickingOrders = remoteOrdersTableView.getSelectionModel().getSelectedItems();
        try {
            if (selectedRemotePickingOrders != null
                    && !selectedRemotePickingOrders.isEmpty()
                    && showConfirmationWindow(String.format("Количество записей для загрузки - %s. Загрузить?", selectedRemotePickingOrders.size())
            )) {
                PickingOrderService.downloadRemoteOrders(remoteOrdersServersCB.getSelectionModel().getSelectedItem(), selectedRemotePickingOrders);
            }
            refreshLocalOrdersList();
        } catch (IOException e) {
            log.error("Error downloadRemoteOrderButtonAction: ", e);
        }
    }

    public void removeRemoteOrderButtonAction(ActionEvent actionEvent) {
        List<PickingOrder> selectedRemotePickingOrders = remoteOrdersTableView.getSelectionModel().getSelectedItems();
        try {
            if (selectedRemotePickingOrders != null
                    && !selectedRemotePickingOrders.isEmpty()
                    && showConfirmationWindow(String.format("Количество записей для удаления - %s. Удалить?", selectedRemotePickingOrders.size())
            )) {
                PickingOrderService.removeRemoteOrders(remoteOrdersServersCB.getSelectionModel().getSelectedItem(),
                        selectedRemotePickingOrders);
            }
        } catch (IOException e) {
            log.error("Error removeRemoteOrderButtonAction:", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка удаления файлов").showAndWait();
        }
        refreshRemoteCache();
    }

    private boolean showConfirmationWindow(String message) {
        Alert confirmWindow = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Подтвердите операцию");
        confirmWindow.setHeaderText(message);
        confirmWindow.setTitle("Подтверждение операции");
        Optional<ButtonType> result = confirmWindow.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    public void refreshRemotePickingsButtonAction(ActionEvent actionEvent) {
        refreshRemoteCache();
    }

    private void refreshRemoteCache() {
        try {
            PickingOrderService.clearRemoteCache();
            PickingOrderService.getRemoteCash(remoteOrdersServersCB.getSelectionModel().getSelectedItem());
            remoteOrdersTableView.getItems().clear();
            remoteOrdersTableView.getItems().addAll(PickingOrderService.getRemotePickingOrders());
        } catch (IOException e) {
            log.error("Error refreshing remote cache", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка обновления списка файлов").showAndWait();
        }
    }

    public void saveFullCodesToFileButtonAction(ActionEvent actionEvent) {
        try {
            var selectedOrders = ordersTableView.getSelectionModel().getSelectedItems();
            if (selectedOrders != null && !selectedOrders.isEmpty()) {
                Stage stage = (Stage) ordersTableView.getScene().getWindow();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Сохранить настройки");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Codes (*.txt)", "*.txt"));
                fileChooser.setInitialFileName("*.txt");
                File destFile = fileChooser.showSaveDialog(stage);
                if (destFile != null) {
                    for (var curOrder : selectedOrders) {
                        var curFile = PickingOrderService.getFileWithCodes(curOrder);
                        LineIterator iterator = FileUtils.lineIterator(curFile, "UTF-8");
                        while (iterator.hasNext()) {
                            FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(iterator.nextLine()), true);
                        }
                        iterator.close();
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Коды успешно сохранены").showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error while saveFullCodesToFileButtonAction", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения полных кодов в файл").showAndWait();
        }
    }

    public void saveShortCodesToFileButtonAction(ActionEvent actionEvent) {
        try {
            var selectedOrders = ordersTableView.getSelectionModel().getSelectedItems();
            if (selectedOrders != null && !selectedOrders.isEmpty()) {
                Stage stage = (Stage) ordersTableView.getScene().getWindow();
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Сохранить настройки");
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Codes (*.txt)", "*.txt"));
                fileChooser.setInitialFileName("*.txt");
                File destFile = fileChooser.showSaveDialog(stage);
                if (destFile != null) {
                    for (var curOrder : selectedOrders) {
                        var curFile = PickingOrderService.getFileWithCodes(curOrder);
                        LineIterator iterator = FileUtils.lineIterator(curFile, "UTF-8");
                        while (iterator.hasNext()) {
                            var splitted = iterator.nextLine().split("\\u001D");
                            FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(splitted[0]), true);
                        }
                        iterator.close();
                    }
                    new Alert(Alert.AlertType.INFORMATION, "Коды успешно сохранены").showAndWait();
                }
            }
        } catch (Exception e) {
            log.error("Error while saveShortCodesToFileButtonAction", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения кодов в файл").showAndWait();
        }
    }

    public void saveCodesForEdoLiteButtonAction(ActionEvent actionEvent) {
        var selectedOrders = ordersTableView.getSelectionModel().getSelectedItems();
        if (selectedOrders == null || selectedOrders.isEmpty()) {
            return;
        }
        Parent root = fxWeaver.loadView(SaveCodesForEdoLiteController.class, new PickingOrdersResource(selectedOrders));
        Stage stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Сохранение кодов");
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
