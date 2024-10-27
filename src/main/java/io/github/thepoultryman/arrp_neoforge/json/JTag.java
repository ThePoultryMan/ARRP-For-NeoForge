package io.github.thepoultryman.arrp_neoforge.json;

import io.github.thepoultryman.arrp_neoforge.util.BaseCloneable;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class JTag extends BaseCloneable<JTag> {
    private Boolean replace = false;
    private final List<String> values = new ArrayList<>();

    /**
     * Sets "replace: true" in the generated tag
     * @return The current {@link JTag} instance
     */
    public JTag replace() {
        this.replace = true;
        return this;
    }

    /**
     * Adds an entry into the tag.
     * @param entry The entry added to the tag.
     * @return The current {@link JTag} instance
     */
    public JTag add(ResourceLocation entry) {
        this.values.add(entry.toString());
        return this;
    }

    /**
     * Adds an entry into the tag. The added entry is a tag.
     * @param tag The tag added to the tag
     * @return The current {@link JTag} instance
     */
    public JTag addTag(ResourceLocation tag) {
        this.values.add("#" + tag.getNamespace() + ":" + tag.getPath());
        return this;
    }
}
