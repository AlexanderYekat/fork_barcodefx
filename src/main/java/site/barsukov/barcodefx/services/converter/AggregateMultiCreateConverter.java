package site.barsukov.barcodefx.services.converter;

import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.aggregation.*;
import site.barsukov.barcodefx.services.dto.AggregateMultiCreateDto;
import site.barsukov.barcodefx.utils.Utils;

import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.getXMLGregorianCalendarNow;

@Component
public class AggregateMultiCreateConverter {
    private static final int ACTION_ID = 30;
    private static final String VERSION_ID = "1";
    private static final String PROG_VERSION = "barCodesFX";
    private static final String FORM_VERSION = "1.03";
    private static final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    public UnitPack toEntity(AggregateMultiCreateDto dto, List<KM> markingCodes) {
        var result = new UnitPack();
        result.setActionId(ACTION_ID);
        result.setVersion(VERSION_ID);
        result.setVerProg(PROG_VERSION);
        result.setFileDateTime(getXMLGregorianCalendarNow());
        result.setVerForm(FORM_VERSION);
        result.setDocumentId(UUID.randomUUID().toString());

        var doc = new UnitPack.Document();
        doc.setDocumentNumber(dto.getFileName());
        doc.setOperationDateTime(getXMLGregorianCalendarNow());

        var organisation = new OrganisationType();
        organisation.setIdInfo(createIdInfo(dto));
        doc.setOrganisation(organisation);

        var packContent = new UnitPack.Document.PackContent();
        if (dto.isStatusChanged()) {
            packContent.setPackCode(dto.getNewStatus());

        } else {
            packContent.setPackCode(dto.getStatus());
        }

        packContent.getCisOrSscc().addAll(createCis(markingCodes));

        doc.getPackContent().add(packContent);
        result.setDocument(doc);

        return result;
    }

    private OrganisationType.IdInfo createIdInfo(AggregateMultiCreateDto dto) {

        var idInfo = new OrganisationType.IdInfo();

        if (dto.isIp()) {
            Utils.checkArgument(dto.getInn().length() == 12, "Ошибка длины ИНН у ИП. Должно быть 12 знаков");

            var spInfoType = new SPInfoType();
            String[] fio = dto.getOrgName().split(" ");
            Utils.checkArgument(fio.length >= 2, "Ошибка заполнения ФИО у ИП");

            var fullNameType = new FullNameType();
            fullNameType.setSurname(fio[0]);
            fullNameType.setFirstName(fio[1]);
            if (fio.length > 2) {
                fullNameType.setPatronymic(fio[2]);
            }
            spInfoType.setFullName(fullNameType);
            spInfoType.setSPTIN(dto.getInn());

            idInfo.setSPInfo(spInfoType);
            return idInfo;

        }

        var lpInfo = new OrganisationType.IdInfo.LPInfo();
        lpInfo.setOrgName(dto.getOrgName());
        lpInfo.setLPTIN(dto.getInn());
        idInfo.setLPInfo(lpInfo);
        return idInfo;
    }

    private List<JAXBElement<String>> createCis(List<KM> markingCodes) {
        return markingCodes.stream().map(km -> {
                    if (km.isSscc()) {
                        return OBJECT_FACTORY.createUnitPackDocumentPackContentSscc(km.getSscc());
                    } else {
                        return OBJECT_FACTORY.createUnitPackDocumentPackContentCis(km.getGtin() + km.getSerial());
                    }
                }
        )
                .collect(Collectors.toList());
    }
}
