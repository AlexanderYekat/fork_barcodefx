package site.barsukov.barcodefx.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import site.barsukov.barcodefx.exception.AppException;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class IOUtils {

    private IOUtils() {
        throw new UnsupportedOperationException();
    }

    public static Set<String> readLinesFromFile(File csvFile){
        //TODO: первый кандидат на оптимизацию, дабы не вычитывать весь файл
        try {
            return FileUtils.readLines(csvFile, "UTF-8")
                    .stream()
                    .filter(StringUtils::isNotBlank)
                    .map(String::trim)
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            log.error("Ошибка чтения файла", e);
            throw new AppException(e);
        }
    }
}
