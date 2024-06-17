package site.barsukov.barcodefx.services;


import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.yo.pickingorder.PickingOrder;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EdoLiteService {
    private final StatisticService statisticService;

    public void saveKmToFileNoGrouping(String fileName, File destDirectory, List<PickingOrder> pickingOrders, int firstLineNumber) throws IOException {
        int counter = 0;
        List<String> escapedCodes = new ArrayList<>();
        for (var curPickingOrder : pickingOrders) {
            var curFile = PickingOrderService.getFileWithCodes(curPickingOrder);
            LineIterator iterator = FileUtils.lineIterator(curFile, "UTF-8");
            while (iterator.hasNext()) {
                counter++;
                var splitted = iterator.nextLine().split("\\u001D");
                var escaped = StringEscapeUtils.escapeCsv(splitted[0]);
                escapedCodes.add(escaped);
            }
            iterator.close();
        }
        var result = firstLineNumber + ",КИЗ," + String.join(",", escapedCodes);
        File destFile = new File(destDirectory.getAbsolutePath() + File.separatorChar + firstLineNumber + "_" + counter + "_" + fileName + ".csv");
        FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(result), true);
        var tg = PickingOrderService.convertTg(pickingOrders.get(0).getProductGroup());
        statisticService.logAction("EDO_LITE_KM_NO_GROUPING", counter, tg);
    }

    public void saveKmToFileGtinGrouping(String fileName, File destDirectory, List<PickingOrder> pickingOrders, int firstLineNumber) throws IOException {
        Map<String, List<String>> allEscapedCodes = new HashMap<>();
        for (var curPickingOrder : pickingOrders) {
            var curFile = PickingOrderService.getFileWithCodes(curPickingOrder);
            LineIterator iterator = FileUtils.lineIterator(curFile, "UTF-8");
            while (iterator.hasNext()) {
                var line = iterator.nextLine();
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                try {
                    var gtin = line.substring(0, 16);
                    var escapedCodes = allEscapedCodes.getOrDefault(gtin, new ArrayList<>());
                    var splitted = line.split("\\u001D");
                    var escaped = StringEscapeUtils.escapeCsv(splitted[0]);
                    escapedCodes.add(escaped);
                    allEscapedCodes.put(gtin, escapedCodes);
                } catch (Exception e) {
                    log.error("Error parsing gtin from {}", line);
                    log.error("Error parsing gtin", e);
                    continue;
                }

            }
            iterator.close();
        }
        int totalCounter = 0;
        int positionCounter = firstLineNumber;
        for (var curEntry : allEscapedCodes.entrySet()) {
            var result = positionCounter + ",КИЗ," + String.join(",", curEntry.getValue());
            File destFile = new File(destDirectory.getAbsolutePath() + File.separatorChar + positionCounter
                    + "_" + curEntry.getValue().size() + "_" + curEntry.getKey() + "_" + fileName + ".csv");
            FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(result), true);
            positionCounter++;
            totalCounter += curEntry.getValue().size();
        }

        var tg = PickingOrderService.convertTg(pickingOrders.get(0).getProductGroup());
        statisticService.logAction("EDO_LITE_KM_GTIN_GROUPING", totalCounter, tg);
    }

    public void saveAggregatesToFileWithGrouping(String fileName, File destDirectory, List<PickingOrder> pickingOrders, String grouping, int firstLineNumber) throws IOException {
        int counter = 0;
        List<String> escapedCodes = new ArrayList<>();
        for (var curPickingOrder : pickingOrders) {
            var curFile = PickingOrderService.getFileWithCodes(curPickingOrder);
            LineIterator iterator = FileUtils.lineIterator(curFile, "UTF-8");
            while (iterator.hasNext()) {
                counter++;
                var splitted = iterator.nextLine().split("\\u001D");
                var escaped = StringEscapeUtils.escapeCsv(splitted[0]);
                escapedCodes.add(escaped);
            }
            iterator.close();
        }
        if (StringUtils.isBlank(grouping)) {
            var result = firstLineNumber + ",ИдентТрансУпак," + String.join(",", escapedCodes);
            File destFile = new File(destDirectory.getAbsolutePath() + File.separatorChar + firstLineNumber + "_" + counter + "_" + fileName + ".csv");
            FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(result), true);
        } else {
            List<Integer> splitted = Arrays.asList(grouping.split(",")).stream().map(s -> Integer.parseInt(s.trim())).toList();
            var codeIterator = escapedCodes.iterator();
            var packSizeIterator = splitted.iterator();
            int lineCounter = firstLineNumber;
            int packCounter = packSizeIterator.next();
            List<String> tempList = new ArrayList<>();
            while (codeIterator.hasNext()) {
                tempList.add(codeIterator.next());
                if (tempList.size() == packCounter) {
                    var result = lineCounter + ",ИдентТрансУпак," + String.join(",", tempList);
                    File destFile = new File(destDirectory.getAbsolutePath() + File.separatorChar + lineCounter + "_" + tempList.size() + "_" + fileName + ".csv");
                    FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(result), true);
                    tempList = new ArrayList<>();
                    lineCounter++;
                    if (packSizeIterator.hasNext()) {
                        packCounter = packSizeIterator.next();
                    } else {
                        packCounter = 0;
                    }
                }
            }

            if (!tempList.isEmpty()) {
                var result = lineCounter + ",ИдентТрансУпак," + String.join(",", tempList);
                File destFile = new File(destDirectory.getAbsolutePath() + File.separatorChar + lineCounter + "_" + tempList.size() + "_" + fileName + ".csv");
                FileUtils.writeLines(destFile, "UTF-8", ImmutableList.of(result), true);
            }
        }
        var tg = PickingOrderService.convertTg(pickingOrders.get(0).getProductGroup());
        statisticService.logAction("EDO_LITE_AGGREGATE_WITH_GROUPING", counter, tg);
    }
}
