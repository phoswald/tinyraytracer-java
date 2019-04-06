package tinyraytracer;

import static java.lang.Math.sqrt;

class Sphere {

    final Vec3f center;
    final float radius;
    final Material material;

    Sphere(Vec3f center, float radius, Material material) {
        this.center = center;
        this.radius = radius;
        this.material = material;
    }

    Float rayIntersect(Vec3f orig, Vec3f dir) {
        Vec3f l = center.minus(orig);
        float tca = l.mul(dir);
        float d2 = l.mul(l) - (tca * tca);
        if (d2 > radius * radius) {
            return null;
        }
        float thc = (float) sqrt(radius * radius - d2);
        float t0 = tca - thc;
        float t1 = tca + thc;
        if (t0 < 0) {
            t0 = t1;
        }
        if (t0 < 0) {
            return null;
        }
        return Float.valueOf(t0);
    }
}
