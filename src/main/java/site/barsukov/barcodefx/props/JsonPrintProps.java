package site.barsukov.barcodefx.props;

import site.barsukov.barcodefx.model.enums.SysPropType;

public class JsonPrintProps extends JsonBaseProp {
    public enum PrintProp implements IProp {
        USER_LABEL_FONT_SIZE("Размер шрифта текста пользователя", "9"),
        LABEL_FONT_SIZE("Размер шрифта текста этикетки (gtin и серийный номер)", "8"),
        DATAMATRIX_IMAGE_WIDTH("Ширина датаматрикса в pt", "62"),
        DATAMATRIX_IMAGE_HEIGHT("Высота датаматрикса в pt", "62"),
        EAN_IMAGE_WIDTH("Ширина sscc (ean) в pt", "132"),
        EAN_IMAGE_HEIGHT("Высота sscc (ean) в pt", "62"),
        HORIZONTAL_A4_LABEL_WIDTH("Ширина этикетки при горизонтальной печати на А4 в pt", "192"),
        HORIZONTAL_A4_LABEL_HEIGHT("Высота этикетки при горизонтальной печати на А4 в pt", "94"),
        VERTICAL_A4_LABEL_WIDTH("Ширина этикетки при вертикальной печати на А4 в pt", "90"),
        VERTICAL_A4_LABEL_HEIGHT("Высота этикетки при вертикальной печати на А4 в pt", "102"),
        NUMBER_OF_HORIZONTAL_COLUMNS("Количество столбцов при горизонтальной печати А4 ", "3"),
        NUMBER_OF_VERTICAL_COLUMNS("Количество столбцов при вертикальной печати А4 ", "6"),
        LABEL_A4_MARGIN_BOTTOM("Значение отступа снизу от края листа при печати этикеток А4 в pt", "43"),
        LABEL_A4_MARGIN_RIGHT("Значение отступа справа от края листа при печати этикеток А4 в pt", "12"),
        LABEL_A4_MARGIN_TOP("Значение отступа сверху от края листа при печати этикеток А4 в pt", "10"),
        LABEL_A4_MARGIN_LEFT("Значение отступа слева от края листа при печати этикеток А4 в pt", "13"),
        LABEL_A4_TEXT_MARGIN_LEFT("Значение отступа слева текста от края этикетки  при печати этикеток А4 в pt", "2"),
        LABEL_CUSTOM_MARGIN_TOP("Значение отступа сверху от края листа при печати этикеток для термопринтера в pt", "0"),
        LABEL_CUSTOM_MARGIN_RIGHT("Значение отступа справа от края листа при печати этикеток для термопринтера в pt", "0"),
        LABEL_CUSTOM_MARGIN_BOTTOM("Значение отступа снизу от края листа при печати этикеток для термопринтера в pt", "0"),
        LABEL_CUSTOM_MARGIN_LEFT("Значение отступа слева от края листа при печати этикеток для термопринтера в pt", "0"),
        DATAMATRIX_IMAGE_MARGIN_TOP("Значение отступа сверху от рамки до изображения датаматрикса в pt", "1"),
        EAN_IMAGE_MARGIN_TOP("Значение отступа сверху от рамки до изображения sscc (ean) в pt", "30"),
        DATAMATRIX_IMAGE_MARGIN_RIGHT("Значение отступа справа от рамки до изображения датаматрикса в pt", "1"),
        EAN_IMAGE_MARGIN_RIGHT("Значение отступа справа от рамки до изображения sscc (ean) в pt", "3"),
        DATAMATRIX_IMAGE_MARGIN_BOTTOM("Значение отступа снизу от рамки до изображения датаматрикса в pt", "1"),
        EAN_IMAGE_MARGIN_BOTTOM("Значение отступа снизу от рамки до изображения sscc (ean) в pt", "10"),
        DATAMATRIX_IMAGE_MARGIN_LEFT("Значение отступа слева от рамки до изображения датаматрикса в pt", "10"),
        EAN_IMAGE_MARGIN_LEFT("Значение отступа слева от рамки до изображения sscc (ean) в pt", "5"),
        KM_CHECK_ENABLED("Проверка кодов маркировки при разборе", "true"),
        KM_CHECK_TAIL_ENABLED("Проверка криптохвоста маркировки при печати", "true"),
        XML_VALIDATION_ENABLED("Проверка соответствия xml файла xsd схеме после генерации", "true"),
        LABEL_COUNTER_PREFIX("Значение префикса в марке перед порядковым номером", "index:"),
        DATAMATRIX_PER_TEMPLATE_LABEL("Количество кодов, передаваемых на один шаблон этикетки", "1"),
        USE_PACKAGE_WORK_TYPE("Обрабатывать файлы пачками.(Все в папке)", "false"),
        LABEL_A4_DISABLE_BOARDERS("Убрать рамки при печати на А4", "false");

        private String descr;
        private String defaultValue;
        private SysPropType sysPropType = SysPropType.PRINT;

        PrintProp(String descr, String defaultValue) {
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
