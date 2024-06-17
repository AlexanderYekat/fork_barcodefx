package site.barsukov.barcodefx.services;

import site.barsukov.barcodefx.model.yo.pickingorder.*;

import java.math.BigDecimal;

import site.barsukov.barcodefx.utils.Utils;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import site.barsukov.barcodefx.serializer.XMLCalendarModule;

public class PickingOrderServiceTest {
//    @Mock
//    XmlMapper xmlMapper;
//    @InjectMocks
//    PickingOrderService pickingOrderService;
//
//    @Before
//    public void setUp() {
//        MockitoAnnotations.initMocks(this);
//    }

//    @Test
//    public void testSavePickingOrder() throws Exception {
//        File result = PickingOrderService.savePickingOrder(new File(getClass().getResource("/site/barsukov/barcodefx/services/PleaseReplaceMeWithTestFile.txt").getFile()), "state", "comment", GoodCategory.SHOES);
//        Assert.assertEquals(new File(getClass().getResource("/site/barsukov/barcodefx/services/PleaseReplaceMeWithTestFile.txt").getFile()), result);
//    }
//
//    @Test
//    public void testGetLocalPickingOrders() throws Exception {
//        List<PickingOrder> result = PickingOrderService.getLocalPickingOrders();
//        Assert.assertEquals(Arrays.<PickingOrder>asList(new PickingOrder()), result);
//    }

//    @Test
//    public void testParcePikingOrder() throws Exception {
//        SymmetricKeyType symmetricKeyType = new SymmetricKeyType();
//        symmetricKeyType.setPassword("Не скажу");
//        symmetricKeyType.setSalt("Супер секрет");
//        symmetricKeyType.setIterationCount(999);
//        symmetricKeyType.setKeyLength(128);
//
//        SecurityType securityType = new SecurityType();
//        securityType.setEncrypted(true);
//        securityType.setSymmetricKey(symmetricKeyType);
//
//        GtinAdditionalInfoType gtinAdditionalInfo1 = new GtinAdditionalInfoType();
//        gtinAdditionalInfo1.setGtin("0122222222222222");
//        gtinAdditionalInfo1.setAdditionalInfo("EAN = 58974");
//
//        GtinAdditionalInfoType gtinAdditionalInfo2 = new GtinAdditionalInfoType();
//        gtinAdditionalInfo2.setGtin("013333333333333");
//        gtinAdditionalInfo2.setAdditionalInfo("EAN = 589747777");
//
//        PickingOrder pickingOrder = new PickingOrder();
//        pickingOrder.setFileName("fileName.txt");
//        pickingOrder.setCisCount(0);
//        pickingOrder.setState("Ready-Steady-GO!");
//        pickingOrder.setComment("Отгрузка на склад супер-поставщика");
//        pickingOrder.setProductGroup(ProductGroupType.ELECTRONICS);
//        pickingOrder.setMd5("1234567890");
//        pickingOrder.setAggregationCode("0123456789``");
//        pickingOrder.setAdditionalInfo("Не забудьте распечатать марки и проверить наличие проводов");
////        pickingOrder.setCodesType(CodesTypeType.AGGREGATE_KI);
//        pickingOrder.setCreationDate(Utils.getXMLGregorianCalendarNow());
//        pickingOrder.setModificationDate(Utils.getXMLGregorianCalendarNow());
//        pickingOrder.setSecurity(securityType);
//        pickingOrder.setVersion(new BigDecimal("1"));
//        pickingOrder.getGtinAdditionalInfo().add(gtinAdditionalInfo1);
//        pickingOrder.getGtinAdditionalInfo().add(gtinAdditionalInfo2);
//
//        XmlMapper xmlMapper = new XmlMapper();
//
//        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
//        xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
//        JaxbAnnotationModule module = new JaxbAnnotationModule();
//        xmlMapper.registerModule(new XMLCalendarModule());
//        xmlMapper.registerModule(module);
//        System.out.println(xmlMapper.writeValueAsString(pickingOrder));
//
//    }
}

//Generated with love by TestMe :) Please report issues and submit feature requests at: http://weirddev.com/forum#!/testme