package cloud.grabsky.crates.configuration;

import cloud.grabsky.configuration.JsonConfiguration;
import cloud.grabsky.configuration.JsonPath;
import net.kyori.adventure.text.Component;

public final class PluginLocale implements JsonConfiguration {

    // Common

    @JsonPath("reload_success")
    public static Component RELOAD_SUCCESS;

    @JsonPath("reload_failure")
    public static Component RELOAD_FAILURE;

    // Crates

    @JsonPath("crate_placed")
    public static String CRATE_PLACED;

    @JsonPath("crate_opened")
    public static String CRATE_OPENED;

    @JsonPath("crate_missing_key")
    public static Component CRATE_MISSING_KEY;

    @JsonPath("crate_occupied")
    public static Component CRATE_OCCUPIED;

    // Crates > Help

    @JsonPath("commands.crates_help")
    public static Component COMMANDS_CRATES_HELP;

    // Crates > Get

    @JsonPath("commands.crates_get_usage")
    public static Component COMMANDS_CRATES_GET_USAGE;

    @JsonPath("commands.crates_get_success")
    public static String COMMANDS_CRATES_GET_SUCCESS;

    // Crates > Give

    @JsonPath("commands.crates_give_usage")
    public static Component COMMANDS_CRATES_GIVE_USAGE;

    @JsonPath("commands.crates_give_success_sender_single")
    public static String COMMANDS_CRATES_GIVE_SUCCESS_SENDER_SINGLE;

    @JsonPath("commands.crates_give_success_sender_all")
    public static String COMMANDS_CRATES_GIVE_SUCCESS_SENDER_ALL;

    @JsonPath("commands.crates_give_success_target")
    public static String COMMANDS_CRATES_GIVE_SUCCESS_TARGET;


    public static final class Commands implements JsonConfiguration {

        // Commands > General

        @JsonPath("missing_permissions")
        public static Component MISSING_PERMISSIONS;

        // Commands > Executors

        @JsonPath("invalid_executor_player")
        public static Component INVALID_EXECUTOR_PLAYER;

        @JsonPath("invalid_executor_console")
        public static Component INVALID_EXECUTOR_CONSOLE;

        // Commands > Arguments > Built-in

        @JsonPath("invalid_integer")
        public static String INVALID_INTEGER;

        @JsonPath("invalid_integer_not_in_range")
        public static String INVALID_INTEGER_NOT_IN_RANGE;

        @JsonPath("invalid_player")
        public static String INVALID_PLAYER;

        // Commands > Arguments > Custom

        @JsonPath("invalid_crate")
        public static String INVALID_CRATE;

        @JsonPath("invalid_key")
        public static String INVALID_KEY;

    }

}
