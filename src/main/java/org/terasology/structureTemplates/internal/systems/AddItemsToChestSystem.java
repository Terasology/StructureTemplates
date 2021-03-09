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
package org.terasology.structureTemplates.internal.systems;

import org.joml.Vector3i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.terasology.engine.entitySystem.entity.EntityManager;
import org.terasology.engine.entitySystem.entity.EntityRef;
import org.terasology.engine.entitySystem.event.ReceiveEvent;
import org.terasology.engine.entitySystem.prefab.Prefab;
import org.terasology.engine.entitySystem.systems.BaseComponentSystem;
import org.terasology.engine.entitySystem.systems.RegisterMode;
import org.terasology.engine.entitySystem.systems.RegisterSystem;
import org.terasology.engine.logic.inventory.InventoryManager;
import org.terasology.engine.logic.inventory.ItemComponent;
import org.terasology.engine.registry.In;
import org.terasology.engine.world.BlockEntityRegistry;
import org.terasology.engine.world.block.BlockComponent;
import org.terasology.engine.world.block.BlockManager;
import org.terasology.engine.world.block.family.BlockFamily;
import org.terasology.engine.world.block.items.BlockItemComponent;
import org.terasology.engine.world.block.items.BlockItemFactory;
import org.terasology.structureTemplates.components.AddItemsToChestComponent;
import org.terasology.structureTemplates.events.BuildStructureTemplateEntityEvent;
import org.terasology.structureTemplates.events.SpawnTemplateEvent;
import org.terasology.structureTemplates.events.StructureBlocksSpawnedEvent;
import org.terasology.structureTemplates.internal.events.BuildStructureTemplateStringEvent;
import org.terasology.structureTemplates.util.BlockRegionTransform;
import org.terasology.structureTemplates.util.ListUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * System to power the {@link AddItemsToChestComponent}
 */
@RegisterSystem(RegisterMode.AUTHORITY)
public class AddItemsToChestSystem extends BaseComponentSystem {
    private static final Logger logger = LoggerFactory.getLogger(ScheduledStructureSpawnSystem.class);

    @In
    private BlockEntityRegistry blockEntityRegistry;

    @In
    private EntityManager entityManager;

    @In
    private InventoryManager inventoryManager;

    @In
    private BlockManager blockManager;

    @ReceiveEvent
    public void onSpawnStructureEvent(StructureBlocksSpawnedEvent event, EntityRef entity,
                                      AddItemsToChestComponent component) {
        BlockRegionTransform transformation = event.getTransformation();

        addItemsToChest(component, transformation);
    }

    @ReceiveEvent
    public void onSpawnTemplateEvent(SpawnTemplateEvent event, EntityRef entity, AddItemsToChestComponent component) {
        BlockRegionTransform transformation = event.getTransformation();

        addItemsToChest(component, transformation);
    }

    private void addItemsToChest(AddItemsToChestComponent component, BlockRegionTransform transformation) {
        BlockItemFactory blockFactory = new BlockItemFactory(entityManager);

        for (AddItemsToChestComponent.ChestToFill chestToFill : component.chestsToFill) {
            Vector3i absolutePosition = transformation.transformVector3i(chestToFill.position);
            EntityRef chest = blockEntityRegistry.getBlockEntityAt(absolutePosition);
            for (AddItemsToChestComponent.Item item : chestToFill.items) {
                addItemToChest(chest, item, blockFactory);
            }
        }
    }

    private void addItemToChest(EntityRef chest, AddItemsToChestComponent.Item item, BlockItemFactory blockFactory) {
        EntityRef itemEntity;
        if (item.itemPrefab != null) {
            itemEntity = entityManager.create(item.itemPrefab);
        } else if (item.blockFamiliy != null) {
            itemEntity = blockFactory.newInstance(item.blockFamiliy, item.amount);
        } else {
            logger.warn("Can't add item to chest as neither blockFamily nor itemPrefab has been defined");
            return;
        }
        if (item.slot != null) {
            inventoryManager.giveItem(chest, EntityRef.NULL, itemEntity, item.slot);
        } else {
            inventoryManager.giveItem(chest, EntityRef.NULL, itemEntity);
        }
    }


    @ReceiveEvent
    public void onBuildTemplateWithScheduledStructurePlacment(BuildStructureTemplateEntityEvent event, EntityRef entity) {
        BlockRegionTransform transformToRelative = event.getTransformToRelative();
        BlockFamily blockFamily = blockManager.getBlockFamily("CoreAdvancedAssets:Chest");

        List<AddItemsToChestComponent.ChestToFill> chestsToFill = describeChestContent(event, blockFamily);
        if (chestsToFill.size() > 0) {
            AddItemsToChestComponent addItemsComponent = new AddItemsToChestComponent();
            addItemsComponent.chestsToFill = chestsToFill;
            event.getTemplateEntity().addOrSaveComponent(addItemsComponent);
        }
    }

