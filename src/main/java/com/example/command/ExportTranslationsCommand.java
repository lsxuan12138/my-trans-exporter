package com.example.command;

import com.example.util.StructureTranslationUtil;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.io.IOException;

public class ExportTranslationsCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                CommandManager.literal("exportstructuretranslations")
                        .requires(source -> source.hasPermissionLevel(2))
                        .executes(ExportTranslationsCommand::executeExport)
                        .then(CommandManager.literal("json")
                                .executes(ExportTranslationsCommand::executeExportJson))
                        .then(CommandManager.literal("csv")
                                .executes(ExportTranslationsCommand::executeExportCsv))
                        .then(CommandManager.literal("lang")
                                .executes(ExportTranslationsCommand::executeExportLang))
        );
    }

    private static int executeExport(CommandContext<ServerCommandSource> context) {
        return executeExportJson(context); // 默认使用JSON格式
    }

    private static int executeExportJson(CommandContext<ServerCommandSource> context) {
        try {
            String filePath = StructureTranslationUtil.exportStructureTranslationsToJson(context.getSource().getWorld());
            context.getSource().sendFeedback(() ->
                    Text.literal("结构翻译已导出到: " + filePath), false);
            return 1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("导出失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeExportCsv(CommandContext<ServerCommandSource> context) {
        try {
            String filePath = StructureTranslationUtil.exportStructureTranslationsToCsv(context.getSource().getWorld());
            context.getSource().sendFeedback(() ->
                    Text.literal("结构翻译已导出到: " + filePath), false);
            return 1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("导出失败: " + e.getMessage()));
            return 0;
        }
    }

    private static int executeExportLang(CommandContext<ServerCommandSource> context) {
        try {
            String filePath = StructureTranslationUtil.exportStructureTranslationsToLang(context.getSource().getWorld());
            context.getSource().sendFeedback(() ->
                    Text.literal("结构翻译已导出到: " + filePath), false);
            return 1;
        } catch (IOException e) {
            context.getSource().sendError(Text.literal("导出失败: " + e.getMessage()));
            return 0;
        }
    }
}