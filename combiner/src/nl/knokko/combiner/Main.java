package nl.knokko.combiner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        handlePrefix("blockTypes");
        handlePrefix("damageCauses");
        handlePrefix("enchantments");
        handlePrefix("entities");
        handlePrefix("materials");
        handlePrefix("particles");
        handlePrefix("potionEffects");
        handlePrefix("sounds");
        handlePrefix("biomes");
        handlePrefix("soundCategories");
        handlePrefix("treeTypes");
        handlePrefix("itemFlags");
        handlePrefix("foodTypes");
        handlePrefix("fuel");
        handlePrefix("smeltables");
        handleRawDamageCauses();
    }

    static String toUpperSnakeCase(String camelCase) {
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < camelCase.length(); index++) {
            char currentChar = camelCase.charAt(index);
            if (Character.isUpperCase(currentChar)) {
                result.append('_');
            }
            result.append(Character.toUpperCase(currentChar));
        }
        return result.toString();
    }

    static void handleRawDamageCauses() {
        List<String> causes12 = getRawDamageCauses("1.12", false);
        List<String> causes13 = getRawDamageCauses("1.13", true);
        List<String> causes14 = getRawDamageCauses("1.14", true);
        List<String> causes15 = getRawDamageCauses("1.15", true);
        List<String> causes16 = getRawDamageCauses("1.16", true);
        List<String> causes17 = getRawDamageCauses("1.17", true);
        List<String> causes18 = getRawDamageCauses("1.18", true);
        List<String> causes19 = getRawDamageCauses("1.19", true);
        List<String> causes20 = getRawDamageCauses("1.20", true);

        List<EnumValue> values = determineVersions(
                new Pair(12, causes12),
                new Pair(13, causes13),
                new Pair(14, causes14),
                new Pair(15, causes15),
                new Pair(16, causes16),
                new Pair(17, causes17),
                new Pair(18, causes18),
                new Pair(19, causes19),
                new Pair(20, causes20)
        );

        try {
            PrintWriter printer = new PrintWriter("rawDamageCausesPart.txt");
            for (EnumValue rawCause : values) {
                printer.println(
                        "\t" + toUpperSnakeCase(rawCause.name) + "(\"" + rawCause.name + "\", " + "VERSION1_" + rawCause.minVersion + ", VERSION1_" + rawCause.maxVersion + "),"
                );
            }
            printer.flush();
            printer.close();
        } catch (IOException io) {
            // Shouldn't happen
            throw new Error(io);
        }
    }

    static void handlePrefix(String prefix) {
        List<String> values12 = getEnumValues(prefix, "1.12");
        List<String> values13 = getEnumValues(prefix, "1.13");
        List<String> values14 = getEnumValues(prefix, "1.14");
        List<String> values15 = getEnumValues(prefix, "1.15");
        List<String> values16 = getEnumValues(prefix, "1.16");
        List<String> values17 = getEnumValues(prefix, "1.17");
        List<String> values18 = getEnumValues(prefix, "1.18");
        List<String> values19 = getEnumValues(prefix, "1.19");
        List<String> values20 = getEnumValues(prefix, "1.20");

        List<EnumValue> values = determineVersions(
                new Pair(12, values12),
                new Pair(13, values13),
                new Pair(14, values14),
                new Pair(15, values15),
                new Pair(16, values16),
                new Pair(17, values17),
                new Pair(18, values18),
                new Pair(19, values19),
                new Pair(20, values20)
        );

        generateMaterialsEnum(new File(prefix + "Part.txt"), values);
    }

    static void generateMaterialsEnum(File dest, Collection<EnumValue> materials) {
        try {
            PrintWriter printer = new PrintWriter(dest);
            for (EnumValue material : materials) {
                printer.println(
                        "\t" + material.name + "(VERSION1_" + material.minVersion + ", VERSION1_" + material.maxVersion + "),"
                );
            }
            printer.flush();
            printer.close();
        } catch (IOException io) {
            // Shouldn't happen
            throw new Error(io);
        }
    }

    static List<EnumValue> determineVersions(Pair...pairs) {

        Map<String, EnumValue> materialMap = new HashMap<>();
        List<EnumValue> result = new ArrayList<>();

        for (Pair pair : pairs) {
            for (String materialName : pair.materialNames) {

                EnumValue existing = materialMap.get(materialName);
                if (existing == null) {
                    EnumValue next = new EnumValue(materialName, pair.version, pair.version);
                    materialMap.put(materialName, next);
                    result.add(next);
                } else {
                    if (pair.version < existing.minVersion)
                        existing.minVersion = pair.version;
                    if (pair.version > existing.maxVersion)
                        existing.maxVersion = pair.version;
                }
            }
        }

        return result;
    }

    static List<String> getEnumValues(String prefix, String version) {
        try {
            List<String> materialNames = new ArrayList<>();
            File file = new File("sets/" + prefix + version + ".txt");
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

    static List<String> getRawDamageCauses(String version, boolean isJson) {
        try {
            List<String> rawCauses = new ArrayList<>();
            File file = new File("sets/lang" + version + (isJson ? ".json" : ".txt"));
            Scanner scanner = new Scanner(file);

            String rawDamagePrefix = "death.attack.";
            if (isJson) rawDamagePrefix = "  \"" + rawDamagePrefix;

            while (scanner.hasNextLine()) {
                String nextLine = scanner.nextLine();
                if (nextLine.startsWith(rawDamagePrefix)) {
                    int endIndexType = nextLine.indexOf('.', rawDamagePrefix.length());
                    if (endIndexType != -1) {
                        String rawCause = nextLine.substring(rawDamagePrefix.length(), endIndexType);
                        if (nextLine.startsWith(rawDamagePrefix + rawCause + ".player")) {
                            rawCauses.add(rawCause);
                        }
                    }
                }
            }

            scanner.close();
            return rawCauses;
        } catch (IOException io) {
            // Shouldn't happen anyway
            throw new Error(io);
        }
    }

    static class Pair {

        final int version;
        final List<String> materialNames;

        Pair(int version, List<String> materialNames) {
            this.version = version;
            this.materialNames= materialNames;
        }
    }

    static class EnumValue {

        final String name;

        int minVersion;
        int maxVersion;

        EnumValue(String name, int minVersion, int maxVersion) {
            this.name = name;
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
        }
    }
}
