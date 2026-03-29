package com.yanis.monsterhunterblocks.entity.custom;

import com.yanis.monsterhunterblocks.entity.ModEntities;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.ai.goal.*;
import net.minecraft.entity.ai.pathing.AmphibiousSwimNavigation;
import net.minecraft.entity.ai.pathing.EntityNavigation;
import net.minecraft.entity.ai.pathing.PathNodeType;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
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

    // Système de hitboxes multi-parties (5 zones)
    public RoyalLudrothPartEntity headPart;
    public RoyalLudrothPartEntity bodyPart;
    public RoyalLudrothPartEntity tailBasePart;
    public RoyalLudrothPartEntity tailMidPart;
    public RoyalLudrothPartEntity tailTipPart;
    private boolean partsSpawned = false;

    private BlockPos homePos;

    // ============================================
    // CONFIGURATION DE LA BOSS BAR
    // ============================================
    // Vous pouvez changer la couleur ici (ex: BossBar.Color.BLUE, RED, GREEN, etc.)
    // Et le style (ex: BossBar.Style.NOTCHED_10 pour la diviser en 10 crans)
    private final ServerBossBar bossBar = (ServerBossBar) new ServerBossBar(
            net.minecraft.text.Text.literal("Royal Ludroth"), BossBar.Color.YELLOW, BossBar.Style.PROGRESS);
            
    public int attackCooldown = 0;

    public RoyalLudrothEntity(EntityType<? extends HostileEntity> entityType, World world) {
        super(entityType, world);
        this.setPathfindingPenalty(PathNodeType.WATER, 0.0F);
        this.setPathfindingPenalty(PathNodeType.WATER_BORDER, 0.0F);
        this.moveControl = new net.minecraft.entity.ai.control.AquaticMoveControl(this, 85, 10, 1.0F, 1.0F, true);
    }

    @Override
    protected EntityNavigation createNavigation(World world) {
        return new AmphibiousSwimNavigation(this, world);
    }

    public static DefaultAttributeContainer.Builder createRoyalLudrothAttributes() {
        return HostileEntity.createHostileAttributes()
                .add(EntityAttributes.GENERIC_MAX_HEALTH, 100.0D)
                .add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.3D)
                .add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 9.0D)
                .add(EntityAttributes.GENERIC_FOLLOW_RANGE, 32.0D)
                .add(EntityAttributes.GENERIC_SCALE, 3.0D);
    }

    @Override
    protected void initGoals() {
        this.goalSelector.add(1, new MeleeAttackGoal(this, 1.5D, true));
        this.goalSelector.add(2, new SwimAroundGoal(this, 1.0D, 10));
        this.goalSelector.add(3, new WanderAroundFarGoal(this, 1.0D));
        this.goalSelector.add(4, new LookAtEntityGoal(this, PlayerEntity.class, 8.0F));
        this.goalSelector.add(5, new LookAroundGoal(this));

        this.targetSelector.add(1, new ActiveTargetGoal<>(this, PlayerEntity.class, true));
        this.targetSelector.add(2, new ActiveTargetGoal<>(this, AnimalEntity.class, true));
        this.targetSelector.add(3,
                new ActiveTargetGoal<>(this, net.minecraft.entity.mob.WaterCreatureEntity.class, true));
    }

    @Override
    public void setTarget(@org.jetbrains.annotations.Nullable LivingEntity target) {
        super.setTarget(target);
        this.setAttacking(target != null);
    }

    @Override
    public void onStartedTrackingBy(ServerPlayerEntity player) {
        super.onStartedTrackingBy(player);
        this.bossBar.addPlayer(player);
    }

    @Override
    public void onStoppedTrackingBy(ServerPlayerEntity player) {
        super.onStoppedTrackingBy(player);
        this.bossBar.removePlayer(player);
    }

    @Override
    public void writeCustomDataToNbt(NbtCompound nbt) {
        super.writeCustomDataToNbt(nbt);
        if (this.homePos != null) {
            nbt.putInt("HomePosX", this.homePos.getX());
            nbt.putInt("HomePosY", this.homePos.getY());
            nbt.putInt("HomePosZ", this.homePos.getZ());
        }
    }

    @Override
    public void readCustomDataFromNbt(NbtCompound nbt) {
        super.readCustomDataFromNbt(nbt);
        if (nbt.contains("HomePosX")) {
            this.homePos = new BlockPos(nbt.getInt("HomePosX"), nbt.getInt("HomePosY"), nbt.getInt("HomePosZ"));
        }
    }

    @Override
    public boolean cannotDespawn() {
        return true;
    }

    @Override
    public boolean tryAttack(Entity target) {
        // Bloque l'attaque si le cooldown n'est pas terminé
        if (this.attackCooldown > 0) {
            return false;
        }

        boolean success = super.tryAttack(target);
        if (success) {
            // ============================================
            // CONFIGURATION DU COMBAT
            // ============================================
            // Définit le cooldown entre chaque attaque (ex: 60 ticks = 3 secondes)
            this.attackCooldown = 60;
            
            // Applique un "Knockback" lourd
            if (target instanceof LivingEntity livingTarget) {
                // Force du recul horizontal
                double knockbackStrength = 2.5D; 
                double d = this.getX() - livingTarget.getX();
                double e = this.getZ() - livingTarget.getZ();
                livingTarget.takeKnockback(knockbackStrength, d, e);
                // Projette la cible très légèrement en l'air
                livingTarget.addVelocity(0, 0.4D, 0);
            }
        }
        return success;
    }

    @Override
    public boolean isPushedByFluids() {
        return false;
    }

    @Override
    public void travel(Vec3d movementInput) {
        if (this.isLogicalSideForUpdatingMovement() && this.isTouchingWater()) {
            this.updateVelocity(this.getMovementSpeed(), movementInput);
            this.move(MovementType.SELF, this.getVelocity());
            this.setVelocity(this.getVelocity().multiply(0.9D));
        } else {
            super.travel(movementInput);
        }
    }

    /**
     * Spawne les sous-parties en tant qu'entités réelles dans le monde.
     * Appelé une seule fois au premier tick côté serveur.
     */
    private void spawnParts() {
        if (this.getWorld().isClient() || this.partsSpawned)
            return;

        this.headPart = new RoyalLudrothPartEntity(
                ModEntities.ROYAL_LUDROTH_PART, this, "head", 3.0F, 3.5F);
        this.bodyPart = new RoyalLudrothPartEntity(
                ModEntities.ROYAL_LUDROTH_PART, this, "body", 5.0F, 7.0F);
        this.tailBasePart = new RoyalLudrothPartEntity(
                ModEntities.ROYAL_LUDROTH_PART, this, "tail_base", 2.5F, 3.0F);
        this.tailMidPart = new RoyalLudrothPartEntity(
                ModEntities.ROYAL_LUDROTH_PART, this, "tail_mid", 3.5F, 4.0F);
        this.tailTipPart = new RoyalLudrothPartEntity(
                ModEntities.ROYAL_LUDROTH_PART, this, "tail_tip", 3.5F, 2.5F);

        // Spawner les sous-parties dans le monde (elles deviennent des entités à part
        // entière)
        this.getWorld().spawnEntity(this.headPart);
        this.getWorld().spawnEntity(this.bodyPart);
        this.getWorld().spawnEntity(this.tailBasePart);
        this.getWorld().spawnEntity(this.tailMidPart);
        this.getWorld().spawnEntity(this.tailTipPart);

        this.partsSpawned = true;
    }

    /**
     * Positionne les sous-parties en fonction de la rotation du corps.
     */
    private void updatePartPositions() {
        if (!this.partsSpawned)
            return;

        double x = this.getX();
        double y = this.getY();
        double z = this.getZ();

        float bodyYawRad = this.bodyYaw * ((float) Math.PI / 180F);
        float sinYaw = MathHelper.sin(bodyYawRad);
        float cosYaw = MathHelper.cos(bodyYawRad);

        // Tête : décalée vers l'avant (7 blocs devant le centre)
        if (this.headPart != null && !this.headPart.isRemoved()) {
            this.headPart.setPosition(x - sinYaw * 7.0D, y + 2.0D, z + cosYaw * 7.0D);
        }

        // Corps : au centre de l'entité
        if (this.bodyPart != null && !this.bodyPart.isRemoved()) {
            this.bodyPart.setPosition(x, y, z);
        }

        // Queue base : juste derrière le corps (6 blocs)
        if (this.tailBasePart != null && !this.tailBasePart.isRemoved()) {
            this.tailBasePart.setPosition(x + sinYaw * 4.25D, y + 0.5D, z - cosYaw * 4.25D);
        }

        // Queue milieu : plus loin derrière (9 blocs)
        if (this.tailMidPart != null && !this.tailMidPart.isRemoved()) {
            this.tailMidPart.setPosition(x + sinYaw * 7.25D, y + 0.25D, z - cosYaw * 7.25D);
        }

        // Queue bout : à l'extrémité (12 blocs)
        if (this.tailTipPart != null && !this.tailTipPart.isRemoved()) {
            this.tailTipPart.setPosition(x + sinYaw * 11.5D, y + 0.0D, z - cosYaw * 11.5D);
        }
    }

    @Override
    public void tick() {
        super.tick();

        // Oxygène illimité
        if (this.isInsideWaterOrBubbleColumn()) {
            this.setAir(300);
        }

        // Spawn les sous-parties au premier tick serveur
        if (!this.partsSpawned) {
            this.spawnParts();
        }

        if (!this.getWorld().isClient()) {
            this.bossBar.setPercent(this.getHealth() / this.getMaxHealth());

            // ============================================
            // INTELLIGENCE ARTIFICIELLE : RESTRICTION TERRITORIALE
            // ============================================
            // Enregistrement permanent du point d'apparition (centre de l'arène)
            if (this.homePos == null) {
                this.homePos = this.getBlockPos();
            } else {
                // Calcule si le mob est trop loin de son point d'origine.
                // Ici '55.0' est le rayon maximal en blocs. Modifiez cette valeur
                // si vous changez la taille de l'arène plus tard.
                double maxRadius = 55.0;

                if (this.squaredDistanceTo(this.homePos.toCenterPos()) > (maxRadius * maxRadius)) {
                    if (this.getTarget() != null) {
                        this.setTarget(null); // Le boss annule son focus sur le joueur
                    }
                    // Le boss est rappelé de force vers le centre
                    // Le '1.2D' est la vitesse de retour (légèrement plus rapide que la marche
                    // normale)
                    this.getNavigation().startMovingTo(this.homePos.getX(), this.homePos.getY(), this.homePos.getZ(),
                            1.2D);
                }
            }

            // ============================================
            // SYSTEME D'ATTAQUE MULTI-HITBOX
            // ============================================
            // Décrémente le cooldown d'attaque (1 par tick)
            if (this.attackCooldown > 0) {
                this.attackCooldown--;
            }

            // Si le boss a une cible ET que son cooldown est prêt (0)
            if (this.attackCooldown == 0 && this.getTarget() != null) {
                LivingEntity target = this.getTarget();
                // Vérifie que le joueur est mortel et proche pour ne pas scanner toute la map
                if (target.isAlive() && target.squaredDistanceTo(this) < 400D) {
                    
                    // Si le joueur coupe/touche LA MOINDRE des hitboxes du boss :
                    boolean touchesAnyPart = false;
                    
                    if (this.headPart != null && target.getBoundingBox().intersects(this.headPart.getBoundingBox())) touchesAnyPart = true;
                    if (this.bodyPart != null && target.getBoundingBox().intersects(this.bodyPart.getBoundingBox())) touchesAnyPart = true;
                    if (this.tailBasePart != null && target.getBoundingBox().intersects(this.tailBasePart.getBoundingBox())) touchesAnyPart = true;
                    if (this.tailMidPart != null && target.getBoundingBox().intersects(this.tailMidPart.getBoundingBox())) touchesAnyPart = true;
                    if (this.tailTipPart != null && target.getBoundingBox().intersects(this.tailTipPart.getBoundingBox())) touchesAnyPart = true;
                    
                    if (touchesAnyPart) {
                        // Frappe le joueur instantanément quel que soit l'angle !
                        this.tryAttack(target);
                        // Déclenche visuellement son animation de mêlée sans bloquer
                        this.swingHand(net.minecraft.util.Hand.MAIN_HAND);
                    }
                }
            }
        }

        // Mise à jour des positions
        this.updatePartPositions();
    }

    // Suppression propre des sous-parties
    @Override
    public void remove(Entity.RemovalReason reason) {
        if (this.headPart != null)
            this.headPart.discard();
        if (this.bodyPart != null)
            this.bodyPart.discard();
        if (this.tailBasePart != null)
            this.tailBasePart.discard();
        if (this.tailMidPart != null)
            this.tailMidPart.discard();
        if (this.tailTipPart != null)
            this.tailTipPart.discard();
        super.remove(reason);
    }

    @Override
    protected void updatePostDeath() {
        ++this.deathAnimationTimer;
        if (this.deathAnimationTimer >= 60) {
            this.remove(Entity.RemovalReason.KILLED);
        }
    }

    // --- GECKOLIB : ANIMATIONS ---

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