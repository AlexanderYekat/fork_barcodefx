package site.barsukov.barcodefx;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.stax2.io.EscapingWriterFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

public class CrptEscapeFactory implements EscapingWriterFactory {
    public static final EscapingWriterFactory theInstance = new CrptEscapeFactory();
    private CrptEscapeFactory() {
    }

    public Writer createEscapingWriterFor(final Writer out, String enc) {
        return new Writer(){
            @Override
            public void write(char[] cbuf, int off, int len) throws IOException {
                String val = "";
                for (int i = off; i < len; i++) {
                    val += cbuf[i];
                }
                String escapedStr =  StringEscapeUtils.escapeXml(val);
                out.write(escapedStr);
            }

            @Override
            public void flush() throws IOException {
                out.flush();
            }

            @Override
            public void close() throws IOException {
                out.close();
            }
        };
    }

    public Writer createEscapingWriterFor(OutputStream out, String enc) {
        throw new IllegalArgumentException("not supported");
    }
}
