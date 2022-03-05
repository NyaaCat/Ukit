package cat.nyaa.ukit.sit;

import cat.nyaa.ukit.utils.Vector3D;
import com.google.gson.*;
import org.bukkit.Material;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;
import java.util.regex.Pattern;

public class SitConfig {
    public static final Serializer serializer = new Serializer();
    private final List<BlockPreference> enabledBlocks;
    private final Map<Material, Vector3D> blockOffset = new HashMap<>();

    public SitConfig() {
        this(List.of(
                new BlockPreference(".*_stairs", new Vector3D(0, -0.5, 0)),
                new BlockPreference(".*_slab", new Vector3D(0, 0, 0)),
                new BlockPreference(".*_carpet", new Vector3D(0, 0, 0))
        ));
    }

    public SitConfig(@Nonnull List<BlockPreference> preferenceList) {
        enabledBlocks = preferenceList;
        var preferences = enabledBlocks.stream().map((blockPreference) -> Pattern.compile(blockPreference.idRegex)).toList();
        var preferenceSettings = enabledBlocks.stream().map((blockPreference) -> blockPreference.locOffset).toList();
        Arrays.stream(Material.values()).forEach((material) -> {
            for (int i = 0; i < preferences.size(); i++) {
                if (preferences.get(i).matcher(material.name().toLowerCase()).matches()) {
                    blockOffset.put(material, preferenceSettings.get(i));
                    break;
                }
            }
        });
    }

    public boolean isEnabled(Material material) {
        return blockOffset.containsKey(material);
    }

    @Nullable
    public Vector3D getOffset(Material material) {
        return blockOffset.get(material);
    }

    static class Serializer implements JsonSerializer<SitConfig>, JsonDeserializer<SitConfig> {
        @Override
        public SitConfig deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
            var jsonArray = jsonElement.getAsJsonObject().getAsJsonArray("enabledBlocks");
            var preferenceList = new ArrayList<BlockPreference>(jsonArray.size());
            jsonArray.forEach((element) -> preferenceList.add(jsonDeserializationContext.deserialize(element, BlockPreference.class)));
            return new SitConfig(preferenceList);
        }

        @Override
        public JsonElement serialize(SitConfig sitConfig, Type type, JsonSerializationContext jsonSerializationContext) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.add("enabledBlocks", jsonSerializationContext.serialize(sitConfig.enabledBlocks));
            return jsonObject;
        }
    }
}
