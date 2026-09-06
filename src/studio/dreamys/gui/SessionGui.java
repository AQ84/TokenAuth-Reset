package studio.dreamys.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.Color;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.Session;
import org.apache.commons.io.IOUtils;
import org.lwjgl.input.Keyboard;
import studio.dreamys.TokenAuth;

/**
 * SessionGui — TokenAuth reset version. Fixed the modern-token (JWT) path:
 * the previous code sent a POST to ".../minecraft/profile/" (trailing slash),
 * which made api.minecraftservices.com return a non-200/empty body and threw
 * NoSuchElementException. Now it GETs ".../minecraft/profile" (no slash) with a
 * Bearer header and reads {name, id} from the 200 response.
 */
public class SessionGui extends GuiScreen {

    private GuiScreen previousScreen;
    private String status = "Session:";
    private GuiTextField sessionField;
    private ScaledResolution sr;

    public SessionGui(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void func_73866_w_() {
        Keyboard.enableRepeatEvents(true);
        this.sr = new ScaledResolution(this.field_146297_k);
        this.sessionField = new GuiTextField(1, this.field_146297_k.field_71466_p, this.sr.func_78326_a() / 2 - 100, this.sr.func_78328_b() / 2, 200, 20);
        this.sessionField.func_146203_f(Short.MAX_VALUE);
        this.sessionField.func_146195_b(true);
        this.field_146292_n.add(new GuiButton(998, this.sr.func_78326_a() / 2 - 100, this.sr.func_78328_b() / 2 + 30, 200, 20, "Login"));
        this.field_146292_n.add(new GuiButton(999, this.sr.func_78326_a() / 2 - 100, this.sr.func_78328_b() / 2 + 60, 200, 20, "Restore"));
        super.func_73866_w_();
    }

    @Override
    public void func_146281_b() {
        Keyboard.enableRepeatEvents(false);
        super.func_146281_b();
    }

    @Override
    public void func_73863_a(int mouseX, int mouseY, float partialTicks) {
        this.func_146276_q_();
        this.field_146297_k.field_71466_p.func_78276_b(this.status, this.sr.func_78326_a() / 2 - this.field_146297_k.field_71466_p.func_78256_a(this.status) / 2, this.sr.func_78328_b() / 2 - 30, Color.WHITE.getRGB());
        this.sessionField.func_146194_f();
        super.func_73863_a(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void func_146284_a(GuiButton button) throws IOException {
        if (button.field_146127_k == 998) {
            try {
                String token;
                String uuid;
                String username;
                String session = this.sessionField.func_146179_b();
                if (session.contains(":")) {
                    username = session.split(":")[0];
                    uuid = session.split(":")[1];
                    token = session.split(":")[2];
                } else {
                    // Modern Minecraft access token (JWT): GET official profile endpoint
                    // with a Bearer header. No trailing slash, GET not POST.
                    HttpURLConnection c = (HttpURLConnection) new URL("https://api.minecraftservices.com/minecraft/profile").openConnection();
                    c.setRequestProperty("Content-type", "application/json");
                    c.setRequestProperty("Authorization", "Bearer " + this.sessionField.func_146179_b());
                    c.setRequestMethod("GET");
                    c.setDoOutput(false);
                    int code = c.getResponseCode();
                    if (code != 200) {
                        c.disconnect();
                        throw new IOException("Minecraft auth failed: HTTP " + code);
                    }
                    InputStream in = c.getInputStream();
                    JsonObject json = new JsonParser().parse(IOUtils.toString(in)).getAsJsonObject();
                    username = json.get("name").getAsString();
                    uuid = json.get("id").getAsString();
                    token = session;
                    c.disconnect();
                }
                this.setSession(new Session(username, uuid, token, "mojang"));
                this.field_146297_k.func_147108_a(this.previousScreen);
            } catch (Exception e) {
                this.status = "§cError: Couldn't set session (check mc logs)";
                e.printStackTrace();
            }
        }
        if (button.field_146127_k == 999) {
            try {
                this.setSession(TokenAuth.originalSession);
                this.field_146297_k.func_147108_a(this.previousScreen);
            } catch (Exception e) {
                this.status = "§cError: Couldn't restore session (check mc logs)";
                e.printStackTrace();
            }
        }
        super.func_146284_a(button);
    }

    @Override
    protected void func_73869_a(char typedChar, int keyCode) throws IOException {
        this.sessionField.func_146201_a(typedChar, keyCode);
        if (keyCode == 1) {
            this.field_146297_k.func_147108_a(this.previousScreen);
        } else {
            super.func_73869_a(typedChar, keyCode);
        }
    }

    /**
     * Set the Minecraft session via reflection — tries the MCP-named setter
     * (func_157129_a), then the deobf setSession, then falls back to writing the
     * field directly. Whichever the running Forge environment resolves, this works
     * without a hard compile-time method reference (which caused IllegalAccessError
     * when the field turned out to be non-public in the live mapping).
     */
    private void setSession(Session session) {
        try {
            java.lang.reflect.Method m = this.field_146297_k.getClass().getMethod("func_157129_a", Session.class);
            m.invoke(this.field_146297_k, session);
            return;
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Method m = this.field_146297_k.getClass().getMethod("setSession", Session.class);
            m.invoke(this.field_146297_k, session);
            return;
        } catch (Exception ignored) {
        }
        try {
            java.lang.reflect.Field f = this.field_146297_k.getClass().getField("field_71449_j");
            f.set(this.field_146297_k, session);
            return;
        } catch (Exception ignored) {
        }
        // Last resort: set via the MC session field through the accessor method.
        try {
            java.lang.reflect.Field f = this.field_146297_k.getClass().getDeclaredField("field_71449_j");
            f.setAccessible(true);
            f.set(this.field_146297_k, session);
        } catch (Exception ignored) {
        }
    }
}
