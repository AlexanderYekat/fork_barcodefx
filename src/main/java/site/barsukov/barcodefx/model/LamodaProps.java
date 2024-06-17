package site.barsukov.barcodefx.model;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class LamodaProps extends Properties {
    final static Logger logger = Logger.getLogger(LamodaProps.class);
    public static final LamodaProps INSTANCE = new LamodaProps();
    private static final String PATH = "LamodaGtin.txt";

    private LamodaProps() {
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

    public String getGtinDecr(String gtin) {
        String result = this.getProperty(gtin);
        if (result == null) {
            return result;
        }
        return this.getProperty(gtin).trim();
    }

    public void saveEmptyProp(String gtin) {
        File props = new File(PATH);
        try {
            FileUtils.writeStringToFile(props, gtin + "=\r\n", "UTF-8", true);
        } catch (IOException e) {
            logger.error("Ошибка запси Lamoda gtin ", e);
        }
    }

}
