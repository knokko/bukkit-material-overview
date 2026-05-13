package nl.knokko.combiner;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Main {

    public static void main(String[] args) {
        handlePrefix("blockTypes", "VBlockType");
        handlePrefix("damageCauses", "VDamageCause");
        handlePrefix("enchantments", "VEnchantment");
        handlePrefix("entities", "VEntityType");
        handlePrefix("materials", "VMaterial");
        handlePrefix("particles", "VParticle");
        handlePrefix("potionEffects", "VEffectType");
        handlePrefix("sounds", "VSoundType");
        handlePrefix("biomes", "VBiome");
        handlePrefix("soundCategories", "VSoundCategory");
        handlePrefix("treeTypes", "VTreeType");
        handlePrefix("itemFlags", "VItemFlag");
        handlePrefix("foodTypes", "VFoodType");
        handlePrefix("fuel", "VFuelType");
        handlePrefix("smeltables", "VFurnaceInput");
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

    private static String versionString(int raw) {
        if (raw <= 21) return "VERSION1_" + raw;
        else return "VERSION" + raw;
    }

    static void handleRawDamageCauses() {
        List<List<String>> causes12 = getRawDamageCauses("1.12", false);
        List<List<String>> causes13 = getRawDamageCauses("1.13", true);
        List<List<String>> causes14 = getRawDamageCauses("1.14", true);
        List<List<String>> causes15 = getRawDamageCauses("1.15", true);
        List<List<String>> causes16 = getRawDamageCauses("1.16", true);
        List<List<String>> causes17 = getRawDamageCauses("1.17", true);
        List<List<String>> causes18 = getRawDamageCauses("1.18", true);
        List<List<String>> causes19 = getRawDamageCauses("1.19", true);
        List<List<String>> causes20 = getRawDamageCauses("1.20", true);
        List<List<String>> causes21 = getRawDamageCauses("1.21", true);
        List<List<String>> causes26 = getRawDamageCauses("26", true);

        List<EnumValue> values = determineVersions(
                new Pair(12, causes12),
                new Pair(13, causes13),
                new Pair(14, causes14),
                new Pair(15, causes15),
                new Pair(16, causes16),
                new Pair(17, causes17),
                new Pair(18, causes18),
                new Pair(19, causes19),
                new Pair(20, causes20),
                new Pair(21, causes21),
                new Pair(26, causes26)
        );

        try {
            PrintWriter printer = new PrintWriter("combiner/rawDamageCausesPart.txt");
            for (EnumValue rawCause : values) {
                printer.println(
                        "\t" + toUpperSnakeCase(rawCause.name) + "(\"" + rawCause.name + "\", " + versionString(rawCause.minVersion) + ", " + versionString(rawCause.maxVersion) + "),"
                );
            }
            printer.flush();
            printer.close();
        } catch (IOException io) {
            // Shouldn't happen
            throw new Error(io);
        }
    }

    static void handlePrefix(String prefix, String className) {
        List<List<String>> values12 = getEnumValues(prefix, "1.12");
        List<List<String>> values13 = getEnumValues(prefix, "1.13");
        List<List<String>> values14 = getEnumValues(prefix, "1.14");
        List<List<String>> values15 = getEnumValues(prefix, "1.15");
        List<List<String>> values16 = getEnumValues(prefix, "1.16");
        List<List<String>> values17 = getEnumValues(prefix, "1.17");
        List<List<String>> values18 = getEnumValues(prefix, "1.18");
        List<List<String>> values19 = getEnumValues(prefix, "1.19");
        List<List<String>> values20 = getEnumValues(prefix, "1.20");
        List<List<String>> values21 = getEnumValues(prefix, "1.21");
        List<List<String>> values26 = getEnumValues(prefix, "26");

        List<EnumValue> values = determineVersions(
                new Pair(12, values12),
                new Pair(13, values13),
                new Pair(14, values14),
                new Pair(15, values15),
                new Pair(16, values16),
                new Pair(17, values17),
                new Pair(18, values18),
                new Pair(19, values19),
                new Pair(20, values20),
                new Pair(21, values21),
                new Pair(26, values26)
        );

        generateMaterialsEnum(new File("combiner/" + prefix + "Part.txt"), className, values);
    }

    static void generateMaterialsEnum(File dest, String className, Collection<EnumValue> materials) {
        try {
            PrintWriter printer = new PrintWriter(dest);
            boolean containsDots = materials.stream().anyMatch(
                    material -> material.name.contains(".") || material.name.contains(":")
            );
            boolean hasMany = materials.size() > 3000;
            for (EnumValue material : materials) {
                String constantName;
                if (containsDots) {
                    constantName = material.name.replace('.', '_').replace(':', '_');
                } else {
                    constantName = material.name;
                }

                if (hasMany) {
                    printer.print("\tpublic static final " + className + " " + constantName + " = new " + className);
                } else {
                    printer.print("\t" + constantName);
                }

                printer.print("(" + versionString(material.minVersion) + ", " + versionString(material.maxVersion));
                if (containsDots) {
                    printer.print(", \"" + material.name + '"');
                }
                for (String parameter : material.parameters) printer.print(", " + parameter);

                if (hasMany) printer.println(");");
                else printer.println("),");
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
            for (List<String> values : pair.materialValues) {

                String materialName = values.get(0);
                List<String> parameterValues = values.subList(1, values.size());
                EnumValue existing = materialMap.get(materialName);
                if (existing == null) {
                    EnumValue next = new EnumValue(materialName, pair.version, pair.version, parameterValues);
                    materialMap.put(materialName, next);
                    result.add(next);
                } else {
                    if (pair.version < existing.minVersion) existing.minVersion = pair.version;
                    if (pair.version > existing.maxVersion) existing.maxVersion = pair.version;
                    if (!parameterValues.equals(existing.parameters)) {
                        System.out.println("[WARNING] Parameter mismatch for " + materialName +
                                ": " + existing.parameters + " vs " + parameterValues);
                        existing.parameters = parameterValues;
                    }
                }
            }
        }

        return result;
    }

    static List<List<String>> getEnumValues(String prefix, String version) {
        try {
            List<List<String>> values = new ArrayList<>();
            File file = new File("sets/" + prefix + version + ".txt");
            Scanner scanner = new Scanner(file);
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                int startBracket = line.indexOf('(');
                if (startBracket == -1) {
                    List<String> value = new ArrayList<>(1);
                    value.add(line);
                    values.add(value);
                } else {
                    int endBracket = line.indexOf(')');
                    if (endBracket == -1) throw new IllegalArgumentException("Malformed line " + line);

                    String name = line.substring(0, startBracket);
                    String parameters = line.substring(startBracket + 1, endBracket);
                    String[] split = parameters.split(", ");

                    List<String> value = new ArrayList<>(1 + split.length);
                    value.add(name);
                    if (split.length > 1 || !split[0].isEmpty()) {
                        value.addAll(Arrays.asList(split));
                    }
                    values.add(value);
                }
            }
            scanner.close();
            return values;
        } catch (IOException io) {
            // Shouldn't happen anyway
            throw new Error(io);
        }
    }

    static List<List<String>> getRawDamageCauses(String version, boolean isJson) {
        try {
            List<List<String>> rawCauses = new ArrayList<>();
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
                            List<String> rawList = new ArrayList<>(1);
                            rawList.add(rawCause);
                            rawCauses.add(rawList);
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
        final List<List<String>> materialValues;

        Pair(int version, List<List<String>> materialValues) {
            this.version = version;
            this.materialValues = materialValues;
        }
    }

    static class EnumValue {

        final String name;

        int minVersion;
        int maxVersion;
        List<String> parameters;

        EnumValue(String name, int minVersion, int maxVersion, List<String> parameters) {
            this.name = name;
            this.minVersion = minVersion;
            this.maxVersion = maxVersion;
            this.parameters = parameters;
        }
    }
}
