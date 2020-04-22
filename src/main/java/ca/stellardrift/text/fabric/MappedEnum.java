/*
 * Copyright © 2020 zml [at] stellardrift [.] ca
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the “Software”), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package ca.stellardrift.text.fabric;

import net.kyori.adventure.util.NameMap;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Represents a mapping between an enum controlled by Minecraft, and an enum controlled by
 * Adventure.
 * <p>
 * Because these enums both refer to known constants, it is relatively easy to generate an automated
 * mapping between the two of them. As the two enums represent the same set of values, it is
 * required that any element that appears in one is present in the other, so mappings will never
 * return null.
 *
 * @param <Mc>  The Minecraft type
 * @param <Adv> The Adventure type
 */
public class MappedEnum<Mc extends Enum<Mc>, Adv extends Enum<Adv>> {
    private final EnumMap<Mc, Adv> mcToAdventure;
    private final EnumMap<Adv, Mc> adventureToMc;

    static <Mc extends Enum<Mc>, Adv extends Enum<Adv>> MappedEnum<Mc, Adv> named(Class<Mc> mcType, Function<String, @Nullable Mc> mcByName, Function<Mc, @Nullable String> mcToName, Class<Adv> advType, final NameMap<Adv> names) {
        return new MappedEnum<>(mcType, mcByName, mcToName, advType, names::name, name -> names.value(name).orElse(null));
    }

    MappedEnum(Class<Mc> mcType, Function<String, @Nullable Mc> mcByName, Function<Mc, @Nullable String> mcToName, Class<Adv> advType, Function<Adv, @Nullable String> advToName, Function<String, @Nullable Adv> advByName) {
        mcToAdventure = new EnumMap<>(mcType);
        adventureToMc = new EnumMap<>(advType);

        for (Adv advElement : advType.getEnumConstants()) {
            @Nullable String mcName = advToName.apply(advElement);
            if (mcName == null) {
                throw new ExceptionInInitializerError("Unable to get name for enum element " + advElement + " of " + advType);
            }
            @Nullable Mc mcElement = mcByName.apply(mcName);
            if (mcElement == null) {
                throw new ExceptionInInitializerError("Unknown MC " + mcType + "  for Adventure " + mcName);
            }
            mcToAdventure.put(mcElement, advElement);
            adventureToMc.put(advElement, mcElement);
        }

        checkCoverage(adventureToMc, advType);
    }

    /**
     * Validates that all members of an enum are present in the given map Throws {@link
     * IllegalStateException} if there is a missing value
     *
     * @param toCheck   The map to check
     * @param enumClass The enum class to verify coverage
     * @param <T>       The type of enum
     */
    private static <T extends Enum<T>> void checkCoverage(Map<T, ?> toCheck, Class<T> enumClass) throws IllegalStateException {
        for (T value : enumClass.getEnumConstants()) {
            if (!toCheck.containsKey(value)) {
                throw new IllegalStateException("Unmapped " + enumClass.getSimpleName() + " element '" + value + '!');
            }
        }
    }

    /**
     * Given a Minecraft enum element, return the equivalent Adventure element.
     *
     * @param mcItem The Minecraft element
     * @return The adventure equivalent.
     */
    public Adv toAdventure(Mc mcItem) {
        return requireNonNull(mcToAdventure.get(mcItem), "Invalid enum value presented: " + mcItem);
    }

    /**
     * Given an Adventure enum element, return the equivalent Minecraft element.
     *
     * @param advItem The Minecraft element
     * @return The adventure equivalent.
     */
    public Mc toMinecraft(Adv advItem) {
        return adventureToMc.get(advItem);
    }
}
