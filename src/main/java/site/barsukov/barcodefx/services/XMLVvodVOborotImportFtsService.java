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
import site.barsukov.barcodefx.context.VvodVOborotImportFtsContext;
import site.barsukov.barcodefx.model.CatalogElement;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.vvodimportfts.IntroduceImportFts;
import site.barsukov.barcodefx.serializer.CrptProductVvodImportFtsSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarImportFtsModule;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class XMLVvodVOborotImportFtsService extends BaseDocService<VvodVOborotImportFtsContext> {
    private static final int VERSION_ID = 3;
    static final Logger logger = Logger.getLogger(XMLVvodVOborotImportFtsService.class);

    private CatalogService catalogService;

    public XMLVvodVOborotImportFtsService(VvodVOborotImportFtsContext context, CatalogService catalogService) {
        super(context);
        this.catalogService = catalogService;
    }

    public File performAction() throws IOException, JAXBException, DatatypeConfigurationException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_vvod_v_oborot_import_fts.xml");

        createVvodImportFts(xmlFile, csvFile);

        return xmlFile;
    }

    private void createVvodImportFts(File xmlFile, File csvFile) throws IOException, JAXBException, DatatypeConfigurationException {
        Validator.validateINN(context.getUserINN());
        Validator.validateDtNum(context.getDeclarationNum());
        List<KM> KMs = readKMsFromFile(csvFile, context);

        IntroduceImportFts result = new IntroduceImportFts();
        result.setTradeParticipantInn(context.getUserINN());
        result.setVersion(VERSION_ID);
        result.setDeclarationNumber(context.getDeclarationNum());
        XMLGregorianCalendar xmlGregorianDtDate =
            DatatypeFactory.newInstance().newXMLGregorianCalendar(context.getDeclarationDate().toString());

        result.setDeclarationDate(xmlGregorianDtDate);
        IntroduceImportFts.ProductsList productsList = new IntroduceImportFts.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(s -> createProduct(s, catalogService))
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(IntroduceImportFts.ProductsList.class, new CrptProductVvodImportFtsSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);
        xmlMapper.registerModule(new XMLCalendarImportFtsModule());

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private static IntroduceImportFts.ProductsList.Product createProduct(KM km, CatalogService catalogService) {
        IntroduceImportFts.ProductsList.Product result = new IntroduceImportFts.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            CatalogElement catalogElement = catalogService.getElement(km.getGtin());
            result.setKi(km.getGtin() + km.getSerial());
            result.setColor(catalogElement.getProperty("V_WEB_90001688"));
            String size = catalogElement.getProperty("V_WEB_90001690");
            if (StringUtils.isNotBlank(size)) {
                try {
                    result.setProductSize(getDictValue(size));
                } catch (Exception e) {
                    throw new IllegalArgumentException(String.format("Ошибка обработки размера %s из каталога для gtin %s", size, km.getGtin()));
                }
            }

        }

        return result;
    }

    private static String getDictValue(String property) {
        try {
            int start = property.indexOf('>');
            return property.substring(start + 1).trim().replaceAll(",", ".");
        } catch (Exception e) {
            throw new IllegalArgumentException(String.format("Ошибка обработки значения справочника со значением %s", property));
        }
    }

    @Override
    public String getServiceName() {
        return "XML_VVOD_V_OBOROT_IMPORT_FTS_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
