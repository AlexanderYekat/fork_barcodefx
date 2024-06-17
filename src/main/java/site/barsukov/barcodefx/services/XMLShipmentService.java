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
import site.barsukov.barcodefx.context.ShipmentContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.shipping.Shipment;
import site.barsukov.barcodefx.model.crpt.shipping.TurnoverEnumType;
import site.barsukov.barcodefx.model.crpt.shipping.WithdrawalShipmentType;
import site.barsukov.barcodefx.serializer.CrptProductShipmentSerializer;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class XMLShipmentService extends BaseDocService<ShipmentContext> {
    private static int ACTION_ID = 10;
    private static int VERSION_ID = 5;
    static final Logger logger = Logger.getLogger(XMLShipmentService.class);

    public XMLShipmentService(ShipmentContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder()  + File.separatorChar + fileName + getRangeString() + "_shipment.xml");

        createShipping(xmlFile, csvFile);

        return xmlFile;
    }

    private void createShipping(File xmlFile, File csvFile) throws IOException, JAXBException {
        validateINNs(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        Shipment result = new Shipment();
        result.setActionId(ACTION_ID);
        result.setVersion(BigDecimal.valueOf(VERSION_ID));
        result.setTradeParticipantInnReceiver(context.getReceiverINN());
        result.setTradeParticipantInnSender(context.getSenderINN());
        result.setTransferDate(context.getTransferDate());
        result.setMoveDocumentDate(context.getMoveDocDate());
        result.setMoveDocumentNumber(context.getMoveDocNum());
        result.setToNotParticipant(!context.isShippingToParticipant());
        result.setTurnoverType(TurnoverEnumType.fromValue(context.getShipmentType()));
        if (!context.isShippingToParticipant()) {
            result.setWithdrawalType(WithdrawalShipmentType.fromValue(context.getWithdrawType()));
            result.setWithdrawalDate(context.getWithdrawDate());
        }
        Shipment.ProductsList productsList = new Shipment.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(XMLShipmentService::createShipmentProduct)
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(Shipment.ProductsList.class, new CrptProductShipmentSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateINNs(ShipmentContext context) {
        Validator.validateINN(context.getReceiverINN());
        Validator.validateINN(context.getSenderINN());
    }

    private static Shipment.ProductsList.Product createShipmentProduct(KM km) {
        Shipment.ProductsList.Product result = new Shipment.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            result.setKi(km.getGtin() + km.getSerial());
        }

        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_SHIPMENT_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
