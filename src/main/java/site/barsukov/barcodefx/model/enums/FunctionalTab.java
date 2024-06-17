package site.barsukov.barcodefx.model.enums;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static site.barsukov.barcodefx.model.enums.GoodCategory.*;

public enum FunctionalTab {
    PRINT("printTab", true),
    VVOD_V_OBOROT_OSTATKI("oborotTab", false),
    VVOD_V_OBOROT_IMPORT_FTS("vOborotImportFtsTab", false),
    VVOD_V_OBOROT_IMPORT_EAS("vOborotCrossborder", false),
    SHIPMENT("shipmentTab", false),
    ACCEPTANCE("acceptanceTab", false),
    CANCEL("cancelTab", false),
    AGGREGATION("aggregationTab", false),
    PRODUCTION("productionTab", false),
    WITHDRAW("withdrawTab", false),
    REMARK("remarkTab", false),
    SVERKA("sverkaTab", false);

    FunctionalTab(String uiName, boolean enabledInPackageMode) {
        this.uiName = uiName;
        this.enabledInPackageMode = enabledInPackageMode;
    }

    private String uiName;
    private boolean enabledInPackageMode;

    public String getUiName() {
        return uiName;
    }


    private static Map<FunctionalTab, Set<GoodCategory>> allowedCategories = fillAllowedCats();
    private static Map<String, FunctionalTab> allTabs = Stream.of(FunctionalTab.values()).collect(Collectors.toMap(FunctionalTab::getUiName, s -> s));

    public boolean isCategoryAllowed(GoodCategory cat) {
        return cat == null || allowedCategories.get(this).contains(cat);

    }

    public boolean checkPackageMode(boolean packageModeEnabled) {
        if (packageModeEnabled) {
            return enabledInPackageMode;
        } else {
            return true;
        }
    }

    public static FunctionalTab getByUiName(String uiName) {
        return allTabs.get(uiName);
    }

    private static Map<FunctionalTab, Set<GoodCategory>> fillAllowedCats() {
        Map<FunctionalTab, Set<GoodCategory>> result = new HashMap<>();
        for (FunctionalTab curTab : FunctionalTab.values()) {
            switch (curTab) {
                case PRINT:
                    result.put(curTab, Set.of(SHOES, ATP, TIRES, CLOTHES, MILK, PERFUMERY, WATER, BEER, SOFTDRINKS));
                    break;
                case VVOD_V_OBOROT_OSTATKI:
                    result.put(curTab, Set.of(TIRES, CLOTHES, SHOES, PERFUMERY));
                    break;
                case VVOD_V_OBOROT_IMPORT_EAS:
                case VVOD_V_OBOROT_IMPORT_FTS:
                case SHIPMENT:
                case ACCEPTANCE:
                case CANCEL:
                case AGGREGATION:
                case PRODUCTION:
                case REMARK:
                case WITHDRAW:
                case SVERKA:
                    result.put(curTab, Set.of(SHOES, TIRES, CLOTHES, MILK, PERFUMERY, WATER, BEER, SOFTDRINKS));
                    break;
            }
        }
        return result;
    }
}
