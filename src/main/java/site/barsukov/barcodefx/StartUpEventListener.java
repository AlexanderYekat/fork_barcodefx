package site.barsukov.barcodefx;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import site.barsukov.barcodefx.model.enums.SysPropType;
import site.barsukov.barcodefx.props.JsonPrintProps;
import site.barsukov.barcodefx.services.PropService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;

import static site.barsukov.barcodefx.props.JsonUpdateProps.UpdateProp.*;
import static site.barsukov.barcodefx.services.PickingOrderService.*;

@Component
@RequiredArgsConstructor
public class StartUpEventListener {
    private static final String UNKNOWN = "UNKNOWN";
    private static final String SESSION_TOKEN = UUID.randomUUID().toString();

    private final PropService propService;

    @Value("${app.version}")
    private String appVersion;

    @EventListener(ApplicationReadyEvent.class)
    public void createOrdersFolders() throws IOException {
        if (Files.notExists(Path.of(PICKING_ORDERS_ROOT_PATH))) {
            Files.createDirectories(Paths.get(PICKING_ORDERS_ROOT_PATH));
        }
        if (Files.notExists(Path.of(LOCAL_PICKING_ORDERS_PATH))) {
            Files.createDirectories(Paths.get(LOCAL_PICKING_ORDERS_PATH));
        }
        if (Files.notExists(Path.of(REMOTE_PICKING_ORDERS_PATH))) {
            Files.createDirectories(Paths.get(REMOTE_PICKING_ORDERS_PATH));
        }
        if (Files.notExists(Path.of(LOCAL_PICKING_ORDERS_ARCHIVE_PATH))) {
            Files.createDirectories(Paths.get(LOCAL_PICKING_ORDERS_ARCHIVE_PATH));
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fillDefaultProps() {
        if (propService.getPropsByType(SysPropType.PRINT).isEmpty()) {
            Arrays.stream(JsonPrintProps.PrintProp.values())
                    .forEach(prop -> propService.saveProp(prop, prop.getDefaultValue()));
        }

        if (!propService.getProp(APP_VERSION).equals(getClass().getPackage().getImplementationVersion())) {
            propService.saveProp(APP_VERSION, appVersion);
        }
        if (UNKNOWN.equals(propService.getProp(CLIENT_ID))) {
            propService.saveProp(CLIENT_ID, UUID.randomUUID().toString());
        }
        if (!SESSION_TOKEN.equals(propService.getProp(SESSION_ID))) {
            propService.saveProp(SESSION_ID, SESSION_TOKEN);
        }
    }
}