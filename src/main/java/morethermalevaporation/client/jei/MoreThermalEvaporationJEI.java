package morethermalevaporation.client.jei;

import mekanism.client.jei.CatalystRegistryHelper;
import mekanism.client.jei.MekanismJEIRecipeType;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import morethermalevaporation.MoreThermalEvaporation;
import morethermalevaporation.common.registries.MoreThermalEvaporationBlocks;
import morethermalevaporation.common.tier.MoreThermalEvaporationTier;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class MoreThermalEvaporationJEI implements IModPlugin {

    public MoreThermalEvaporationJEI() {
    }

    @Override
    public ResourceLocation getPluginUid() {
        return MoreThermalEvaporation.rl("jei_plugin");
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registry) {
        for (MoreThermalEvaporationTier tier : MoreThermalEvaporationTier.values()) {
            CatalystRegistryHelper.register(registry, MekanismJEIRecipeType.EVAPORATING, MoreThermalEvaporationBlocks.CONTROLLERS.get(tier),
                    MoreThermalEvaporationBlocks.VALVES.get(tier), MoreThermalEvaporationBlocks.BLOCKS.get(tier));
        }

    }

    // ↓↓↓ Just Enough Mekanism Multiblocks連携 ↓↓↓
    @Override
    public void registerCategories(IRecipeCategoryRegistration registry) {
        if (MoreThermalEvaporation.JustEnoughMekanismMultiblocksLoaded) {
            MoreThermalEvaporationJEIHelper.registerCategories(registry);
        }
    }


}
