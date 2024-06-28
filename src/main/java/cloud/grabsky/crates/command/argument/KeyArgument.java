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
import cloud.grabsky.crates.crate.Key;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PUBLIC)
public final class KeyArgument implements ArgumentParser<Key>, CompletionsProvider {

    private final @NotNull Crates plugin;

    @Override
    public Key parse(final @NotNull RootCommandContext context, final @NotNull ArgumentQueue arguments) throws ArgumentParseException, MissingInputException {
        final String value = arguments.nextString();
        final @Nullable Key key = plugin.getCratesManager().getKey(value);
        // ...
        if (key == null)
            throw new KeyArgument.Exception(value);
        // ...
        return key;
    }

    @Override
    public @NotNull List<String> provide(final @NotNull RootCommandContext context) throws CommandLogicException {
        return plugin.getCratesManager().getKeyNames().stream().toList();
    }

    /**
     * {@link Exception} is thrown when invalid value is provided for {@link Key} argument type.
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
            Message.of(PluginLocale.Commands.INVALID_KEY).placeholder("input", this.inputValue).send(context.getExecutor().asCommandSender());
        }

    }

}
