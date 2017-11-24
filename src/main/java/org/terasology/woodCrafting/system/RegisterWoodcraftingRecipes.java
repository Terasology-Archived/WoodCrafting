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
import org.terasology.entitySystem.prefab.Prefab;
import org.terasology.entitySystem.prefab.PrefabManager;
import org.terasology.entitySystem.systems.BaseComponentSystem;
import org.terasology.entitySystem.systems.RegisterSystem;
import org.terasology.multiBlock.Basic3DSizeFilter;
import org.terasology.multiBlock.MultiBlockFormRecipeRegistry;
import org.terasology.multiBlock.UniformBlockReplacementCallback;
import org.terasology.multiBlock.recipe.UniformMultiBlockFormItemRecipe;
import org.terasology.processing.system.ToolTypeEntityFilter;
import org.terasology.processing.system.UseOnTopFilter;
import org.terasology.registry.In;
import org.terasology.woodCrafting.Woodcrafting;
import org.terasology.workstation.system.WorkstationRegistry;
import org.terasology.workstationCrafting.component.CraftInHandRecipeComponent;
import org.terasology.workstationCrafting.component.CraftingStationMaterialComponent;
import org.terasology.workstationCrafting.system.CraftInHandRecipeRegistry;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcess;
import org.terasology.workstationCrafting.system.CraftingWorkstationProcessFactory;
import org.terasology.workstationCrafting.system.recipe.behaviour.ConsumeItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.PresenceItemCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.behaviour.ReduceDurabilityCraftBehaviour;
import org.terasology.workstationCrafting.system.recipe.hand.CompositeTypeBasedCraftInHandRecipe;
import org.terasology.workstationCrafting.system.recipe.hand.CraftInHandIngredientPredicate;
import org.terasology.workstationCrafting.system.recipe.hand.CraftInHandRecipe;
import org.terasology.workstationCrafting.system.recipe.hand.PlayerInventorySlotResolver;
import org.terasology.workstationCrafting.system.recipe.render.RecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.render.result.BlockRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.render.result.ItemRecipeResultFactory;
import org.terasology.workstationCrafting.system.recipe.workstation.DefaultWorkstationRecipe;
import org.terasology.world.block.BlockManager;

/**
 * Utility class used to register workstation crafting recipes.
 */
@RegisterSystem
public class RegisterWoodcraftingRecipes extends BaseComponentSystem {
    @In
    private CraftInHandRecipeRegistry recipeRegistry;
    @In
    private WorkstationRegistry workstationRegistry;
    @In
    private MultiBlockFormRecipeRegistry multiBlockFormRecipeRegistry;
    @In
    private BlockManager blockManager;
    @In
    private PrefabManager prefabManager;
    @In
    private EntityManager entityManager;

    /**
     * Initialize the component system used to register crafting recipes.
     */
    @Override
    public void initialise() {
        workstationRegistry.registerProcessFactory(Woodcrafting.WOODCRAFTING_PROCESS, new CraftingWorkstationProcessFactory());

        // Add all the recipes for forming the workstation, nature crafting, wood plank creation, and custom block shapes.
        addWorkstationFormingRecipes();
        addCraftInHandRecipes();
        addWoodPlankRecipes();
        addStandardWoodWorkstationBlockShapeRecipes();
    }

    /**
     * Add the recipes for making wood planks to the stations that support the Woodcrafting process.
     */
    private void addWoodPlankRecipes() {

        // Register a (recipe) process for turning wood logs into wood planks. This will require a workstation that supports a Woodcrafting
        // process level of 30. This is intended to be a more efficient recipe for higher tier workstations.
        workstationRegistry.registerProcess(Woodcrafting.WOODCRAFTING_PROCESS,
                new CraftingWorkstationProcess(Woodcrafting.WOODCRAFTING_PROCESS, Woodcrafting.WOODCRAFTING_PROCESS_LEVEL_STANDARD,
                        "Materials|Woodcrafting:WoodPlank:LVL2", new PlankRecipe(3), null, entityManager));

        // Register a (recipe) process for turning wood logs into wood planks. This will require a workstation that supports a Woodcrafting
        // process level of 10.
        workstationRegistry.registerProcess(Woodcrafting.WOODCRAFTING_PROCESS,
                new CraftingWorkstationProcess(Woodcrafting.WOODCRAFTING_PROCESS, Woodcrafting.WOODCRAFTING_PROCESS_LEVEL_BASIC,
                        "Materials|Woodcrafting:WoodPlank", new PlankRecipe(2), null, entityManager));
    }

