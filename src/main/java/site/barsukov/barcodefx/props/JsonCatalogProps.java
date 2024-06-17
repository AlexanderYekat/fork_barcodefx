package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public class JsonCatalogProps extends JsonBaseProp {
    public enum CatalogProp implements IProp {

        FILE_PATH("Путь к файлу каталога", "Файл не выбран"),
        CATALOG_ENABLED("Использовать каталог", "false");
        private String descr;
        private String defaultValue;
        private SysPropType sysPropType = SysPropType.CATALOG;

        CatalogProp(String descr, String defaultValue) {
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
