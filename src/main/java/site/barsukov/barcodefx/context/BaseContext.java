package site.barsukov.barcodefx.context;

import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.props.JsonAppProps;
import site.barsukov.barcodefx.services.PropService;
import site.barsukov.barcodefx.services.StatisticService;

public abstract class BaseContext {
    private GoodCategory category;
    private String csvFileName;
    private boolean rangeEnabled;
    private String rangeFrom;
    private String rangeTo;
    private boolean validateKM;
    private boolean validateKMTail;
    private JsonAppProps jsonAppProps;
    private String resultFolder;
    private StatisticService statisticService;
    private PropService propService;

    public boolean isRangeEnabled() {
        return rangeEnabled;
    }

    public void setRangeEnabled(boolean rangeEnabled) {
        this.rangeEnabled = rangeEnabled;
    }

    public void setRangeFrom(String rangeFrom) {
        this.rangeFrom = rangeFrom;
    }

    public void setRangeTo(String rangeTo) {
        this.rangeTo = rangeTo;
    }

    public int getRangeFrom() {
        try {
            return Integer.parseInt(rangeFrom.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка определения начала диапазона строк для обработки.");
        }
    }

    public int getRangeTo() {
        try {
            return Integer.parseInt(rangeTo.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Ошибка определения конца диапазона строк для обработки.");
        }
    }

    public String getCsvFileName() {
        return csvFileName;
    }

    public void setCsvFileName(String csvFileName) {
        this.csvFileName = csvFileName;
    }

    public boolean isValidateKM() {
        return validateKM;
    }

    public void setValidateKM(boolean validateKM) {
        this.validateKM = validateKM;
    }

    public boolean isValidateKMTail() {
        return validateKMTail;
    }

    public void setValidateKMTail(boolean validateKMTail) {
        this.validateKMTail = validateKMTail;
    }

    public JsonAppProps getJsonAppProps() {
        return jsonAppProps;
    }

    public void setJsonAppProps(JsonAppProps jsonAppProps) {
        this.jsonAppProps = jsonAppProps;
    }

    public GoodCategory getCategory() {
        return category;
    }

    public void setCategory(GoodCategory category) {
        this.category = category;
    }

    public String getResultFolder() {
        return resultFolder;
    }

    public void setResultFolder(String resultFolder) {
        this.resultFolder = resultFolder;
    }

    public StatisticService getStatisticService() {
        return statisticService;
    }

    public void setStatisticService(StatisticService statisticService) {
        this.statisticService = statisticService;
    }

    public PropService getPropService() {
        return propService;
    }

    public void setPropService(PropService propService) {
        this.propService = propService;
    }
}
