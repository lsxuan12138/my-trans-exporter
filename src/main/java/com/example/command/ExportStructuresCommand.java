package com.example.command;

import com.example.util.StructureTranslationUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

public class ExportStructuresCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("exportStructureTrans")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ExportStructuresCommand::executeExportJson)
                        .then(CommandManager.literal("json")
                                .executes(ExportStructuresCommand::executeExportJson))
                        .then(CommandManager.literal("csv")
                                .executes(ExportStructuresCommand::executeExportCsv))
                        .then(CommandManager.literal("lang")
                                .executes(ExportStructuresCommand::executeExportLang))
        );
    }

    private static int executeExportJson(CommandContext<ServerCommandSource> context) {
        return executeExport(context, "json");
    }

    private static int executeExportCsv(CommandContext<ServerCommandSource> context) {
        return executeExport(context, "csv");
    }

    private static int executeExportLang(CommandContext<ServerCommandSource> context) {
        return executeExport(context, "lang");
    }

    private static int executeExport(CommandContext<ServerCommandSource> context, String format) {
        try {
            var source = context.getSource();
            var world = source.getWorld();
            String filePath = switch (format) {
                case "csv" -> StructureTranslationUtil.exportStructureTranslationsToCsv(world);
                case "lang" -> StructureTranslationUtil.exportStructureTranslationsToLang(world);
                default -> StructureTranslationUtil.exportStructureTranslationsToJson(world);
            };
            context.getSource().sendFeedback(() ->
                    Text.literal("结构翻译已导出到: " + filePath), false);
            return 1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("导出失败: " + e.getMessage()));
            return 0;
        }
    }


}