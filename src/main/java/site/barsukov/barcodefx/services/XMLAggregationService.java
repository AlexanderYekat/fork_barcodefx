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
import site.barsukov.barcodefx.context.AggregationContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.crpt.aggregation.*;
import site.barsukov.barcodefx.serializer.CrptDocumentAggregationSerializer;
import site.barsukov.barcodefx.serializer.CrptKMAggregationSerializer;
import site.barsukov.barcodefx.serializer.XMLIsoCalendarModule;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.File;
import java.io.IOException;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;

public class XMLAggregationService extends BaseDocService<AggregationContext> {
    final static Logger logger = Logger.getLogger(XMLAggregationService.class);
    private static final int ACTION_ID = 30;
    private static final String VERSION_ID = "1";

    public XMLAggregationService(AggregationContext context) {
        super(context);
    }

    public File performAction() throws IOException, JAXBException, DatatypeConfigurationException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File xmlFile = new File(context.getResultFolder()  + File.separatorChar + fileName + getRangeString() + "_aggregation.xml");

        createAggregation(xmlFile, csvFile);

        return xmlFile;
    }

    private void createAggregation(File xmlFile, File csvFile) throws IOException, DatatypeConfigurationException {
        validateINNs(context);
        List<KM> KMs = readKMsFromFile(csvFile, context);

        UnitPack result = new UnitPack();
        result.setActionId(ACTION_ID);
        result.setVersion(VERSION_ID);
        result.setVerProg("barCodesFX");
        result.setFileDateTime(getXMLGregorianCalendarNow());
        result.setVerForm("1.03");
        result.setDocumentId(UUID.randomUUID().toString());

        UnitPack.Document doc = new UnitPack.Document();
        doc.setDocumentNumber(context.getDocumentNumber());
        doc.setOperationDateTime(getXMLGregorianCalendarNow());

        OrganisationType organisation = new OrganisationType();
        organisation.setIdInfo(createIdInfo());
        doc.setOrganisation(organisation);

        UnitPack.Document.PackContent packContent = new UnitPack.Document.PackContent();
        packContent.setPackCode(context.getSscc());
        fillCis(packContent, KMs);

        doc.getPackContent().add(packContent);
        result.setDocument(doc);

        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.getFactory().getXMLOutputFactory().setProperty(XMLOutputFactory2.P_TEXT_ESCAPER,
                CrptEscapeFactory.theInstance);   //for escaping <>&'" symbols
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        JaxbAnnotationModule module = new JaxbAnnotationModule();
        SimpleModule customSerializer = new SimpleModule();
        customSerializer.addSerializer(UnitPack.Document.PackContent.class, new CrptKMAggregationSerializer());
        xmlMapper.registerModule(customSerializer);
        xmlMapper.registerModule(new XMLIsoCalendarModule());

        SimpleModule customSerializer2 = new SimpleModule();
        customSerializer2.addSerializer(UnitPack.Document.class, new CrptDocumentAggregationSerializer());
        xmlMapper.registerModule(customSerializer2);

        xmlMapper.registerModule(module);

        String xml = xmlMapper.writeValueAsString(result);

        FileUtils.writeStringToFile(xmlFile, xml, "UTF-8");
    }

    private void fillCis(UnitPack.Document.PackContent packContent, List<KM> KMs) {
        for (KM curKm : KMs) {
            ObjectFactory objectFactory = new ObjectFactory();
            JAXBElement<String> jaxbElement;
            if(curKm.isSscc()){
                 jaxbElement =
                    objectFactory.createUnitPackDocumentPackContentSscc(curKm.getSscc());
            }else{
                jaxbElement =
                    objectFactory.createUnitPackDocumentPackContentCis(curKm.getGtin() + curKm.getSerial());
            }

            packContent.getCisOrSscc().add(jaxbElement);
        }
    }

    private void validateINNs(AggregationContext context) {
        Validator.validateINN(context.getParticipantINN());
    }

    private XMLGregorianCalendar getXMLGregorianCalendarNow()
            throws DatatypeConfigurationException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
        return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
    }

    private  OrganisationType.IdInfo createIdInfo(){
        OrganisationType.IdInfo idInfo = new OrganisationType.IdInfo();

        if (context.isIp()) {
            Utils.checkArgument(context.getParticipantINN().length() == 12,"Ошибка длины ИНН у ИП. Должно быть 12 знаков");
            //ИП
            SPInfoType spInfoType = new SPInfoType();
            String[] fio = context.getOrgName().split(" ");
            if (fio.length < 2) {
                throw new IllegalArgumentException("Ошибка заполнения ФИО у ИП");
            }
            FullNameType fullNameType = new FullNameType();
            fullNameType.setSurname(fio[0]);
            fullNameType.setFirstName(fio[1]);
            if (fio.length > 2) {
                fullNameType.setPatronymic(fio[2]);
            }
            spInfoType.setFullName(fullNameType);
            spInfoType.setSPTIN(context.getParticipantINN());

            idInfo.setSPInfo(spInfoType);

        } else {
            OrganisationType.IdInfo.LPInfo lpInfo = new OrganisationType.IdInfo.LPInfo();
            lpInfo.setOrgName(context.getOrgName());
            lpInfo.setLPTIN(context.getParticipantINN());

            idInfo.setLPInfo(lpInfo);
        }

        return idInfo;
    }
    @Override
    public String getServiceName() {
        return "XML_AGGREGATION_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return true;
    }
}
