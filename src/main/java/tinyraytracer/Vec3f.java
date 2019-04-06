package tinyraytracer;

import static java.lang.Math.sqrt;

class Vec3f {

    final float x;
    final float y;
    final float z;

    Vec3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vec3f neg() {
        return new Vec3f(-x, -y, -z);
    }

    Vec3f plus(Vec3f other) {
        return new Vec3f(x + other.x, y + other.y, z + other.z);
    }

    Vec3f minus(Vec3f other) {
        return new Vec3f(x - other.x, y - other.y, z - other.z);
    }

    Vec3f mul(float factor) {
        return new Vec3f(x * factor, y * factor, z * factor);
    }

    float mul(Vec3f other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    float norm() {
        return (float) sqrt((x * x + y * y + z * z));
    }

    Vec3f normalize() {
        return mul(1.0f / norm());
    }
}
