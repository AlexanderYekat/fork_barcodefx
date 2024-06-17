package site.barsukov.barcodefx.controllers;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.printing.PdfPrintingProfile;
import site.barsukov.barcodefx.model.printing.ProfileType;
import site.barsukov.barcodefx.model.printing.ZplPrintingProfile;
import site.barsukov.barcodefx.model.printing.dao.PrintingProfileDao;
import site.barsukov.barcodefx.repository.PrintingProfileRepo;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static site.barsukov.barcodefx.ListFiller.fillTemplates;

@Component
@FxmlView("/fxml/printing_profile_editor.fxml")
public class PrintingProfileEditorController implements Initializable {
    static final Logger logger = Logger.getLogger(PrintingProfileEditorController.class);

    public RadioButton pdfMainCheckBox;
    public RadioButton zplMainCheckBox;
    public TextField profileName;
    public TextField zplIpPrinter;
    public TextField zplPortPrinter;
    public TextArea zplTemplate;
    public TextField pdfLabelWidth;
    public TextField pdfLabelHeight;
    public CheckBox pdfPrintMarkLabel;
    public TextField pdfMarkNumberStart;
    public ComboBox<String> pdfTemplateFileName;
    public RadioButton pdfIsA4;
    public RadioButton pdfIsTermoprinter;
    public RadioButton pdfIsHorizontal;
    public RadioButton pdfIsVertical;
    public ListView<String> printingProfileList;

    @Autowired
    private PrintingProfileRepo printingProfileRepo;

