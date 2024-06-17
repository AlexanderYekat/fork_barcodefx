package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.JsonSFAnswer;
import site.barsukov.barcodefx.model.VersionAnswerDto;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

@Slf4j
@Service
public class VersionService {
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public VersionAnswerDto getLastVersionInfo() {
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet("https://sourceforge.net/projects/barcodesfx/best_release.json");
        try {
            log.info(request.toString());
            HttpResponse response = client.execute(request);
            BufferedReader rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String line;
            line = rd.readLine();
            JsonSFAnswer answer = MAPPER.readValue(line, JsonSFAnswer.class);
            VersionAnswerDto release = answer.getPlatformRelease().getWindows();
            release.setVersion(getVersion(release.getFilename()));
            return release;
        } catch (IOException e) {
            log.error("Ошибка получения обновления");
            return null;
        }
    }

    private String getVersion(String filename) {
        filename = FilenameUtils.removeExtension(filename);
        return filename.replace("/barCodesFX-", "");
    }
}
