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
import org.apache.poi.ss.usermodel.DateUtil;
import org.codehaus.stax2.XMLOutputFactory2;
import site.barsukov.barcodefx.CrptEscapeFactory;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.context.VvodVOborotCrossborderContext;
import site.barsukov.barcodefx.model.CatalogElement;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.crossborder.CertificateTypeType;
import site.barsukov.barcodefx.model.crpt.crossborder.VvodCrossborder;
import site.barsukov.barcodefx.serializer.CrptProductVvodImportCrossborderSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarImportFtsModule;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.model.crpt.crossborder.CertificateTypeType.CONFORMITY_CERTIFICATE;
import static site.barsukov.barcodefx.model.crpt.crossborder.CertificateTypeType.CONFORMITY_DECLARATION;
import static site.barsukov.barcodefx.utils.Utils.toXMLGregorianCalendar;

public class XMLVvodVOborotCrossborderService extends BaseDocService<VvodVOborotCrossborderContext> {
    private static final int VERSION_ID = 3;

    static final Logger logger = Logger.getLogger(XMLVvodVOborotCrossborderService.class);

    private CatalogService catalogService;

    public XMLVvodVOborotCrossborderService(VvodVOborotCrossborderContext context, CatalogService catalogService) {
        super(context);
        this.catalogService = catalogService;
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_vvod_v_oborot_crossborder.xml");

        createVvodCrossbording(xmlFile, csvFile);

        return xmlFile;
    }

    private void createVvodCrossbording(File xmlFile, File csvFile) throws IOException, JAXBException {

        Validator.validateINN(context.getUserINN());
        List<KM> KMs = readKMsFromFile(csvFile, context);

        VvodCrossborder result = new VvodCrossborder();
        result.setTradeParticipantInn(context.getUserINN());
        result.setSenderTaxNumber(context.getSenderINN());
        result.setExporterName(context.getExporterName());
        result.setCountryOksm(context.getCountryOksm());
        result.setImportDate(toXMLGregorianCalendar(context.getImportDate()));
        result.setPrimaryDocumentNumber(context.getPrimaryDocNum());
        result.setPrimaryDocumentDate(toXMLGregorianCalendar(context.getPrimaryDocDate()));
        result.setVersion(VERSION_ID);


        result.setTradeParticipantInn(context.getUserINN());

        VvodCrossborder.ProductsList productsList = new VvodCrossborder.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(s -> createProduct(s, context, catalogService))
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);


        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        //TODO
        customSerializer.addSerializer(VvodCrossborder.ProductsList.class, new CrptProductVvodImportCrossborderSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);
        xmlMapper.registerModule(new XMLCalendarImportFtsModule());

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private static VvodCrossborder.ProductsList.Product createProduct(KM km, VvodVOborotCrossborderContext context, CatalogService catalogService) {

        VvodCrossborder.ProductsList.Product result = new VvodCrossborder.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            CatalogElement catalogElement = catalogService.getElement(km.getGtin());
            result.setKi(km.getGtin() + km.getSerial());
            result.setTnvedCode(getTnved(catalogElement));
            result.setCost(getCost(catalogElement));
            result.setVatValue(getVat(catalogElement));
            result.setCertificateType(getSertType(catalogElement));
            result.setCertificateNumber(getSertNumber(catalogElement));
            result.setCertificateDate(getSertDate(catalogElement));
            result.setCertificateDate(getSertDate(catalogElement));
            result.setVsdNumber(StringUtils.isBlank(context.getVsdNumber()) ? null : context.getVsdNumber());
        }
        return result;
    }

    private static String getSertNumber(CatalogElement catalogElement) {
        if (catalogElement == null) {
            return null;
        } else {
            return catalogElement.getProperty("V_PROD_CERT_NUMBER");
        }
    }

    private static CertificateTypeType getSertType(CatalogElement catalogElement) {
        if (catalogElement == null) {
            return null;
        }
        try {
            String sertType = getDictKey(catalogElement.getProperty("V_PROD_CERT_TYPE"));
            if (StringUtils.isBlank(sertType)) {
                return null;
            }
            if (sertType.startsWith("DECL")) {
                return CONFORMITY_DECLARATION;
            } else if (sertType.startsWith("CERT")) {
                return CONFORMITY_CERTIFICATE;
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error("Ошибка обработки типа сертификата ", e);
            return null;
        }
    }

    private static BigDecimal getVat(CatalogElement catalogElement) {
        try {
            return new BigDecimal(catalogElement.getProperty("VAT"));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }

    }

    private static BigDecimal getCost(CatalogElement catalogElement) {
        try {
            return new BigDecimal(catalogElement.getProperty("COST"));
        } catch (Exception e) {
            return BigDecimal.ZERO;
        }
    }

    private static XMLGregorianCalendar getSertDate(CatalogElement catalogElement) {
        if (catalogElement == null) {
            return null;
        }
        try {
            Date date = DateUtil.getJavaDate(Double.parseDouble(catalogElement.getProperty("V_PROD_CERT_ISSUE_DATE")));
            var localDate = LocalDate.ofInstant(date.toInstant(), ZoneId.systemDefault());
            return toXMLGregorianCalendar(localDate);
        } catch (Exception e) {
            logger.error("Ошибка обработки  даты ", e);
            return null;
        }
    }


    private static String getTnved(CatalogElement catalogElement) {
        if (catalogElement == null) {
            return null;
        }
        try {
            return getDictKey(catalogElement.getProperty("V_CLASS_TNVED"));
        } catch (Exception e) {
            logger.error("Error while getting tnved in " + catalogElement.getGtin());
            return null;
        }
    }

    private static String getDictKey(String property) {
        try {
            int start = property.indexOf('<');
            int end = property.indexOf('>');
            return property.substring(start + 1, end);
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Ошибка обработки справочника со значением %s", property));
        }

    }

    @Override
    public String getServiceName() {
        return "XML_VVOD_V_OBOROT_CROSSBORDER_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
