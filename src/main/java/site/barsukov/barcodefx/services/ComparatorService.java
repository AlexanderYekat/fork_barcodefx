package site.barsukov.barcodefx.services;

import site.barsukov.barcodefx.context.ComparatorContext;
import site.barsukov.barcodefx.model.KM;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ComparatorService extends BaseDocService<ComparatorContext> {
    private final String STRING_END = "\r\n\r\n";
    private final String STRING_SEPARATOR = "-----------------------------------------------";

    public ComparatorService(ComparatorContext context) {
        super(context);
    }

    public String getReport(StatisticService statisticService) throws IOException {
        File sourceFile = new File(context.getCsvFileName());
        StringBuilder result = new StringBuilder();
        result.append("Результат сравнения двух файлов");
        result.append(STRING_END);
        result.append("Изначальный файл: " + sourceFile.getName());
        result.append(STRING_END);
        result.append("Предоставленный файл: " + context.getComparedFile().getName());
        result.append(STRING_END);
        List<KM> originalKMs = readKMsFromFile(sourceFile, context);
        List<KM> comparedKMs = readKMsFromFile(context.getComparedFile(), context);

        result.append("Кодов в изначальном файле: " + originalKMs.size());
        result.append(STRING_END);
        result.append("Кодов в предоставленном файле: " + comparedKMs.size());
        result.append(STRING_END);
        result.append(STRING_SEPARATOR);
        result.append(STRING_END);

        List<KM> compareResult1 = filter(originalKMs, comparedKMs);
        List<KM> compareResult2 = filter(comparedKMs, originalKMs);
        if (compareResult1.isEmpty() && compareResult2.isEmpty()) {
            result.append("Коды в обоих файлах совпадают, различий нет.");
            result.append(STRING_END);
        } else {
            result.append("Коды в файлах не совпадают, есть различия.");
            result.append(STRING_END);
            result.append("Количество кодов, которые есть в изначальном файле, но отсутствуют в заявленном: " + compareResult1.size());
            result.append(STRING_END);
            result.append("Количество кодов, которые  есть в заявленном файле, но отсутствуют в изначальном: " + compareResult2.size());
            result.append(STRING_END);
            result.append("Различающиеся коды: ");
            result.append(STRING_END);
            for (KM curKm : compareResult1) {
                result.append(curKm.getSuzString());
                result.append(STRING_END);
            }
            result.append(STRING_SEPARATOR);
            result.append(STRING_END);
            for (KM curKm : compareResult2) {
                result.append(curKm.getSuzString());
                result.append(STRING_END);
            }
        }
        logResult();
        return result.toString();

    }

    @Override
    public String getServiceName() {
        return "COMPARATOR_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return false;
    }

    @Override
    public File performAction() throws Exception {
        return null;
    }

    private List<KM> filter(List<KM> originalKMs, List<KM> comparedKMs) {
        List<KM> result = new ArrayList<>();
        for (KM curKm : originalKMs) {
            Optional<KM> temp = comparedKMs.stream().filter(s -> kmEquals(curKm, s)).findFirst();
            if (temp.isEmpty()) {
                result.add(curKm);
            }
        }
        return result;
    }

    private boolean kmEquals(KM km1, KM km2) {
        if (context.isIgnoreTails()) {
            return km1.getSerial().equals(km2.getSerial())
                && km1.getGtin().equals(km2.getGtin());
        } else {
            return km1.getSuzString().equals(km2.getSuzString());
        }
    }
}
