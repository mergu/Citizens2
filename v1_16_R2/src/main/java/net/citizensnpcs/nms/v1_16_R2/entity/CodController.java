package net.citizensnpcs.nms.v1_16_R2.entity;

import org.bukkit.Bukkit;
import net.minecraft.server.v1_16_R2.EntityMinecartAbstract;
import org.bukkit.craftbukkit.v1_16_R2.CraftServer;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftCod;
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftEntity;
import org.bukkit.entity.Cod;
import org.bukkit.util.Vector;

import net.citizensnpcs.api.event.NPCEnderTeleportEvent;
import net.citizensnpcs.api.event.NPCPushEvent;
import net.citizensnpcs.api.npc.NPC;
import net.citizensnpcs.nms.v1_16_R2.util.NMSImpl;
import net.citizensnpcs.nms.v1_16_R2.util.PlayerControllerMove;
import net.citizensnpcs.npc.CitizensNPC;
import net.citizensnpcs.npc.ai.NPCHolder;
import net.citizensnpcs.util.Util;
import net.minecraft.server.v1_16_R2.BlockPosition;
import net.minecraft.server.v1_16_R2.ControllerMove;
import net.minecraft.server.v1_16_R2.DamageSource;
import net.minecraft.server.v1_16_R2.Entity;
import net.minecraft.server.v1_16_R2.EntityBoat;
import net.minecraft.server.v1_16_R2.EntityCod;
import net.minecraft.server.v1_16_R2.EntityTypes;
import net.minecraft.server.v1_16_R2.IBlockData;
import net.minecraft.server.v1_16_R2.NBTTagCompound;
import net.minecraft.server.v1_16_R2.SoundEffect;
import net.minecraft.server.v1_16_R2.Vec3D;
import net.minecraft.server.v1_16_R2.World;

public class CodController extends MobEntityController {
    public CodController() {
        super(EntityCodNPC.class);
    }

    @Override
    public Cod getBukkitEntity() {
        return (Cod) super.getBukkitEntity();
    }

    public static class CodNPC extends CraftCod implements NPCHolder {
        private final CitizensNPC npc;

        public CodNPC(EntityCodNPC entity) {
            super((CraftServer) Bukkit.getServer(), entity);
            this.npc = entity.npc;
        }

        @Override
        public NPC getNPC() {
            return npc;
        }
    }

    public static class EntityCodNPC extends EntityCod implements NPCHolder {
        private final CitizensNPC npc;
        private ControllerMove oldMoveController;

        public EntityCodNPC(EntityTypes<? extends EntityCod> types, World world) {
            this(types, world, null);
        }

        public EntityCodNPC(EntityTypes<? extends EntityCod> types, World world, NPC npc) {
            super(types, world);
            this.npc = (CitizensNPC) npc;
            if (npc != null) {
                NMSImpl.clearGoals(npc, goalSelector, targetSelector);
                this.oldMoveController = this.moveController;
                this.moveController = new ControllerMove(this);
            }
        }

        @Override
        protected void a(double d0, boolean flag, IBlockData block, BlockPosition blockposition) {
            if (npc == null || !npc.isFlyable()) {
                super.a(d0, flag, block, blockposition);
            }
        }

        @Override
        public boolean b(float f, float f1) {
            if (npc == null || !npc.isFlyable()) {
                return super.b(f, f1);
            }
            return false;
        }

        @Override
        public void checkDespawn() {
            if (npc == null) {
                super.checkDespawn();
            }
        }

        @Override
        public void collide(net.minecraft.server.v1_16_R2.Entity entity) {
            // this method is called by both the entities involved - cancelling
            // it will not stop the NPC from moving.
            super.collide(entity);
            if (npc != null)
                Util.callCollisionEvent(npc, entity.getBukkitEntity());
        }

        @Override
        public boolean d(NBTTagCompound save) {
            return npc == null ? super.d(save) : false;
        }

