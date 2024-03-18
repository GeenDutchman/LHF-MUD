package com.lhf.game.item.concrete;

import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import com.lhf.game.Lockable;
import com.lhf.game.item.IItem;
import com.lhf.game.item.Usable;
import com.lhf.messages.CommandContext;

public class LockKey extends Usable {

    private final UUID lockedItemUuid;
    private final UUID keyUuid;

    /**
     * Creates a LockKey keyed to an item's UUID. Only usable once.
     * 
     * @param lockedItemUuid
     */
    public LockKey(UUID lockedItemUuid) {
        super(LockKey.generateKeyName(lockedItemUuid), "A key for ... something.", 1, null);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    /**
     * Creates a LockKey keyed to an item's UUID.
     * 
     * @param lockedItemUuid
     * @param useSoManyTimes if > 0 then can use that many times, if < 0 then has
     *                       infinite uses
     */
    public LockKey(UUID lockedItemUuid, int useSoManyTimes) {
        super(LockKey.generateKeyName(lockedItemUuid), "A key for ... something.", useSoManyTimes, null);
        this.lockedItemUuid = lockedItemUuid;
        this.keyUuid = UUID.randomUUID();
    }

    @Override
    public LockKey makeCopy() {
        return new LockKey(lockedItemUuid, this.numCanUseTimes);
    }

    public static String generateKeyName(UUID lockedItemUuid) {
        return "Key " + lockedItemUuid.toString();
    }

    @Override
    public Consumer<IItem> produceItemConsumer(CommandContext ctx) {
        return new Consumer<IItem>() {
            @Override
            public void accept(IItem item) {
                if (item != null && item instanceof Lockable lockable
                        && LockKey.this.keyUuid.equals(lockable.getLockUUID())) {
                    if (lockable.isUnlocked()) {
                        lockable.lock();
                    } else {
                        lockable.unlock();
                    }
                    LockKey.this.sendNotice(ctx, ctx.getCreature(), LockKey.this.getItemUseBuilder(ctx, item)
                            .setMessage(lockable.isUnlocked() ? "It is now unlocked." : "It is now locked."));
                }
                LockKey.this.sendNotice(ctx, ctx.getCreature(), LockKey.this.getItemUseBuilder(ctx, item));
            }
        };

    }

    public UUID getDoorwayUuid() {
        return lockedItemUuid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lockedItemUuid, keyUuid);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof LockKey)) {
            return false;
        }
        LockKey other = (LockKey) obj;
        return Objects.equals(lockedItemUuid, other.lockedItemUuid) && Objects.equals(keyUuid, other.keyUuid);
    }

}