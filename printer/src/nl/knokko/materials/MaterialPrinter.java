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
            printEnum(Material.values(), "materials", Enum::name);
            printEnum(Enchantment.values(), "enchantments", Enchantment::getName);
            printEnum(EntityDamageEvent.DamageCause.values(), "damageCauses", EntityDamageEvent.DamageCause::name);
            printEnum(Material.values(), "blockTypes", Material::name, Material::isBlock);
            printEnum(EntityType.values(), "entities", EntityType::name);
            // Apparently, the first element of PotionEffectType.values() is null
            printEnum(PotionEffectType.values(), "potionEffects", PotionEffectType::getName, Objects::nonNull);
            printEnum(Particle.values(), "particles", Particle::name);
            printEnum(Sound.values(), "sounds", Sound::name);
            printEnum(Biome.values(), "biomes", Enum::name);
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

    private <T>void printEnum(T[] toPrint, String prefix, Function<T,String> nameFunction, Predicate<T> filter) throws IOException {
        PrintWriter writer = new PrintWriter(prefix + ".txt");
        for (T value : toPrint) {
            if (filter.test(value)) {
                writer.println(nameFunction.apply(value));
            }
        }
        writer.flush();
        writer.close();
    }
}
