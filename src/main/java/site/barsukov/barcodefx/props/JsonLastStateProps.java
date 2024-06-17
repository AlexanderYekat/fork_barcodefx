package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;
import site.barsukov.barcodefx.services.PickingOrderService;

public class JsonLastStateProps extends JsonBaseProp {
    public enum LastStateProp implements IProp {

        CATEGORY("Последняя выбранная категория", "SHOES"),
        LABEL_HEIGHT("Высота этикетки", "113"),
        LABEL_WIDTH("Ширина этикетки", "164"),
        LABEL_TEXT("Текст на этикетке", ""),
        LABEL_START_NUMBER("Начинать нумерацию с", "1"),
        LABEL_NUMERATE("Нумеровать", "false"),
        LABEL_SIZE_A4("Этикетка А4", "true"),
        LABEL_SIZE_TERMOPRINTER("Этикетка термопринтера", "false"),
        LABEL_HORIZONTAL("Этикетка горизонтальная", "true"),
        LABEL_VERTICAL("Этикетка вертикальная", "false"),

        AGGREGATION_ORG_NAME("Агрегация название организации", ""),
        AGGREGATION_DOC_NUM("Агрегация номер документа", ""),
        AGGREGATION_IS_IP("Агрегация признак ИП ", "false"),
        AGGREGATION_INN("Агрегация ИНН", ""),

        PICKING_ORDER_LAST_COMMENT("Прошлый комментарий", ""),
        PICKING_ORDER_LAST_STATUS("Прошлый статус", ""),
        PICKING_ORDER_LAST_TYPE("Прошлый тип кодов", "KM"),
        PICKING_ORDER_LAST_ENABLE_PRINTING("Печатать", "false"),
        PICKING_ORDER_LAST_PRINT_PROFILE("Профиль печати", ""),

        AGG_MULTI_CREATE_INN("Прошлый ИНН организации", ""),
        AGG_MULTI_CREATE_ORG_NAME("Прошлое название организации", ""),
        AGG_MULTI_CREATE_CHANGE_STATUS("Прошлый переключатель смены статуса", "false"),
        AGG_MULTI_CREATE_NEW_STATUS("Прошлый статус", ""),
        AGG_MULTI_SAVE_DIRECTORY("Прошлая папка для сохранения агрегатов", PickingOrderService.LOCAL_PICKING_ORDERS_PATH),

        V_OBOROT_OSTATKI_INN("Ввод в оборот остатки ИНН", ""),
        V_OBOROT_IMPORT_FTS_INN("Ввод в оборот импорт ФТС ИНН", ""),
        V_OBOROT_TRANSGRAN_INN_UOT("Ввод в оборот трансграничный ИНН УОТ-а", ""),
        V_OBOROT_TRANSGRAN_INN_SENDER("Ввод в оборот трансграничный ИНН отправителя", ""),
        V_OBOROT_TRANSGRAN_SENDER_NAME("Ввод в оборот трансграничный имя отправителя", ""),
        V_OBOROT_TRANSGRAN_COUNTRY_OKSM("Ввод в оборот трансграничный код страны", ""),
        V_OBOROT_TRANSGRAN_IMPORT_DATE("Ввод в оборот трансграничный дата импорта", ""),
        V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_NUMBER("Ввод в оборот номер первичного документа", ""),
        V_OBOROT_TRANSGRAN_PRIMARY_DOCUMENT_DATE("Ввод в оборот дата первичного документа", ""),
        V_OBOROT_TRANSGRAN_VSD("Ввод в оборот трансграничный ВСД", ""),
        V_OBOROT_PRODUCTION_RF_PARTICIPANT_INN("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_PRODUCER_INN("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_OWNER_INN("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_CERT_NUMBER("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_PRODUCT_DATE("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_CERTIFICATE_DATE("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_TNVED("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_VSD("Ввод в оборот производство РФ", ""),
        V_OBOROT_PRODUCTION_RF_PRODUCTION_ORDER("Ввод в оборот производство РФ", "OWN_PRODUCTION"),
        V_OBOROT_PRODUCTION_RF_CERT_TYPE("Ввод в оборот производство РФ", "CONFORMITY_CERTIFICATE"),
        SCANNER_CONTROLLER_PORT("Окно сканирования", "COM1"),
        SCANNER_CONTROLLER_BIT("Окно сканирования", "5"),
        SCANNER_CONTROLLER_STOP_BIT("Окно сканирования", "1"),
        SCANNER_CONTROLLER_SPEED("Окно сканирования", "115200"),
        INTERFACE_SCANNER_CONTROLLER_PORT("Окно сканирования - отдельный интерфейс", "COM1"),
        INTERFACE_SCANNER_CONTROLLER_BIT("Окно сканирования - отдельный интерфейс", "5"),
        INTERFACE_SCANNER_CONTROLLER_STOP_BIT("Окно сканирования - отдельный интерфейс", "1"),
        INTERFACE_SCANNER_CONTROLLER_SPEED("Окно сканирования - отдельный интерфейс", "115200"),
        ;

        private final String descr;
        private final String defaultValue;
        private final SysPropType sysPropType = SysPropType.LAST_STATE;

        LastStateProp(String descr, String defaultValue) {
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
