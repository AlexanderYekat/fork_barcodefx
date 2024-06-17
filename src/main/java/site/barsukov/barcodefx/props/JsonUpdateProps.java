package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public class JsonUpdateProps extends JsonBaseProp {
    public enum UpdateProp implements IProp {

        CHECK_UPDATES("Проверять обновления", "true"),
        STATS_ENABLED("Отправлять статистику", "true"),
        STATS_URL("Адрес для отправки статистики", "http://fx.mark-solutions.ru:65344"),
        CLIENT_ID("ID экземпляра программы", "UNKNOWN"),
        SESSION_ID("ID сессии", "UNKNOWN"),
        APP_VERSION("Версия программы", "UNKNOWN"),
        IGNORABLE_VERSIONS("Пропускаемые версии", "");
        private String descr;
        private String defaultValue;
        private SysPropType sysPropType = SysPropType.UPDATE;

        UpdateProp(String descr, String defaultValue) {
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
