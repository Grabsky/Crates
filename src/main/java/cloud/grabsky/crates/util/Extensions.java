package cloud.grabsky.crates.util;

import org.jetbrains.annotations.NotNull;

public final class Extensions {

    public static boolean equalsAny(final @NotNull String self, final @NotNull String... any) {
        for (final String s : any)
            if (self.equals(s) == true)
                return true;
        return false;
    }

    public static boolean equalsAnyIgnoreCase(final @NotNull String self, final @NotNull String... any) {
        for (final String s : any)
            if (self.equalsIgnoreCase(s) == true)
                return true;
        return false;
    }

}
