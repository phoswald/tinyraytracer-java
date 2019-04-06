package tinyraytracer;

class Material {

    final float refractiveIndex;
    final Vec4f albedo;
    final Vec3f diffuseColor;
    final float specularExponent;

    Material(float refractiveIndex, Vec4f albedo, Vec3f diffuseColor, float specularExponent) {
        this.refractiveIndex = refractiveIndex;
        this.albedo = albedo;
        this.diffuseColor = diffuseColor;
        this.specularExponent = specularExponent;
    }
}
