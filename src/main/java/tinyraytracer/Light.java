package tinyraytracer;

class Light {

    final Vec3f position;
    final float intensity;

    Light(Vec3f position, float intensity) {
        this.position = position;
        this.intensity = intensity;
    }
}
