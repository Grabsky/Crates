package net.skydistrict.crates.utils;

import net.skydistrict.crates.Crates;
import net.skydistrict.crates.builders.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;

public class Parser {

    /** Parses ItemStack from given path */
    public static ItemStack rewardItem(FileConfiguration fc, String path) {
        final Material material = Material.getMaterial(fc.getString(path + ".material"));
        final ItemBuilder builder = new ItemBuilder(material, fc.getInt(path + ".amount", 1));
        // Applying ItemMeta if set
        if (fc.isConfigurationSection(path + ".meta")) {
            // Applying name
            if (fc.isString(path + ".meta.name")) {
                builder.setName(fc.getString(path + ".meta.name"));
            }
            // Applying lore
            if (fc.isList(path + ".meta.lore")) {
                builder.setLore(fc.getStringList(path + ".meta.lore").toArray(new String[0]));
            }
            // Applying Enchantment(s)
            if (fc.isConfigurationSection(path + ".meta.enchantments")) {
                for (String enchantment : fc.getConfigurationSection(path + ".meta.enchantments").getKeys(false)) {
                    builder.addEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(enchantment)), fc.getInt(path + ".meta.enchantments." + enchantment));
                }
            }
            // Applying ItemFlag(s)
            if (fc.isList(path + ".meta.itemflags")) {
                final List<String> stringItemFlags = fc.getStringList(path + ".meta.itemflags");
                final ItemFlag[] itemFlags = new ItemFlag[stringItemFlags.size()];
                for (int i = 0; i < stringItemFlags.size(); i++) {
                    itemFlags[i] = ItemFlag.valueOf(stringItemFlags.get(i));
                }
                builder.setItemFlags(itemFlags);
            }
            // Applying CustomModelData
            if (fc.isInt(path + ".meta.custom-model-data")) {
                builder.setCustomModelData(fc.getInt(path + ".meta.custom-model-data"));
            }
            // Applying skull texture (value)
            if (material == Material.PLAYER_HEAD && fc.isString(path + ".meta.skull-value")) {
                builder.setSkullValue(fc.getString(path + ".meta.skull-value"));
            }
        }
        return builder.build();
    }

    /** Parses Crate Key from given path */
    public static ItemStack keyItem(FileConfiguration fc, String path, String crateId) {
        final Material material = Material.getMaterial(fc.getString(path + ".material"));
        final ItemBuilder builder = new ItemBuilder(material, fc.getInt(path + ".amount", 1));
        // Applying name
        if (fc.isString(path + ".meta.name")) {
            builder.setName(fc.getString(path + ".meta.name"));
        }
        // Applying lore
        if (fc.isList(path + ".meta.lore")) {
            builder.setLore(fc.getStringList(path + ".meta.lore").toArray(new String[0]));
        }
        // Applying
        if (fc.isInt(path + ".meta.custom-model-data")) {
            builder.setCustomModelData(fc.getInt(path + ".meta.custom-model-data"));
        }
        builder.addEnchantment(Enchantment.ARROW_INFINITE, 1);
        builder.setItemFlags(ItemFlag.HIDE_ENCHANTS);
        builder.getPersistentDataContainer().set(Crates.CRATE_ID, PersistentDataType.STRING, crateId);
        return builder.build();
    }
}
