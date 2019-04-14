package com.github.phoswald.tinyraytracer;

import static java.lang.Math.PI;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static java.lang.Math.tan;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class Raytracer {

    private final Vec3f backgroundColor = new Vec3f(0.2f, 0.7f, 0.8f);
    private final Vec3f one = new Vec3f(1.0f, 1.0f, 1.0f);
    private final Vec3f checkerboardNormal = new Vec3f(0.0f, 1.0f, 0.0f);

    private final Material ivory = new Material(1.0f, new Vec4f(0.6f, 0.3f, 0.1f, 0.0f), new Vec3f(0.4f, 0.4f, 0.3f), 50.f);
    private final Material glass = new Material(1.5f, new Vec4f(0.0f, 0.5f, 0.1f, 0.8f), new Vec3f(0.6f, 0.7f, 0.8f), 125.f);
    private final Material redRubber = new Material(1.0f, new Vec4f(0.9f, 0.1f, 0.0f, 0.0f), new Vec3f(0.3f, 0.1f, 0.1f), 10.f);
    private final Material mirror = new Material(1.0f, new Vec4f(0.0f, 10.0f, 0.8f, 0.0f), new Vec3f(1.0f, 1.0f, 1.0f), 1425.f);
    private final Material checkerboardOdd = new Material(1.0f, new Vec4f(1.0f, 0.0f, 0.0f, 0.0f), new Vec3f(0.3f, 0.3f, 0.3f), 0.0f);
    private final Material checkerboardEven = new Material(1.0f, new Vec4f(1.0f, 0.0f, 0.0f, 0.0f), new Vec3f(0.3f, 0.2f, 0.1f), 0.0f);

    private final List<Sphere> spheres = Arrays.asList(
            new Sphere(new Vec3f(-3f, 0f, -16f), 2, ivory),
            new Sphere(new Vec3f(-1.0f, -1.5f, -12f), 2, glass),
            new Sphere(new Vec3f(1.5f, -0.5f, -18f), 3, redRubber),
            new Sphere(new Vec3f(7f, 5f, -18f), 4, mirror));

    private final List<Light> lights = Arrays.asList(
            new Light(new Vec3f(-20f, 20f, 20f), 1.5f),
            new Light(new Vec3f(30f, 50f, -25f), 1.8f),
            new Light(new Vec3f(30f, 20f, 30f), 1.7f));

    private static Vec3f reflect(Vec3f dir, Vec3f normal) {
        return dir.minus(normal.mul(2.f * (dir.mul(normal))));
    }

    private static Vec3f refract(Vec3f dir, Vec3f normal, float refractiveIndexMaterial, float refractiveIndexAir) { // Snell's law
        float cosi = -max(-1.f, min(1.f, dir.mul(normal)));
        if (cosi < 0) {
            // if the ray comes from the inside the object, swap the air and the media
            return refract(dir, normal.neg(), refractiveIndexAir, refractiveIndexMaterial);
        }

        float eta = refractiveIndexAir / refractiveIndexMaterial;
        float k = 1 - eta * eta * (1 - cosi * cosi);
        if(k < 0) {
            return new Vec3f(1, 0, 0); // total reflection, no ray to refract. refract it anyways, this has no physical meaning
        } else {
            return dir.mul(eta).plus(normal.mul(eta * cosi - (float) sqrt(k)));
        }
    }

    private Intersection sceneIntersect(Vec3f orig, Vec3f dir) {
        Intersection ixn = null;
        float minimalDist = Float.MAX_VALUE;

        for (Sphere sphere : spheres) {
            Float dist = sphere.rayIntersect(orig, dir);
            if (dist != null && dist.floatValue() < minimalDist) {
                minimalDist = dist.floatValue();
                Vec3f hit = orig.plus(dir.mul(dist.floatValue()));
                ixn = new Intersection(hit, (hit.minus(sphere.center)).normalize(), sphere.material);
            }
        }

        if (abs(dir.y) > 1e-3) {
            float dist = -(orig.y + 4) / dir.y; // the checkerboard plane has equation y = -4
            Vec3f hit = orig.plus(dir.mul(dist));
            if (dist > 0.0f && abs(hit.x) < 10.0f && hit.z < -10.0f && hit.z > -30.0f && dist < minimalDist) {
                minimalDist = dist;
                int x = (int) (.5 * hit.x + 1000);
                int z = (int) (.5 * hit.z);
                ixn = new Intersection(hit, checkerboardNormal, (x + z) % 2 == 1 ? checkerboardOdd : checkerboardEven);
            }
        }

        return ixn;
    }

    private Vec3f castRay(Vec3f orig, Vec3f dir, int depth) {
        Intersection ixn;
        if (depth > 4 || (ixn = sceneIntersect(orig, dir)) == null) {
            return backgroundColor;
        }

        Vec3f reflectDir = reflect(dir, ixn.normal).normalize();
        Vec3f refractDir = refract(dir, ixn.normal, ixn.material.refractiveIndex, 1.0f).normalize();
        Vec3f reflectOrig = reflectDir.mul(ixn.normal) < 0 ?
                ixn.hit.minus(ixn.normal.mul(1e-3f)) :
                ixn.hit.plus(ixn.normal.mul(1e-3f)); // offset the original point to avoid occlusion by the object itself
        Vec3f refractOrig = refractDir.mul(ixn.normal) < 0 ?
                ixn.hit.minus(ixn.normal.mul(1e-3f)) :
                ixn.hit.plus(ixn.normal.mul(1e-3f));
        Vec3f reflectColor = castRay(reflectOrig, reflectDir, depth + 1);
        Vec3f refractColor = castRay(refractOrig, refractDir, depth + 1);

        float diffuseIntensity = 0;
        float specularIntensity = 0;
        for (Light light : lights) {
            Vec3f lightDir = (light.position.minus(ixn.hit)).normalize();
            float lightDistance = (light.position.minus(ixn.hit)).norm();
            Vec3f shadowOrig = lightDir.mul(ixn.normal) < 0 ?
                    ixn.hit.minus(ixn.normal.mul(1e-3f)) :
                    ixn.hit.plus(ixn.normal.mul(1e-3f)); // checking if the point lies in the shadow of the light
            Intersection shadowIxn = sceneIntersect(shadowOrig, lightDir);
            if (shadowIxn != null && shadowIxn.hit.minus(shadowOrig).norm() < lightDistance) {
                continue;
            }

            diffuseIntensity += light.intensity * max(0.f, lightDir.mul(ixn.normal));
            specularIntensity += pow(max(0.f, reflect(lightDir.neg(), ixn.normal).neg().mul(dir)), ixn.material.specularExponent) * light.intensity;
        }

        return (ixn.material.diffuseColor.mul(diffuseIntensity).mul(ixn.material.albedo.x))
                .plus(one.mul(specularIntensity).mul(ixn.material.albedo.y))
                .plus(reflectColor.mul(ixn.material.albedo.z))
                .plus(refractColor.mul(ixn.material.albedo.w));
    }

    private static int toByte(float f) {
        return (int) (255.0 * max(0.f, min(1.f, f)));
    }

    private void render() throws IOException {
        int width = 1024;
        int height = 768;
        Vec3f[] framebuffer = new Vec3f[width * height];
        float fov = (float) (PI / 3.0);
        Vec3f orig = new Vec3f(0, 0, 0);

        for (int j = 0; j < height; j++) { // actual rendering loop
            for (int i = 0; i < width; i++) {
                float dirX = (float) ((i + 0.5) - width / 2.0);
                float dirY = (float) (-(j + 0.5) + height / 2.0); // this flips the image at the same time
                float dirZ = (float) (-height / (2.0 * tan(fov / 2.0)));
                framebuffer[i + j * width] = castRay(orig, new Vec3f(dirX, dirY, dirZ).normalize(), 0);
            }
        }

        try (OutputStream stream = Files.newOutputStream(Paths.get("out.ppm"))) {
            stream.write(("P6\n" + width + " " + height + "\n255\n").getBytes(StandardCharsets.US_ASCII));
            for (int i = 0; i < height * width; i++) {
                Vec3f pixel = framebuffer[i];
                float max = max(pixel.x, max(pixel.y, pixel.z));
                if (max > 1.0f) {
                    pixel = pixel.mul(1.0f / max);
                }
                stream.write(toByte(pixel.x));
                stream.write(toByte(pixel.y));
                stream.write(toByte(pixel.z));
            }
        }
    }

    public static void main(String[] args) throws IOException {
        new Raytracer().render();
    }
}
