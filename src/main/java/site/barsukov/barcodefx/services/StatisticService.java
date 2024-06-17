package site.barsukov.barcodefx.services;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import site.barsukov.barcodefx.model.JsonSFAnswer;
import site.barsukov.barcodefx.model.LogDTO;
import site.barsukov.barcodefx.model.VersionAnswerDto;
import site.barsukov.barcodefx.model.enums.GoodCategory;
import site.barsukov.barcodefx.props.JsonAppProps;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticService {

    private final PropService propService;

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Async
    public void logAction(String serviceName, long linesProcessed, GoodCategory category){
        try {
            if (propService.getBooleanProp(STATS_ENABLED)) {
                LogDTO logDTO = new LogDTO();
                String clientId = propService.getProp(CLIENT_ID);
                logDTO.setClientId(clientId);
                logDTO.setSessionId(propService.getProp(SESSION_ID));
                logDTO.setAppVersion(propService.getProp(APP_VERSION));
                logDTO.setLinesProcessed(linesProcessed);
                logDTO.setServiceName(serviceName);
                if (category != null) {
                    logDTO.setCategoryName(category.name());
                }

                String statUrl = propService.getProp(STATS_URL);
                HttpPost post = new HttpPost(statUrl + "/barcodesfx/stat");


                String jsonString = MAPPER.writeValueAsString(logDTO);
                post.setEntity(new StringEntity(jsonString));

                post.setHeader("Accept", "application/json");
                post.setHeader("Content-type", "application/json; charset=utf-8");
                post.setHeader("Barcodes-token", clientId);


                try (CloseableHttpClient httpClient = HttpClientBuilder.create().build()) {
                    CloseableHttpResponse response = httpClient.execute(post);
                } catch (Exception e) {
                    log.error("Ошибка отправки статистики ", e);
                }
            }
        } catch (Exception e) {
            log.error("Ошибка отправки статистики ", e);
        }
    }
}
