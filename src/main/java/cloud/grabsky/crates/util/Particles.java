/*
 * MIT License
 *
 * Copyright (c) 2024 Grabsky <44530932+Grabsky@users.noreply.github.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 *   of this software and associated documentation files (the "Software"), to deal
 *   in the Software without restriction, including without limitation the rights
 *   to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *   copies of the Software, and to permit persons to whom the Software is
 *   furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 *   copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * HORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package cloud.grabsky.crates.util;

import com.squareup.moshi.Json;
import org.bukkit.Particle;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

// Moshi should be able to create instance of the object despite the constructor being private.
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public final class Particles {

    @Getter(AccessLevel.PUBLIC)
    private final Particle particle;

    @Getter(AccessLevel.PUBLIC)
    private final int amount;

    @Getter(AccessLevel.PUBLIC)
    private final float speed;

    @Json(name = "offset_x")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetX;

    @Json(name = "offset_y")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetY;

    @Json(name = "offset_z")
    @Getter(AccessLevel.PUBLIC)
    private final double offsetZ;

}