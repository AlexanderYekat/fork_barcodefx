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
import site.barsukov.barcodefx.context.WithdrawContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.WithdrawalProduct;
import site.barsukov.barcodefx.model.crpt.withdraw.Withdrawal;
import site.barsukov.barcodefx.model.crpt.withdraw.WithdrawalPrimaryDocumentType;
import site.barsukov.barcodefx.model.crpt.withdraw.WithdrawalType;
import site.barsukov.barcodefx.serializer.CrptProductWithdrawSerializer;
import site.barsukov.barcodefx.serializer.XMLCalendarImportFtsModule;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.toXMLGregorianCalendar;

public class XMLWithdrawService extends BaseDocService<WithdrawContext> {
    private static final int VERSION_ID = 7;
    static final Logger logger = Logger.getLogger(XMLWithdrawService.class);

    public XMLWithdrawService(WithdrawContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder()  + File.separatorChar + fileName + getRangeString() + "_withdraw.xml");

        createWithdraw(xmlFile, csvFile);

        return xmlFile;
    }

    private void createWithdraw(File xmlFile, File csvFile) throws IOException, JAXBException {
        validateINNs(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        String pattern = "dd.MM.yyyy";
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);

        Withdrawal result = new Withdrawal();
        result.setVersion(BigDecimal.valueOf(VERSION_ID));
        result.setKktNumber(context.getKktNumber());
        result.setPrimaryDocumentCustomName(context.getPrimaryDocumentCustomName());
        if (context.getPrimaryDocumentDate() != null) {
            result.setPrimaryDocumentDate(toXMLGregorianCalendar(context.getPrimaryDocumentDate()));  //ДД.ММ.ГГГГ
        }
        result.setPrimaryDocumentNumber(context.getPrimaryDocumentNumber());
        if (StringUtils.isNotBlank(context.getPrimaryDocumentType())) {
            result.setPrimaryDocumentType(WithdrawalPrimaryDocumentType.valueOf(context.getPrimaryDocumentType()));
        }
        result.setTradeParticipantInn(context.getTradeParticipantInn());
        if (context.getWithdrawalDate() != null) {
            result.setWithdrawalDate(toXMLGregorianCalendar(context.getWithdrawalDate()));//ДД.ММ.ГГГГ
        }
        if (StringUtils.isNotBlank(context.getWithdrawalType())) {
            result.setWithdrawalType(WithdrawalType.valueOf(context.getWithdrawalType()));
        }
        Withdrawal.ProductsList productsList = new Withdrawal.ProductsList();

        List<WithdrawalProduct> productList = new ArrayList<>();
        for (KM curKm : KMs) {
            WithdrawalProduct curProduct = new WithdrawalProduct();
            curProduct.setKi(curKm.getGtin() + curKm.getSerial());
            curProduct.setCost(BigDecimal.ZERO);
            productList.add(curProduct);
        }

        productsList.getProduct().addAll(productList.stream()
            .map(XMLWithdrawService::createWithdrawProduct)
            .collect(Collectors.toList()));

        result.setProductsList(productsList);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
            CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);


        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(Withdrawal.ProductsList.class, new CrptProductWithdrawSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(module);
        xmlMapper.registerModule(new XMLCalendarImportFtsModule());

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void validateINNs(WithdrawContext context) {
        Validator.validateINN(context.getTradeParticipantInn());
    }

    private static Withdrawal.ProductsList.Product createWithdrawProduct(WithdrawalProduct product) {
        Withdrawal.ProductsList.Product result = new Withdrawal.ProductsList.Product();
        result.setCis(product.getKi());
        result.setCost(product.getCost());
        if (product.getPrimaryDocumentType() != null) {
            result.setPrimaryDocumentType(WithdrawalPrimaryDocumentType.valueOf(product.getPrimaryDocumentType()));
        }
        result.setPrimaryDocumentNumber(product.getDocumentNumber());
        if (product.getPrimaryDocumentDate() != null) {
            result.setPrimaryDocumentDate(toXMLGregorianCalendar(product.getPrimaryDocumentDate()));
        }
        result.setPrimaryDocumentCustomName(product.getPrimaryDocumentCustomName());

        return result;
    }

    @Override
    public String getServiceName() {
        return "XML_WITHDRAW_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
