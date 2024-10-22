package io.github.thepoultryman.arrp_neoforge.impl;

import com.google.gson.JsonObject;
import io.github.thepoultryman.arrp_neoforge.ARRPForNeoForge;
import io.github.thepoultryman.arrp_neoforge.api.RuntimeResourcePack;
import io.github.thepoultryman.arrp_neoforge.json.JLang;
import io.github.thepoultryman.arrp_neoforge.json.JTag;
import io.github.thepoultryman.arrp_neoforge.json.animation.JAnimation;
import io.github.thepoultryman.arrp_neoforge.json.loot.JLootTable;
import io.github.thepoultryman.arrp_neoforge.json.model.JModel;
import io.github.thepoultryman.arrp_neoforge.json.recipe.JRecipe;
import io.github.thepoultryman.arrp_neoforge.json.state.JState;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.AbstractPackResources;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.IoSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.BiFunction;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class RuntimeResourcePackImpl implements RuntimeResourcePack {
    private static final int RESOURCE_PACK_VERSION = 34;

    private final Set<String> KEY_WARNINGS = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final ResourceLocation id;
    private final PackLocationInfo info;
    private final Lock lock = new ReentrantLock();
    private final Map<ResourceLocation, Supplier<byte[]>> assets = new ConcurrentHashMap<>();
    private final Map<ResourceLocation, Supplier<byte[]>> data = new ConcurrentHashMap<>();
    private final Map<List<String>, Supplier<byte[]>> root = new ConcurrentHashMap<>();

    public RuntimeResourcePackImpl(ResourceLocation resourceLocation) {
        this.id = resourceLocation;
        this.info = new PackLocationInfo(
                this.id.getNamespace() + ";" + this.id.getPath(),
                Component.literal("Runtime Resource Pack for " + this.id),
                PackSource.DEFAULT,
                Optional.empty());
    }

    @Override
    public @Nullable IoSupplier<InputStream> getRootResource(String @NotNull ... pElements) {
        this.lock();
        Supplier<byte[]> byteArraySupplier = this.root.get(Arrays.asList(pElements));
        if (byteArraySupplier == null) {
            this.lock.unlock();
            return null;
        }
        this.lock.unlock();
        return () -> new ByteArrayInputStream(byteArraySupplier.get());
    }

    @Override
    public @Nullable IoSupplier<InputStream> getResource(@NotNull PackType pPackType, @NotNull ResourceLocation pLocation) {
        this.lock();
        Supplier<byte[]> byteArraySupplier = this.getSys(pPackType).get(pLocation);
        if (byteArraySupplier == null) {
            this.lock.unlock();
            return null;
        }
        this.lock.unlock();
        return () -> new ByteArrayInputStream(byteArraySupplier.get());
    }

    @Override
    public void listResources(@NotNull PackType pPackType, @NotNull String pNamespace, @NotNull String pPath, @NotNull ResourceOutput pResourceOutput) {
        this.lock();
        for (ResourceLocation resourceLocation : this.getSys(pPackType).keySet()) {
            Supplier<byte[]> byteArraySupplier = this.getSys(pPackType).get(resourceLocation);
            if (byteArraySupplier == null) {
                this.lock.unlock();
                continue;
            }
            IoSupplier<InputStream> ioSupplier = () -> new ByteArrayInputStream(byteArraySupplier.get());
            if (resourceLocation.getNamespace().equals(pNamespace) && resourceLocation.getPath().startsWith(pPath)) {
                pResourceOutput.accept(resourceLocation, ioSupplier);
            }
        }
        this.lock.unlock();
    }

    @Override
    public @NotNull Set<String> getNamespaces(@NotNull PackType pType) {
        this.lock();
        Set<String> namespaces = new HashSet<>();
        for (ResourceLocation resourceLocation : this.getSys(pType).keySet()) {
            namespaces.add(resourceLocation.getNamespace());
        }
        this.lock.unlock();
        return namespaces;
    }

    @Override
    public @Nullable <T> T getMetadataSection(@NotNull MetadataSectionSerializer<T> pDeserializer) {
        InputStream inputStream = null;
        try {
            IoSupplier<InputStream> ioSupplier = this.getRootResource("pack.mcmeta");
            if (ioSupplier != null) {
                inputStream = ioSupplier.get();
            }
        } catch (IOException exception) {
            throw new RuntimeException(exception);
        }
        if (inputStream != null) {
            return AbstractPackResources.getMetadataFromStream(pDeserializer, inputStream);
        } else {
            if (pDeserializer.getMetadataSectionName().equals("pack")) {
                JsonObject object = new JsonObject();
                object.addProperty("pack_format", RESOURCE_PACK_VERSION);
                object.addProperty("description", "runtime resource pack");
                return pDeserializer.fromJson(object);
            }
            if (KEY_WARNINGS.add(pDeserializer.getMetadataSectionName())) {
                ARRPForNeoForge.LOGGER.info("\"{}\" is an unsupported metadata key", pDeserializer.getMetadataSectionName());
            }
            return null;
        }
    }

    @Override
    public @NotNull PackLocationInfo location() {
        return this.info;
    }

    @Override
    public void close() {
        ARRPForNeoForge.LOGGER.info("Closing Runtime Resource Pack {}", this.id);
        this.lock.unlock();
    }

    private Map<ResourceLocation, Supplier<byte[]>> getSys(PackType packType) {
        return packType == PackType.CLIENT_RESOURCES ? this.assets : this.data;
    }

    private void lock() {
        if (!this.lock.tryLock()) {
            this.lock.lock();
        }
    }

    @Override
    public void addRecoloredImage(ResourceLocation resourceLocation, InputStream inputStream, IntUnaryOperator pixel) {

    }

    @Override
    public void mergeLang(ResourceLocation resourceLocation, JLang lang) {

    }

    @Override
    public byte[] addLang(ResourceLocation resourceLocation, JLang lang) {
        return new byte[0];
    }

    @Override
    public byte[] addLootTable(ResourceLocation resourceLocation, JLootTable lootTable) {
        return new byte[0];
    }

    @Override
    public void addLazyResource(PackType packType, ResourceLocation path, BiFunction<RuntimeResourcePack, ResourceLocation, byte[]> data) {

    }

    @Override
    public byte[] addResource(PackType packType, ResourceLocation path, byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] addRootResource(String path, byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] addAsset(ResourceLocation path, byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] addData(ResourceLocation path, byte[] data) {
        return new byte[0];
    }

    @Override
    public byte[] addModel(ResourceLocation path, JModel model) {
        return new byte[0];
    }

    @Override
    public byte[] addBlockSate(ResourceLocation path, JState state) {
        return new byte[0];
    }

    @Override
    public byte[] addTexture(ResourceLocation path, BufferedImage image) {
        return new byte[0];
    }

    @Override
    public byte[] addAnimation(ResourceLocation path, JAnimation animation) {
        return new byte[0];
    }

    @Override
    public byte[] addTag(ResourceLocation path, JTag tag) {
        return new byte[0];
    }

    @Override
    public byte[] addRecipe(ResourceLocation path, JRecipe recipe) {
        return new byte[0];
    }

    @Override
    public void load(Path path) throws IOException {

    }
}
