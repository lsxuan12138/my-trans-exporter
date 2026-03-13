package com.example.util;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.biome.Biome;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class BiomeTranslationUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String exportBiomeTranslationsToJson(ServerWorld world) throws IOException {
        List<BiomeData> biomes = collectBiomeData(world);

        JsonArray jsonArray = new JsonArray();
        for (BiomeData data : biomes) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", data.id.toString());
            obj.addProperty("translationKey", data.translationKey);
            obj.addProperty("translatedName", data.translatedName);
            obj.addProperty("namespace", data.id.getNamespace());
            obj.addProperty("path", data.id.getPath());
            jsonArray.add(obj);
        }

        JsonObject root = new JsonObject();
        root.addProperty("exportTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        root.addProperty("world", world.getRegistryKey().getValue().toString());
        root.addProperty("totalBiomes", biomes.size());
        root.add("biomes", jsonArray);

        return writeToFile(GSON.toJson(root), "json");
    }

    public static String exportBiomeTranslationsToCsv(ServerWorld world) throws IOException {
        List<BiomeData> biomes = collectBiomeData(world);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Translation Key,Translated Name,Namespace,Path\n");

        for (BiomeData data : biomes) {
            csv.append(escapeCsv(data.id.toString())).append(",");
            csv.append(escapeCsv(data.translationKey)).append(",");
            csv.append(escapeCsv(data.translatedName)).append(",");
            csv.append(escapeCsv(data.id.getNamespace())).append(",");
            csv.append(escapeCsv(data.id.getPath())).append("\n");
        }

        return writeToFile(csv.toString(), "csv");
    }

    public static String exportBiomeTranslationsToLang(ServerWorld world) throws IOException {
        List<BiomeData> biomes = collectBiomeData(world);
        JsonObject root = new JsonObject();
        for (BiomeData data : biomes) {
            root.addProperty(data.translationKey, data.translatedName);
        }
        return writeToFile(GSON.toJson(root), "json");
    }

    private static List<BiomeData> collectBiomeData(ServerWorld world) {
        List<BiomeData> list = new ArrayList<>();
        Registry<Biome> biomeRegistry = world.getRegistryManager().get(RegistryKeys.BIOME);

        for (RegistryKey<Biome> key : biomeRegistry.getKeys()) {
            Identifier id = key.getValue();
            String translationKey = Util.createTranslationKey("biome", id);
            String translatedName = I18n.translate(translationKey);

            // 如果无翻译，则尝试格式化名称
            if (translatedName.equals(translationKey)) {
                translatedName = formatBiomeName(id);
            }

            list.add(new BiomeData(id, translationKey, translatedName));
        }

        // 按ID排序
        list.sort(Comparator.comparing(data -> data.id.toString()));
        return list;
    }

    private static String formatBiomeName(Identifier id) {
        String name = id.getPath();
        name = name.replace('_', ' ');
        return capitalizeWords(name);
    }

    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) return str;
        String[] words = str.split(" ");
        StringBuilder result = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                result.append(Character.toUpperCase(word.charAt(0)))
                        .append(word.substring(1).toLowerCase())
                        .append(" ");
            }
        }
        return result.toString().trim();
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static String writeToFile(String content, String extension) throws IOException {
        String fileName = String.format("biome_translations_%s.%s",
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()),
                extension);

        Path gameDir = Path.of(".");
        Path exportDir = gameDir.resolve("exported_trans");

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Path filePath = exportDir.resolve(fileName);
        try (BufferedWriter writer = Files.newBufferedWriter(filePath)) {
            writer.write(content);
        }

        return filePath.toAbsolutePath().toString();
    }

    private record BiomeData(Identifier id, String translationKey, String translatedName) {
    }
}