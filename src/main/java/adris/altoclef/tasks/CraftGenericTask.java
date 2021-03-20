package adris.altoclef.tasks;

import adris.altoclef.AltoClef;
import adris.altoclef.Debug;
import adris.altoclef.tasksystem.Task;
import adris.altoclef.util.CraftingRecipe;
import adris.altoclef.util.ItemTarget;
import adris.altoclef.util.csharpisbetter.Timer;
import adris.altoclef.util.csharpisbetter.Util;
import adris.altoclef.util.slots.CraftingTableSlot;
import adris.altoclef.util.slots.PlayerSlot;
import adris.altoclef.util.slots.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CraftGenericTask extends Task {

    private final CraftingRecipe _recipe;
    private Timer _invTimer;

    public CraftGenericTask(CraftingRecipe recipe) {
        _recipe = recipe;
    }

    @Override
    protected void onStart(AltoClef mod) {

    }

    @Override
    protected Task onTick(AltoClef mod) {
        if (_invTimer == null) {
            _invTimer = new Timer(mod.getModSettings().getContainerItemMoveDelay());
        } else {
            _invTimer.setInterval(mod.getModSettings().getContainerItemMoveDelay());
        }
        boolean delayedCraft = (_invTimer.getDuration() > 0);

        if (!_invTimer.elapsed()) {
            // Each "tick" past here is one operation.
            // Wait until timer comes back.
            return null;
        } else {
            _invTimer.reset();
        }

        boolean bigCrafting = (mod.getPlayer().currentScreenHandler instanceof CraftingScreenHandler);

        if (!bigCrafting) {
            if (!(mod.getPlayer().currentScreenHandler instanceof PlayerScreenHandler)) {
                // Make sure we're not in another screen before we craft,
                // otherwise crafting will be むだな、ぞ
                mod.getPlayer().closeHandledScreen();
                // Just to be safe
                if (delayedCraft) return null;
            }
        }

        // For each slot in table
        for (int craftSlot = 0; craftSlot < _recipe.getSlotCount(); ++craftSlot) {
            ItemTarget toFill = _recipe.getSlot(craftSlot);
            Slot currentCraftSlot;
            if (bigCrafting) {
                // Craft in table
                currentCraftSlot = CraftingTableSlot.getInputSlot(craftSlot, _recipe.isBig());
            } else {
                // Craft in window
                currentCraftSlot = PlayerSlot.getCraftInputSlot(craftSlot);
            }
            ItemStack present = mod.getInventoryTracker().getItemStackInSlot(currentCraftSlot);
            if (toFill.isEmpty()) {
                if (present.getItem() != Items.AIR) {
                    // Move this item OUT if it should be empty
                    mod.getInventoryTracker().throwSlot(currentCraftSlot);
                    if (delayedCraft) return null;
                }
            } else {
                boolean isSatisfied = toFill.matches(present.getItem());
                if (!isSatisfied) {
                    List<Integer> validSlots = mod.getInventoryTracker().getInventorySlotsWithItem(toFill.getMatches());
                    if (validSlots.size() == 0) {
                        Debug.logWarning("Does not have materials necessary for slot " + craftSlot + " for recipe. Craft failed.");
                        // TODO: Cancel/fail
                        return null;
                    }
                    int itemSlot = validSlots.get(0);
                    Slot itemToMove = Slot.getFromInventory(itemSlot);
                    // Satisfy this current slot.
                    //Debug.logMessage("NEEDS: " + toFill + " : FOUND: " + mod.getInventoryTracker().getItemStackInSlot(itemToMove).getItem().getTranslationKey());
                    //Debug.logMessage("Moving: " + itemToMove.getWindowSlot() + " -> " + currentCraftSlot.getWindowSlot());
                    mod.getInventoryTracker().moveItems(itemToMove, currentCraftSlot, 1);
                    if (delayedCraft) return null;
                }
            }
        }

        Slot outputSlot = bigCrafting? CraftingTableSlot.OUTPUT_SLOT : PlayerSlot.CRAFT_OUTPUT_SLOT;

        //Debug.logMessage("RECEIVING CRAFTING OUTPUT: " + bigCrafting);
        // Swap to inventory hotbar
        // This should only be one call to clickSlot, but it's two calls for some reason?

        /*
        boolean movedClean = false;
        if (ResourceTask.ensureInventoryFree(mod)) {
            List<Integer> emptySlots = mod.getInventoryTracker().getEmptyInventorySlots();
            if (emptySlots.size() != 0) {
                Slot freeSlot = Slot.getFromInventory(emptySlots.get(0));
                assert freeSlot != null;
                Debug.logMessage("MOVED: " + outputSlot.getWindowSlot() + " -> " + freeSlot.getWindowSlot());
                movedClean = mod.getInventoryTracker().moveItems(outputSlot, freeSlot, 1) == 1;
                if (!movedClean) {
                    Debug.logWarning("Failed to receive output from inventory craft! Throwing craft output item.");
                }
            } else {
                Debug.logWarning("Failed to find free spot in inventory! Throwing craft output item.");
            }
        } else {
            Debug.logWarning("Failed to free up inventory for craft! Throwing craft output item.");
        }
        if (!movedClean) {
            mod.getInventoryTracker().throwSlot(outputSlot);
        }*/

        mod.getInventoryTracker().clickSlot(outputSlot, 0, SlotActionType.QUICK_MOVE);
        //mod.getInventoryTracker().clickSlot(outputSlot, 2, SlotActionType.SWAP);

        return null;
    }

    @Override
    protected void onStop(AltoClef mod, Task interruptTask) {

    }

    @Override
    protected boolean isEqual(Task obj) {
        if (obj instanceof CraftGenericTask) {
            return ((CraftGenericTask)obj)._recipe.equals(_recipe);
        }
        return false;
    }

    @Override
    protected String toDebugString() {
        return "Crafting " + _recipe.toString();
    }
}