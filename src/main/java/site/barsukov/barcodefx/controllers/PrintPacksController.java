package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.context.DataMatrixContext;
import site.barsukov.barcodefx.model.AboutTextEnum;
import site.barsukov.barcodefx.services.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@FxmlView("/fxml/print_packs.fxml")
public class PrintPacksController implements Initializable {
    static final Logger logger = Logger.getLogger(PrintPacksController.class);
    private final AboutService aboutService;
    private final CatalogService catalogService;
    public CheckBox endToEndNumbering;
    public TextField packSize;
    public CheckBox groupByGtin;
    private DataMatrixContext dataMatrixContext;
    private boolean useTemplate;
    private Boolean success;
    @Autowired
    private final MarkingCodeService markingCodeService;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
    }

    public void printButtonAction(ActionEvent actionEvent) {
        try {
            success = false;
            if (!groupByGtin.isSelected()) {
                printPacks(new File(dataMatrixContext.getCsvFileName()));
            } else {
                List<File> groupedFiles = groupFilesByGtin(new File(dataMatrixContext.getCsvFileName()));
                for (var curFile : groupedFiles) {
                    printPacks(curFile);
                }
            }
            success = true;
        } catch (IOException e) {
            logger.error("Error while printing packs", e);
        } finally {
            Stage stage = (Stage) packSize.getScene().getWindow();
            stage.getScene().setUserData(success);
            stage.close();
        }
    }

    private List<File> groupFilesByGtin(File csvFile) throws IOException {
        List<File> result = new ArrayList<>();
        Map<String, List<String>> groupedCodes = new HashMap<>();
        List<String> lines = FileUtils.readLines(csvFile, "UTF-8")
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());

        for (var line : lines) {
            var gtin = getGtin(line);
            var groupedList = groupedCodes.getOrDefault(gtin, new ArrayList<>());
            groupedList.add(line);
            groupedCodes.put(gtin, groupedList);
        }

        for (var set : groupedCodes.entrySet()) {
            var fileName = FilenameUtils.getName(csvFile.getAbsolutePath());
            var filePath = FilenameUtils.getFullPath(csvFile.getAbsolutePath());
            var newFile = new File(filePath + set.getKey() + "_" + fileName);
            FileUtils.writeLines(newFile, set.getValue());
            result.add(newFile);
        }
        return result;
    }

    private static String getGtin(String code) {
        if (code == null || code.length() < 16) {
            return "unknown_gtin";
        } else {
            return code.substring(0, 16);
        }

    }

    private void printPacks(File csvFile) throws IOException {
        dataMatrixContext.setCsvFileName(csvFile.getAbsolutePath());
        List<String> lines = FileUtils.readLines(csvFile, "UTF-8")
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        int packSizeInt = Integer.parseInt(packSize.getText());
        int totalSize = lines.size();
        dataMatrixContext.setMarkNumberStart(1);

        for (int i = 1; i <= totalSize; i += packSizeInt) {
            dataMatrixContext.setRangeEnabled(true);
            dataMatrixContext.setRangeFrom(Integer.toString(i));
            int rangeTo = i + packSizeInt - 1;
            if (rangeTo > totalSize) {
                rangeTo = totalSize;
            }
            dataMatrixContext.setRangeTo(Integer.toString(rangeTo));
            if (endToEndNumbering.isSelected()) {
                dataMatrixContext.setMarkNumberStart(i);
                dataMatrixContext.setTotalPageNumber(totalSize);
            }
            if (useTemplate) {
                PDFTemplatesCreateService pdfTemplatesCreateService = new PDFTemplatesCreateService(dataMatrixContext, catalogService);
                pdfTemplatesCreateService.performAction();
            } else {
                PDFCreateService pdfCreateService = new PDFCreateService(dataMatrixContext);
                pdfCreateService.performAction();
            }
        }
    }

    public void setDataMatrixContext(DataMatrixContext dataMatrixContext) {
        this.dataMatrixContext = dataMatrixContext;
    }

    public void setUseTemplate(boolean useTemplate) {
        this.useTemplate = useTemplate;
    }

    public void openAboutPackPrintAction(MouseEvent mouseEvent) {
        aboutService.openAboutWindow(AboutTextEnum.PACK_PRINTING);
    }
}
