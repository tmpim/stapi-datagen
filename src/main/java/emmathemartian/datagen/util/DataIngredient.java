package emmathemartian.datagen.util;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import emmathemartian.datagen.DataGenException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.modificationstation.stationapi.api.registry.ItemRegistry;
import net.modificationstation.stationapi.api.tag.TagKey;
import net.modificationstation.stationapi.api.util.Identifier;

public record DataIngredient(Either<Item, TagKey<Item>> item, int count, int damage) {
    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        item.ifLeft((left) -> {
            Identifier maybeItem = ItemRegistry.INSTANCE.getId(left);
            if (maybeItem == null)
                throw new DataGenException("Item did not exist in registry: " + left);
            object.addProperty("item", maybeItem.toString());
            if (damage != 0)
                object.addProperty("damage", damage);
        }).ifRight((right) -> object.addProperty("tag", right.id().toString()));

        if (object.size() == 0) {
            throw new DataGenException("Ingredient.toJson() called but Ingredient.item was not present.");
        }

        if (count != 1)
            object.addProperty("count", count);

        return object;
    }

    public static DataIngredient of(Item item, int count, int damage) {
        return new DataIngredient(Either.left(item), count, damage);
    }

    public static DataIngredient of(Item item, int count) {
        return of(item, count, 0);
    }

    public static DataIngredient of(Item item) {
        return of(item, 1, 0);
    }

    public static DataIngredient of(ItemStack stack) {
        return of(stack.getItem(), stack.count, stack.getDamage());
    }

    public static DataIngredient of(TagKey<Item> tag, int count) {
        return new DataIngredient(Either.right(tag), count, 0);
    }

    public static DataIngredient of(TagKey<Item> tag) {
        return of(tag, 1);
    }
}
