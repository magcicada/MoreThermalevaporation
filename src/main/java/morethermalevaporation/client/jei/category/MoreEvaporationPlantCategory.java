
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
