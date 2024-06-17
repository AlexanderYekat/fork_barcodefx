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
import site.barsukov.barcodefx.context.VvodVOborotContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.vvodostatkov.VvodOstatky;
import site.barsukov.barcodefx.serializer.CrptProductVvodSerializer;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class XMLVvodVOborotService extends BaseDocService<VvodVOborotContext> {
    private static final String ACTION_ID = "5.4";
    private static final int VERSION_ID = 2;

    static final Logger logger = Logger.getLogger(XMLVvodVOborotService.class);

    public XMLVvodVOborotService(VvodVOborotContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_vvod_v_oborot.xml");

        createVvodOstatky(xmlFile, csvFile);

        return xmlFile;
    }

    private void createVvodOstatky(File xmlFile, File csvFile) throws IOException, JAXBException {
        Validator.validateINN(context.getUserINN());
        List<KM> KMs = readKMsFromFile(csvFile, context);

        VvodOstatky result = new VvodOstatky();
        result.setActionId(ACTION_ID);
        result.setTradeParticipantInn(context.getUserINN());
        result.setVersion(VERSION_ID);
        VvodOstatky.ProductsList productsList = new VvodOstatky.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(XMLVvodVOborotService::createProduct)
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(VvodOstatky.ProductsList.class, new CrptProductVvodSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private static VvodOstatky.ProductsList.Product createProduct(KM km) {
        VvodOstatky.ProductsList.Product result = new VvodOstatky.ProductsList.Product();
        if (km.isSscc()) {
            result.setKitu(km.getSscc());
        } else {
            result.setKi(km.getGtin() + km.getSerial());
        }

        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_VVOD_V_OBOROT_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
