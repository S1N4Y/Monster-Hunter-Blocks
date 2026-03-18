package com.yanis.monsterhunterblocks.entity.custom;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.AnimationState;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class RoyalLudrothEntity extends HostileEntity implements GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public int deathAnimationTimer = 0;

    public RoyalLudrothEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        // Annule les pénalités de pathfinding liées à l'eau
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 0.0F);

        // Paramètres : Entité, Rotation X max, Rotation Y max, Vitesse Eau (1.0F),
        // Vitesse Terre (1.0F), Flottabilité
        this.moveControl = new net.minecraft.entity.ai.control.AquaticMoveControl(this, 85, 10, 1.0F, 1.0F, true);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        // Navigation hybride (terre + 3D Eau)
        return new AmphibiousSwimNavigation(this, world);
    }

    public static DefaultAttributeContainer.Builder createRoyalLudrothAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 50.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 6.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D); // Champ de vision augmenté
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.add(2, new SwimAroundGoal(this, 1.0D, 10)); // Patrouille aquatique
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D)); // Patrouille terrestre
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, AnimalEntity.class, true));
        // Autorise formellement l'attaque des créatures marines (Poissons, Calmars,
        // etc.)
        this.targetSelector.add(3,
                new ActiveTargetGoal<>(this, net.minecraft.entity.mob.WaterCreatureEntity.class, true));
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
        super.setTarget(target);
        this.setAttacking(target != null);
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        // Renouvellement de l'oxygène
        if (this.isInsideWaterOrBubbleColumn()) {
            this.setAir(300);
        }
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathAnimationTimer;
        if (this.deathAnimationTimer >= 60) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "movement", 5, this::movementPredicate));
        controllers.add(new AnimationController<>(this, "attack", 5, this::attackPredicate));
    }

    private <T extends GeoEntity> PlayState movementPredicate(AnimationState<T> event) {
        if (this.isDead()) {
            event.getController().setAnimation(RawAnimation.begin().thenPlayAndHold("death"));
            return PlayState.CONTINUE;
        }

        if (this.hurtTime > 0) {
            event.getController().setAnimation(RawAnimation.begin().thenPlay("hurt"));
            return PlayState.CONTINUE;
        }

        boolean isMoving = event.isMoving() || this.getVelocity().horizontalLengthSquared() > 0.002D
                || !this.getNavigation().isIdle();
        boolean hasTarget = this.isAttacking();

        if (this.isTouchingWater()) {
            if (isMoving) {
                event.getController().setAnimation(RawAnimation.begin().thenLoop(hasTarget ? "swimfast" : "swim"));
            } else {
                event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
            }
            return PlayState.CONTINUE;
        }

        if (isMoving) {
            event.getController().setAnimation(RawAnimation.begin().thenLoop(hasTarget ? "run" : "walk"));
            return PlayState.CONTINUE;
        }

        event.getController().setAnimation(RawAnimation.begin().thenLoop("idle"));
        return PlayState.CONTINUE;
    }

    private <T extends GeoEntity> PlayState attackPredicate(AnimationState<T> event) {
        if (this.handSwinging && event.getController().getAnimationState().equals(AnimationController.State.STOPPED)) {
            event.getController().forceAnimationReset();
            event.getController().setAnimation(RawAnimation.begin().thenPlay("attack"));
        }
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}