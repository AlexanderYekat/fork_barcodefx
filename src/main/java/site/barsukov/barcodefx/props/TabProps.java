package site.barsukov.barcodefx.props;

import org.apache.log4j.Logger;
import site.barsukov.barcodefx.model.enums.FunctionalTab;
import site.barsukov.barcodefx.model.enums.GoodCategory;

import java.io.*;
import java.nio.charset.StandardCharsets;


public class TabProps extends BaseProps {
    final static Logger logger = Logger.getLogger(TabProps.class);
    public static final TabProps INSTANCE = new TabProps();
    private static final String PATH = "tabs.properties";

    private TabProps() {
    }

    public void updateProperties() {
        try (FileInputStream fis = new FileInputStream(PATH)) {
            InputStreamReader isr = new InputStreamReader(fis, StandardCharsets.UTF_8);
            this.load(isr);
            logger.info(this);

        } catch (IOException io) {
            logger.error(io);
        }
    }

    public void saveProps() {
        try {
            File file = new File(PATH);
            file.createNewFile();
            FileWriter fileWriter = new FileWriter(PATH);
            this.store(fileWriter, "myComment");
        } catch (IOException e) {
            logger.error(e);
        }

    }

    public boolean isVisibleInProps(String tabName) {
        return getBooleanProperty(tabName, true);
    }

    public boolean isVisible(String tabName, GoodCategory category, boolean packageMode) {
        FunctionalTab functionalTab = FunctionalTab.getByUiName(tabName);

        return getBooleanProperty(tabName, true)
            && functionalTab.isCategoryAllowed(category)
            && functionalTab.checkPackageMode(packageMode);
    }

    public void setVisible(String tabName, boolean value) {
        this.setProperty(tabName, Boolean.toString(value));
    }


}
