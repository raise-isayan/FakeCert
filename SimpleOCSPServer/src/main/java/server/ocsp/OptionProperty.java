package server.ocsp;

import com.google.gson.annotations.Expose;
import extension.helpers.json.JsonUtil;
import extension.view.base.IOptionProperty;
import java.io.File;
import java.io.IOException;

/**
 *
 * @author raise.isayan
 */
public final class OptionProperty implements IOptionProperty<OCSPProperty> {
    public final static String OCSP_PROPERTY = "OCSPPropery";

    public void setProperty(IOptionProperty<OCSPProperty> property) {
        this.setOption(property.getOption());
    }

    @Expose
    private final OCSPProperty ocsp = new OCSPProperty();

    public OCSPProperty getOption() {
        return this.ocsp;
    }

    public void setOption(OCSPProperty property) {
        this.ocsp.setProperty(property);
    }

    public void saveToJson(File fo) throws IOException {
        JsonUtil.saveToJson(fo, this, true);
    }

    public void loadFromJson(File fi) throws IOException {
        OptionProperty load = JsonUtil.loadFromJson(fi, OptionProperty.class, true);
        this.setOption(load.getOption());
    }

    public String stringToJson() {
        return JsonUtil.jsonToString(this, true);
    }

    public void stringFromJson(String json) {
        OptionProperty load = JsonUtil.jsonFromString(json, OptionProperty.class, true);
        this.setOption(load.getOption());
    }

}
