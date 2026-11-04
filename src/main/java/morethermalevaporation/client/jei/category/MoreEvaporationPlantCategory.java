package morethermalevaporation.client.jei.category;

import giselle.jei_mekanism_multiblocks.client.gui.CheckBoxWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWidget;
import giselle.jei_mekanism_multiblocks.client.gui.IntSliderWithButtons;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockCategory;
import giselle.jei_mekanism_multiblocks.client.jei.MultiblockWidget;
import giselle.jei_mekanism_multiblocks.client.jei.ResultWidget;
import giselle.jei_mekanism_multiblocks.client.jei.category.ICostConsumer;
import giselle.jei_mekanism_multiblocks.client.jei.category.ResistiveHeaterCategory;
import giselle.jei_mekanism_multiblocks.common.JEI_MekanismMultiblocks;
import giselle.jei_mekanism_multiblocks.common.util.VolumeTextHelper;
import mekanism.api.heat.HeatAPI;
import mekanism.api.math.FloatingLong;
import mekanism.common.MekanismLang;
import mekanism.common.config.MekanismConfig;
import mekanism.common.content.evaporation.EvaporationMultiblockData;
import mekanism.common.registries.MekanismBlocks;
import mekanism.common.util.MekanismUtils;
import mekanism.common.util.UnitDisplayUtils.TemperatureUnit;
import mekanism.common.util.text.EnergyDisplay;
import mekanism.common.util.text.TextUtils;
import mekanism.generators.common.registries.GeneratorsBlocks;
import mezz.jei.api.helpers.IGuiHelper;
import morethermalevaporation.MoreThermalEvaporation;
import morethermalevaporation.common.MoreThermalEvaporationLang;
import morethermalevaporation.common.content.evaporation.MoreThermalEvaporationMultiblockData;
import morethermalevaporation.common.content.evaporation.MoreThermalEvaporationType;
import morethermalevaporation.common.registries.MoreThermalEvaporationBlocks;
import morethermalevaporation.common.registries.MoreThermalEvaporationItems;
import morethermalevaporation.common.tier.MoreThermalEvaporationTier;
import morethermalevaporation.common.upgrade.MoreThermalEvaporationUpgrade;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import giselle.jei_mekanism_multiblocks.client.preview.IPreviewBuilder;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewBuilder;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewLevel;
import giselle.jei_mekanism_multiblocks.client.preview.PreviewSelectors;
import mekanism.common.block.attribute.Attribute;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Consumer;

public class MoreEvaporationPlantCategory extends MultiblockCategory<MoreEvaporationPlantCategory.MoreEvaporationPlantWidget> {
    private final MoreThermalEvaporationTier tier;

    public MoreEvaporationPlantCategory(IGuiHelper helper, MoreThermalEvaporationTier tier, Class<? extends MoreEvaporationPlantWidget> widgetClass) {
        // NOTE JEI表示順の為にティア順をパスに追加
        super(helper, createRecipeType(MoreThermalEvaporation.rl(tier.ordinal() + "_" + tier.getBaseTier().getLowerName() + "_evaporation_plant"), widgetClass), MoreThermalEvaporationLang.getLangPlant(tier).translate(), MoreThermalEvaporationBlocks.CONTROLLERS.get(tier).getItemStack());
        this.tier = tier;
    }

