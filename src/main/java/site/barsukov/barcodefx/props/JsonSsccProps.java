package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public class JsonSsccProps extends JsonBaseProp {
    public enum SsccProp implements IProp {

        LAST_PREFIX("", "0"),
        LAST_EXTENSION("", "0"),
        LAST_SERIAL("", "0");
        private String descr;
        private String defaultValue;
        private SysPropType sysPropType = SysPropType.SSCC;

        SsccProp(String descr, String defaultValue) {
            this.descr = descr;
            this.defaultValue = defaultValue;
        }

        public String getDescr() {
            return descr;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        @Override
        public SysPropType getSysPropType() {
            return sysPropType;
        }
    }

}
