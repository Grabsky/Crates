/*
 * MIT License
 *
 * Copyright (c) 2023 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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