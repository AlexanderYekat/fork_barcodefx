package site.barsukov.barcodefx.props;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JsonAppProps {
    private JsonCatalogProps jsonCatalogProps;
    private JsonPrintProps jsonPrintProps;
    private JsonSsccProps jsonSsccProps;
    private JsonUpdateProps jsonUpdateProps;
    private JsonLastStateProps jsonLastStateProps;
}
