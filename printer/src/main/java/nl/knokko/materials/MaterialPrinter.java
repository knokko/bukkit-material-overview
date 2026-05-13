package nl.knokko.materials;

import org.bukkit.*;
import org.bukkit.block.Biome;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.inventory.Recipe;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.registry.RegistryAware;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public class MaterialPrinter extends JavaPlugin {

    @Override
    public void onEnable() {
        try {
            printEnum(Material.values(), "materials", Enum::name, m -> true, m -> Integer.toString(m.getMaxStackSize()));
            printRegistry(Registry.ENCHANTMENT, "enchantments", null, null);
            printEnum(EntityDamageEvent.DamageCause.values(), "damageCauses", EntityDamageEvent.DamageCause::name);
            printEnum(Material.values(), "blockTypes", Material::name, Material::isBlock);
            printEnum(EntityType.values(), "entities", EntityType::name);
            printRegistry(Registry.EFFECT, "potionEffects", null, null);
            printEnum(Particle.values(), "particles", Particle::name);
            printRegistry(Registry.SOUNDS, "sounds", null, null);
            printRegistry(Registry.BIOME, "biomes", null, null);
            printEnum(SoundCategory.values(), "soundCategories", SoundCategory::name);
            printEnum(TreeType.values(), "treeTypes", TreeType::name);
            printEnum(ItemFlag.values(), "itemFlags", ItemFlag::name);
            printEnum(Material.values(), "foodTypes", Material::name, Material::isEdible);
            printEnum(Material.values(), "fuel", Material::name, Material::isFuel);
            printEnum(Material.values(), "smeltables", Material::name, candidate -> {
                Iterator<Recipe> recipes = Bukkit.recipeIterator();
                while (recipes.hasNext()) {
                    Recipe recipe = recipes.next();
                    if (recipe instanceof FurnaceRecipe && ((FurnaceRecipe) recipe).getInput().getType() == candidate) {
                        return true;
                    }
                }
                return false;
            });
        } catch (IOException io) {
            // Shouldn't happen anyway
            throw new RuntimeException(io);
        }
    }

    private <T>void printEnum(T[] toPrint, String prefix, Function<T,String> nameFunction) throws IOException {
        printEnum(toPrint, prefix, nameFunction, o -> true);
    }

    @SafeVarargs
    private <T extends RegistryAware & Keyed> void printRegistry(
            Registry<T> registry, String prefix, Function<T, String> nameFunction,
            Predicate<T> filter, Function<T, String>... parameters
    ) throws IOException {
        PrintWriter writer = new PrintWriter(prefix + ".txt");
        for (T value : registry) {
            if (value.isRegistered() && (filter == null || filter.test(value))) {
                if (nameFunction == null) writer.print(value.getKeyOrThrow().getKey());
                else writer.print(nameFunction.apply(value));
                writer.print('(');
                for (int index = 0; index < parameters.length; index++) {
                    writer.print(parameters[index].apply(value));
                    if (index < parameters.length - 1) writer.print(", ");
                }
                writer.println(')');
            }
        }
        writer.flush();
        writer.close();
    }

    @SafeVarargs
    private <T> void printEnum(
            T[] toPrint, String prefix, Function<T, String> nameFunction,
            Predicate<T> filter, Function<T, String>... parameters) throws IOException {
        PrintWriter writer = new PrintWriter(prefix + ".txt");
        for (T value : toPrint) {
            if (filter.test(value)) {
                writer.print(nameFunction.apply(value));
                writer.print('(');
                for (int index = 0; index < parameters.length; index++) {
                    writer.print(parameters[index].apply(value));
                    if (index < parameters.length - 1) writer.print(", ");
                }
                writer.println(')');
            }
        }
        writer.flush();
        writer.close();
    }
}
