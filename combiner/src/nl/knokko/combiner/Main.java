package nl.knokko.combiner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {

        String prefix = "materials";
        Set<String> materials12 = getMaterialNames(prefix, "1.12");
        Set<String> materials13 = getMaterialNames(prefix, "1.13");
        Set<String> materials14 = getMaterialNames(prefix, "1.14");
        Set<String> materials15 = getMaterialNames(prefix, "1.15");
        Set<String> materials16 = getMaterialNames(prefix, "1.16");

        Collection<Material> materials = determineVersions(
                new Pair(12, materials12),
                new Pair(13, materials13),
                new Pair(14, materials14),
                new Pair(15, materials15),
                new Pair(16, materials16)
        );

        generateMaterialsEnum(new File("enumPart.txt"), "VERSION1_", materials);
    }

    static void generateMaterialsEnum(File dest, String prefix, Collection<Material> materials) {
        try {
            PrintWriter printer = new PrintWriter(dest);
            for (Material material : materials) {
                printer.println(
                        "\t" + material.name + "(" + prefix + material.minVersion + ", " + prefix + material.maxVersion + "),"
                );
            }
            printer.flush();
            printer.close();
        } catch (IOException io) {
            // Shouldn't happen
            throw new Error(io);
        }
    }

    static Collection<Material> determineVersions(Pair...pairs) {

        Map<String, Material> materialMap = new HashMap<>();
        for (Pair pair : pairs) {
            for (String materialName : pair.materialNames) {

                Material existing = materialMap.get(materialName);
                if (existing == null) {
                    materialMap.put(materialName, new Material(materialName, pair.version, pair.version));
                } else {
                    if (pair.version < existing.minVersion)
                        existing.minVersion = pair.version;
                    if (pair.version > existing.maxVersion)
                        existing.maxVersion = pair.version;
                }
            }
        }

        return materialMap.values();
    }

    static Set<String> getMaterialNames(String prefix, String version) {
        try {
            Set<String> materialNames = new TreeSet<>();
            File file = new File("../sets/" + prefix + version + ".txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                materialNames.add(scanner.nextLine());
            }
            scanner.close();
            return materialNames;
        } catch (IOException io) {
            // Shouldn't happen anyway
            throw new Error(io);
        }
    }

    static class Pair {

        final int version;
        final Set<String> materialNames;

        Pair(int version, Set<String> materialNames) {
            this.version = version;
            this.materialNames= materialNames;
        }
    }

    static class Material {

        final String name;

        int minVersion;
        int maxVersion;

        Material(String name, int minVersion, int maxVersion) {
            this.name = name;
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
        }
    }
}
