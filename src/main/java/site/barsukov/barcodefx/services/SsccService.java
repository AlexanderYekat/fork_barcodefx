package site.barsukov.barcodefx.services;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.props.JsonSsccProps;

import java.util.ArrayList;
import java.util.List;

import static site.barsukov.barcodefx.props.JsonSsccProps.SsccProp.*;

@Service
@RequiredArgsConstructor
public class SsccService {

    private final PropService propService;

    public void saveProps(String lastExtension, String lastPrefix, String lastSerial) {
        propService.saveProp(LAST_PREFIX, lastPrefix);
        propService.saveProp(LAST_SERIAL, lastSerial);
        propService.saveProp(LAST_EXTENSION, lastExtension);
    }

    public Integer getLastExtension() {
        return propService.getFloatProp(LAST_EXTENSION).intValue();
    }

    public String getLastSerial() {
        return propService.getProp(LAST_SERIAL);
    }

    public String getPrefix() {
        return propService.getProp(LAST_PREFIX);
    }

    public String getNextCode() {
        var extension = Integer.toString(getLastExtension());
        var prefix = getPrefix();
        var serial = Integer.parseInt(getLastSerial())+1;
        var generated = generateCodes(serial,
            1,
            extension,
            prefix);

        saveProps(extension, prefix, Integer.toString(serial));
        return generated.get(0);
    }

    public List<String> generateCodes(int start, int numberOfCodes, String extension, String prefix) {
        List<String> result = new ArrayList<>();
        int end = start + numberOfCodes;
        for (int i = start; i < end; i++) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("00");
            String serial = StringUtils.leftPad(Integer.toString(i), 16 - prefix.length(), '0');
            String sscc = extension + prefix + serial;
            if (sscc.length() > 17) {
                throw new IllegalArgumentException("Длина SSCC превышает 17 символов " + sscc);
            }
            stringBuilder.append(sscc);
            stringBuilder.append(getControlDigit(sscc));
            result.add(stringBuilder.toString());
        }
        return result;
    }


    private String getControlDigit(String sscc) {
        int result = 0;
        int summ = 0;
        for (int i = 0; i < sscc.length(); i++) {
            int intChar = Character.getNumericValue(sscc.charAt(i));
            if (i % 2 != 0) {
                summ = summ + intChar;
            } else {
                summ = summ + intChar * 3;
            }
        }
        for (int i = 0; i < 10; i++) {
            if ((summ + i) % 10 == 0) {
                result = i;
            }
        }
        return Integer.toString(result);
    }
}
