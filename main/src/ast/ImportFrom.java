package ast;

import java.util.List;

/**
 * Created by Nik on 25-5-15.
 */
public class ImportFrom extends Import {

    private final String location;

    public ImportFrom(LocInfo locInfo, String location) {
        super(locInfo);
        this.location = location;
    }
}
