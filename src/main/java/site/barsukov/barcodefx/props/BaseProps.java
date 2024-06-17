package site.barsukov.barcodefx.props;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.Properties;

public class BaseProps extends Properties {
    final static Logger logger = Logger.getLogger(BaseProps.class);

    float getFloatProperty(String propertyName, float defaultValue) {
        try {
            return Float.parseFloat(this.getProperty(propertyName).trim());
        } catch (Exception e) {
            logger.warn("Параметр " + propertyName + " не найден. Возвращаем дефолтное значение - " + defaultValue);
            return defaultValue;
        }
    }

    String getStringProperty(String propertyName, String defaultValue) {
        try {

            String result = this.getProperty(propertyName).trim();
            if (StringUtils.isBlank(result)) {
                logger.warn("Параметр " + propertyName + " не найден. Возвращаем дефолтное значение - " + defaultValue);
                return defaultValue;
            } else {
                return result;
            }
        } catch (Exception e) {
            logger.warn("Параметр " + propertyName + " не найден. Возвращаем дефолтное значение - " + defaultValue);
            return defaultValue;
        }
    }

    boolean getBooleanProperty(String propertyName, boolean defaultValue) {
        try {
            String value = this.getProperty(propertyName).trim();
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return Boolean.valueOf(value);
        } catch (Exception e) {
            logger.warn("Параметр " + propertyName + " не найден. Возвращаем дефолтное значение - " + defaultValue);
            return defaultValue;
        }
    }

}
