package site.barsukov.barcodefx.model.enums;

public enum RemarkCause {
    KM_SPOILED("Испорчено либо утеряно СИ с КМ"),
//    DESCRIPTION_ERRORS("Выявлены ошибки описания товара "),
    RETAIL_RETURN("Возврат товаров с поврежденным СИ/без СИ при розничной реализации"),
    REMOTE_SALE_RETURN("Возврат товаров с поврежденным СИ/без СИ при дистанционном способе продажи");
    private String uiName;

    RemarkCause(String uiName) {
        this.uiName = uiName;
    }
}
