package site.barsukov.barcodefx.services;

import com.google.common.collect.Sets;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;
import site.barsukov.barcodefx.model.VersionAnswerDto;

import java.awt.*;
import java.net.URI;
import java.util.Optional;
import java.util.Set;

import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.*;

@Slf4j
@RequiredArgsConstructor
@org.springframework.stereotype.Service
public class UpdateService extends Service {

    private final VersionService versionService;
    private final PropService propService;


    @Override
    protected Task createTask() { //todo why?
        return new Task() {
            @Override
            protected Object call() throws Exception {
                try {
                    Thread.sleep(15000);
                } catch (InterruptedException e) {
                    log.error("Unexpected interruption ", e);
                }
                Platform.runLater(() -> {
                    if (propService.getBooleanProp(CHECK_UPDATES)) {
                        checkVersion();
                    }
                });
                return null;
            }
        };
    }

    private void checkVersion() {
        String curVersion = getClass().getPackage().getImplementationVersion();
        VersionAnswerDto answer = versionService.getLastVersionInfo();
        Set<String> ignorableVersions = getIgnorableVersions();
        if (answer != null
            && !answer.getVersion().equals(curVersion)
            && !ignorableVersions.contains(answer.getVersion())) {

            ButtonType ignoreButton = new ButtonType("Пропустить версию", ButtonBar.ButtonData.NEXT_FORWARD);
            ButtonType okButton = new ButtonType("Да", ButtonBar.ButtonData.OK_DONE);
            ButtonType cancelButton = new ButtonType("Нет", ButtonBar.ButtonData.CANCEL_CLOSE);
            Alert confirmWindow = new Alert(
                Alert.AlertType.CONFIRMATION,
                "Скачать новую версию?", ignoreButton, cancelButton, okButton);

            confirmWindow.setHeaderText(String.format("Доступна новая версия программы - %s.", answer.getVersion()));
            confirmWindow.setTitle("Проверка обновлений");
            Optional<ButtonType> result = confirmWindow.showAndWait();
            if (result.isPresent() && result.get() == okButton) {
                try {
                    Desktop.getDesktop().browse(new URI(answer.getUrl()));
                } catch (Exception e) {
                    log.error("Error while opening update page: ", e);
                }
            } else if (result.isPresent() && result.get() == ignoreButton) {
                ignorableVersions.add(answer.getVersion());
                saveIgnorableVersions(ignorableVersions);
            }

        }
    }

    private Set<String> getIgnorableVersions() {
        String ignorableString = propService.getProp(IGNORABLE_VERSIONS);
        return Sets.newHashSet((ignorableString.split(";")));
    }

    private synchronized void saveIgnorableVersions(Set<String> ignorableVersions) {
        propService.saveProp(IGNORABLE_VERSIONS, String.join(";", ignorableVersions));
    }
}
