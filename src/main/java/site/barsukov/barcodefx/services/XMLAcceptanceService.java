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
import site.barsukov.barcodefx.context.AcceptanceContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.acceptance.Acceptance;
import site.barsukov.barcodefx.model.crpt.acceptance.TurnoverEnumType;
import site.barsukov.barcodefx.serializer.CrptProductAcceptanceSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarModule;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class XMLAcceptanceService extends BaseDocService<AcceptanceContext> {
    private static final int ACTION_ID = 11;
    private static final int VERSION_ID = 6;

    public XMLAcceptanceService(AcceptanceContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException, DatatypeConfigurationException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_acceptance.xml");

        createAcceptance(xmlFile, csvFile);

        return xmlFile;
    }

    private void createAcceptance(File xmlFile, File csvFile) throws IOException, JAXBException, DatatypeConfigurationException {
        validateINNs(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        Acceptance result = new Acceptance();
        result.setActionId(ACTION_ID);
        result.setVersion(BigDecimal.valueOf(VERSION_ID));
        result.setTradeParticipantInnReceiver(context.getReceiverINN());
        result.setTradeParticipantInnSender(context.getSenderINN());
        result.setTransferDate(context.getTransferDate());
        result.setMoveDocumentDate(context.getMoveDocDate());
        result.setMoveDocumentNumber(context.getMoveDocNum());
        XMLGregorianCalendar xmlGregorianTransferDate =
            DatatypeFactory.newInstance().newXMLGregorianCalendar(context.getTransferDateDate().toString());
        result.setShipmentDate(xmlGregorianTransferDate);
        result.setReceptionDate(xmlGregorianTransferDate);
        result.setShipmentId(context.getShipmentId());
        result.setTurnoverType(TurnoverEnumType.fromValue(context.getAcceptanceType()));

        Acceptance.ProductsList productsList = new Acceptance.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(XMLAcceptanceService::createShipmentProduct)
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(Acceptance.ProductsList.class, new CrptProductAcceptanceSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(new XMLCalendarModule());
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateINNs(AcceptanceContext context) {
        Validator.validateINN(context.getReceiverINN());
        Validator.validateINN(context.getSenderINN());
    }

    private static Acceptance.ProductsList.Product createShipmentProduct(KM km) {
        Acceptance.ProductsList.Product result = new Acceptance.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            result.setKi(km.getGtin() + km.getSerial());
        }

        result.setAcceptType(true);
        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_ACCEPTANCE_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
