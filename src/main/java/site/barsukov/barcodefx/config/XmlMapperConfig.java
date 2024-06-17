package site.barsukov.barcodefx.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.codehaus.stax2.XMLOutputFactory2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import site.barsukov.barcodefx.CrptEscapeFactory;
import site.barsukov.barcodefx.model.crpt.aggregation.UnitPack;
import site.barsukov.barcodefx.serializer.CrptDocumentAggregationSerializer;
import site.barsukov.barcodefx.serializer.CrptKMAggregationSerializer;
import site.barsukov.barcodefx.serializer.XMLIsoCalendarModule;

@Configuration
public class XmlMapperConfig {

    @Bean
    public XmlMapper xmlMapper(){
        var xmlMapper = new XmlMapper();
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
        return xmlMapper;
    }
}
