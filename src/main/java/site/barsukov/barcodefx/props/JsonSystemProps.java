package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public class JsonSystemProps extends JsonBaseProp {
    public enum SystemProp implements IProp {
        NEWS_WINDOW_HEIGHT("Высота окна с новостями", "782"),
        NEWS_WINDOW_WIDTH("Ширина окна с новостями", "1277"),
        USE_DEFAULT_SAVE_FOLDER("Всегда сохранять файлы в папку по умолчанию", "false"),
        DEFAULT_SAVE_FOLDER("Папка по умолчанию для сохранения файлов", ""),
        DEFAULT_INTERFACE("Интерфейс по умолчанию (NONE, CLASSIC, SCANNER)", "NONE"),
        SERVERS_PARAMS("Параметры подключения к серверам", "");

        private String descr;
        private String defaultValue;
        private SysPropType sysPropType = SysPropType.SYSTEM;

        SystemProp(String descr, String defaultValue) {
            this.descr = descr;
            this.defaultValue = defaultValue;
        }

        public String getDescr() {
            return descr;
        }

        public String getDefaultValue() {
            return defaultValue;
        }

        public SysPropType getSysPropType() {
            return sysPropType;
        }
    }

}
