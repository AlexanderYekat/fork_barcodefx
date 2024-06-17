package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;

public class NewsController implements Initializable {
    public WebView newsWebView;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        WebEngine webEngine = newsWebView.getEngine();
        String html = "<script type=\"text/javascript\" src=\"https://barsukov.site/informer/1\"></script>";

// Load a page from remote url.
        webEngine.loadContent(html);
    }

    public void openFullSite(ActionEvent actionEvent) throws URISyntaxException, IOException {
        Desktop.getDesktop().browse(new URI("https://barsukov.site/?utm_source=barCodesFX&utm_medium=app"));
    }
}
