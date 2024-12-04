/*
 * Crates (https://github.com/Grabsky/Crates)
 *
 * Copyright (C) 2024  Grabsky <michal.czopek.foss@proton.me>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License v3 as published by
 * the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License v3 for more details.
 */
package cloud.grabsky.crates.command.template;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.RootCommandManager;
import cloud.grabsky.commands.argument.IntegerArgument;
import cloud.grabsky.commands.argument.PlayerArgument;
import cloud.grabsky.commands.exception.IncompatibleSenderException;
import cloud.grabsky.crates.configuration.PluginLocale;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

public enum CommandExceptionTemplate implements Consumer<RootCommandManager> {
    /* SINGLETON */ INSTANCE;

    @Override
    public void accept(final @NotNull RootCommandManager manager) {

        manager.setExceptionHandler(IncompatibleSenderException.class, (e, context) -> {
            Message.of((e.getExpectedType() == Player.class)
                    ? PluginLocale.Commands.INVALID_EXECUTOR_PLAYER
                    : (e.getExpectedType() == ConsoleCommandSender.class)
                            ? PluginLocale.Commands.INVALID_EXECUTOR_CONSOLE
                            : null
            ).send(context.getExecutor().asCommandSender());
        });

        // IntegerArgument

        manager.setExceptionHandler(IntegerArgument.ParseException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_INTEGER)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

        manager.setExceptionHandler(IntegerArgument.RangeException.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_INTEGER_NOT_IN_RANGE)
                    .placeholder("input", e.getInputValue())
                    .placeholder("min", e.getMin())
                    .placeholder("max", e.getMax())
                    .send(context.getExecutor().asCommandSender());
        });

        // PlayerArgument

        manager.setExceptionHandler(PlayerArgument.Exception.class, (e, context) -> {
            Message.of(PluginLocale.Commands.INVALID_PLAYER)
                    .placeholder("input", e.getInputValue())
                    .send(context.getExecutor().asCommandSender());
        });

    }

}