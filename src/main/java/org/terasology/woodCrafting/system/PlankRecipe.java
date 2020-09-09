// Copyright 2020 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.woodCrafting.system;

import com.google.common.base.Predicate;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.logic.common.DisplayNameComponent;
import org.terasology.engine.utilities.Assets;
import org.terasology.inventory.rendering.nui.layers.ingame.ItemIcon;
import org.terasology.processing.component.TreeTypeComponent;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.workstation.AbstractWorkstationRecipe;
import org.terasology.workstationCrafting.system.recipe.workstation.CraftingStationIngredientPredicate;
import org.terasology.workstationCrafting.system.recipe.workstation.CraftingStationToolPredicate;

import java.util.List;

/**
 * This class represents the recipe used to craft planks.
 */
public class PlankRecipe extends AbstractWorkstationRecipe {
    public PlankRecipe(int plankCount) {
        Predicate<EntityRef> woodPredicate = new CraftingStationIngredientPredicate("Woodcrafting:wood");
        Predicate<EntityRef> axePredicate = new CraftingStationToolPredicate("axe");

        addIngredientBehaviour(new ConsumeWoodIngredientBehaviour(woodPredicate, 1, new InventorySlotTypeResolver(
                "INPUT")));
        addToolBehaviour(new ReduceDurabilityCraftBehaviour(axePredicate, 1, new InventorySlotTypeResolver("TOOL")));

        setResultFactory(new PlankRecipeResultFactory(Assets.getPrefab("Woodcrafting:WoodPlank").get(), plankCount));
    }

    private final class PlankRecipeResultFactory extends ItemRecipeResultFactory {
        private PlankRecipeResultFactory(Prefab prefab, int count) {
            super(prefab, count);
        }

        @Override
        public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
            super.setupDisplay(parameters, itemIcon);

            final String[] split = parameters.get(0).split("\\|");
            if (split.length > 1) {
                itemIcon.setTooltip(split[1] + " Plank");
            }
        }

        @Override
        public EntityRef createResult(List<String> parameters, int multiplier) {
            final EntityRef result = super.createResult(parameters, multiplier);
            final String woodParameter = parameters.get(0);
            final String[] split = woodParameter.split("\\|");
            if (split.length > 0) {
                String treeType = split[1];

                DisplayNameComponent displayName = result.getComponent(DisplayNameComponent.class);
                displayName.name = treeType + " Plank";
                result.saveComponent(displayName);

                TreeTypeComponent treeTypeComponent = new TreeTypeComponent();
                treeTypeComponent.treeType = treeType;
                result.addComponent(treeTypeComponent);
            }

            return result;
        }
    }

    private final class ConsumeWoodIngredientBehaviour extends ConsumeItemCraftBehaviour {
        private ConsumeWoodIngredientBehaviour(Predicate<EntityRef> matcher, int count,
                                               InventorySlotResolver resolver) {
            super(matcher, count, resolver);
        }

        @Override
        protected String getParameter(List<Integer> slots, EntityRef item) {
            final TreeTypeComponent treeType = item.getComponent(TreeTypeComponent.class);
            if (treeType != null) {
                return super.getParameter(slots, item) + "|" + treeType.treeType;
            } else {
                return super.getParameter(slots, item);
            }
        }

        @Override
        protected List<Integer> getSlots(String parameter) {
            final String[] split = parameter.split("\\|");
            return super.getSlots(split[0]);
        }
    }
}
