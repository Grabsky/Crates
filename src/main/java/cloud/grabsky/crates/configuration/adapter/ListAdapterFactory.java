package cloud.grabsky.crates.configuration.adapter;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum ListAdapterFactory implements JsonAdapter.Factory {
    INSTANCE; // SINGLETON

    @Override
    public @Nullable JsonAdapter<?> create(final @NotNull Type type, final @NotNull Set<? extends Annotation> annotations, final @NotNull Moshi moshi) {
        // Skipping non-list types.
        if (List.class.isAssignableFrom(Types.getRawType(type)) == false)
            return null;
        // Getting delegate for this List<?> type.
        final var delegate = moshi.nextAdapter(this, type, annotations);
        // Getting adapter for the generic type of the List<?> type.
        final var adapter = moshi.adapter(Types.collectionElementType(type, List.class), annotations);
        // Returning JsonAdapter which handles both, single and multi-value elements.
        return new JsonAdapter<>() {

            @Override
            public @Nullable Object fromJson(final @NotNull JsonReader in) throws IOException {
                // Peeking next token, using delegate if it's an array.
                if (in.peek() == JsonReader.Token.BEGIN_ARRAY)
                    return delegate.fromJson(in);
                // Parsing single element using adapter we got earlier.
                final var obj = adapter.fromJson(in);
                // Returning new ArrayList containing single element.
                return new ArrayList<>() {{
                    add(obj);
                }};
            }

            @Override
            public void toJson(final @NotNull JsonWriter out, final @Nullable Object value) throws IOException {
                delegate.toJson(out, value);
            }

        };
    }
}