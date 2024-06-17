package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;
import site.barsukov.barcodefx.CrptEscapeFactory;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.context.CancellationContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.cancellation.KmCancellation;
import site.barsukov.barcodefx.model.crpt.cancellation.KmCancellationReasonType;
import site.barsukov.barcodefx.serializer.CrptKMCancellationSerializer;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class XMLCancellationService extends BaseDocService<CancellationContext> {
    private static final int ACTION_ID = 14;
    private static final int VERSION_ID = 2;
    static final Logger logger = Logger.getLogger(XMLCancellationService.class);

    public XMLCancellationService(CancellationContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_cancellation.xml");

        createCancellation(xmlFile, csvFile);

        return xmlFile;
    }

    private void createCancellation(File xmlFile, File csvFile) throws IOException, JAXBException {
        validateINNs(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        KmCancellation result = new KmCancellation();
        result.setActionId(ACTION_ID);
        result.setVersion(VERSION_ID);
        result.setCancellationDocumentDate(context.getCancellationDate());
        result.setCancellationDocumentNumber(context.getCancellationDocNum());
        result.setCancellationReason(KmCancellationReasonType.fromValue(context.getReason()));
        result.setTradeParticipantInn(context.getParticipantINN());

        KmCancellation.KmList cancellationList = new KmCancellation.KmList();
        cancellationList.getKm().addAll(KMs.stream()
            .map(s -> createCancellationProduct(s, context.getReason()))
            .collect(Collectors.toList()));

        result.setKmList(cancellationList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(KmCancellation.KmList.class, new CrptKMCancellationSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateINNs(CancellationContext context) {
        Validator.validateINN(context.getParticipantINN());
    }

    private static KmCancellation.KmList.Km createCancellationProduct(KM km, String cancellationReason) {
        KmCancellation.KmList.Km result = new KmCancellation.KmList.Km();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            result.setKit(km.getGtin() + km.getSerial());
        }

        result.setCancellationReason(KmCancellationReasonType.fromValue(cancellationReason));
        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_CANCELLATION_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
