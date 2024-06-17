package site.barsukov.barcodefx.props;

import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class JsonBaseProp {
    static final Logger logger = Logger.getLogger(JsonBaseProp.class);
    Map<IProp, String> props = new HashMap<>();

    public Map<IProp, String> getProps() {
        return props;
    }

    public void setProps(Map<IProp, String> props) {
        this.props = props;
    }

    public String getProp(IProp prop) {
        return props.getOrDefault(prop, prop.getDefaultValue());
    }

    public void setProp(IProp prop, String value) {
        props.put(prop, value);
    }

    public void setProp(IProp prop, Boolean value) {
        props.put(prop, Boolean.toString(value));
    }

    public Boolean getBooleanProp(IProp prop) {
        try {
            return Boolean.valueOf(props.getOrDefault(prop, prop.getDefaultValue()));
        } catch (Exception e) {
            logger.error("Get boolean prop error: ", e);
            return Boolean.valueOf(prop.getDefaultValue());
        }
    }

    public Float getFloatProp(IProp prop) {
        try {
            return Float.parseFloat(props.getOrDefault(prop, prop.getDefaultValue()));
        } catch (Exception e) {
            logger.error("Get float prop error: ", e);
            return Float.parseFloat(prop.getDefaultValue());
        }
    }

    public Integer getIntProp(IProp prop) {
        try {
            return Integer.parseInt(props.getOrDefault(prop, prop.getDefaultValue()));
        } catch (Exception e) {
            logger.error("Get integer prop error: ", e);
            return Integer.parseInt(prop.getDefaultValue());
        }
    }
}