    @Override
    protected void getRecipeCatalystItemStacks(Consumer<ItemStack> consumer) {
        super.getRecipeCatalystItemStacks(consumer);
        consumer.accept(MoreThermalEvaporationBlocks.BLOCKS.get(this.tier).getItemStack());
        consumer.accept(MoreThermalEvaporationBlocks.VALVES.get(this.tier).getItemStack());
        consumer.accept(MoreThermalEvaporationBlocks.CONTROLLERS.get(this.tier).getItemStack());
        consumer.accept(MekanismBlocks.STRUCTURAL_GLASS.getItemStack());

        if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded) {
            consumer.accept(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack());
        }

    }

    public abstract static class MoreEvaporationPlantWidget extends MultiblockWidget {
        protected CheckBoxWidget useAdvancedSolarGeneratorCheckBox;
        protected CheckBoxWidget useStructureUpgradeCheckBox;
        protected CheckBoxWidget useLargeTypesCheckBox;
        protected IntSliderWithButtons valvesWidget;

        public MoreEvaporationPlantWidget() {

        }

        protected abstract MoreThermalEvaporationTier getTier();
    private static final byte[][] BOTTOM_ALLOWED_GRID = {
            {0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 0},
            {1, 1, 1, 1, 1, 1, 1, 1, 1},
            {0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 1, 1, 1, 1, 1, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0}
    };
    private static final byte[][] CENTER_FRAME_ALLOWED_GRID = {
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 1, 3, 3, 3, 1, 1, 0},
            {1, 1, 3, 3, 3, 3, 3, 1, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 3, 3, 3, 3, 3, 3, 3, 1},
            {1, 1, 3, 3, 3, 3, 3, 1, 1},
            {0, 1, 1, 3, 3, 3, 1, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0}
    };
    private static final byte[][] CENTER_ALLOWED_GRID = {
            {0, 0, 1, 2, 2, 2, 1, 0, 0},
            {0, 1, 1, 3, 3, 3, 1, 1, 0},
            {1, 1, 3, 3, 3, 3, 3, 1, 1},
            {2, 3, 3, 3, 3, 3, 3, 3, 2},
            {2, 3, 3, 3, 3, 3, 3, 3, 2},
            {2, 3, 3, 3, 3, 3, 3, 3, 2},
            {1, 1, 3, 3, 3, 3, 3, 1, 1},
            {0, 1, 1, 3, 3, 3, 1, 1, 0},
            {0, 0, 1, 2, 2, 2, 1, 0, 0}
    };
    private static final byte[][] TOP_ALLOWED_GRID = {
            {0, 0, 0, 0, 1, 0, 0, 0, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 1, 3, 3, 3, 3, 3, 1, 0},
            {0, 1, 3, 3, 3, 3, 3, 1, 0},
            {1, 1, 3, 3, 3, 3, 3, 1, 1},
            {0, 1, 3, 3, 3, 3, 3, 1, 0},
            {0, 1, 3, 3, 3, 3, 3, 1, 0},
            {0, 0, 1, 1, 1, 1, 1, 0, 0},
            {0, 0, 0, 0, 1, 0, 0, 0, 0}
    };
		@Override
		public boolean canCreatePreview()
		{
			return true;
		}

		@Override
		protected void fillPreview(IPreviewBuilder builder)
		{
			super.fillPreview(builder);

			Vec3i dimension = this.getDimension();
			BlockPos controllerPos = new BlockPos(dimension.getX() - 2, 1, dimension.getZ() - 1);

			boolean useGlass = this.isUseGlass();
			boolean useAdvancedSolarGenerator = this.isUseAdvancedSolarGenerator();
			BlockState edgeState = MoreThermalEvaporationBlocks.BLOCKS.get(getTier()).getBlock().defaultBlockState();
			BlockState valveState = MoreThermalEvaporationBlocks.VALVES.get(getTier()).getBlock().defaultBlockState();
			BlockState sideState = useGlass ? this.getGlassBlock().defaultBlockState() : edgeState;

			builder.setBlockShell(edgeState, sideState);
			builder.setBlock(PreviewSelectors.top(), Blocks.AIR.defaultBlockState());
			builder.setBlock(controllerPos, Attribute.setFacing(Attribute.setActive(MoreThermalEvaporationBlocks.CONTROLLERS.get(getTier()).getBlock().defaultBlockState(), true), Direction.SOUTH));
			builder.replaceBlock(PreviewSelectors.shellSidesCCW(), sideState, valveState, this.getValveCount());

			if (useGlass && !useAdvancedSolarGenerator)
			{
				builder.setBlock(PreviewSelectors.topCorners(), edgeState);
				builder.setBlock(PreviewSelectors.topEdges(), sideState);
			}
			else
			{
				builder.setBlock(PreviewSelectors.topEdges(), edgeState);
			}

			if (useAdvancedSolarGenerator)
			{
				builder.setBlock(PreviewSelectors.topCorners(), GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getBlock().defaultBlockState());
			}

		}


        @Override
        public PreviewLevel createPreview() {
            if (!isUseLargeType()) return super.createPreview();
            PreviewBuilder builder = new PreviewBuilder(new Vec3i(9, getDimensionHeight(), 9));
            fillLargePreview(builder);
            return new PreviewLevel(builder);
        }

        private void fillLargePreview(IPreviewBuilder builder) {
            BlockState casing = MoreThermalEvaporationBlocks.BLOCKS.get(getTier()).getBlock().defaultBlockState();
            BlockState side = isUseGlass() ? getGlassBlock().defaultBlockState() : casing;
            BlockState valve = MoreThermalEvaporationBlocks.VALVES.get(getTier()).getBlock().defaultBlockState();
            int remainingValves = getValveCount();
            boolean controllerPlaced = false;
            int top = getDimensionHeight() - 1;
            for (int y = 0; y <= top; y++) {
                byte[][] grid = y == 0 ? BOTTOM_ALLOWED_GRID : y == top ? TOP_ALLOWED_GRID
                        : y == 1 || y == top - 1 ? CENTER_FRAME_ALLOWED_GRID : CENTER_ALLOWED_GRID;
                for (int z = 0; z < 9; z++) {
                    for (int x = 0; x < 9; x++) {
                        int requirement = grid[z][x];
                        if (requirement != 1 && requirement != 2) continue;
                        BlockState state = casing;
                        if (requirement == 2) {
                            if (!controllerPlaced) {
                                Direction facing = z == 0 ? Direction.NORTH : z == 8 ? Direction.SOUTH
                                        : x == 0 ? Direction.WEST : Direction.EAST;
                                state = Attribute.setFacing(Attribute.setActive(MoreThermalEvaporationBlocks.CONTROLLERS.get(getTier()).getBlock().defaultBlockState(), true), facing);
                                controllerPlaced = true;
                            } else if (remainingValves > 0) {
                                state = valve;
                                remainingValves--;
                            } else state = side;
                        }
                        builder.setBlock(new BlockPos(x, y, z), state);
                    }
                }
            }
            if (isUseAdvancedSolarGenerator()) {
                for (int x : new int[]{1, 7}) for (int z : new int[]{1, 7})
                    builder.setBlock(new BlockPos(x, top, z), GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getBlock().defaultBlockState());
            }
        }


        @Override
        public int getSideBlocks() {
            // 1 Controller
            // 4 Empty top inner
            return super.getSideBlocks() - 5;
        }

        public int getFreeFrameCount() {
            // 自由フレーム - コントローラ1個分
            return 12 * (this.getDimensionHeight() - 4) - 1;
        }

        @Override
        protected void collectOtherConfigs(Consumer<AbstractWidget> consumer) {
            super.collectOtherConfigs(consumer);

            if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded) {
                consumer.accept(this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR.getItemStack().getHoverName()), true));
                this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseAdvancedSolarGeneratorChanged);
            } else {
                this.useAdvancedSolarGeneratorCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.empty(), false);
                this.useAdvancedSolarGeneratorCheckBox.addSelectedChangedHandler(this::onUseAdvancedSolarGeneratorChanged);
            }

            consumer.accept(this.useStructureUpgradeCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", MoreThermalEvaporationItems.STRUCTURE_UPGRADE.getItemStack().getHoverName()), false));
            this.useStructureUpgradeCheckBox.addSelectedChangedHandler(this::onUseStructureUpgradeChanged);

            consumer.accept(this.useLargeTypesCheckBox = new CheckBoxWidget(0, 0, 0, 0, Component.translatable("text.jei_mekanism_multiblocks.specs.use_things", MoreThermalEvaporationLang.MULTIBLOCK_TYPE.translate(MoreThermalEvaporationLang.TYPE_LARGE.translate())), false));
            this.useLargeTypesCheckBox.addSelectedChangedHandler(this::onUseLargeTypeChanged);

            consumer.accept(this.valvesWidget = new IntSliderWithButtons(0, 0, 0, 0, "text.jei_mekanism_multiblocks.specs.valves", 0, 2, 0));
            this.valvesWidget.getSlider().addValueChangeHanlder(this::onValvesChanged);

            this.updateValveSliderLimit();
        }

        @Override
        public void load(CompoundTag tag) {
            super.load(tag);

            this.setUseAdvancedSolarGenerator(tag.getBoolean("UseAdvancedSolarGenerator"));
            this.setUseStructureUpgrade(tag.getBoolean("UseStructureUpgrade"));
            this.setUseLargeTypes(tag.getBoolean("UseLargeType"));
            this.setValveCount(tag.getInt("ValveCount"));
        }

        @Override
        public void save(CompoundTag tag) {
            super.save(tag);

            tag.putBoolean("UseAdvancedSolarGenerator", this.isUseAdvancedSolarGenerator());
            tag.putBoolean("UseStructureUpgrade", this.isUseStructureUpgrade());
            tag.putBoolean("UseLargeType", this.isUseLargeType());
            tag.putInt("ValveCount", this.getValveCount());
        }

        @Override
        protected void onDimensionChanged() {
            super.onDimensionChanged();

            this.updateValveSliderLimit();
        }

        public void updateValveSliderLimit() {
            IntSliderWidget valvesSlider = this.valvesWidget.getSlider();
            int minValves = valvesSlider.getMinValue();
            int valves = valvesSlider.getValue();
            valvesSlider.setMinValue(this.isUseAdvancedSolarGenerator() && !this.isUseLargeType() ? 2 : 3);
            valvesSlider.setMaxValue(this.isUseLargeType() ? this.getFreeFrameCount() : this.getSideBlocks());
            valvesSlider.setValue(valves + (valvesSlider.getMinValue() - minValves));
        }

        protected void onValvesChanged(int valves) {
            this.markNeedUpdate();
        }

        @Override
        protected void onUseGlassChanged(boolean useGlass) {
            super.onUseGlassChanged(useGlass);
        }

        protected void onUseAdvancedSolarGeneratorChanged(boolean useAdvancedSolarGenerator) {
            this.markNeedUpdate();

            this.updateValveSliderLimit();
        }

        protected void onUseStructureUpgradeChanged(boolean useStructureUpgrade) {
            this.markNeedUpdate();

            MoreThermalEvaporationTier tier = getTier();
            int currentHeight = this.getDimensionHeight();

            this.heightWidget.getSlider().setMaxValue(
                    useStructureUpgrade
                            ? tier.getHeight() + MoreThermalEvaporationUpgrade.STRUCTURE.getMax()
                            : tier.getHeight()
            );

            this.heightWidget.getSlider().setValue(currentHeight);
        }

        protected void onUseLargeTypeChanged(boolean useLargeTypes) {
            this.markNeedUpdate();

            int currentHeight = this.getDimensionHeight();

            this.heightWidget.getSlider().setMinValue(
                    useLargeTypes ? 5 : 3
            );

            this.heightWidget.getSlider().setValue(currentHeight);

            this.updateValveSliderLimit();
        }

        @Override
        protected void collectCost(ICostConsumer consumer) {
            super.collectCost(consumer);
            MoreThermalEvaporationTier tier = getTier();

            int corners = this.getEdgeBlocks();
            int sides = this.getSideBlocks();
            int valves = this.getValveCount();
            sides -= valves;

            int casing = 0;
            int glasses = 0;
            int advancedSolarGenerators = 0;
            int upgrades = 0;

            if (isUseLargeType()) {
                final int top = 24;
                final int centerFrame = 32 * 2;
                final int bottom = 49;
                int frame = 20 * (this.getDimensionHeight() - 4);

                if (this.isUseGlass()) {
                    // getLargeSideBlocks() は「コントローラー1個分」が除外済みの自由枠数
                    // 自由枠のうちバルブ以外の場所をすべてガラスにする
                    glasses = this.getFreeFrameCount() - valves;
                } else {
                    // 自由枠のうちバルブ以外の場所をすべてCasingとして加算する
                    casing += this.getFreeFrameCount() - valves;
                }

                if (this.isUseAdvancedSolarGenerator()) {
                    advancedSolarGenerators += 4;
                }

                casing += top + centerFrame + frame + bottom;

            } else {

                if (this.isUseGlass()) {
                    casing = corners;
                    glasses = sides;

                    if (this.isUseAdvancedSolarGenerator()) {
                        // Replace top corner to solar generator
                        casing -= 4;
                        advancedSolarGenerators += 4;
                    } else {
                        // Replace top side to glass
                        casing -= 8;
                        glasses += 8;
                    }

                } else {
                    // Remove top vertices
                    casing = corners + sides - 4;

                    if (this.isUseAdvancedSolarGenerator()) {
                        advancedSolarGenerators += 4;
                    }

                }
            }

            if (this.isUseStructureUpgrade()) {
                upgrades = this.getDimensionHeight() - 18;
            }

            consumer.accept(new ItemStack(MoreThermalEvaporationBlocks.CONTROLLERS.get(tier), 1));
            consumer.accept(new ItemStack(MoreThermalEvaporationBlocks.VALVES.get(tier), valves));
            consumer.accept(new ItemStack(MoreThermalEvaporationBlocks.BLOCKS.get(tier), casing));
            consumer.accept(new ItemStack(this.getGlassBlock(), glasses));

            if (JEI_MekanismMultiblocks.MekanismGeneratorsLoaded) {
                consumer.accept(new ItemStack(GeneratorsBlocks.ADVANCED_SOLAR_GENERATOR, advancedSolarGenerators));
            }

            consumer.accept(new ItemStack(MoreThermalEvaporationItems.STRUCTURE_UPGRADE.get(), upgrades));

        }

        @Override
        protected void collectResult(Consumer<AbstractWidget> consumer) {
            super.collectResult(consumer);
            MoreThermalEvaporationTier tier = getTier();
            long dimHeight = this.getDimensionHeight();
            long inputCapacity = tier == MoreThermalEvaporationTier.CREATIVE ? Integer.MAX_VALUE : getInputCapacity(tier, dimHeight);
            long outputCapacity = Math.min(Integer.MAX_VALUE, (long) tier.getOutputTankCapacity() * (isUseLargeType() ? MoreThermalEvaporationType.LARGE.getMultiplier() : MoreThermalEvaporationType.NORMAL.getMultiplier()));
            double maxTemp = tier.getMultiplierTemp() * (isUseLargeType() ? MoreThermalEvaporationType.LARGE.getMultiplier() : MoreThermalEvaporationType.NORMAL.getMultiplier());
            double maxSpeed = (maxTemp - HeatAPI.AMBIENT_TEMP) * MekanismConfig.general.evaporationTempMultiplier.get() * ((double) dimHeight / MoreThermalEvaporationMultiblockData.MAX_HEIGHT);
            ResultWidget speedWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.max_speed"), Component.literal("x" + TextUtils.format(maxSpeed)));
            speedWidget.setTooltipMessage(Component.translatable("text.jei_mekanism_multiblocks.tooltip.when_temp_ge", MekanismUtils.getTemperatureDisplay(maxTemp, TemperatureUnit.KELVIN, false)));
            consumer.accept(speedWidget);
            consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.input_tank"), VolumeTextHelper.formatMB(inputCapacity)));
            consumer.accept(new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.output_tank"), VolumeTextHelper.formatMB(outputCapacity)));

            this.createRequiredHeaterEnergyWidget(consumer);
        }

        private void createRequiredHeaterEnergyWidget(Consumer<AbstractWidget> consumer) {
            FloatingLong plainRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.AMBIENT_TEMP);
            FloatingLong coldestRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.getAmbientTemp(Integer.MIN_VALUE));
            FloatingLong hotestRequiredEnergy = this.getRequiredHeaterEnergy(HeatAPI.getAmbientTemp(Integer.MAX_VALUE));
            ResultWidget requiredEnergyWidget = new ResultWidget(Component.translatable("text.jei_mekanism_multiblocks.result.required_heater_usage"), Component.translatable("%s/t", EnergyDisplay.of(plainRequiredEnergy).getTextComponent()));
            Component heaterName = new ItemStack(MekanismBlocks.RESISTIVE_HEATER).getHoverName();
            Component valveName = new ItemStack(MekanismBlocks.THERMAL_EVAPORATION_VALVE).getHoverName();
            requiredEnergyWidget.setTooltipMessage(//
                    Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.plain", Component.translatable("%s %s/t", TextUtils.format(plainRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
                    Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.coldest", Component.translatable("%s %s/t", TextUtils.format(coldestRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
                    Component.translatable("text.jei_mekanism_multiblocks.tooltip.required_heater_usage.hottest", Component.translatable("%s %s/t", TextUtils.format(hotestRequiredEnergy.longValue()), Component.translatable(MekanismLang.ENERGY_JOULES_SHORT.getTranslationKey()))), //
                    Component.translatable("text.jei_mekanism_multiblocks.tooltip.heater_near_and_1_sink_1", heaterName, valveName), //
                    Component.translatable("text.jei_mekanism_multiblocks.tooltip.heater_near_and_1_sink_2", heaterName, valveName));
            consumer.accept(requiredEnergyWidget);
        }

        public FloatingLong getRequiredHeaterEnergy(double ambientTemp) {
            double heat = this.getMaxMultiplierHeat(ambientTemp);
            return ResistiveHeaterCategory.getHeatTransferableEnergy(ambientTemp, heat, HeatAPI.DEFAULT_INVERSE_CONDUCTION).ceil();
        }

        public double getMaxMultiplierHeat(double ambientTemp) {
            int activeSolars = this.isUseAdvancedSolarGenerator() ? 4 : 0;
            double heatCapacity = this.getDimensionHeight() * MekanismConfig.general.evaporationHeatCapacity.get();
            double maxMultiplierTemp = this.getTier().getMultiplierTemp() * (isUseLargeType() ? MoreThermalEvaporationType.LARGE.getMultiplier() : MoreThermalEvaporationType.NORMAL.getMultiplier());
            double gain = activeSolars * MekanismConfig.general.evaporationSolarMultiplier.get() * heatCapacity;
            double loss = MekanismConfig.general.evaporationHeatDissipation.get() * Math.sqrt(Math.abs(maxMultiplierTemp - ambientTemp)) * heatCapacity;
            return loss - gain;
        }

        public long getInputCapacity(MoreThermalEvaporationTier tier, long dimHeight) {
            if (isUseLargeType()) {
                return ((dimHeight * 81) / 4) * tier.getInputTankCapacity();
            }
            return dimHeight * 4 * tier.getInputTankCapacity();
        }

        public int getValveCount() {
            return this.valvesWidget.getSlider().getValue();
        }

        public void setValveCount(int valveCount) {
            this.valvesWidget.getSlider().setValue(valveCount);
        }

        public boolean isUseAdvancedSolarGenerator() {
            return JEI_MekanismMultiblocks.MekanismGeneratorsLoaded && this.useAdvancedSolarGeneratorCheckBox.isSelected();
        }

        public void setUseAdvancedSolarGenerator(boolean useAdvancedSolarGenerator) {
            this.useAdvancedSolarGeneratorCheckBox.setSelected(useAdvancedSolarGenerator);
        }

        public boolean isUseStructureUpgrade() {
            return this.useStructureUpgradeCheckBox.isSelected();
        }

        public boolean isUseLargeType() {
            return this.useLargeTypesCheckBox.isSelected();
        }

        public void setUseStructureUpgrade(boolean useStructureUpgrade) {
            this.useStructureUpgradeCheckBox.setSelected(useStructureUpgrade);
        }

        public void setUseLargeTypes(boolean useLargeType) {
            this.useLargeTypesCheckBox.setSelected(useLargeType);
        }

        @Override
        public int getDimensionWidthMin() {
            return 4;
        }

        @Override
        public int getDimensionWidthMax() {
            return 4;
        }

        @Override
        public int getDimensionLengthMin() {
            return 4;
        }

        @Override
        public int getDimensionLengthMax() {
            return 4;
        }

        @Override
        public int getDimensionHeightMin() {
            return 3;
        }

        @Override
        public int getDimensionHeightMax() {
            MoreThermalEvaporationTier tier = getTier();
            return tier.getHeight();
        }

        @Override
        public Block getGlassBlock() {
            return MekanismBlocks.STRUCTURAL_GLASS.getBlock();
        }

    }

    public static class BasicEvaporationPlantWidget extends MoreEvaporationPlantWidget {

        @Override
        protected MoreThermalEvaporationTier getTier() {
            return MoreThermalEvaporationTier.BASIC;
        }
    }

    public static class AdvancedEvaporationPlantWidget extends MoreEvaporationPlantWidget {

        @Override
        protected MoreThermalEvaporationTier getTier() {
            return MoreThermalEvaporationTier.ADVANCED;
        }
    }

    public static class EliteEvaporationPlantWidget extends MoreEvaporationPlantWidget {

        @Override
        protected MoreThermalEvaporationTier getTier() {
            return MoreThermalEvaporationTier.ELITE;
        }
    }

    public static class UltimateEvaporationPlantWidget extends MoreEvaporationPlantWidget {
        @Override
        protected MoreThermalEvaporationTier getTier() {
            return MoreThermalEvaporationTier.ULTIMATE;
        }
    }

    public static class CreativeEvaporationPlantWidget extends MoreEvaporationPlantWidget {
        @Override
        protected MoreThermalEvaporationTier getTier() {
            return MoreThermalEvaporationTier.CREATIVE;
        }
    }

}