    private void addWorkstationFormingRecipes() {
        multiBlockFormRecipeRegistry.addMultiBlockFormItemRecipe(
                new UniformMultiBlockFormItemRecipe(new ToolTypeEntityFilter("axe"), new UseOnTopFilter(),
                        new StationTypeFilter("Woodcrafting:BasicWoodcraftingStation"), new Basic3DSizeFilter(2, 1, 1, 1),
                        "Woodcrafting:BasicWoodcraftingStation",
                        new UniformBlockReplacementCallback<Void>(blockManager.getBlock("Woodcrafting:BasicWoodStation"))));
    }

    private void addStandardWoodWorkstationBlockShapeRecipes() {
        addPlankBlockRecipes();
        addWorkstationBlockShapesRecipe(Woodcrafting.WOODCRAFTING_PROCESS, Woodcrafting.WOODCRAFTING_PROCESS_LEVEL_STANDARD,
                "Building|Fine Planks|Woodcrafting:FinePlankBlock", "Woodcrafting:plank", 4, "hammer", 1,
                "Woodcrafting:FinePlank", 1);
    }

    private void addPlankBlockShapeRecipe(String shape, int ingredientMultiplier, int durabilityMultiplier, int resultMultiplier) {
        addPlankBlockShapeRecipe(shape, "Engine", ingredientMultiplier, durabilityMultiplier, resultMultiplier);
    }

    private void addPlankBlockShapeRecipe(String shape, String module, int ingredientMultiplier, int durabilityMultiplier, int resultMultiplier) {
        String recipeName = "Building|Planks|Woodcrafting:PlankBlock";
        if (shape != null) {
            recipeName += shape;
        }

        String resultShape = null;
        if (shape != null) {
            resultShape = module + ":" + shape;
        }

        workstationRegistry.registerProcess(Woodcrafting.WOODCRAFTING_PROCESS,
                new CraftingWorkstationProcess(Woodcrafting.WOODCRAFTING_PROCESS, Woodcrafting.WOODCRAFTING_PROCESS_LEVEL_STANDARD,
                        recipeName, new PlankBlockRecipe(2 * ingredientMultiplier, durabilityMultiplier, resultShape,
                        4 * resultMultiplier), null, entityManager));
    }

    private void addPlankBlockRecipes() {
        addPlankBlockShapeRecipe(null, 1, 1, 1);
        addPlankBlockShapeRecipe("Stair", 3, 4, 2);

        addPlankBlockShapeRecipe("Slope", 1, 2, 2);
        addPlankBlockShapeRecipe("UpperHalfSlope", 1, 2, 2);
        addPlankBlockShapeRecipe("SlopeCorner", 1, 2, 2);

        addPlankBlockShapeRecipe("SteepSlope", 1, 1, 2);
        addPlankBlockShapeRecipe("QuarterSlope", 1, 8, 2);

        addPlankBlockShapeRecipe("HalfBlock", 1, 2, 1);
        addPlankBlockShapeRecipe("EighthBlock", 1, 8, 1);
        addPlankBlockShapeRecipe("HalfSlope", 1, 4, 2);
        addPlankBlockShapeRecipe("HalfSlopeCorner", 1, 6, 1);

        addPlankBlockShapeRecipe("PillarTop", "StructuralResources", 1, 1, 2);
        addPlankBlockShapeRecipe("Pillar", "StructuralResources", 1, 1, 2);
        addPlankBlockShapeRecipe("PillarBase", "StructuralResources", 1, 1, 2);
    }

    /**
     * Add all of the CraftInHand/NatureCrafting recipes to the independent CraftInHandRecipeRegistry.
     */
    private void addCraftInHandRecipes() {
        // Add a NatureCrafting recipe for collecting seeds from a seeding fruit.
        addCraftInHandRecipe("Woodcrafting:SeedingFruits", new SeedingFruitRecipe());

        // Iterate through all the CraftInHand/NatureCrafting recipes and parse their details.
        for (Prefab prefab : prefabManager.listPrefabs(CraftInHandRecipeComponent.class)) {
            parseCraftInHandRecipe(prefab.getComponent(CraftInHandRecipeComponent.class));
        }
    }

