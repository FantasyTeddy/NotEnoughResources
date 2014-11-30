package neresources.registry;

import neresources.api.messages.RemoveMobMessage;
import neresources.api.utils.DropItem;
import neresources.utils.ClassScraper;
import neresources.utils.MobHelper;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MobRegistry
{
    private Set<MobEntry> registry = new LinkedHashSet<MobEntry>();

    private static MobRegistry instance = null;

    public static MobRegistry getInstance()
    {
        if (instance == null)
            return instance = new MobRegistry();
        return instance;
    }

    public boolean registerMob(MobEntry entry)
    {
        return registerMob(entry.getMobName(), entry);
    }

    public boolean registerMob(String key, MobEntry entry)
    {
        if (!registry.contains(entry))
        {
            registry.add(entry);
            return true;
        }
        return false;
    }

    public MobEntry getMobEntry(String key)
    {
        return null;//registry.get(key);
    }

    public List<MobEntry> getMobsThatDropItem(ItemStack item)
    {
        List<MobEntry> list = new ArrayList<MobEntry>();
        for (MobEntry entry : registry)
            if (MobHelper.dropsItem(entry, item)) list.add(entry);
        return list;
    }

    public List<MobEntry> getMobs()
    {
        return new ArrayList<MobEntry>(registry);
    }

    public void removeMobDrops(ChangeMobDrop entry)
    {
        for (MobEntry regEntry : getRegistryMatches(entry.getFilterClass(), entry.isExactMatch(), entry.witherSkeleton()))
            for (ItemStack item : entry.removeItems())
                regEntry.removeDrop(item);
    }

    public void addMobDrops(ChangeMobDrop entry)
    {
        for (MobEntry regEntry : getRegistryMatches(entry.getFilterClass(), entry.isExactMatch(), entry.witherSkeleton()))
            for (DropItem item : entry.addItems())
                regEntry.addDrop(item);
    }

    public void removeMob(RemoveMobMessage message)
    {
        registry.removeAll(getRegistryMatches(message.getFilterClass(), message.isStrict(), message.isWither()));
    }

    public Set<MobEntry> getRegistryMatches(Class clazz, boolean exactMatch, boolean witherSkeleton)
    {
        Set<MobEntry> result = new LinkedHashSet<MobEntry>();
        int wither = witherSkeleton ? 1 : 0;
        for (MobEntry regEntry : registry)
        {
            Set classes = new LinkedHashSet();
            if (exactMatch) classes.add(regEntry.getEntity().getClass());
            else classes = ClassScraper.getGeneralizations(regEntry.getEntity().getClass());
            for (Object generalClass : classes)
            {
                if (generalClass == clazz)
                {
                    if (exactMatch && clazz == EntitySkeleton.class)
                        if (((EntitySkeleton) regEntry.getEntity()).getSkeletonType() != wither) break;
                    result.add(regEntry);
                }
            }
        }
        return result;
    }
}