    private List<AddItemsToChestComponent.ChestToFill> describeChestContent(BuildStructureTemplateEntityEvent event, BlockFamily blockFamily) {
        List<AddItemsToChestComponent.ChestToFill> chestsToFill = new ArrayList<>();
        for (Vector3i position : event.findAbsolutePositionsOf(blockFamily)) {
            EntityRef blockEntity = blockEntityRegistry.getBlockEntityAt(position);
            BlockComponent blockComponent = blockEntity.getComponent(BlockComponent.class);
            List<AddItemsToChestComponent.Item> itemsToAdd = describeItemsOfEntity(blockEntity);
            if (itemsToAdd.size() > 0) {
                AddItemsToChestComponent.ChestToFill chestToFill = new AddItemsToChestComponent.ChestToFill();
                Vector3i absolutePosition = blockComponent.getPosition(new Vector3i());
                Vector3i relativePosition = event.getTransformToRelative().transformVector3i(absolutePosition);
                chestToFill.position = relativePosition;
                chestToFill.items = itemsToAdd;
                chestsToFill.add(chestToFill);
            }
        }
        return chestsToFill;
    }

    private List<AddItemsToChestComponent.Item> describeItemsOfEntity(EntityRef blockEntity) {
        List<AddItemsToChestComponent.Item> itemsToAdd = new ArrayList<>();
        int numberOfSlots = inventoryManager.getNumSlots(blockEntity);
        for (int slot = 0; slot < numberOfSlots; slot++) {
            Optional<AddItemsToChestComponent.Item> optionalItem = describeItemInSlot(blockEntity, slot);
            if (optionalItem.isPresent()) {
                itemsToAdd.add(optionalItem.get());
            }
        }
        return itemsToAdd;
    }


    private Optional<AddItemsToChestComponent.Item> describeItemInSlot(EntityRef blockEntity, int slot) {
        EntityRef item = inventoryManager.getItemInSlot(blockEntity, slot);
        ItemComponent itemComponent = item.getComponent(ItemComponent.class);
        if (itemComponent == null) {
            return Optional.empty();
        }
        BlockItemComponent blockItemComponent = item.getComponent(BlockItemComponent.class);
        if (blockItemComponent != null) {
            AddItemsToChestComponent.Item itemVO = new AddItemsToChestComponent.Item();
            itemVO.blockFamiliy = blockItemComponent.blockFamily;
            itemVO.amount = itemComponent.stackCount;
            itemVO.slot = slot;
            return Optional.of(itemVO);
        } else {
            Prefab prefab = item.getParentPrefab();
            AddItemsToChestComponent.Item itemVO = new AddItemsToChestComponent.Item();
            itemVO.itemPrefab = prefab;
            itemVO.amount = itemComponent.stackCount;
            itemVO.slot = slot;
            return Optional.of(itemVO);
        }
    }

    @ReceiveEvent
    public void onBuildTemplateStringWithBlockRegions(BuildStructureTemplateStringEvent event, EntityRef template,
                                                      AddItemsToChestComponent component) {
        StringBuilder sb = new StringBuilder();
        sb.append("    \"AddItemsToChest\": {\n");
        sb.append("        \"chestsToFill\": [\n");
        ListUtil.visitList(component.chestsToFill, (AddItemsToChestComponent.ChestToFill chestToFill, boolean lastChest) -> {
            sb.append("            {\n");
            sb.append("                \"position\": [");
            sb.append(chestToFill.position.x);
            sb.append(", ");
            sb.append(chestToFill.position.y);
            sb.append(", ");
            sb.append(chestToFill.position.z);
            sb.append("],\n");
            sb.append("                \"items\": [\n");
            ListUtil.visitList(chestToFill.items, (AddItemsToChestComponent.Item item, boolean lastItem) -> {
                sb.append("                        ");
                appendItemJson(sb, item);
                if (lastItem) {
                    sb.append("\n");
                } else {
                    sb.append(",\n");
                }
            });
            sb.append("                ]\n");
            if (lastChest) {
                sb.append("            }\n");
            } else {
                sb.append("            },\n");
            }
        });
        sb.append("        ]\n");
        sb.append("    }");
        event.addJsonForComponent(sb.toString(), AddItemsToChestComponent.class);
    }

    private void appendItemJson(StringBuilder sb, AddItemsToChestComponent.Item item) {
        boolean firstProperty = true;
        sb.append("{");
        if (item.slot != null) {
            sb.append("\"slot\": ");
            sb.append(item.slot);
            firstProperty = false;
        }
        if (item.itemPrefab != null) {
            if (!firstProperty) {
                sb.append(", ");
            }
            sb.append("\"itemPrefab\": \"");
            sb.append(item.itemPrefab.getUrn().toString());
            sb.append("\"");
            firstProperty = false;
        }
        if (item.blockFamiliy != null) {
            if (!firstProperty) {
                sb.append(", ");
            }
            sb.append("\"blockFamiliy\": \"");
            sb.append(item.blockFamiliy.getURI().toString());
            sb.append("\"");
            firstProperty = false;
        }
        if (item.amount != 1) {
            if (!firstProperty) {
                sb.append(", ");
            }
            sb.append("\"amount\": ");
            sb.append(item.amount);
        }
        sb.append("}");
    }

}
