package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.codehaus.stax2.XMLOutputFactory2;
import site.barsukov.barcodefx.CrptEscapeFactory;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.context.ProductionContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.productionrf.CertificateTypeType;
import site.barsukov.barcodefx.model.crpt.productionrf.IntroduceRf;
import site.barsukov.barcodefx.model.crpt.productionrf.ProductionOrderType;
import site.barsukov.barcodefx.model.production.WaterLicense;
import site.barsukov.barcodefx.serializer.CrptProductProductionRFSerializer;
import site.barsukov.barcodefx.serializer.CrptWaterLicenceSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarImportFtsModule;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.toXMLGregorianCalendar;


public class XMLProductionService extends BaseDocService<ProductionContext> {
    private static final int VERSION_ID = 9;
    static final Logger logger = Logger.getLogger(XMLProductionService.class);

    public XMLProductionService(ProductionContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_production_rf.xml");

        createProductionRF(xmlFile, csvFile);

        return xmlFile;
    }

    private void createProductionRF(File xmlFile, File csvFile) throws IOException, JAXBException {
        validateINNs(context);
        validateTnved(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        IntroduceRf result = new IntroduceRf();
        result.setTradeParticipantInn(context.getParticipantInn());
        result.setProducerInn(context.getProducerInn());
        result.setOwnerInn(context.getOwnerInn());
        result.setProductionDate(toXMLGregorianCalendar(context.getProductDate()));
        if (context.getProductionOrder() != null) {
            result.setProductionOrder(ProductionOrderType.valueOf(context.getProductionOrder()));
        }

        IntroduceRf.ProductsList list = new IntroduceRf.ProductsList();
        list.getProduct().addAll(KMs.stream()
                .map(s -> XMLProductionService.createProductionProduct(s, context))
                .collect(Collectors.toList()));
        result.setProductsList(list);
        result.setVersion(VERSION_ID);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
                CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        xmlMapper.registerModule(new XMLCalendarImportFtsModule());

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(IntroduceRf.ProductsList.class, new CrptProductProductionRFSerializer());
        customSerializer.addSerializer(IntroduceRf.ProductsList.Product.LicencesList.class, new CrptWaterLicenceSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateTnved(ProductionContext context) {
        if (StringUtils.isBlank(context.getTnvedCode())) {
            throw new IllegalArgumentException("Не заполнен ТН ВЭД");
        }
        if (context.getTnvedCode().length() != 10) {
            throw new IllegalArgumentException("ТН ВЭД должен быть десятизначный");
        }
    }

    private void validateINNs(ProductionContext context) {
        Validator.validateINN(context.getOwnerInn());
        Validator.validateINN(context.getParticipantInn());
        Validator.validateINN(context.getProducerInn());
    }

    private static IntroduceRf.ProductsList.Product createProductionProduct(KM km, ProductionContext context) {
        IntroduceRf.ProductsList.Product result = new IntroduceRf.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            result.setKi(km.getGtin() + km.getSerial());
        }
        result.setProductionDate(toXMLGregorianCalendar(context.getProductDate()));
        result.setTnvedCode(context.getTnvedCode());
        if (context.getCertificateType() != null) {
            result.setCertificateType(CertificateTypeType.valueOf(context.getCertificateType()));
        }
        result.setCertificateNumber(StringUtils.isBlank(context.getCertificateNumber()) ? null : context.getCertificateNumber());
        result.setCertificateDate(toXMLGregorianCalendar(context.getCertificateDate()));
        result.setVsdNumber(StringUtils.isBlank(context.getVsdNumber()) ? null : context.getVsdNumber());
        if (!context.getWaterLicenses().isEmpty()) {
            var list = new IntroduceRf.ProductsList.Product.LicencesList();
            list.getLicence().addAll(context.getWaterLicenses().stream().map(XMLProductionService::toLicence).toList());
            result.setLicencesList(list);
        }


        return result;
    }

    private static IntroduceRf.ProductsList.Product.LicencesList.Licence toLicence(WaterLicense s) {
        var result = new IntroduceRf.ProductsList.Product.LicencesList.Licence();
        result.setNumber(s.getNumber());
        result.setDate(toXMLGregorianCalendar(s.getDate()));
        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_PRODUCTION_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
