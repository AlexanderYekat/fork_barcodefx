package site.barsukov.barcodefx.services;

import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxWeaver;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.controllers.AboutController;
import site.barsukov.barcodefx.model.AboutResource;
import site.barsukov.barcodefx.model.AboutTextEnum;

@Service
@RequiredArgsConstructor
public class AboutService {

    private final FxWeaver fxWeaver;

    public void openAboutWindow(AboutTextEnum textType) {
        Parent root = fxWeaver.loadView(AboutController.class, new AboutResource(textType));
        var stage = new Stage();
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("About");
        var scene = new Scene(root);
        stage.setScene(scene);
        stage.showAndWait();
    }
}
