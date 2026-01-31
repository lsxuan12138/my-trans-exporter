package com.example.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureSet;
import net.minecraft.structure.StructureSet.WeightedEntry;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.world.gen.structure.Structure;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

public class StructureTranslationUtil {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String exportStructureTranslationsToJson(ServerWorld world) throws IOException {
        List<StructureData> structures = collectStructureData(world);

        JsonArray jsonArray = new JsonArray();
        for (StructureData data : structures) {
            JsonObject jsonObj = new JsonObject();
            jsonObj.addProperty("id", data.id.toString());
            jsonObj.addProperty("translationKey", data.translationKey);
            jsonObj.addProperty("translatedName", data.translatedName);
            jsonObj.addProperty("namespace", data.id.getNamespace());
            jsonObj.addProperty("path", data.id.getPath());
            jsonObj.addProperty("structureSet", data.structureSet != null ? data.structureSet.toString() : "none");
            jsonArray.add(jsonObj);
        }

        JsonObject root = new JsonObject();
        root.addProperty("exportTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        root.addProperty("world", world.getRegistryKey().getValue().toString());
        root.addProperty("totalStructures", structures.size());
        root.add("structures", jsonArray);

        String json = GSON.toJson(root);
        return writeToFile(json, "json");
    }

    public static String exportStructureTranslationsToCsv(ServerWorld world) throws IOException {
        List<StructureData> structures = collectStructureData(world);

        StringBuilder csv = new StringBuilder();
        csv.append("ID,Translation Key,Translated Name,Namespace,Path,Structure Set\n");

        for (StructureData data : structures) {
            csv.append(escapeCsv(data.id.toString())).append(",");
            csv.append(escapeCsv(data.translationKey)).append(",");
            csv.append(escapeCsv(data.translatedName)).append(",");
            csv.append(escapeCsv(data.id.getNamespace())).append(",");
            csv.append(escapeCsv(data.id.getPath())).append(",");
            csv.append(escapeCsv(data.structureSet != null ? data.structureSet.toString() : "none")).append("\n");
        }

        return writeToFile(csv.toString(), "csv");
    }

    //    public static String exportStructureTranslationsToLang(ServerWorld world) throws IOException {
//        List<StructureData> structures = collectStructureData(world);
//        StringBuilder lang = new StringBuilder();
//        lang.append("# Structure translations exported at: ")
//                .append(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()))
//                .append("\n");
//        lang.append("# Total structures: ").append(structures.size()).append("\n\n");
//
//        for (StructureData data : structures) {
//            lang.append("# ").append(data.id.toString()).append("\n");
//            lang.append(data.translationKey).append("=").append(data.translatedName).append("\n\n");
//        }
//
//        return writeToFile(lang.toString(), "lang");
//    }
    public static String exportStructureTranslationsToLang(ServerWorld world) throws IOException {
        List<StructureData> structures = collectStructureData(world);
        JsonObject root = new JsonObject();
        for (StructureData data : structures) {
            root.addProperty(data.translationKey, data.translatedName);

        }
        String json = GSON.toJson(root);
        return writeToFile(json, "json");

    }

    private static List<StructureData> collectStructureData(ServerWorld world) {
        List<StructureData> structures = new ArrayList<>();
        Registry<Structure> structureRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE);
        Registry<StructureSet> structureSetRegistry = world.getRegistryManager().get(RegistryKeys.STRUCTURE_SET);

        for (Structure structure : structureRegistry) {
            Identifier id = structureRegistry.getId(structure);
            if (id != null) {
                String translationKey = Util.createTranslationKey("structure", id);
                String translatedName = net.minecraft.client.resource.language.I18n.translate(translationKey);

                // 如果没有翻译，使用格式化名称
                if (translatedName.equals(translationKey)) {
                    translatedName = formatStructureName(id);
                }

                Identifier structureSetId = getStructureSetForStructure(structure, structureSetRegistry);
                structures.add(new StructureData(id, translationKey, translatedName, structureSetId));
            }
        }

        // 按ID排序
        structures.sort(Comparator.comparing(data -> data.id.toString()));
        return structures;
    }

    private static Identifier getStructureSetForStructure(Structure structure, Registry<StructureSet> structureSetRegistry) {
        for (StructureSet set : structureSetRegistry) {
            for (WeightedEntry entry : set.structures()) {
                if (entry.structure().value().equals(structure)) {
                    return structureSetRegistry.getId(set);
                }
            }
        }
        return null;
    }

    private static String formatStructureName(Identifier id) {
        String name = id.getPath();
        name = name.replace('_', ' ');
        return capitalizeWords(name);
    }

    private static String capitalizeWords(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
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
        String fileName = String.format("structure_translations_%s.%s",
                new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss").format(new Date()),
                extension);

        Path gameDir = Paths.get(".");
        Path exportDir = gameDir.resolve("exported_structures");

        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
        }

        Path filePath = exportDir.resolve(fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath.toFile()))) {
            writer.write(content);
        }

        return filePath.toAbsolutePath().toString();
    }

    private record StructureData(Identifier id, String translationKey, String translatedName, Identifier structureSet) {
    }
}