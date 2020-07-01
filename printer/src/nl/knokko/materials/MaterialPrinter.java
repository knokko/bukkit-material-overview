package nl.knokko.materials;

import org.bukkit.Material;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.io.PrintWriter;

public class MaterialPrinter extends JavaPlugin {

    @Override
    public void onEnable() {
        Material[] allMaterials = Material.values();

        try {
            PrintWriter printer = new PrintWriter("materials.txt");
            for (Material material : allMaterials) {
                printer.println(material.name());
            }
            printer.flush();
            printer.close();
        } catch (IOException io) {
            // Shouldn't happen anyway
            throw new RuntimeException(io);
        }
    }
}
