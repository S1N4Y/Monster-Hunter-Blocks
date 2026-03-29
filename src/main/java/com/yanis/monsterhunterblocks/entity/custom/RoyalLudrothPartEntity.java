package com.yanis.monsterhunterblocks.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

public class RoyalLudrothPartEntity extends Entity {
    public RoyalLudrothEntity owner;
    public String partName;

    // DataTracker pour synchroniser les dimensions serveur → client
    private static final TrackedData<Float> PART_WIDTH = DataTracker.registerData(
            RoyalLudrothPartEntity.class, TrackedDataHandlerRegistry.FLOAT);
    private static final TrackedData<Float> PART_HEIGHT = DataTracker.registerData(
            RoyalLudrothPartEntity.class, TrackedDataHandlerRegistry.FLOAT);

    // Constructeur pour le factory (EntityType registration)
    public RoyalLudrothPartEntity(EntityType<?> type, World world) {
        super(type, world);
        this.noClip = true;
        this.partName = "unknown";
    }

    // Constructeur pour l'instanciation manuelle depuis le parent
    public RoyalLudrothPartEntity(EntityType<?> type, RoyalLudrothEntity owner, String name, float width,
            float height) {
        super(type, owner.getWorld());
        this.owner = owner;
        this.partName = name;
        this.noClip = true;
        // Définir les dimensions via DataTracker (synchronisé au client automatiquement)
        this.dataTracker.set(PART_WIDTH, width);
        this.dataTracker.set(PART_HEIGHT, height);
        this.calculateDimensions();
    }

    @Override
    protected void initDataTracker(DataTracker.Builder builder) {
        builder.add(PART_WIDTH, 1.0F);
        builder.add(PART_HEIGHT, 1.0F);
    }

    // Quand le client reçoit les données synchronisées, recalculer la bounding box
    @Override
    public void onTrackedDataSet(TrackedData<?> data) {
        super.onTrackedDataSet(data);
        if (PART_WIDTH.equals(data) || PART_HEIGHT.equals(data)) {
            this.calculateDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(EntityPose pose) {
        return EntityDimensions.fixed(
                this.dataTracker.get(PART_WIDTH),
                this.dataTracker.get(PART_HEIGHT));
    }

    @Override
    protected void readCustomDataFromNbt(NbtCompound nbt) {
    }

    @Override
    protected void writeCustomDataToNbt(NbtCompound nbt) {
    }

    @Override
    public boolean canHit() {
        return true;
    }

    @Override
    public boolean collidesWith(Entity other) {
        // Optionnel : Vous pouvez retourner true pour que les autres entités aient une collision normale avec
        // Mais isCollidable gère la majeure partie de "l'imperméabilité" du joueur
        return false;
    }

    @Override
    public boolean isCollidable() {
        // Rend l'entité dure comme un bloc, un Shulker ou un Bateau (le joueur ne peut pas passer au travers)
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean damage(DamageSource source, float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (this.owner != null) {
            return this.owner.damage(source, amount);
        }
        return false;
    }

    @Override
    public boolean isPartOf(Entity entity) {
        return this == entity || (this.owner != null && this.owner == entity);
    }

    @Override
    public void tick() {
        if (this.owner != null && this.owner.isRemoved()) {
            this.discard();
        }
    }

    @Override
    public boolean shouldSave() {
        return false;
    }
}