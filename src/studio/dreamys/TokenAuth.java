package studio.dreamys;

import java.awt.Color;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.Session;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import studio.dreamys.gui.SessionGui;

/**
 * TokenAuth — reset version.
 *
 * Rebuilt from the original TokenAuth-1.2.0 with the Discord webhook exfiltration
 * removed. The original preInit decoded a base64 Discord webhook URL and, on every
 * launch, POSTed the player's Microsoft access token + username + UUID to it — a
 * credential stealer. That block has been deleted; this mod now only:
 *   - adds a "TokenAuth" button to the Multiplayer screen
 *   - shows the current username/UUID
 *   - opens SessionGui to log in with an access token (legacy or modern JWT)
 * No data leaves the client.
 */
@Mod(modid = "ta", name = "TokenAuth", version = "1.2.0-reset")
public class TokenAuth {

    public static Minecraft mc = Minecraft.func_71410_x();
    public static Session originalSession = TokenAuth.mc.field_71449_j;

    @net.minecraftforge.fml.common.Mod.EventHandler
    public void preInit(FMLPreInitializationEvent e) {
        MinecraftForge.register(this);
        // Credential-stealing webhook exfiltration removed here.
    }

    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post e) {
        if (e.gui instanceof GuiMultiplayer) {
            e.buttonList.add(new GuiButton(999, 5, 5, 100, 20, "TokenAuth"));
        }
    }

    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post e) {
        if (e.gui instanceof GuiMultiplayer) {
            String status = String.format("User: §a%s §rUUID: §a%s", TokenAuth.mc.field_71449_j.func_111285_a(), TokenAuth.mc.field_71449_j.func_148255_b());
            Minecraft.func_71410_x().field_71466_p.func_78276_b(status, 115, 10, Color.WHITE.getRGB());
        }
    }

    @SubscribeEvent
    public void onActionPerformedPre(GuiScreenEvent.ActionPerformedEvent.Pre e) {
        if (e.gui instanceof GuiMultiplayer && e.button.field_146127_k == 999) {
            Minecraft.func_71410_x().func_147108_a((GuiScreen) new SessionGui(e.gui));
        }
    }
}
