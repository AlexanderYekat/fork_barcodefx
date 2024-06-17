package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import site.barsukov.barcodefx.context.BaseContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;
import site.barsukov.barcodefx.serializer.XMLCalendarModule;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.Validator.isAtpKMValid;
import static site.barsukov.barcodefx.Validator.isKMValid;

public abstract class BaseDocService<T extends BaseContext> {
    public static final String ZMS_EXTENSION = "zms";
    private static final String GS_SEPARATOR = String.valueOf('\u001D');
    static final Logger logger = Logger.getLogger(BaseDocService.class);
    T context;
    private long linesProcessed = 0;
    private static final XmlMapper xmlMapper = new XmlMapper();

    static {
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        JaxbAnnotationModule module = new JaxbAnnotationModule();
        xmlMapper.registerModule(new XMLCalendarModule());
        xmlMapper.registerModule(module);
    }

    public BaseDocService(T context) {
        this.context = context;
    }

    public static KM createKM(String csvLine, GoodCategory category) {
        KM result = new KM();
        if (csvLine.startsWith("00")) {
            result.setSscc(true);
        }
        if (!(category == GoodCategory.ATP && csvLine.length() == 29)) {
            String[] splitStrings = csvLine.split(GS_SEPARATOR);
            result.setGtin(splitStrings[0].substring(0, 16));
            result.setSerial(splitStrings[0].substring(16));
        } else if (category == GoodCategory.ATP && !csvLine.contains(GS_SEPARATOR) && csvLine.length() == 29) {
            result.setGtin("01" + csvLine.substring(0, 14));
            result.setSerial("21" + csvLine.substring(14, 21));
        }
        result.setSuzString(csvLine);
        return result;
    }

    public String getRangeString() {
        StringBuilder result = new StringBuilder();
        if (context.isRangeEnabled()) {
            result.append("_");
            result.append(context.getRangeFrom());
            result.append("-");
            result.append(context.getRangeTo());
        }
        return result.toString();
    }

    public List<KM> readKMsFromFile(T context) throws IOException {
        return readKMsFromFile(new File(context.getCsvFileName()), context, false);
    }

    public List<KM> readKMsFromFile(File csvFile, T context) throws IOException {
        return readKMsFromFile(csvFile, context, false);
    }

    public List<KM> readKMsFromFile(File csvFile, T context, boolean validateTails) throws IOException {
        List<KM> result = readLinesFromFile(csvFile, context).stream()
                .map(s -> createKM(s, context.getCategory()))
                .collect(Collectors.toList());
        if (context.isValidateKM() && !allKMSareValid(result)) {
            throw new IllegalArgumentException("Ошибка проверки кодов маркировки. Подробности в файле лога. " +
                    "Проверку можно отключить в настройках");
        }

        if (context.isValidateKMTail() && validateTails && !allKMsTailsAreValid(result, context.getCategory())) {
            throw new IllegalArgumentException("Ошибка проверки хвостов кодов маркировки. Подробности в файле лога. " +
                    "Проверку можно отключить в настройках");
        }
        linesProcessed = result.size();
        return result;
    }

    public final File generateResult() throws Exception {
        File result = performAction();
        logResult();
        return result;
    }

    void logResult() {
        String serviceName = getServiceName();
        context.getStatisticService().logAction(serviceName, linesProcessed, context.getCategory());
    }

    public abstract String getServiceName();

    public abstract boolean isXmlResult();

    protected abstract File performAction() throws Exception;

    private boolean allKMsTailsAreValid(List<KM> KMs, GoodCategory category) {
        boolean result = true;
        for (KM curKM : KMs) {
            if (!checkTailLength(curKM, category) && !curKM.isSscc()) {
                logger.warn("Ошибка проверки криптохвоста у КМ (длины и ИП): " + curKM.getSuzString());
                result = false;
            }
        }
        return result;
    }

    private boolean checkTailLength(KM curKM, GoodCategory category) {
        if (category.getTailLength() > 0) { //у АТП и молока нет криптохвоста
            String[] splitedLines = curKM.getSuzString().split(GS_SEPARATOR);
            String tail = splitedLines[splitedLines.length - 1];
            return tail.startsWith(category.getCryptoAI()) && tail.length() == 2 + category.getTailLength();
        } else {
            return true;
        }
    }

