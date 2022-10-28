package dev.ftb.ftbsbc.tools.content.spawner;

import dev.ftb.ftbsbc.tools.ToolsRegistry;
import dev.ftb.ftbsbc.tools.integration.kubejs.data.SpawnerDataKjs;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Big old copy and paste from vanilla
 */
public class BitsSpawnerBlockEntity extends BlockEntity {
    private short spawnDelay = 20;
    private short minDelay = 50;
    private short maxDelay = 300;
    private short spawnRange = 5;

    public double spin;
    public double oSpin;
    private int requiredPlayerRange = 16;
    private final Random random = new Random();

    private Entity displayEntity;

    public BitsSpawnerBlockEntity(BlockPos arg2, BlockState arg3) {
        super(ToolsRegistry.BITS_BLOCK_ENTITY.get(), arg2, arg3);
    }

    private boolean isNearPlayer(Level arg, BlockPos arg2) {
        return arg.hasNearbyAlivePlayer((double)arg2.getX() + 0.5, (double)arg2.getY() + 0.5, (double)arg2.getZ() + 0.5, this.requiredPlayerRange);
    }

    public void clientTick(Level arg, BlockPos arg2) {
        if (!this.isNearPlayer(arg, arg2)) {
            this.oSpin = this.spin;
        } else {
            double d0 = (double)arg2.getX() + arg.random.nextDouble();
            double d1 = (double)arg2.getY() + arg.random.nextDouble();
            double d2 = (double)arg2.getZ() + arg.random.nextDouble();
            arg.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
            arg.addParticle(ParticleTypes.FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
            arg.addParticle(ParticleTypes.ENCHANT, d0, d1, d2, 0.0, 0.0, 0.0);
            if (this.spawnDelay > 0) {
                --this.spawnDelay;
            }
            this.oSpin = this.spin;
            this.spin = (this.spin + (double)(1000.0f / ((float)this.spawnDelay + 200.0f))) % 360.0;
        }
    }

    private void serverTick(ServerLevel level, BlockPos pos) {
        if (!this.isNearPlayer(level, pos)) {
            return;
        }

        if (this.spawnDelay > 0) {
            this.spawnDelay --;
            return;
        }

        for (int i = 0; i < Math.max(SpawnerDataKjs.minSpawnAmount, random.nextInt(SpawnerDataKjs.maxSpawnAmount)); i ++) {
            int x = pos.getX() + (random.nextInt(this.spawnRange) * (random.nextFloat() > 0.5 ? -1 : 1)),
                    y = pos.getY() + 1,
                    z = pos.getZ() + (random.nextInt(this.spawnRange) * (random.nextFloat() > 0.5 ? -1 : 1));

            CompoundTag compound = new CompoundTag();
            List<SpawnerDataKjs.SpawnableEntity> validMobsForBiome = getValidMobsForBiome();
            SpawnerDataKjs.SpawnableEntity entityId = validMobsForBiome.get(random.nextInt(validMobsForBiome.size()));

            compound.putString("id", entityId.entityId().toString());
            Entity entity = EntityType.loadEntityRecursive(compound, level, e -> {
                e.moveTo(x, y, z);
                return e;
            });

            if (entity == null) {
                this.spawnDelay = delay();
                return;
            }

            entity.moveTo(entity.getX(), entity.getY(), entity.getZ(), random.nextFloat() * 360.0f, 0.0f);
            if (entity instanceof Mob) {
                ((Mob) entity).finalizeSpawn(level, level.getCurrentDifficultyAt(entity.blockPosition()), MobSpawnType.SPAWNER, null, null);
            }

            if (!level.tryAddFreshEntityWithPassengers(entity)) {
                this.spawnDelay = delay();
                return;
            }

            level.levelEvent(2004, pos, 0);
            if (entity instanceof Mob) {
                ((Mob) entity).spawnAnim();
            }
        }

        this.spawnDelay = this.delay();
    }

    private List<SpawnerDataKjs.SpawnableEntity> getValidMobsForBiome() {
        if (level == null) {
            return new ArrayList<>();
        }

        Holder<Biome> biome = level.getBiome(this.getBlockPos());
        return SpawnerDataKjs.entitiesToSpawn.stream().filter(e -> e.allowedBiome() == null || biome.is(e.allowedBiome())).toList();
    }

    private short delay() {
        return (short) (this.maxDelay <= this.minDelay ? this.minDelay : this.minDelay + this.random.nextInt(this.maxDelay - this.minDelay));
    }

    public static void clientTick(Level arg, BlockPos arg2, BlockState arg3, BitsSpawnerBlockEntity blockEntity) {
        blockEntity.clientTick(arg, arg2);
    }

    public static void serverTick(Level arg, BlockPos arg2, BlockState arg3, BitsSpawnerBlockEntity blockEntity) {
        blockEntity.serverTick((ServerLevel)arg, arg2);
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void load(CompoundTag arg) {
        super.load(arg);

        this.spawnDelay = arg.getShort("spawnDelay");
        this.minDelay = arg.getShort("minDelay");
        this.maxDelay = arg.getShort("maxDelay");
        this.spawnRange = arg.getShort("spawnRange");
    }

    @Override
    protected void saveAdditional(CompoundTag arg) {
        super.saveAdditional(arg);

        arg.putShort("spawnDelay", this.spawnDelay);
        arg.putShort("minDelay", this.minDelay);
        arg.putShort("maxDelay", this.maxDelay);
        arg.putShort("spawnRange", this.spawnRange);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Nullable
    public Entity getOrCreateDisplayEntity() {
        if (this.displayEntity == null) {
            this.displayEntity = EntityType.WITHER.create(this.level);
        }
        return this.displayEntity;
    }
}
