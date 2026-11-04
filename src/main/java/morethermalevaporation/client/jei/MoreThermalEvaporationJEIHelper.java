package morethermalevaporation.client.jei;

import giselle.jei_mekanism_multiblocks.client.jei.JEI_MekanismMultiblocks_JeiPlugin;
import giselle.jei_mekanism_multiblocks.common.config.JEI_MekanismMultiblocks_Config;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import morethermalevaporation.client.jei.category.MoreEvaporationPlantCategory;
import morethermalevaporation.common.tier.MoreThermalEvaporationTier;

public final class MoreThermalEvaporationJEIHelper {
    private MoreThermalEvaporationJEIHelper() {}

    public static void registerCategories(IRecipeCategoryRegistration registry) {
        if (!JEI_MekanismMultiblocks_Config.CLIENT.evaporationPlantVisible.get()) return;
        var helper = registry.getJeiHelpers().getGuiHelper();
        var plugin = JEI_MekanismMultiblocks_JeiPlugin.instance();
        plugin.addCategory(registry, new MoreEvaporationPlantCategory(helper, MoreThermalEvaporationTier.BASIC, MoreEvaporationPlantCategory.BasicEvaporationPlantWidget.class));
        plugin.addCategory(registry, new MoreEvaporationPlantCategory(helper, MoreThermalEvaporationTier.ADVANCED, MoreEvaporationPlantCategory.AdvancedEvaporationPlantWidget.class));
        plugin.addCategory(registry, new MoreEvaporationPlantCategory(helper, MoreThermalEvaporationTier.ELITE, MoreEvaporationPlantCategory.EliteEvaporationPlantWidget.class));
        plugin.addCategory(registry, new MoreEvaporationPlantCategory(helper, MoreThermalEvaporationTier.ULTIMATE, MoreEvaporationPlantCategory.UltimateEvaporationPlantWidget.class));
        plugin.addCategory(registry, new MoreEvaporationPlantCategory(helper, MoreThermalEvaporationTier.CREATIVE, MoreEvaporationPlantCategory.CreativeEvaporationPlantWidget.class));
    }
}
