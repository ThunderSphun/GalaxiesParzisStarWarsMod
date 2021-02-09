package com.parzivail.pswg.client.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.parzivail.pswg.client.screen.widget.SimpleListWidget;
import com.parzivail.pswg.container.SwgSpeciesRegistry;
import com.parzivail.pswg.species.SpeciesVariable;
import com.parzivail.pswg.species.SwgSpecies;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;

import java.util.Optional;

@Environment(EnvType.CLIENT)
public class SpeciesSelectScreen extends Screen
{
	//	public static final Identifier BACKGROUND = Resources.identifier("textures/block/");
	public static final Identifier BACKGROUND = new Identifier("textures/gui/options_background.png");

	private final Screen parent;

	private SimpleListWidget<SwgSpecies> speciesListWidget;
	private SimpleListWidget<SpeciesVariable> speciesVariableListWidget;
	private SimpleListWidget<String> speciesVariableValueListWidget;

	public SpeciesSelectScreen(Screen parent)
	{
		super(new TranslatableText("screen.pswg.species_select"));
		this.parent = parent;
	}

	private void updateAbility()
	{
	}

	@Override
	protected void init()
	{
		this.addButton(new ButtonWidget(this.width / 2 - 100, this.height - 32, 200, 20, ScreenTexts.DONE, (button) -> {
			this.client.openScreen(this.parent);
		}));

		speciesVariableValueListWidget = new SimpleListWidget<>(client, 310, 50, 120, 200, 15, entry -> {
			updateAbility();
		});
		speciesVariableValueListWidget.setEntrySelector(entries -> {
			SimpleListWidget.Entry<SpeciesVariable> selectedVariable = speciesVariableListWidget.getSelected();
			if (selectedVariable == null)
				return entries.get(0);

			SpeciesVariable variable = selectedVariable.getValue();
			String defaultValue = variable.getDefaultValue();
			Optional<SimpleListWidget.Entry<String>> entry = entries.stream().filter(stringEntry -> defaultValue.equals(stringEntry.getValue())).findFirst();
			return entry.orElseGet(() -> entries.get(0));
		});

		speciesVariableListWidget = new SimpleListWidget<>(client, 180, 50, 120, 200, 15, entry -> {
			if (entry != null)
				speciesVariableValueListWidget.setEntries(entry.getPossibleValues());
			else
				speciesVariableValueListWidget.clear();
			updateAbility();
		});
		speciesVariableListWidget.setEntryFormatter(var -> new TranslatableText(var.getName().toString()));

		speciesListWidget = new SimpleListWidget<>(client, 50, 50, 120, 200, 15, entry -> {
			if (entry != null)
				speciesVariableListWidget.setEntries(entry.getVariables());
			else
				speciesVariableListWidget.clear();
			updateAbility();
		});
		speciesListWidget.setEntryFormatter(species -> new TranslatableText(species.getSlug().toString()));

		this.children.add(speciesVariableValueListWidget);
		this.children.add(speciesVariableListWidget);
		this.children.add(speciesListWidget);

		speciesListWidget.setEntries(SwgSpeciesRegistry.getSpecies());
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers)
	{
		if (super.keyPressed(keyCode, scanCode, modifiers))
			return true;
		else if (this.speciesListWidget.getSelected() != null)
			return this.speciesListWidget.keyPressed(keyCode, scanCode, modifiers);
		else if (this.speciesVariableListWidget.getSelected() != null)
			return this.speciesVariableListWidget.keyPressed(keyCode, scanCode, modifiers);
		else if (this.speciesVariableValueListWidget.getSelected() != null)
			return this.speciesVariableValueListWidget.keyPressed(keyCode, scanCode, modifiers);
		else
			return false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount)
	{
		return this.speciesListWidget.mouseScrolled(mouseX, mouseY, amount) ||
		       this.speciesVariableListWidget.mouseScrolled(mouseX, mouseY, amount) ||
		       this.speciesVariableValueListWidget.mouseScrolled(mouseX, mouseY, amount);
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta)
	{
		this.renderBackground(matrices);
		this.speciesListWidget.render(matrices, mouseX, mouseY, delta);
		this.speciesVariableListWidget.render(matrices, mouseX, mouseY, delta);
		this.speciesVariableValueListWidget.render(matrices, mouseX, mouseY, delta);
		drawCenteredText(matrices, this.textRenderer, this.title, this.width / 2, 15, 16777215);
		super.render(matrices, mouseX, mouseY, delta);
	}

	public void renderBackgroundTexture(int vOffset)
	{
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		this.client.getTextureManager().bindTexture(BACKGROUND);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		float f = 32.0F;
		bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE_COLOR);
		bufferBuilder.vertex(0.0D, this.height, 0.0D).texture(0.0F, (float)this.height / 32.0F + (float)vOffset).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.width, this.height, 0.0D).texture((float)this.width / 32.0F, (float)this.height / 32.0F + (float)vOffset).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(this.width, 0.0D, 0.0D).texture((float)this.width / 32.0F, (float)vOffset).color(64, 64, 64, 255).next();
		bufferBuilder.vertex(0.0D, 0.0D, 0.0D).texture(0.0F, (float)vOffset).color(64, 64, 64, 255).next();
		tessellator.draw();
	}
}
