package site.barsukov.barcodefx.utils;

import site.barsukov.barcodefx.model.enums.GoodCategory;

public class ScannerUtils {
    public static String fixLine(String text, GoodCategory goodCategory) {
        Integer serial = goodCategory.getSerialLength();
        if (text.length() < 18 + serial + 6) {
            return text;
        }
        if (!text.contains("\u001D")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(text.substring(0, 18 + serial));
            stringBuilder.append('\u001D');
            stringBuilder.append(text.substring(18 + serial, 17 + serial + 7));
            String cryptoTail = text.substring(18 + serial + 6);
            if (cryptoTail.length() > 0) {
                stringBuilder.append('\u001D');
                stringBuilder.append(cryptoTail);
            }
            return stringBuilder.toString();
        }
        return text;
    }
}
