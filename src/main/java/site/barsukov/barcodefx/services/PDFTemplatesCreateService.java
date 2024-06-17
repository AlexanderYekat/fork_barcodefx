package site.barsukov.barcodefx.services;

import com.google.common.collect.Lists;
import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.HtmlConverter;
import com.itextpdf.html2pdf.attach.impl.tags.ImgTagWorker;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.AreaBreak;
import com.itextpdf.layout.element.IBlockElement;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.property.AreaBreakType;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import site.barsukov.barcodefx.context.DataMatrixContext;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.LabelInfo;
import site.barsukov.barcodefx.model.PropDto;
import site.barsukov.barcodefx.model.enums.SysPropType;
import site.barsukov.barcodefx.templates.CustomTagWorkerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static site.barsukov.barcodefx.utils.Utils.TEMPLATES_FOLDER_PATH;
import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.DATAMATRIX_PER_TEMPLATE_LABEL;

public class PDFTemplatesCreateService extends BaseDocService<DataMatrixContext> {

    static final Logger logger = Logger.getLogger(PDFTemplatesCreateService.class);

    private CatalogService catalogService;

    public PDFTemplatesCreateService(DataMatrixContext context, CatalogService catalogService) {
        super(context);
        this.catalogService = catalogService;
    }

    public File performAction() throws IOException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File pdfFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + ".pdf");

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile.getAbsolutePath()));
        // IO

        createCustomPDF(pdfDoc, csvFile);
        return pdfFile;
    }


    private void createCustomPDF(PdfDocument pdfDoc, File csvFile) throws IOException {
        List<KM> KMs = readKMsFromFile(csvFile, context, true);
        pdfDoc.setDefaultPageSize(new PageSize(context.getWidth(), context.getHeight()));
        ConverterProperties properties = new ConverterProperties();
        Document document = new Document(pdfDoc);
        document.setMargins(0, 0, 0, 0);


        File htmlSource = new File(TEMPLATES_FOLDER_PATH + File.separatorChar + context.getTemplateName());
        int counter = 0;
        Map<String, ImgTagWorker> imgTagWorkerCash = new HashMap<>();
        List<List<KM>> partitions = Lists.partition(KMs, context.getPropService().getIntegerProp(DATAMATRIX_PER_TEMPLATE_LABEL));
        for (List<KM> curKm : partitions) {
            LabelInfo labelInfo = new LabelInfo();

            labelInfo.setCatalogElement(catalogService.getElements(curKm.stream().map(KM::getGtin).collect(Collectors.toList())));
            labelInfo.setPrintPageNumber(context.getPrintMarkNumber());
            labelInfo.setPageNumber(counter + context.getMarkNumberStart());
            labelInfo.setUserLabelText(context.getUserLabelText());
            if (context.getTotalPageNumber() == 0) {
                labelInfo.setTotalPageNumber(KMs.size());
            } else {
                labelInfo.setTotalPageNumber(context.getTotalPageNumber());
            }
            labelInfo.setPrintingProps(getPrintingProps());
            properties.setTagWorkerFactory(new CustomTagWorkerFactory(curKm, labelInfo, pdfDoc, imgTagWorkerCash));
            List<IElement> elements =
                HtmlConverter.convertToElements(new FileInputStream(htmlSource), properties);

            for (IElement element : elements) {
                document.add((IBlockElement) element);
            }
            if (!curKm.get(curKm.size() - 1).equals(KMs.get(KMs.size() - 1))) {
                document.add(new AreaBreak(AreaBreakType.NEXT_PAGE));
            }
            document.flush();
            counter++;
        }
        document.close();
    }

    private Map<String, String> getPrintingProps(){
        return context.getPropService().getPropsByType(SysPropType.PRINT)
                .stream().collect(Collectors.toMap(PropDto::getName, PropDto::getValue));
    }

    @Override
    public String getServiceName() {
        return "PDF_TEMPLATES_CREATE_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return false;
    }
}
