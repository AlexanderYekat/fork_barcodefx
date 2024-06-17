package site.barsukov.barcodefx.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.CatalogElement;
import site.barsukov.barcodefx.props.JsonCatalogProps;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

import static site.barsukov.barcodefx.props.JsonCatalogProps.CatalogProp.FILE_PATH;

@Slf4j
@Service
@RequiredArgsConstructor
public class CatalogService {
    private final PropService propService;

    private Map<String, CatalogElement> elements = new HashMap<>();
    private volatile boolean loaded = false;

    public void reloadData() {
        elements = new HashMap<>();
        loaded = false;
        loadCatalog();
    }

    public CatalogElement getElement(String gtin) {
        loadCatalog();
        CatalogElement result = elements.get(gtin);
        return Objects.requireNonNullElseGet(result, () -> new CatalogElement(gtin));
    }

    public Map<String, CatalogElement> getElements(List<String> gtins) {
        loadCatalog();
        Map<String, CatalogElement> result = new HashMap<>();
        for (String curGtin : gtins) {
            result.put(curGtin, getElement(curGtin));
        }
        return result;
    }


    private synchronized void loadCatalog() {
        try {
            boolean enabled = propService.getBooleanProp(JsonCatalogProps.CatalogProp.CATALOG_ENABLED);
            if (enabled && !loaded) {
                readCatalog();
            }
        } catch (Exception e) {
            log.error("Ошибка загрузки каталога ", e);
        } finally {
            loaded = true;
        }


    }

    private void readCatalog() throws IOException {
        Map<Integer, String> keysOrder = new HashMap<>();
        int counter = 0;
        // Read XSL file
        FileInputStream inputStream = new FileInputStream(new File(propService.getProp(FILE_PATH)));

        // Get the workbook instance for XLS file
        XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

        // Get first sheet from the workbook
        XSSFSheet sheet = workbook.getSheetAt(0);

        // Get iterator to all the rows in current sheet
        Iterator<Row> rowIterator = sheet.iterator();

        while (rowIterator.hasNext()) {
            CatalogElement curElement = new CatalogElement();
            Row row = rowIterator.next();
            // Get iterator to all cells of current row
            Iterator<Cell> cellIterator = row.cellIterator();

            while (cellIterator.hasNext()) {
                Cell cell = cellIterator.next();
                CellType cellType = cell.getCellType();
                if (counter == 0) {
                    switch (cellType) {
                        case STRING:
                            keysOrder.put(cell.getColumnIndex(), cell.getStringCellValue());
                            break;
                    }

                } else {
                    switch (cellType) {
                        case STRING:
                            curElement.addValue(keysOrder.get(cell.getColumnIndex()), cell.getStringCellValue());
                            break;
                        case NUMERIC:
                            curElement.addValue(keysOrder.get(cell.getColumnIndex()), String.format("%.0f", cell.getNumericCellValue()));
                    }
                }
                // Change to getCellType() if using POI 4.x

            }
            String gtin = StringUtils.leftPad(curElement.getProperty("V_PROD_COVER_GTIN"), 14, '0');
            curElement.setGtin("01" + gtin);
            counter++;
            elements.put(curElement.getGtin(), curElement);
        }
    }
}
