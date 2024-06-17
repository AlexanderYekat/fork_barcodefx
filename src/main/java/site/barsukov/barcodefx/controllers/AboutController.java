package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.AboutTextEnum;
import site.barsukov.barcodefx.props.JsonAppProps;
import site.barsukov.barcodefx.services.PropService;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ResourceBundle;

import static site.barsukov.barcodefx.model.AboutTextEnum.ABOUT;
import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.STATS_ENABLED;

@Component
@FxmlView("/fxml/about.fxml")
public class AboutController implements Initializable {
    final static Logger logger = Logger.getLogger(AboutController.class);

    public TextArea texAreaAbout;
    public CheckBox statCollectAgreement;
    private AboutTextEnum aboutTextEnum;
    @Autowired
    private PropService propService;

    public void openHomePage(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://sourceforge.net/projects/barcodesfx/"));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        statCollectAgreement.setVisible(false);
        AboutTextEnum aboutTextEnum = (AboutTextEnum) resources.getObject(null);
        this.aboutTextEnum = aboutTextEnum;
        String path = "";

        switch (aboutTextEnum) {
            case ABOUT -> {
                path = "descr/about.txt";
                statCollectAgreement.setVisible(true);
                boolean statEnabled = propService.getBooleanProp(STATS_ENABLED);
                statCollectAgreement.setSelected(statEnabled);
            }
            case SERIAL_WHY -> path = "descr/whySerial.txt";
            case SSCC_ABOUT -> path = "descr/ssccAbout.txt";
            case AGGREGATION_NAME_ABOUT -> path = "descr/aggregationName.txt";
            case AGGREGATION_DOC_NUM -> path = "descr/aggregationDocNumAbout.txt";
            case WITHDRAW_EXPLANATION -> path = "descr/withdrawExplanation.txt";
            case TEMPLATES_EXPLANATION -> path = "descr/templatesExplanation.txt";
            case CROSSBORDER -> path = "descr/crossborderAbout.txt";
            case PACK_PRINTING -> path = "descr/packPrintingAbout.txt";
        }

        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(path);
        try {
            String text = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            texAreaAbout.setText(text);
        } catch (IOException e) {
            logger.error(e);
        }
    }

    public void saveProps() {
        if (aboutTextEnum == ABOUT) {
            propService.saveProp(STATS_ENABLED, statCollectAgreement.isSelected());
        }
    }
}
