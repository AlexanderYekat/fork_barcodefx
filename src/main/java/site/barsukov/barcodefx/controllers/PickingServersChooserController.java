package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.PickingServer;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.services.PickingOrderService;
import site.barsukov.barcodefx.services.PropService;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("/fxml/picking_server_chooser.fxml")
public class PickingServersChooserController implements Initializable {
    final static Logger logger = Logger.getLogger(PickingServersChooserController.class);

    public Label pickingOrdersCount;
    public ComboBox<PickingServer> serversListCB;
    private List<PickingOrder> pickingOrderList;
    @Autowired
    private PropService propService;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            serversListCB.getItems().addAll(propService.getServers());
            if (!serversListCB.getItems().isEmpty()) {
                serversListCB.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            logger.error("Error initializing PickingServersChooserController", e);
        }
    }

    public void setOrders(List<PickingOrder> pickingOrderList) {
        this.pickingOrderList = pickingOrderList;
        pickingOrdersCount.setText(Integer.toString(pickingOrderList.size()));
    }

    public void sendOrdersButtonAction(ActionEvent actionEvent) {
        PickingServer server = serversListCB.getSelectionModel().getSelectedItem();
        if (server != null) {
            try {
                PickingOrderService.sendOrdersToRemoteServer(server, pickingOrderList);
                Stage stage = (Stage) pickingOrdersCount.getScene().getWindow();
                new Alert(Alert.AlertType.INFORMATION, "Ордера отправлены").showAndWait();
                stage.close();
            } catch (Exception e) {
                logger.error("Error sending files to server:", e);
                new Alert(Alert.AlertType.ERROR, "Ошибка при отправке ордеров на сервер.").showAndWait();
            }
        }
    }
}
