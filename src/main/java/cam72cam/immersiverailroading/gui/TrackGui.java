package cam72cam.immersiverailroading.gui;

import java.io.IOException;
import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import cam72cam.immersiverailroading.ImmersiveRailroading;
import cam72cam.immersiverailroading.items.ItemTrackBlueprint;
import cam72cam.immersiverailroading.items.nbt.ItemGauge;
import cam72cam.immersiverailroading.library.GuiText;
import cam72cam.immersiverailroading.library.Gauge;
import cam72cam.immersiverailroading.library.TrackItems;
import cam72cam.immersiverailroading.library.TrackPositionType;
import cam72cam.immersiverailroading.net.ItemRailUpdatePacket;
import cam72cam.immersiverailroading.tile.TileRailPreview;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.init.Items;
import net.minecraftforge.fml.client.config.GuiCheckBox;
import net.minecraftforge.fml.client.config.GuiSlider;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.StringUtils;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TrackGui extends GuiScreen {
	private GuiButton typeButton;
	private GuiTextField lengthInput;
	private GuiSlider quartersSlider;
	private GuiCheckBox isPreviewCB;
	private GuiButton gaugeButton;

	private int slot;
	private int length;
	private int quarters;
	private Gauge gauge;
	private boolean isPreview;
	private TrackItems type;
	private TrackPositionType posType;
	private GuiButton posTypeButton;
	private GuiButton bedTypeButton;
	private ItemPickerGUI bedSelector;
	private GuiButton bedFillButton;
	private ItemPickerGUI bedFillSelector;

	private final Predicate<String> integerFilter = new Predicate<String>() {
		public boolean apply(@Nullable String inputString) {
			if (StringUtils.isNullOrEmpty(inputString)) {
				return true;
			}
			int val;
			try {
				val = Integer.parseInt(inputString);
			} catch (NumberFormatException e) {
				return false;
			}
			return val > 0 && val <= 100;
		}
	};
	private BlockPos tilePreviewPos;

	public TrackGui() {
		slot = Minecraft.getMinecraft().player.inventory.currentItem;
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		init(stack);
	}

	public TrackGui(World world, int posX, int posY, int posZ) {
		this.tilePreviewPos = new BlockPos(posX, posY, posZ);
		TileRailPreview te = TileRailPreview.get(world, tilePreviewPos);
		if (te != null) {
			init(te.getItem());
		}
	}

	private void init(ItemStack stack) {
		stack = stack.copy();
		length = ItemTrackBlueprint.getLength(stack);
		quarters = ItemTrackBlueprint.getQuarters(stack);
		type = ItemTrackBlueprint.getType(stack);
		gauge = ItemGauge.get(stack);
		posType = ItemTrackBlueprint.getPosType(stack);
		isPreview = ItemTrackBlueprint.isPreview(stack);
		NonNullList<ItemStack> oreDict = NonNullList.create();
		
		oreDict.add(new ItemStack(Items.AIR));
		
		for (ItemStack ore : OreDictionary.getOres(ImmersiveRailroading.ORE_RAIL_BED)) {
			if (ore.getItemDamage() == OreDictionary.WILDCARD_VALUE) {
				ore.getItem().getSubItems(null, ore.getItem().getCreativeTab(), oreDict);
			} else {
				oreDict.add(ore);
			}
		}
		bedSelector = new ItemPickerGUI(oreDict, (ItemStack bed) -> {
			bedTypeButton.displayString = GuiText.SELECTOR_RAIL_BED.toString(getBedstackName());
			this.mc.displayGuiScreen(this);
		});
		bedSelector.choosenItem = ItemTrackBlueprint.getBed(stack);
		bedFillSelector = new ItemPickerGUI(oreDict, (ItemStack bed) -> {
			bedTypeButton.displayString = GuiText.SELECTOR_RAIL_BED.toString(getBedstackName());
			this.mc.displayGuiScreen(this);
		});
		bedFillSelector.choosenItem = ItemTrackBlueprint.getBedFill(stack);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
        this.lengthInput.drawTextBox();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}
	
	@Override
	public void updateScreen()
    {
        this.lengthInput.updateCursorCounter();
    }

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}
	
	public void setWorldAndResolution(Minecraft mc, int width, int height) {
		super.setWorldAndResolution(mc, width, height);
		bedSelector.setWorldAndResolution(mc, width, height);
		bedFillSelector.setWorldAndResolution(mc, width, height);
	}
	
	public void setGuiSize(int w, int h) {
		this.setGuiSize(w, h);
		bedSelector.setGuiSize(w, h);
		bedFillSelector.setGuiSize(w, h);
	}
	
	public String getBedstackName() {
		if (bedSelector.choosenItem.getItem() != Items.AIR) {
			return bedSelector.choosenItem.getDisplayName();
		}
		return GuiText.NONE.toString();
	}
	public String getBedFillName() {
		if (bedFillSelector.choosenItem.getItem() != Items.AIR) {
			return bedFillSelector.choosenItem.getDisplayName();
		}
		return GuiText.NONE.toString();
	}

	public void initGui() {
		int buttonID = 0;

		typeButton = new GuiButton(buttonID++, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_TYPE.toString(type));
		this.buttonList.add(typeButton);

		this.lengthInput = new GuiTextField(buttonID++, this.fontRenderer, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, 200, 20);
		this.lengthInput.setText("" + length);
		this.lengthInput.setMaxStringLength(3);
		this.lengthInput.setValidator(this.integerFilter);
		this.lengthInput.setFocused(true);

		this.quartersSlider = new GuiSlider(buttonID++, this.width / 2 - 75, this.height / 8 - 24 + buttonID * 30, "", 1, 4, quarters,
				null) {
			@Override
			public void updateSlider() {
				super.updateSlider();
				displayString = GuiText.SELECTOR_QUARTERS.toString(this.getValueInt() * (90.0/4));
			}
		};
		quartersSlider.updateSlider();
		quartersSlider.showDecimal = false;
		quartersSlider.visible = type == TrackItems.SWITCH || type == TrackItems.TURN;
		this.buttonList.add(quartersSlider);
		
		bedTypeButton = new GuiButton(buttonID++, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_RAIL_BED.toString(getBedstackName()));
		this.buttonList.add(bedTypeButton);

		bedFillButton = new GuiButton(buttonID++, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_RAIL_BED_FILL.toString(getBedFillName()));
		this.buttonList.add(bedFillButton);
		
		posTypeButton = new GuiButton(buttonID++, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_POSITION.toString(posType));
		this.buttonList.add(posTypeButton);
		
		isPreviewCB = new GuiCheckBox(buttonID++, this.width / 2 - 75, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_PLACE_BLUEPRINT.toString(), isPreview);
		this.buttonList.add(isPreviewCB);
		
		gaugeButton = new GuiButton(buttonID++, this.width / 2 - 100, this.height / 8 - 24 + buttonID * 30, GuiText.SELECTOR_GAUGE.toString(gauge));
		this.buttonList.add(gaugeButton);
		
		bedSelector.initGui();
	}

	protected void actionPerformed(GuiButton button) throws IOException {
		if (button == typeButton) {
			type =  TrackItems.values()[((type.ordinal() + 1) % (TrackItems.values().length))];
			typeButton.displayString = GuiText.SELECTOR_TYPE.toString(type);
			quartersSlider.visible = type == TrackItems.SWITCH || type == TrackItems.TURN;
		}
		if (button == gaugeButton) {
			gauge = Gauge.values()[((gauge.ordinal() + 1) % (Gauge.values().length))];
			gaugeButton.displayString = GuiText.SELECTOR_GAUGE.toString(gauge);
		}
		if (button == posTypeButton) {
			posType = TrackPositionType.values()[((posType.ordinal() + 1) % (TrackPositionType.values().length))];
			posTypeButton.displayString = GuiText.SELECTOR_POSITION.toString(posType);
		}
		if (button == bedTypeButton) {
			this.mc.displayGuiScreen(bedSelector);
		}
		if (button == bedFillButton) {
			this.mc.displayGuiScreen(bedFillSelector);
		}
		if (button == isPreviewCB) {
			isPreview = isPreviewCB.isChecked();
		}
	}
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.lengthInput.textboxKeyTyped(typedChar, keyCode);
        // Enter or ESC
        if (keyCode == 1 || keyCode == 28 || keyCode == 156) {
        	if (!this.lengthInput.getText().isEmpty()) {
        		if (this.tilePreviewPos != null) {
    				ImmersiveRailroading.net.sendToServer(
    						new ItemRailUpdatePacket(tilePreviewPos, Integer.parseInt(lengthInput.getText()), quartersSlider.getValueInt(), type, gauge.value(), posType, bedSelector.choosenItem, bedFillSelector.choosenItem, isPreview));
        		} else {
				ImmersiveRailroading.net.sendToServer(
						new ItemRailUpdatePacket(slot, Integer.parseInt(lengthInput.getText()), quartersSlider.getValueInt(), type, gauge.value(), posType, bedSelector.choosenItem, bedFillSelector.choosenItem, isPreview));
        		}
        	}
			this.mc.displayGuiScreen(null);
			if (this.mc.currentScreen == null)
				this.mc.setIngameFocus();
        }
	}
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException
    {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.lengthInput.mouseClicked(mouseX, mouseY, mouseButton);
    }
}
