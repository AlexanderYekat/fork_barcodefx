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
import site.barsukov.barcodefx.utils.Utils;
import site.barsukov.barcodefx.Validator;
import site.barsukov.barcodefx.context.RemarkContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.remark.Remark;
import site.barsukov.barcodefx.model.crpt.remark.RemarkCauseType;
import site.barsukov.barcodefx.serializer.CrptProductRemarkSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarModule;

import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import java.io.File;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.deleteBlankString;

public class XMLRemarkService extends BaseDocService<RemarkContext> {
    //    private static final int ACTION_ID = 11;
    private static final int VERSION_ID = 6;
    static final Logger logger = Logger.getLogger(XMLRemarkService.class);

    public XMLRemarkService(RemarkContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException, DatatypeConfigurationException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + "_remark.xml");

        createRemark(xmlFile, csvFile);

        return xmlFile;
    }

    private void createRemark(File xmlFile, File csvFile) throws IOException {
        String pattern = "dd.MM.yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        validateINNs(context);
        Utils.checkArgument(context.getRemarkCause() != null, "Не указана причина перемаркировки");
        Utils.checkArgument(context.getRemarkDate() != null, "Не указана дата перемаркировки");

        List<KM> KMs = readKMsFromFile(csvFile, context);

        Remark result = new Remark();
        result.setTradeParticipantInn(context.getParticipantINN());
        result.setRemarkDate(context.getRemarkDate().format(formatter));
        result.setRemarkCause(RemarkCauseType.valueOf(context.getRemarkCause().name()));
        result.setVersion(VERSION_ID);

        Remark.ProductsList productsList = new Remark.ProductsList();
        productsList.getProduct().addAll(KMs.stream()
            .map(s -> createRemarkProduct(s, context))
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(Remark.ProductsList.class, new CrptProductRemarkSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(new XMLCalendarModule());
        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateINNs(RemarkContext context) {
        Validator.validateINN(context.getParticipantINN());
    }

    private static Remark.ProductsList.Product createRemarkProduct(KM km, RemarkContext context) {
        Remark.ProductsList.Product result = new Remark.ProductsList.Product();
        result.setNewKi(km.getGtin() + km.getSerial());
        result.setTnvedCode10(deleteBlankString(context.getRemarkTnvd()));
        result.setColor(deleteBlankString(context.getRemarkColor()));
        result.setProductSize(deleteBlankString(context.getRemarkSize()));

        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_REMARK_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
