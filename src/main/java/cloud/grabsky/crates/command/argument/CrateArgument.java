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
package cloud.grabsky.crates.command.argument;

import cloud.grabsky.bedrock.components.Message;
import cloud.grabsky.commands.ArgumentQueue;
import cloud.grabsky.commands.RootCommandContext;
import cloud.grabsky.commands.component.ArgumentParser;
import cloud.grabsky.commands.component.CompletionsProvider;
import cloud.grabsky.commands.exception.ArgumentParseException;
import cloud.grabsky.commands.exception.CommandLogicException;
import cloud.grabsky.commands.exception.MissingInputException;
import cloud.grabsky.crates.Crates;
import cloud.grabsky.crates.configuration.PluginLocale;
import cloud.grabsky.crates.crate.Crate;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class CrateArgument implements ArgumentParser<Crate>, CompletionsProvider {

    private final @NotNull Crates plugin;

    @Override
    public Crate parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        final @Nullable Crate crate = plugin.getCratesManager().getCrate(value);
        // ...
        if (crate == null)
            throw new CrateArgument.Exception(value);
        // ...
        return crate;
    }

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) throws CommandLogicException {
        return plugin.getCratesManager().getCrateNames().stream().toList();
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for {@link Crate} argument type.
     */
    public static final class Exception extends ArgumentParseException {

        public Exception(final String inputValue) {
            super(inputValue);
        }

        public Exception(final String inputValue, final Throwable cause) {
            super(inputValue, cause);
        }

        @Override
        public void accept(final RootCommandContext context) {
            Message.of(PluginLocale.Commands.INVALID_CRATE).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