        @Override
        public void g(Vec3D vec3d) {
            if (npc == null || !npc.isFlyable()) {
                super.g(vec3d);
            } else {
                NMSImpl.flyingMoveLogic(this, vec3d);
            }
        }

        @Override
        public void enderTeleportTo(double d0, double d1, double d2) {
            if (npc == null) {
                super.enderTeleportTo(d0, d1, d2);
                return;
            }
            NPCEnderTeleportEvent event = new NPCEnderTeleportEvent(npc);
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCancelled()) {
                super.enderTeleportTo(d0, d1, d2);
            }
        }

        @Override
        public CraftEntity getBukkitEntity() {
            if (npc != null && !(super.getBukkitEntity() instanceof NPCHolder)) {
                NMSImpl.setBukkitEntity(this, new CodNPC(this));
            }
            return super.getBukkitEntity();
        }

        @Override
        public NPC getNPC() {
            return npc;
        }

        @Override
        protected SoundEffect getSoundAmbient() {
            return NMSImpl.getSoundEffect(npc, super.getSoundAmbient(), NPC.AMBIENT_SOUND_METADATA);
        }

        @Override
        protected SoundEffect getSoundDeath() {
            return NMSImpl.getSoundEffect(npc, super.getSoundDeath(), NPC.DEATH_SOUND_METADATA);
        }

        @Override
        protected SoundEffect getSoundHurt(DamageSource damagesource) {
            return NMSImpl.getSoundEffect(npc, super.getSoundHurt(damagesource), NPC.HURT_SOUND_METADATA);
        }

        @Override
        public void i(double x, double y, double z) {
            if (npc == null) {
                super.i(x, y, z);
                return;
            }
            if (NPCPushEvent.getHandlerList().getRegisteredListeners().length == 0) {
                if (!npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true))
                    super.i(x, y, z);
                return;
            }
            Vector vector = new Vector(x, y, z);
            NPCPushEvent event = Util.callPushEvent(npc, vector);
            if (!event.isCancelled()) {
                vector = event.getCollisionVector();
                super.i(vector.getX(), vector.getY(), vector.getZ());
            }
            // when another entity collides, this method is called to push the
            // NPC so we prevent it from doing anything if the event is
            // cancelled.
        }

        @Override
        public boolean isClimbing() {
            if (npc == null || !npc.isFlyable()) {
                return super.isClimbing();
            } else {
                return false;
            }
        }

        @Override
        public boolean isLeashed() {
            if (npc == null)
                return super.isLeashed();
            boolean protectedDefault = npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            if (!protectedDefault || !npc.data().get(NPC.LEASH_PROTECTED_METADATA, protectedDefault))
                return super.isLeashed();
            if (super.isLeashed()) {
                unleash(true, false); // clearLeash with client update
            }
            return false; // shouldLeash
        }

        @Override
        public void mobTick() {
            if (npc != null) {
                if (!npc.useMinecraftAI()) {
                    NMSImpl.setNotInSchool(this);
                }
                NMSImpl.updateMinecraftAIState(npc, this);
                if (npc.useMinecraftAI() && this.moveController != this.oldMoveController) {
                    this.moveController = this.oldMoveController;
                }
                if (!npc.useMinecraftAI() && this.moveController == this.oldMoveController) {
                    this.moveController = new PlayerControllerMove(this);
                }
            }
            super.mobTick();
            if (npc != null) {
                npc.update();
            }
        }

        @Override
        public void movementTick() {
            boolean lastInWater = this.v;
            if (npc != null) {
                this.v = false;
            }
            super.movementTick();
            if (npc != null) {
                this.v = lastInWater;
            }
        }

        @Override
        protected boolean n(Entity entity) {
            if (npc != null && (entity instanceof EntityBoat || entity instanceof EntityMinecartAbstract)) {
                return !npc.data().get(NPC.DEFAULT_PROTECTED_METADATA, true);
            }
            return super.n(entity);
        }
    }
}
