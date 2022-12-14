package dev.ftb.ftbsbc.dimensions.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.prebuilt.PrebuiltStructure;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class StartSelectScreen extends Screen {
	// Store this so we don't have to ask the server for it again in-case we need to go back.
	public Consumer<PrebuiltStructure> onSelect;
	private StartList startList;
	private EditBox searchBox;
	private Button createButton;
	private AbstractTexture fallbackIcon;

	public StartSelectScreen(Consumer<PrebuiltStructure> onSelect) {
		super(TextComponent.EMPTY);
		this.onSelect = onSelect;
	}

	@Override
	protected void init() {
		super.init();

		this.startList = new StartList(this.getMinecraft(), this.width, this.height, 80, this.height - 40);
		this.searchBox = new EditBox(this.font, this.width / 2 - 160 / 2, 40, 160, 20, TextComponent.EMPTY);
		this.searchBox.setResponder(this.startList::searchList);

		this.addRenderableWidget(new Button(this.width / 2 - 130, this.height - 30, 100, 20, new TranslatableComponent("screens.ftbsbc.back"), btn -> onClose()));

		this.addRenderableWidget(this.createButton = new Button(this.width / 2 - 20, this.height - 30, 150, 20, new TranslatableComponent("screens.ftbsbc.create"), btn -> {
			if (this.startList.getSelected() == null) {
				return;
			}

			this.onSelect.accept(this.startList.getSelected().islandDir);
			if (this.getMinecraft().level != null) {
				this.onClose();
			}
		}));

		this.createButton.active = false;

		this.addWidget(this.searchBox);
		this.addWidget(this.startList);

		this.fallbackIcon = minecraft.getTextureManager().getTexture(PrebuiltStructure.DEFAULT_IMAGE);
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float partialTick) {
		this.startList.render(matrices, mouseX, mouseY, partialTick);
		this.searchBox.render(matrices, mouseX, mouseY, partialTick);

		super.render(matrices, mouseX, mouseY, partialTick);

		String value = new TranslatableComponent("screens.ftbsbc.select_start").getString();
		this.font.drawShadow(matrices, value, this.width / 2f - this.font.width(value) / 2f, 20, 0xFFFFFF);
	}

	public class StartList extends AbstractSelectionList<StartList.Entry> {
		public StartList(Minecraft minecraft, int width, int height, int top, int bottom) {
			super(minecraft, width, height, top, bottom, 50); // 30 = item height
			this.children().addAll(StoneBlockDataKjs.PREBUILT_STRUCTURES.values().stream().map(Entry::new).toList());
		}

		@Override
		public int getRowWidth() {
			return 340;
		}

		@Override
		protected int getScrollbarPosition() {
			return this.width / 2 + 170;
		}

		@Override
		public void setSelected(@Nullable StartSelectScreen.StartList.Entry entry) {
			StartSelectScreen.this.createButton.active = entry != null;
			super.setSelected(entry);
		}

		public void searchList(String value) {
			this.children().clear();

			String lowerValue = value.toLowerCase();
			if (lowerValue.equals("")) {
				this.children().addAll(StoneBlockDataKjs.PREBUILT_STRUCTURES.values().stream().map(Entry::new).toList());
				return;
			}

			this.children().addAll(StoneBlockDataKjs.PREBUILT_STRUCTURES.values().stream()
					.filter(island -> island.name.getString().toLowerCase().contains(lowerValue))
					.map(Entry::new).toList()
			);
		}

		@Override
		public void updateNarration(NarrationElementOutput arg) {

		}

		public class Entry extends AbstractSelectionList.Entry<Entry> {
			private final PrebuiltStructure islandDir;
			private long lastClickTime;

			public Entry(PrebuiltStructure island) {
				this.islandDir = island;
			}

			@Override
			public boolean mouseClicked(double x, double y, int partialTick) {
				StartList.this.setSelected(this);

				if (Util.getMillis() - this.lastClickTime < 250L) {
					StartSelectScreen.this.onClose();
					StartSelectScreen.this.onSelect.accept(this.islandDir);
					return true;
				} else {
					this.lastClickTime = Util.getMillis();
					return false;
				}
			}

			@Override
			public void render(PoseStack matrices, int entryId, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean bl, float partialTicks) {
				Font font = Minecraft.getInstance().font;

				int startX = left + 80;
				font.drawShadow(matrices, this.islandDir.name, startX, top + 10, 0xFFFFFF);
				font.drawShadow(matrices, new TranslatableComponent("screens.ftbsbc.by", this.islandDir.author), startX, top + 26, 0xD3D3D3);

				// Register the texture
				minecraft.getTextureManager().getTexture(this.islandDir.image);
				AbstractTexture texture = minecraft.getTextureManager().getTexture(this.islandDir.image, StartSelectScreen.this.fallbackIcon);
				RenderSystem.setShaderTexture(0, texture.getId());
				blit(matrices, left + 7, top + 7, 0f, 0f, 56, 32, 56, 32);
			}
		}
	}
}
