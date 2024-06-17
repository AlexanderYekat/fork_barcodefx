package site.barsukov.barcodefx.templates;

import com.itextpdf.html2pdf.attach.ITagWorker;
import com.itextpdf.html2pdf.attach.ProcessorContext;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.layout.IPropertyContainer;
import com.itextpdf.layout.element.Image;
import com.itextpdf.styledxmlparser.node.IElementNode;
import site.barsukov.barcodefx.CreateMarkTableUtil;
import site.barsukov.barcodefx.model.KM;
import site.barsukov.barcodefx.model.LabelInfo;

public class DatamatrixTagWorker implements ITagWorker {
    private KM km;
    private Image image;
    private LabelInfo labelInfo;
    private boolean printFnc;
    private PdfDocument pdfDocument;

    public DatamatrixTagWorker(KM km,  LabelInfo labelInfo, boolean printFnc, PdfDocument pdfDocument) {
        this.km = km;
        this.labelInfo = labelInfo;
        this.printFnc = printFnc;
        this.pdfDocument = pdfDocument;
    }

    @Override
    public void processEnd(IElementNode element, ProcessorContext context) {

        image = CreateMarkTableUtil.createDMImage(km, pdfDocument, labelInfo.getPrintingProps(), printFnc, labelInfo.getDmEnconodation());
        image.setHeight(labelInfo.getDatamtrixHeight());
        image.setWidth(labelInfo.getDatamtrixWidth());
        image.setMarginRight(labelInfo.getDatamtrixMarginRight());
        image.setMarginLeft(labelInfo.getDatamtrixMarginLeft());
        image.setMarginTop(labelInfo.getDatamtrixMarginTop());
        image.setMarginBottom(labelInfo.getDatamtrixMarginBottom());
    }

    @Override
    public boolean processContent(String content, ProcessorContext context) {
        return false;
    }

    @Override
    public boolean processTagChild(ITagWorker childTagWorker, ProcessorContext context) {
        return false;
    }

    @Override
    public IPropertyContainer getElementResult() {
        return image;
    }
}
