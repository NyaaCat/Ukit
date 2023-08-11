package cat.nyaa.ukit.utils;

import org.bukkit.Location;

public class Vector3D {
    public double x;
    public double y;
    public double z;

    public Vector3D() {
    }

    public Vector3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3D fromLongArray(long[] array) {
        return new Vector3D(Double.longBitsToDouble(array[0]), Double.longBitsToDouble(array[1]), Double.longBitsToDouble(array[2]));
    }

    public static Vector3D fromBukkitLocation(Location location) {
        return new Vector3D(location.getX(), location.getY(), location.getZ());
    }

    public double angle2dTo(Vector3D o) {
        var dx = o.x - x;
        var dz = o.z - z;
        var angleInDegrees = Math.atan2(dz, dx) * 180 / Math.PI;

        if (angleInDegrees >= 0)
            angleInDegrees += 90;
        if (angleInDegrees <= 0)
            angleInDegrees += 450;

        if (angleInDegrees > 360)
            angleInDegrees -= 360;

        return angleInDegrees;
    }

    public long[] toLangArray() {
        var array = new long[3];
        array[0] = Double.doubleToLongBits(x);
        array[1] = Double.doubleToLongBits(y);
        array[2] = Double.doubleToLongBits(z);
        return array;
    }

    public Vector3D add(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
        return this;
    }

    @Override
    public String toString() {
        return "Vector3D{" +
                "x=" + x +
                ", y=" + y +
                ", z=" + z +
                '}';
    }
}
