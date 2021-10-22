package me.grabsky.crates.crates.json;

import com.google.gson.annotations.Expose;
import me.grabsky.indigo.json.JsonItem;

public class JsonCrate {
    @Expose private String name;
    @Expose private String previewName;
    @Expose private JsonItem crateKey;
    @Expose private JsonReward[] rewards;

    public String getName() {
        return name;
    }

    public String getPreviewName() {
        return previewName;
    }

    public JsonItem getCrateKeyItem() {
        return crateKey;
    }

    public JsonReward[] getJsonRewards() {
        return rewards;
    }
}