    private boolean allKMSareValid(List<KM> KMs) {
        boolean result = true;
        for (KM curKM : KMs) {
            if (!isCodeValid(curKM)) {
                result = false;
            }
        }
        return result;
    }

    private boolean isCodeValid(KM curKm) {
        boolean result;
        if (curKm.isSscc()) {
            result = curKm.getSuzString().length() == 20;
        } else {
            result = isKMValid(curKm, context.getCategory());
            if (context.getCategory() == GoodCategory.ATP && !result) {
                result = isAtpKMValid(curKm);
            }
        }

        return result;
    }

    private List<String> createSublist(List<String> source, T context) {
        if (context.getRangeFrom() > context.getRangeTo()) {
            throw new IllegalArgumentException("Ошибочно задан диапазон. Начало диапазона больше конца.");
        }
        if (source.size() < context.getRangeFrom()) {
            throw new IllegalArgumentException("Ошибочно задан диапазон. Начало диапазона превышает количество строк.");
        }
        if (context.getRangeFrom() <= 0) {
            throw new IllegalArgumentException("Ошибочно задан диапазон. Начало диапазона должно быть больше нуля.");
        }
        if (source.size() < context.getRangeTo()) {
            logger.warn("Ошибочно задан диапазон. Конец диапазона превышает количество строк. Конец диапазона заменен на количество строк.");
            context.setRangeTo(Integer.toString(source.size()));
        }
        return source.subList(context.getRangeFrom() - 1, context.getRangeTo());
    }

    private List<String> readLinesFromFile(File csvFile, T context) throws IOException {
        //TODO: первый кандидат на оптимизацию, дабы не вычитывать весь файл
        List<String> rawLines;
        if (isZmsFile(csvFile)) {
            rawLines = readCodesFromZmsFile(csvFile);
        } else {
            rawLines = FileUtils.readLines(csvFile, "UTF-8");
        }
        List<String> lines = rawLines
                .stream()
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
        if (context.isRangeEnabled()) {
            return createSublist(lines, context);
        }
        List<String> result = new ArrayList<>();
        lines.forEach(s -> result.add(s.trim()));
        return result;
    }

    public T getContext() {
        return context;
    }

    private static boolean isZmsFile(File file) {
        var ext = FilenameUtils.getExtension(file.getAbsolutePath());
        return ZMS_EXTENSION.equals(ext);
    }

    private List<String> readCodesFromZmsFile(File file) throws IOException {
        if (!isZmsFile(file)) {
            throw new IllegalArgumentException("Файл не является файлом zms");
        }
        var zip = new ZipFile(file);
        var xmls = zip.getFileHeaders().stream().filter(s -> s.getFileName().endsWith(".xml")).collect(Collectors.toList());
        if (xmls.size() != 1) {
            throw new IllegalArgumentException("Ошибка метаданных контейнера");
        }
        var tempDir = FileUtils.getTempDirectoryPath();
        zip.extractFile(xmls.get(0), tempDir);
        File metaFile = new File(tempDir + File.separatorChar + xmls.get(0).getFileName());
        var pickingOrder = xmlMapper.readValue(FileUtils.readFileToString(metaFile, "UTF-8"), PickingOrder.class);
        zip.extractFile(pickingOrder.getFileName(), tempDir);

        File codesFile = new File(tempDir + File.separatorChar + pickingOrder.getFileName());

        var md5 = countMd5(codesFile);
        if (!Objects.equals(md5, pickingOrder.getMd5())) {
            throw new IllegalArgumentException("Ошибка проверки контрольной суммы");
        }

        return FileUtils.readLines(codesFile, "UTF-8");
    }

    private static String countMd5(File file) {
        try (InputStream is = Files.newInputStream(Paths.get(file.getAbsolutePath()))) {
            return org.apache.commons.codec.digest.DigestUtils.md5Hex(is);
        } catch (IOException e) {
//            log.error("Ошибка расчета md5 для " + file.getAbsolutePath(), e);
            return null;
        }
    }

}
