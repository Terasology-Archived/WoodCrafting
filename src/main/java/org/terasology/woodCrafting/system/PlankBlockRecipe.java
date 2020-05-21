/*
 * Copyright 2016 MovingBlocks
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.terasology.woodCrafting.system;

import com.google.common.base.Predicate;
import org.terasology.entitySystem.entity.EntityManager;
import org.terasology.entitySystem.entity.EntityRef;
import org.terasology.inGameHelpAPI.components.ItemHelpComponent;
import org.terasology.logic.common.DisplayNameComponent;
import org.terasology.processing.component.TreeTypeComponent;
import org.terasology.registry.CoreRegistry;
import org.terasology.rendering.nui.layers.ingame.inventory.ItemIcon;
import org.terasology.rendering.nui.widgets.TooltipLine;
import org.terasology.utilities.Assets;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeFluidBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.InventorySlotTypeResolver;
import org.terasology.workstationCrafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.render.result.BlockRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.workstation.AbstractWorkstationRecipe;
import org.terasology.workstationCrafting.system.recipe.workstation.CraftingStationIngredientPredicate;
import org.terasology.workstationCrafting.system.recipe.workstation.CraftingStationToolPredicate;
import org.terasology.world.block.Block;
import org.terasology.world.block.BlockManager;
import org.terasology.world.block.BlockUri;
import org.terasology.world.block.items.BlockItemFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class represents the recipe used to craft plank blocks.
 */
public class PlankBlockRecipe extends AbstractWorkstationRecipe {
    public PlankBlockRecipe(int ingredientCount, int toolDurability, String shape, int resultCount) {
        Predicate<EntityRef> plankPredicate = new CraftingStationIngredientPredicate("Woodcrafting:plank");
        Predicate<EntityRef> hammerPredicate = new CraftingStationToolPredicate("hammer");

        addIngredientBehaviour(new ConsumePlankIngredientBehaviour(plankPredicate, ingredientCount, new InventorySlotTypeResolver("INPUT")));
        addToolBehaviour(new ReduceDurabilityCraftBehaviour(hammerPredicate, toolDurability, new InventorySlotTypeResolver("TOOL")));

        setResultFactory(new PlankBlockRecipeResultFactory(shape, resultCount));
    }

    public PlankBlockRecipe(int ingredientCount, int toolDurability, String shape, Block block, int resultCount) {
        Predicate<EntityRef> plankPredicate = new CraftingStationIngredientPredicate("Woodcrafting:plank");
        Predicate<EntityRef> hammerPredicate = new CraftingStationToolPredicate("hammer");

        addIngredientBehaviour(new ConsumePlankIngredientBehaviour(plankPredicate, ingredientCount, new InventorySlotTypeResolver("INPUT")));
        addToolBehaviour(new ReduceDurabilityCraftBehaviour(hammerPredicate, toolDurability, new InventorySlotTypeResolver("TOOL")));

        setResultFactory(new PlankBlockRecipeResultFactory(shape, block, resultCount));
    }

    private final class PlankBlockRecipeResultFactory extends BlockRecipeResultFactory {
        private String shape;

        private PlankBlockRecipeResultFactory(String shape, int count) {
            super(count);
            this.shape = shape;
        }

        private PlankBlockRecipeResultFactory(String shape, Block block, int count) {
            super(block, count);
            this.shape = shape;
        }

        /**
         * Get an instance of this recipe's resultant plank block using the passed in parameters.
         *
         * @param parameters    These parameters contain information like the tree type that will be used to get the
         *                      block.
         * @return              An instance of this recipe's resultant plank block. It has both the specified block type
         *                      and shape.
         */
        @Override
        protected Block getBlock(List<String> parameters) {
            if (super.getBlock(parameters) != null) {
                return super.getBlock(parameters);
            }

            String[] split = parameters.get(0).split("\\|");
            BlockManager blockManager = CoreRegistry.get(BlockManager.class);
            if (split.length == 2) {
                String treeType = split[1];
                String blockType = "Woodcrafting:" + treeType + "Plank";
                BlockUri customBlockUri = new BlockUri("Woodcrafting:" + treeType + "Plank");
                if (blockManager.getBlockFamily(customBlockUri) != null) {
                    return blockManager.getBlockFamily(appendShapeIfNeeded(blockType)).getArchetypeBlock();
                }
            }
            return blockManager.getBlockFamily(appendShapeIfNeeded("CoreAssets:Plank")).getArchetypeBlock();
        }

