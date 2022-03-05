package cat.nyaa.ukit.sit;

import cat.nyaa.ukit.utils.Vector3D;

public class BlockPreference {
    public String idRegex = ".*_slab";
    public Vector3D locOffset = new Vector3D(0, 0, 0);

    public BlockPreference() {
    }

    public BlockPreference(String idRegex, Vector3D locOffset) {
        this.idRegex = idRegex;
        this.locOffset = locOffset;
    }
}
