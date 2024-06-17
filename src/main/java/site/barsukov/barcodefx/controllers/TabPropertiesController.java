package site.barsukov.barcodefx.controllers;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.Callback;
import net.rgielen.fxweaver.core.FxmlView;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.TabVisibleDto;
import site.barsukov.barcodefx.props.TabProps;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

@Component
@FxmlView("/fxml/tab_properties.fxml")
public class TabPropertiesController implements Initializable {
    static final Logger logger = Logger.getLogger(TabPropertiesController.class);
    public TableView<TabVisibleDto> tabsList;
    private List<Tab> allTabs;


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        TableColumn<TabVisibleDto, String> tabName = new TableColumn<TabVisibleDto, String>("Вкладка");
        tabName.setCellValueFactory(new PropertyValueFactory<TabVisibleDto, String>("name"));
        tabsList.getColumns().add(tabName);

        tabsList.setEditable(true);
        TableColumn<TabVisibleDto, Boolean> visibility = new TableColumn<>("Видимость");

        visibility.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<TabVisibleDto, Boolean>, ObservableValue<Boolean>>() {

            @Override
            public ObservableValue<Boolean> call(TableColumn.CellDataFeatures<TabVisibleDto, Boolean> param) {
                TabVisibleDto dto = param.getValue();

                SimpleBooleanProperty booleanProp = new SimpleBooleanProperty(dto.isVisible());

                booleanProp.addListener(new ChangeListener<Boolean>() {

                    @Override
                    public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                                        Boolean newValue) {
                        dto.setVisible(newValue);
                    }
                });
                return booleanProp;
            }
        });

        visibility.setCellFactory(new Callback<TableColumn<TabVisibleDto, Boolean>, //
            TableCell<TabVisibleDto, Boolean>>() {
            @Override
            public TableCell<TabVisibleDto, Boolean> call(TableColumn<TabVisibleDto, Boolean> p) {
                CheckBoxTableCell<TabVisibleDto, Boolean> cell = new CheckBoxTableCell<TabVisibleDto, Boolean>();
                cell.setAlignment(Pos.CENTER);
                return cell;
            }
        });

        tabsList.getColumns().add(visibility);

    }


    public void okButtonAction(ActionEvent actionEvent) {
        TabProps.INSTANCE.updateProperties();
        for (TabVisibleDto curTab : tabsList.getItems()) {
            TabProps.INSTANCE.setVisible(curTab.getId(), curTab.isVisible());
        }
        TabProps.INSTANCE.saveProps();
        Stage stage = (Stage) tabsList.getScene().getWindow();
        stage.close();
    }

    public void cancelButtonAction(ActionEvent actionEvent) {
        Stage stage = (Stage) tabsList.getScene().getWindow();
        stage.close();
    }

    public void setTabs(List<Tab> allTabs) {
        this.allTabs = allTabs;
    }

    public void filData() {
        TabProps.INSTANCE.updateProperties();
        for (Tab curTab : allTabs) {
            tabsList.getItems().add(new TabVisibleDto(curTab.getId(), curTab.getText(),
                TabProps.INSTANCE.isVisibleInProps(curTab.getId())));
        }
    }
}
