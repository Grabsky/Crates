package cloud.grabsky.crates.util;

import org.jetbrains.annotations.Nullable;

public final class Numbers {

    public static @Nullable Integer toInt(final String string) {
        try {
            return Integer.parseInt(string);
        } catch (final NumberFormatException _) {
            return null;
        }
    }

}
