package com.github.phoswald.tinyraytracer;

class Intersection {

    final Vec3f hit;
    final Vec3f normal;
    final Material material;

    Intersection(Vec3f hit, Vec3f normal, Material material) {
        this.hit = hit;
        this.normal = normal;
        this.material = material;
    }
}