        /**
         * If the shape name is not null, append it to the value. The value is intended to be a module path or a block
         * type name.
         *
         * @param value     The input string to potentially be appended to.
         * @return          The modified input string (if shape is not null), or the original input string.
         */
        private String appendShapeIfNeeded(String value) {
            if (shape != null) {
                return value + ":" + shape;
            } else {
                return value;
            }
        }

        @Override
        public EntityRef createResult(List<String> parameters, int multiplier) {
            EntityRef blockEntity = super.createResult(parameters, multiplier);
            DisplayNameComponent displayName = blockEntity.getComponent(DisplayNameComponent.class);

            // If this block has no DisplayNameComponent, add one.
            if (displayName == null) {
                displayName = new DisplayNameComponent();
            }

            displayName.name = shape;
            displayName.description = "A " + shape + " block made out of wood.";
            blockEntity.addOrSaveComponent(displayName);

            return blockEntity;
        }

        /**
         * Setup the display (mesh and description) of the recipe's resultant block.
         *
         * @param parameters    A list of parameters AKA inputs required for producing this recipe.
         * @param itemIcon      The block's icon.
         */
        @Override
        public void setupDisplay(List<String> parameters, ItemIcon itemIcon) {
            Block blockToDisplay = getBlock(parameters);
            itemIcon.setMesh(blockToDisplay.getMesh());
            itemIcon.setMeshTexture(Assets.getTexture("engine:terrain").get());

            // Get the block's entity, and use it to set the tooltip lines for this item. This include its name, category,
            // and (short) description.
            EntityRef blockEntity = blockToDisplay.getEntity();
            DisplayNameComponent displayName = blockEntity.getComponent(DisplayNameComponent.class);

            // If this block has no DisplayNameComponent, add one.
            if (displayName == null) {
                displayName = new DisplayNameComponent();
                blockEntity.addComponent(displayName);
            }

            // This is done just in case the recipe calls for a generic cubic (1 by 1 by 1) plank block, As by default,
            // that has no shape name.
            if (shape == null) {
                shape = "Generic Cubic Plank";
            }

            // Get the wood type name of this plank. If it exists, add it to the name and description of the plank
            // block. If not, use the default name and description.
            String[] split = parameters.get(0).split("\\|");
            if (split.length == 2) {
                String treeType = split[1];
                displayName.name = treeType + " " + shape;
                displayName.description = "A " + shape + " block made out of " + treeType + " wood.";
            } else {
                displayName.name = shape;
                displayName.description = "A " + shape + " block made out of wood.";
            }

            ArrayList<TooltipLine> tooltipLines = new ArrayList<>(Arrays.asList(new TooltipLine(displayName.name)));

            // If this block's entity is registered into the InGameHelp system, get its category and add it into the
            // tooltip.
            if (blockEntity.hasComponent(ItemHelpComponent.class)) {
                ItemHelpComponent itemHelp = blockEntity.getComponent(ItemHelpComponent.class);
                tooltipLines.add(new TooltipLine(itemHelp.getCategory()));
            }
            // If this block's entity has a description, add it into the tooltip.
            if (!displayName.description.equals("")) {
                tooltipLines.add(new TooltipLine(displayName.description));
            }

            // Set the block entity's full tooltip or description.
            itemIcon.setTooltipLines(tooltipLines);
        }
    }

    private final class ConsumePlankIngredientBehaviour extends ConsumeItemCraftBehaviour {
        private ConsumePlankIngredientBehaviour(Predicate<EntityRef> matcher, int count, InventorySlotResolver resolver) {
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
