package site.barsukov.barcodefx.controllers;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.PropDto;
import site.barsukov.barcodefx.model.enums.SysPropType;
import site.barsukov.barcodefx.props.JsonAppProps;
import site.barsukov.barcodefx.props.JsonSystemProps;
import site.barsukov.barcodefx.services.PropService;

import java.net.URL;
import java.util.ResourceBundle;

@Component
@FxmlView("/fxml/system_properties.fxml")
@RequiredArgsConstructor
public class SystemPropertiesController implements Initializable {
    public TableView<PropDto> propList;
    private final PropService propService;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<PropDto, String> propName = new TableColumn<>("Название");
        propName.setCellValueFactory(new PropertyValueFactory<>("name"));
        propList.getColumns().add(propName);

        TableColumn<PropDto, String> propDescr = new TableColumn<>("Описание");
        propDescr.setCellValueFactory(new PropertyValueFactory<>("descr"));
        propList.getColumns().add(propDescr);

        TableColumn<PropDto, String> propValue = new TableColumn<>("Значение");
        propValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        propValue.setEditable(true);
        propValue.setCellFactory(TextFieldTableCell.forTableColumn());
        propValue.setOnEditCommit(new EventHandler<>() {
            @Override
            public void handle(TableColumn.CellEditEvent<PropDto, String> event) {
                (event.getTableView().getItems().get(
                        event.getTablePosition().getRow())
                ).setValue(event.getNewValue());
            }
        });

        propList.getColumns().add(propValue);

        propList.setEditable(true);
    }

    public void okButtonAction(ActionEvent actionEvent) {
        for (PropDto curProp : propList.getItems()) {
            propService.saveProp(curProp);
        }
        Stage stage = (Stage) propList.getScene().getWindow();
        stage.close();
    }

    public void cancelButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) propList.getScene().getWindow();
        stage.close();
    }

    public void fillData() {
        propList.getItems().addAll(propService.getPropsByType(SysPropType.SYSTEM));
    }
}