    private void addShapeRecipe(String processType, int processLevel, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount, shape,
                "engine", ingredientMultiplier, resultMultiplier, toolDurabilityMultiplier);
    }

    private void addShapeRecipe(String processType, int processLevel, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                String tool, int toolDurability, String blockResultPrefix, int blockResultCount,
                                String shape, String module, int ingredientMultiplier, int resultMultiplier, int toolDurabilityMultiplier) {
        DefaultWorkstationRecipe shapeRecipe = new DefaultWorkstationRecipe();
        shapeRecipe.addIngredient(ingredient, ingredientBasicCount * ingredientMultiplier);
        shapeRecipe.addRequiredTool(tool, toolDurability * toolDurabilityMultiplier);
        shapeRecipe.setResultFactory(new BlockRecipeResultFactory(blockManager.getBlockFamily(blockResultPrefix + ":" + module + ":" + shape).getArchetypeBlock(),
                blockResultCount * resultMultiplier));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, processLevel, recipeNamePrefix + shape, shapeRecipe, null, entityManager));
    }

    private void addWorkstationBlockShapesRecipe(String processType, int processLevel, String recipeNamePrefix, String ingredient, int ingredientBasicCount,
                                                 String tool, int toolDurability, String blockResultPrefix, int blockResultCount) {
        DefaultWorkstationRecipe fullBlockRecipe = new DefaultWorkstationRecipe();
        fullBlockRecipe.addIngredient(ingredient, ingredientBasicCount);
        fullBlockRecipe.addRequiredTool(tool, toolDurability);
        fullBlockRecipe.setResultFactory(new BlockRecipeResultFactory(blockManager.getBlockFamily(blockResultPrefix).getArchetypeBlock(), blockResultCount));

        workstationRegistry.registerProcess(processType, new CraftingWorkstationProcess(processType, recipeNamePrefix, fullBlockRecipe, null, entityManager));

        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Stair", 3, 4, 2);

        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Slope", 1, 2, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "UpperHalfSlope", 1, 2, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SlopeCorner", 1, 2, 2);

        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "SteepSlope", 1, 1, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "QuarterSlope", 1, 8, 2);

        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfBlock", 1, 2, 1);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "EighthBlock", 1, 8, 1);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlope", 1, 4, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "HalfSlopeCorner", 1, 6, 1);

        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarTop", "structuralResources", 1, 1, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "Pillar", "structuralResources", 1, 1, 2);
        addShapeRecipe(processType, processLevel, recipeNamePrefix, ingredient, ingredientBasicCount, tool, toolDurability, blockResultPrefix, blockResultCount,
                "PillarBase", "structuralResources", 1, 1, 2);
    }

    private void parseCraftInHandRecipe(CraftInHandRecipeComponent recipeComponent) {
        String recipeId = recipeComponent.recipeId;
        RecipeResultFactory resultFactory;
        if (recipeComponent.blockResult != null) {
            resultFactory = new BlockRecipeResultFactory(blockManager.getBlockFamily(recipeComponent.blockResult).getArchetypeBlock(), 1);
        } else {
            resultFactory = new ItemRecipeResultFactory(prefabManager.getPrefab(recipeComponent.itemResult), 1);
        }
        CompositeTypeBasedCraftInHandRecipe recipe = new CompositeTypeBasedCraftInHandRecipe(resultFactory);

        if (recipeComponent.recipeComponents != null) {
            for (String component : recipeComponent.recipeComponents) {
                String[] split = component.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ConsumeItemCraftBehaviour(new CraftInHandIngredientPredicate(type), count, PlayerInventorySlotResolver.singleton()));
            }
        }
        if (recipeComponent.recipeTools != null) {
            for (String tool : recipeComponent.recipeTools) {
                String[] split = tool.split("\\*");
                int durability = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new ReduceDurabilityCraftBehaviour(new CraftInHandIngredientPredicate(type), durability,
                        PlayerInventorySlotResolver.singleton()));
            }
        }
        if (recipeComponent.recipeActivators != null) {
            for (String activator : recipeComponent.recipeActivators) {
                String[] split = activator.split("\\*");
                int count = Integer.parseInt(split[0]);
                String type = split[1];
                recipe.addItemCraftBehaviour(new PresenceItemCraftBehaviour(new CraftInHandIngredientPredicate(type), count, PlayerInventorySlotResolver.singleton()));
            }
        }
        addCraftInHandRecipe(recipeId, recipe);
    }

    private void addCraftInHandRecipe(String recipeId, CraftInHandRecipe craftInHandRecipe) {
        recipeRegistry.addCraftInHandRecipe(recipeId, craftInHandRecipe);
    }

    private final class StationTypeFilter implements Predicate<EntityRef> {
        private String stationType;

        private StationTypeFilter(String stationType) {
            this.stationType = stationType;
        }

        @Override
        public boolean apply(EntityRef entity) {
            CraftingStationMaterialComponent stationMaterial = entity.getComponent(CraftingStationMaterialComponent.class);
            return stationMaterial != null && stationMaterial.stationType.equals(stationType);
        }
    }
}
