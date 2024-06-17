package site.barsukov.barcodefx.services;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.utils.IOUtils;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.Validator.isAtpKMValid;
import static site.barsukov.barcodefx.Validator.isKMValid;

@Slf4j
@Service
public class MarkingCodeService {
    private static final String GS_SEPARATOR = String.valueOf('\u001D');

    public List<KM> readKMsFromFile(File csvFile, GoodCategory category) {
        return IOUtils.readLinesFromFile(csvFile).stream()
                .map(s -> createMarkingCode(s, category))
                .collect(Collectors.toList());
    }

    public Set<KM> validateMarkingCodes(List<KM> markingCodes, GoodCategory category) {
        return markingCodes.stream().filter(mc -> !isCodeValid(mc, category))
                .collect(Collectors.toSet());
    }

    public Set<KM> validateMarkingCodesTails(List<KM> markingCodes, GoodCategory category) {
        return markingCodes.stream()
                .filter(mc -> !checkTailLength(mc, category) && !mc.isSscc())
                .collect(Collectors.toSet());
    }

    private KM createMarkingCode(String csvLine, GoodCategory category) { //todo check line length
        var builder = KM.builder();
        if (csvLine.startsWith("00")) {
            builder.sscc(true);
        }
        if (Objects.isNull(category) || !(category == GoodCategory.ATP && csvLine.length() == 29)) {
            var splitStrings = csvLine.split(GS_SEPARATOR);
            builder.gtin(splitStrings[0].substring(0, 16));
            builder.serial(splitStrings[0].substring(16));
        } else if (!csvLine.contains(GS_SEPARATOR)) {
            builder.gtin("01" + csvLine.substring(0, 14));
            builder.serial("21" + csvLine.substring(14, 21));
        }
        builder.suzString(csvLine);
        return builder.build();
    }

    private boolean checkTailLength(KM curKM, GoodCategory category) {
        if (category != GoodCategory.ATP && category != GoodCategory.MILK) { //у АТП и молока нет криптохвоста
            var splitLines = curKM.getSuzString().split(GS_SEPARATOR);
            var tail = splitLines[splitLines.length - 1];
            return tail.startsWith("92") && tail.length() == 2 + category.getTailLength();
        }
        return true;
    }

    private boolean isCodeValid(KM curKm, GoodCategory category) {
        if (curKm.isSscc()) {
            return curKm.getSuzString().length() == 20;
        }

        if (isKMValid(curKm, category)) {
            return true;
        }

        if (category == GoodCategory.ATP) {
            return isAtpKMValid(curKm);
        }

        return false;
    }
}
