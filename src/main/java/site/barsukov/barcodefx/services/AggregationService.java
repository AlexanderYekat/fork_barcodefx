package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.exception.AppException;
import site.barsukov.barcodefx.model.crpt.aggregation.UnitPack;
import site.barsukov.barcodefx.services.converter.AggregateMultiCreateConverter;
import site.barsukov.barcodefx.services.dto.AggregateMultiCreateDto;
import site.barsukov.barcodefx.services.dto.AggregateMultiCreateValidationResult;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.KM_CHECK_ENABLED;
import static site.barsukov.barcodefx.services.PickingOrderService.LOCAL_PICKING_ORDERS_PATH;
import static site.barsukov.barcodefx.services.PickingOrderService.updatePickingOrderMeta;

@Slf4j
@Service
@RequiredArgsConstructor
public class AggregationService {
    private static final String SERVICE_NAME = "XML_AGGREGATION_SERVICE";
    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PropService propService;
    private final MarkingCodeService markingCodeService;
    private final AggregateMultiCreateConverter converter;
    private final XmlMapper xmlMapper;
    private final StatisticService statisticService;

    public AggregateMultiCreateValidationResult createAggregate(AggregateMultiCreateDto dto) {
        Validator.validateINN(dto.getInn());
        var markingCodes = markingCodeService.readKMsFromFile(new File(LOCAL_PICKING_ORDERS_PATH + dto.getFileName()), dto.getGoodCategory());

        if (propService.getBooleanProp(KM_CHECK_ENABLED)) {
            var notValidKms = markingCodeService.validateMarkingCodes(markingCodes, dto.getGoodCategory());
            if (!notValidKms.isEmpty()) {
                return AggregateMultiCreateValidationResult.builder()
                        .markingCodes(notValidKms)
                        .fileName(dto.getFileName())
                        .error(true)
                        .build();
            }
        }

        var unitPack = converter.toEntity(dto, markingCodes);
        writeXml(unitPack, dto);

        if (dto.isStatusChanged()) {
            try {
                updatePickingOrderMeta(dto.getFileName(), dto.getNewStatus());
            } catch (IOException e) {
                throw new AppException(e);
            }
        }
        statisticService.logAction(SERVICE_NAME, markingCodes.size(), dto.getGoodCategory());

        return AggregateMultiCreateValidationResult.builder().error(false).build();
    }

    private void writeXml(UnitPack unitPack, AggregateMultiCreateDto dto) {
        try {
            var filename = FilenameUtils.getBaseName(dto.getFileName()).replace("scanned", "");
            var unitPackFile = new File(dto.getResultPath() + "/" + filename + ".xml");
            FileUtils.writeStringToFile(unitPackFile, xmlMapper.writeValueAsString(unitPack), "UTF-8");
        } catch (IOException e) {
            throw new AppException(e);
        }
    }

    public void writeErrors(Set<AggregateMultiCreateValidationResult> errors, String path) {
        try {
            MAPPER.writeValue(new File(path + "/errors.json"), errors);
        } catch (IOException e) {
            log.error("Ошибка сохранения ошибок", e);
        }
    }
}
