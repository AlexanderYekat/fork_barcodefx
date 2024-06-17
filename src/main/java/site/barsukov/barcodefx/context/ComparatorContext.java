package site.barsukov.barcodefx.context;

import lombok.Data;

import java.io.File;

@Data
public class ComparatorContext extends BaseContext {
    private boolean ignoreTails;
    private File comparedFile;
}
