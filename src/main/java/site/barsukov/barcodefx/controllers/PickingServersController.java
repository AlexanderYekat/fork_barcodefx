package site.barsukov.barcodefx.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.BcfxFtpClient;
import site.barsukov.barcodefx.model.PickingServer;
import site.barsukov.barcodefx.services.PropService;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("/fxml/picking_servers.fxml")
public class PickingServersController implements Initializable {
    final static Logger logger = Logger.getLogger(PickingServersController.class);
    public TextField serverNameTF;
    public TextField serverFolderTF;
    public ListView<PickingServer> serversList;
    public RadioButton zcloudRb;
    public RadioButton folderRb;
    public RadioButton ftpRb;
    public TextField zcloudLoginTF;
    public TextField ftpPathTf;
    public TextField ftpPortTf;
    public TextField ftpLoginTf;
    public PasswordField ftpPasswordTf;
    public PasswordField zcloudPasswordTF;
    public Button testFtpConnectionButton;
    public CheckBox activeFtpMode;
    @Autowired
    private PropService propService;
    private ToggleGroup rbGroup = new ToggleGroup();
    private static final String Z_CLOUD_URL = "fx.mark-solutions.ru";

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try {
            refreshServerList();
            folderRb.setToggleGroup(rbGroup);
            folderRb.setSelected(true);
            ftpRb.setToggleGroup(rbGroup);
            zcloudRb.setToggleGroup(rbGroup);
            rbGroup.getSelectedToggle().selectedProperty().addListener((observable, oldValue, newValue) -> {
                disableAll();
                if (folderRb.isSelected()) {
                    enableFolder();
                } else if (ftpRb.isSelected()) {
                    enableFtp();
                } else if (zcloudRb.isSelected()) {
                    enableZcloud();
                }
            });

            serversList.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<PickingServer>() {

                @Override
                public void changed(ObservableValue<? extends PickingServer> observable, PickingServer oldValue, PickingServer newValue) {
                    if (newValue == null) {
                        return;
                    }
                    serverNameTF.setText(newValue.getName());

                    switch (newValue.getType()) {
                        case Z_CLOUD:
                            clearAll();
                            disableAll();
                            enableZcloud();
                            zcloudRb.setSelected(true);
                            zcloudPasswordTF.setText(newValue.getPassword());
                            zcloudLoginTF.setText(newValue.getLogin());
                            break;
                        case FOLDER:
                            clearAll();
                            disableAll();
                            enableFolder();
                            folderRb.setSelected(true);
                            serverFolderTF.setText(newValue.getPath());
                            break;
                        case FTP:
                            clearAll();
                            disableAll();
                            enableFtp();
                            ftpRb.setSelected(true);
                            ftpLoginTf.setText(newValue.getLogin());
                            ftpPasswordTf.setText(newValue.getPassword());
                            activeFtpMode.setSelected(newValue.isActive());
                            if (newValue.getPort() != null) {
                                ftpPortTf.setText(newValue.getPort().toString());
                            }

                            ftpPathTf.setText(newValue.getPath());
                            break;
                        default:
                            throw new IllegalStateException("Unexpected value: " + newValue.getType());
                    }
                }
            });
            if (!serversList.getItems().isEmpty()) {
                serversList.getSelectionModel().select(0);
            }
        } catch (Exception e) {
            logger.error("Error initializing servers: ", e);
        }
    }

    private void clearAll() {
        serverFolderTF.clear();
        ftpLoginTf.clear();
        ftpPasswordTf.clear();
        ftpPathTf.clear();
        ftpPortTf.clear();
        zcloudPasswordTF.clear();
        zcloudLoginTF.clear();
    }

    private void disableAll() {
        serverFolderTF.setDisable(true);
        ftpLoginTf.setDisable(true);
        ftpPasswordTf.setDisable(true);
        ftpPathTf.setDisable(true);
        ftpPortTf.setDisable(true);
        testFtpConnectionButton.setDisable(true);
        activeFtpMode.setDisable(true);
        zcloudPasswordTF.setDisable(true);
        zcloudLoginTF.setDisable(true);

    }

    private void enableFolder() {
        serverFolderTF.setDisable(false);
    }

    private void enableZcloud() {
        zcloudLoginTF.setDisable(false);
        zcloudPasswordTF.setDisable(false);
    }

    private void enableFtp() {
        ftpLoginTf.setDisable(false);
        ftpPasswordTf.setDisable(false);
        ftpPathTf.setDisable(false);
        ftpPortTf.setDisable(false);
        testFtpConnectionButton.setDisable(false);
        activeFtpMode.setDisable(false);
    }

    public void chooseFolderButtonAction(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Выберите общую папку для обмена");
        File directory = directoryChooser.showDialog(new Stage());
        if (directory != null) {
            serverFolderTF.setText(directory.getAbsolutePath());
        }
    }

    public void deleteButtonAction(ActionEvent actionEvent) throws JsonProcessingException {
        if (serversList.getSelectionModel().getSelectedItem() != null) {
            serversList.getItems().remove(serversList.getSelectionModel().getSelectedItem());
            try {
                saveServerList();
            } catch (Exception e) {
                logger.error("Error deleting server: ", e);
            }
        }
    }

    public void saveButtonAction(ActionEvent actionEvent) {
        PickingServer selectedServer = serversList.getSelectionModel().getSelectedItem();
        if (selectedServer == null && serversList.getItems().isEmpty()) {
            selectedServer = new PickingServer();
            serversList.getItems().add(selectedServer);
        }
        selectedServer.setName(serverNameTF.getText());
        RadioButton selectedToggle = (RadioButton) rbGroup.getSelectedToggle();
        if ("folderRb".equals(selectedToggle.getId())) {
            selectedServer.setPath(serverFolderTF.getText());
            selectedServer.setType(PickingServer.ServerType.FOLDER);
        } else if ("ftpRb".equals(selectedToggle.getId())) {
            selectedServer.setType(PickingServer.ServerType.FTP);
            selectedServer.setLogin(ftpLoginTf.getText().trim());
            selectedServer.setPassword(ftpPasswordTf.getText().trim());
            selectedServer.setPath(ftpPathTf.getText().trim());
            selectedServer.setActive(activeFtpMode.isSelected());
            try {
                selectedServer.setPort(Integer.parseInt(ftpPortTf.getText().trim()));
            } catch (Exception e) {
                logger.error("Error parsing ftp port: ", e);
            }

        } else if ("zcloudRb".equals(selectedToggle.getId())) {
            selectedServer.setType(PickingServer.ServerType.Z_CLOUD);
            selectedServer.setLogin(zcloudPasswordTF.getText().trim());
            selectedServer.setPassword(zcloudPasswordTF.getText().trim());
            selectedServer.setPath(Z_CLOUD_URL);
            selectedServer.setPort(65321);
            selectedServer.setActive(false);
        }

        try {
            saveServerList();
            refreshServerList();
        } catch (Exception e) {
            logger.error("Error saving servers list: ", e);
        }

    }

    public void addServerButtonAction(ActionEvent actionEvent) {
        PickingServer server = new PickingServer();
        server.setName("Новый сервер");
        server.setType(PickingServer.ServerType.FOLDER);
        serversList.getItems().add(server);
    }

    private void saveServerList() throws JsonProcessingException, GeneralSecurityException, UnsupportedEncodingException {
        propService.saveServers(serversList.getItems());
    }

    private void refreshServerList() throws IOException, GeneralSecurityException {
        List<PickingServer> servers = propService.getServers();
        serversList.getItems().clear();
        serversList.getItems().addAll(servers);
    }

    public void testFtpConnectionButtonAction(ActionEvent actionEvent) {
        PickingServer server = new PickingServer();
        server.setType(PickingServer.ServerType.FTP);
        server.setLogin(ftpLoginTf.getText().trim());
        server.setPassword(ftpPasswordTf.getText().trim());
        server.setPath(ftpPathTf.getText().trim());
        try {
            server.setPort(Integer.parseInt(ftpPortTf.getText().trim()));
        } catch (Exception e) {
            logger.error("Error parsing ftp port: ", e);
        }

        BcfxFtpClient ftpClient = new BcfxFtpClient(server);

        if (ftpClient.testConnection()) {
            String message = "Успешное подключение к серверу";
            new Alert(Alert.AlertType.INFORMATION, message).showAndWait();
        } else {
            String message = "Не удалось подключиться к серверу";
            new Alert(Alert.AlertType.ERROR, message).showAndWait();
        }
    }
}