    private ObjectMapper mapper;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        refreshProfileList();
        pdfMainCheckBox.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                if (new_val) {
                    enablePdfElements();
                    disableZplElements();
                }
            });

        zplMainCheckBox.selectedProperty().addListener(
            (ObservableValue<? extends Boolean> ov, Boolean old_val, Boolean new_val) -> {
                if (new_val) {
                    enableZplElements();
                    disablePdfElements();
                }
            });

        printingProfileList.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            resetValues(newValue);
        });

        groupRadioButtons();
        fillTemplates(pdfTemplateFileName);

        profileName.setText("Новый профиль");

        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    private void resetValues(String newValue) {
        try {
            if (StringUtils.isBlank(newValue)) {
                return;
            }
            var entity = printingProfileRepo.getFirstByName(newValue).get();
            profileName.setText(entity.getName());
            if (entity.getType() == ProfileType.ZPL) {
                zplMainCheckBox.setSelected(true);
                var template = mapper.readValue(entity.getProfile(), ZplPrintingProfile.class);
                zplTemplate.setText(template.getTemplate());
                zplIpPrinter.setText(template.getPrinterIp());
                zplPortPrinter.setText(Integer.toString(template.getPrinterPort()));
                setPdfToDefault();
            } else {
                pdfMainCheckBox.setSelected(true);
                var template = mapper.readValue(entity.getProfile(), PdfPrintingProfile.class);
                pdfIsA4.setSelected(!template.isTermoPrinter());
                pdfIsTermoprinter.setSelected(template.isTermoPrinter());
                pdfIsHorizontal.setSelected(template.isHorizontal());
                pdfIsVertical.setSelected(template.isVertical());
                pdfLabelHeight.setText(template.getHeight());
                pdfLabelWidth.setText(template.getWidth());
                pdfMarkNumberStart.setText(Integer.toString(template.getMarkNumberStart()));
                pdfPrintMarkLabel.setSelected(template.getPrintMarkNumber());
                pdfTemplateFileName.getSelectionModel().select(template.getTemplateFileName());
                setZplToDefault();
            }
        } catch (Exception e) {
            logger.error("Error reading printing profile: ", e);
        }
    }

    private void setPdfToDefault() {
        pdfIsA4.setSelected(true);
        pdfIsHorizontal.setSelected(true);
        pdfLabelHeight.setText("");
        pdfLabelWidth.setText("");
        pdfMarkNumberStart.setText("");
        pdfPrintMarkLabel.setSelected(false);
        pdfTemplateFileName.getSelectionModel().select(0);
    }

    private void setZplToDefault() {
        zplTemplate.setText("");
        zplPortPrinter.setText("");
        zplIpPrinter.setText("");
    }

    private void refreshProfileList() {
        printingProfileList.getItems().clear();
        var allProfiles = StreamSupport.stream(printingProfileRepo.findAll().spliterator(), false)
            .map(PrintingProfileDao::getName).collect(Collectors.toList());
        printingProfileList.getItems().addAll(allProfiles);
    }

    private void groupRadioButtons() {
        //основные чекбоксы
        ToggleGroup mainGroup = new ToggleGroup();
        pdfMainCheckBox.setToggleGroup(mainGroup);
        pdfMainCheckBox.setSelected(true);
        zplMainCheckBox.setToggleGroup(mainGroup);

        //чекбокс формата
        ToggleGroup sizeGroup = new ToggleGroup();
        pdfIsA4.setToggleGroup(sizeGroup);
        pdfIsA4.setSelected(true);
        pdfIsTermoprinter.setToggleGroup(sizeGroup);

        //чекбокс ориентации
        ToggleGroup orientationGroup = new ToggleGroup();
        pdfIsHorizontal.setToggleGroup(orientationGroup);
        pdfIsHorizontal.setSelected(true);
        pdfIsVertical.setToggleGroup(orientationGroup);
    }

    private void disableZplElements() {
        zplIpPrinter.setDisable(true);
        zplPortPrinter.setDisable(true);
        zplTemplate.setDisable(true);
    }

    private void enableZplElements() {
        zplIpPrinter.setDisable(false);
        zplPortPrinter.setDisable(false);
        zplTemplate.setDisable(false);
    }

    private void disablePdfElements() {
        pdfIsA4.setDisable(true);
        pdfIsTermoprinter.setDisable(true);
        pdfIsHorizontal.setDisable(true);
        pdfIsVertical.setDisable(true);
        pdfLabelHeight.setDisable(true);
        pdfLabelWidth.setDisable(true);
        pdfMarkNumberStart.setDisable(true);
        pdfPrintMarkLabel.setDisable(true);
        pdfTemplateFileName.setDisable(true);
    }

    private void enablePdfElements() {
        pdfIsA4.setDisable(false);
        pdfIsTermoprinter.setDisable(false);
        pdfIsHorizontal.setDisable(false);
        pdfIsVertical.setDisable(false);
        pdfLabelHeight.setDisable(false);
        pdfLabelWidth.setDisable(false);
        pdfMarkNumberStart.setDisable(false);
        pdfPrintMarkLabel.setDisable(false);
        pdfTemplateFileName.setDisable(false);
    }

    public void saveProfileButtonAction(ActionEvent actionEvent) {
        try {
            var entity = printingProfileRepo.getFirstByName(profileName.getText().trim()).orElseGet(PrintingProfileDao::new);
            entity.setName(profileName.getText().trim());
            entity.setType(zplMainCheckBox.isSelected() ? ProfileType.ZPL : ProfileType.PDF);
            entity.setProfile(getProfileString());
            printingProfileRepo.save(entity);
        } catch (Exception e) {
            logger.error("Ошибка сохранения профиля печати: ", e);
            new Alert(Alert.AlertType.ERROR, "Ошибка сохранения профиля печати. Подробности в логе").showAndWait();
        }
        refreshProfileList();
    }

    private String getProfileString() throws JsonProcessingException {
        if (zplMainCheckBox.isSelected()) {
            var template = new ZplPrintingProfile();
            template.setName(profileName.getText().trim());
            template.setPrinterIp(zplIpPrinter.getText().trim());
            template.setPrinterPort(Integer.parseInt(zplPortPrinter.getText().trim()));
            template.setTemplate(zplTemplate.getText());
            return mapper.writeValueAsString(template);
        } else {
            var template = new PdfPrintingProfile();
            template.setName(profileName.getText().trim());
            template.setVertical(pdfIsVertical.isSelected());
            template.setHorizontal(pdfIsHorizontal.isSelected());
            template.setTermoPrinter(pdfIsTermoprinter.isSelected());
            template.setHeight(pdfLabelHeight.getText().trim());
            template.setWidth(pdfLabelWidth.getText().trim());
            template.setTemplateFileName(pdfTemplateFileName.getSelectionModel().getSelectedItem());
//            template.setUserLabelText();
            template.setPrintMarkNumber(pdfPrintMarkLabel.isSelected());
            template.setMarkNumberStart(Integer.parseInt(pdfMarkNumberStart.getText().trim()));
//            template.setPrintInPacks();
//            template.setPackSize();
            return mapper.writeValueAsString(template);
        }
    }

    public void deleteProfileButtonAction(ActionEvent actionEvent) {
        var entity = printingProfileRepo.getFirstByName(profileName.getText().trim());
        if (entity.isPresent()) {
            printingProfileRepo.delete(entity.get());
            refreshProfileList();
        }
    }


}
