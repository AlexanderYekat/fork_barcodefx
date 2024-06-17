package site.barsukov.barcodefx.model.enums;

import lombok.Getter;
import org.springframework.lang.Nullable;

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public enum GoodCategory {
    SHOES("Обувь", 13, 88, "91", "92", 129),
    ATP("АТП", 7, 0, "91", null, 32),
    TIRES("Шины", 13, 44, "91", "92", 85),
    CLOTHES("Одежда", 13, 44, "91", "92", 85),
    PERFUMERY("Парфюм", 13, 44, "91", "92", 85),
    MILK("Молоко", 6, 0, "93", null, 31),
    WATER("Вода", 13, 0, "93", null, 38),
    BEER("Пиво", 7, 0, "93", null, 32),
    SOFTDRINKS("Соки", 13, 0, "93", null, 38);

    private static Map<String, GoodCategory> names = Stream.of(GoodCategory.values())
            .collect(Collectors.toMap(GoodCategory::getShortName, s -> s));

    private String shortName;
    private int serialLength;
    private int tailLength;
    private int totalLength;
    private String checkKeyAI;
    private String cryptoAI;


    GoodCategory(String shortName, int serialLength, int tailLength, String checkKeyAI, String cryptoAI, int totalLength) {
        this.shortName = shortName;
        this.serialLength = serialLength;
        this.tailLength = tailLength;
        this.checkKeyAI = checkKeyAI;
        this.cryptoAI = cryptoAI;
        this.totalLength = totalLength;
    }

    public static GoodCategory getByShortName(String name) {
        return names.get(name);
    }

    @Nullable
    public static GoodCategory getByName(String name) {
        if (Objects.isNull(name)) {
            return null;
        }

        return Arrays.stream(GoodCategory.values())
                .filter(e -> e.name().equals(name))
                .findFirst().orElse(null);
    }


    @Override
    public String toString() {
        return shortName;
    }
}
