package site.barsukov.barcodefx.services;

import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.IElement;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.VerticalAlignment;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import site.barsukov.barcodefx.CreateMarkTableUtil;
import site.barsukov.barcodefx.context.DataMatrixContext;
import site.barsukov.barcodefx.model.KM;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static site.barsukov.barcodefx.props.JsonPrintProps.PrintProp.*;

public class PDFCreateService extends BaseDocService<DataMatrixContext> {

    private final PropService propService;

    public PDFCreateService(DataMatrixContext context) {
        super(context);
        propService = context.getPropService();
    }

    public File performAction() throws IOException {
        File csvFile = new File(context.getCsvFileName());
        String fileName = FilenameUtils.removeExtension(csvFile.getName());
        File pdfFile = new File(context.getResultFolder() + File.separatorChar + fileName + getRangeString() + ".pdf");

        PdfDocument pdfDoc = new PdfDocument(new PdfWriter(pdfFile.getAbsolutePath()));
        if (context.isTermoPrinter()) {
            createCustomPDF(pdfDoc, csvFile);
        } else {
            createA4PDF(pdfDoc, csvFile);
        }

        return pdfFile;
    }

    private int getNumberOfColumns() {
        if (context.isHorizontal()) {
            return propService.getFloatProp(NUMBER_OF_HORIZONTAL_COLUMNS).intValue();
        } else if (context.isVertical()) {
            return propService.getFloatProp(NUMBER_OF_VERTICAL_COLUMNS).intValue();
        } else {
            throw new IllegalArgumentException("Ошибка определения направления этикетки.");
        }
    }


    private void addRows(List<KM> kms, Table table, PdfDocument document) throws IOException {

        int colNum = table.getNumberOfColumns();
        int counter = 0;
        for (int i = 0; i < kms.size(); i++) {
            if (context.isHorizontal()) {
                table.addCell(CreateMarkTableUtil.createMarkTableHorizontal(kms.get(i), document,
                    propService.getFloatProp(HORIZONTAL_A4_LABEL_HEIGHT),
                    propService.getFloatProp(HORIZONTAL_A4_LABEL_WIDTH),
                    context, i + context.getMarkNumberStart()));

            } else if (context.isVertical()) {
                table.addCell(CreateMarkTableUtil.createMarkTableVertical(kms.get(i), document,
                    propService.getFloatProp(VERTICAL_A4_LABEL_HEIGHT),
                    propService.getFloatProp(VERTICAL_A4_LABEL_WIDTH),
                    context, i + context.getMarkNumberStart()));
            } else {
                throw new IllegalArgumentException("Ошибка определения направления этикетки.");
            }
            counter++;
            if (counter == colNum) {
                counter = 0;
                table.startNewRow();
            }
        }
    }


    private void createA4PDF(PdfDocument pdfDoc, File csvFile) throws IOException {
        try (Document doc = new Document(pdfDoc)) {

            Table table = new Table(getNumberOfColumns());
            List<KM> KMs = readKMsFromFile(csvFile, context, true);
            addRows(KMs, table, pdfDoc);
            doc.setMargins(
                propService.getFloatProp(LABEL_A4_MARGIN_TOP),
                propService.getFloatProp(LABEL_A4_MARGIN_RIGHT),
                propService.getFloatProp(LABEL_A4_MARGIN_BOTTOM),
                propService.getFloatProp(LABEL_A4_MARGIN_LEFT));
            if (propService.getBooleanProp(LABEL_A4_DISABLE_BOARDERS)) {
                tableDeleteBoarders(table);
            }
            doc.add(table);
            doc.close();
        }

    }

    private void tableDeleteBoarders(Table table) {
        for (IElement child : table.getChildren()) {
            if (child instanceof Table) {
                tableDeleteBoarders((Table) child);
            }
            if (child instanceof Cell) {
                tableDeleteBoarders((Cell) child);
            }
        }
    }

    private void tableDeleteBoarders(Cell cell) {
        cell.setBorder(Border.NO_BORDER);
        for (IElement child : cell.getChildren()) {
            if (child instanceof Table) {
                tableDeleteBoarders((Table) child);
            }
            if (child instanceof Cell) {
                tableDeleteBoarders((Cell) child);
            }
        }
    }

    private void createCustomPDF(PdfDocument pdfDoc, File csvFile) throws IOException {
        pdfDoc.setDefaultPageSize(new PageSize(context.getWidth(), context.getHeight()));

        try (Document doc = new Document(pdfDoc)) {

            List<KM> KMs = readKMsFromFile(csvFile, context, true);
            for (int i = 0; i < KMs.size(); i++) {
                Table table = CreateMarkTableUtil.createMarkTableHorizontal(KMs.get(i), pdfDoc,
                    //magic число 5, без него едет верстка
                    context.getHeight() - propService.getFloatProp(LABEL_CUSTOM_MARGIN_TOP) - propService.getFloatProp(LABEL_CUSTOM_MARGIN_BOTTOM) - 5,
                    context.getWidth() - propService.getFloatProp(LABEL_CUSTOM_MARGIN_LEFT) - propService.getFloatProp(LABEL_CUSTOM_MARGIN_RIGHT),
                    context, i + context.getMarkNumberStart());
                table.getCell(0, 0).setBorder(Border.NO_BORDER);
                table.getCell(0, 0).setVerticalAlignment(VerticalAlignment.MIDDLE);
                if (!KMs.get(i).isSscc()) {
                    table.getCell(0, 1).setBorder(Border.NO_BORDER);

                } else {
                }
                doc.setMargins(propService.getFloatProp(LABEL_CUSTOM_MARGIN_TOP),
                    propService.getFloatProp(LABEL_CUSTOM_MARGIN_RIGHT),
                    propService.getFloatProp(LABEL_CUSTOM_MARGIN_BOTTOM),
                    propService.getFloatProp(LABEL_CUSTOM_MARGIN_LEFT));
                doc.add(table);
                doc.flush();
            }
            doc.close();
        }
    }

    @Override
    public String getServiceName() {
        return "PDF_CREATE_SERVICE";
    }

    @Override
    public boolean isXmlResult() {
        return false;
    }
}
