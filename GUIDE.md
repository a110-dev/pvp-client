# aleksa04_1: PvP client guide for Minecraft 26.3

Contents

1. Feature set and competitive defaults
2. Architecture: dependencies, Mod Menu, example code, frame-time rules
3. Competitive configuration: HUD layout, keybinds, video settings
4. What is built today vs. roadmap

Rules first: servers differ on which client mods they allow (zoom, toggle sprint, fullbright, keystroke
displays are the usual gray areas). Check your server's mod policy before enabling anything. Nothing in
aleksa04_1 touches combat mechanics, reach, hitboxes or aim.

For the current module list and control-center instructions, see `README.md`.

---

## 1. Feature set and recommended competitive defaults

### 1.1 Performance and FPS

The client stays lightweight and uses the public Fabric HUD API. Its optional Performance Profile also
reduces several vanilla graphics settings; actual FPS gains depend on the hardware and world.

Lithium, ImmediatelyFast, and FerriteCore are bundled as nested Fabric mods in the PvPClient 1.4.0 JAR.
Their licenses and matching source archives are included under `META-INF`. Do not also add separate
copies of those three to the instance's `mods` folder. Sodium and Entity Culling remain separate files;
install their official Fabric builds marked compatible with Minecraft 26.3 if desired. The client checks
whether optimization mods are loaded on its Performance tab:

| Mod | 26.3 version | Role |
|---|---|---|
| [Sodium](https://modrinth.com/mod/sodium) | Install separately; use the Fabric release marked 26.3-compatible | Optimized renderer |
| [Lithium](https://modrinth.com/mod/lithium) | Bundled: 0.26.1 Fabric | Game logic and ticking |
| [ImmediatelyFast](https://modrinth.com/mod/immediatelyfast) | Bundled: 1.17.1 Fabric | Immediate-mode and GUI rendering |
| [FerriteCore](https://modrinth.com/mod/ferrite-core) | Bundled: 9.0.0 Fabric | Memory use |
| [Entity Culling](https://modrinth.com/mod/entityculling) | Install separately; use the Fabric release marked 26.3-compatible | Skips hidden entities and block entities |
| Iris | Optional; check the 26.3 listing | Shader support; shader packs can reduce FPS |
| Mod Menu | 21.0.0 | Config entry point |

Recommended PvP stack: Fabric API, PvPClient 1.4.0 (with Lithium, ImmediatelyFast, and FerriteCore
nested inside), plus optional Sodium, Entity Culling, and Mod Menu. Not verified: Sodium Extra and similar
option add-ons for 26.3, so they are left out.

Memory and latency defaults:

| Setting | Default for PvP | Why |
|---|---|---|
| RAM (-Xms = -Xmx) | 3 to 4 GB | Equal min/max avoids heap resizing hitches; more RAM does not add FPS |
| GC | JVM default (G1) first; try `-XX:+UseZGC` if you see GC micro-stutters | ZGC trades a little throughput for very short pauses. Test on your machine |
| Max framerate | Unlimited | Lowest input latency |
| VSync | Off | VSync adds up to a frame of delay |
| Raw mouse input | On | Bypasses OS mouse smoothing |

### 1.2 PvP HUD elements

| Element | Default | Notes |
|---|---|---|
| Keystrokes | On, bottom right | WASD, LMB/RMB with CPS, space bar |
| CPS | On, top left | Frame-rate-accurate (sampled every frame, not every tick) |
| Ping | On, top left | Shows `--` in singleplayer or when unknown |
| FPS | On, top left | |
| Armor status | On, middle right | Durability number, colour turns yellow below 50 percent, red below 20 |
| Potion effects | On, middle left | Name, level, time left |
| Direction | On, bottom left | Cardinal facing plus yaw |
| Coordinates | On, bottom left | Turn off for streaming |
| FPS graph, clock, speed, dimension, world day, weather | Off | Optional readouts |
| Health, food/saturation, biome, held item, armor warning, memory | Off | Health and food can replace and move vanilla bars; armor warning appears at 20 percent durability or less |
| Custom crosshair | Off | Cross, dot, ring, adjustable gap, and attack bloom |
| Sneak status | Off | Shows whether the player is crouching |

Design rule: never put anything in the middle third of the screen. Your eyes live on the crosshair.

### 1.3 Customization and quality of life

| Feature | Status | Competitive default |
|---|---|---|
| Toggle Sprint | Built | On. Sprint stays on while W is held; K flips it on or off |
| Zoom | Built | Hold C, scroll to change FOV levels, and release for smooth restore |
| Corner placement per module | Built | See section 3.1 |
| Custom crosshair | Built | Cross, dot, and ring shapes with adjustable gap and bloom |
| Animated control center | Built | Eight themes, top tabs, search, settings, positions, and keybind capture |
| Per-module RGB, scale, and position | Built | Gear page plus drag-and-drop HUD editor |
| Screenshot gallery | Built | Preview, copy images to clipboard, and confirmed deletion |
| Quick reconnect | Built | Multiplayer pause and death screens, plus control center |
| Hit Color Flash | Built | Custom color pulse on attack input; no reach changes |
| Target Distance | Built | Read-only distance display; it does not change attack reach |
| HUD scale | Built | Scales every HUD module from 70 to 150 percent |
| Toggle feedback | Built | Animated toast confirms module changes |
| Performance profile | Built | Reversible low-effects graphics options |
| Particle multiplier | Roadmap | 0.25x to 0.5x. Until then, use the vanilla Particles option: Minimal |
| Fullbright | Roadmap, see below | Off unless your server allows it |
| Cosmetics | Roadmap, see below | Off by default |

Fullbright: the vanilla brightness slider cannot exceed 100 percent in current versions, so real fullbright
needs a lightmap mixin. That is the one piece that would add a mixin to this project and is the most
likely to break on game updates, which is why it is not in the first jar. Many competitive servers also ban it.

Cosmetics: capes, hats and so on are only visible to other players if you run a service that tells other
clients what to render. A purely local cosmetic (visible only to you) is easy and safe; shared cosmetics are a
backend project, not a mod feature. Do not reuse Mojang or OptiFine cape textures.

### 1.4 Fabric and mod integration

- Entry points: `client` (the mod) and `modmenu` (optional config button).
- One small client input mixin routes the wheel to zoom levels while the zoom key is held. The HUD uses the
  Fabric HUD API and zoom uses the public FOV option. The mixin does not change reach or attack behavior.
- Soft dependency on Mod Menu (`suggests`, compile-only), so the client still starts without it.

---

## 2. Architecture and starter code

### 2.1 Build dependencies (26.3)

`gradle.properties`:

```properties
minecraft_version=26.3
loader_version=0.19.5
loom_version=1.17.21
fabric_api_version=0.160.5+26.3
```

`build.gradle` (the 26.1+ differences from older guides: new Loom plugin id, no `mappings` line, plain
`implementation` instead of `modImplementation`, Java 25):

```groovy
plugins {
    id 'net.fabricmc.fabric-loom' version "${loom_version}"
}

repositories {
    maven { url = 'https://maven.terraformersmc.com/releases/' }
}

dependencies {
    minecraft "com.mojang:minecraft:${project.minecraft_version}"
    implementation "net.fabricmc:fabric-loader:${project.loader_version}"
    implementation "net.fabricmc.fabric-api:fabric-api:${project.fabric_api_version}"
    compileOnly "com.terraformersmc:modmenu:21.0.0"
}

tasks.withType(JavaCompile).configureEach { it.options.release = 25 }
```

Use Gradle 9.5.0 and JDK 25.

### 2.2 Project layout

```
src/main/java/dev/pvpclient/
  PvpClient.java          entrypoint: keys, tick loop, HUD registration
  Config.java             Gson, config/aleksa04_1.json
  ClickTracker.java       CPS sampling
  ZoomHandler.java        hold-to-zoom
  ModuleScreen.java       menu (also the Mod Menu screen)
  ModMenuIntegration.java Mod Menu entrypoint
  module/
    Module.java           base: id, name, enabled, anchor, width/height/render
    TextModule.java       one-line readouts
    ModuleManager.java    registry, persistence, corner layout
    Anchor.java           six screen anchors
    impl/                 Fps, Cps, Ping, Coords, Direction, ToggleSprint,
                          Keystrokes, Armor, Potions
```

Adding a feature = one class in `impl/` plus one line in `ModuleManager.init()`. Persistence, the menu row and
HUD layout come for free.

### 2.3 Mod Menu integration

`fabric.mod.json`:

```json
"entrypoints": {
  "client": ["dev.pvpclient.PvpClient"],
  "modmenu": ["dev.pvpclient.ModMenuIntegration"]
},
"suggests": { "modmenu": "*" }
```

`ModMenuIntegration.java`:

```java
public final class ModMenuIntegration implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return ModuleScreen::new;   // ModuleScreen(Screen parent) returns to Mod Menu on Done
    }
}
```

### 2.4 Keybind-triggered HUD overlay (Toggle Sprint)

Module (HUD line plus the logic):

```java
public final class ToggleSprintModule extends TextModule {
    public ToggleSprintModule() { super("sprint", "Toggle Sprint", Anchor.BOTTOM_LEFT, true); }

    @Override protected String line(Minecraft mc) { return "Sprint: Toggled"; }

    public void tick(Minecraft mc) {
        if (!enabled || mc.player == null || mc.screen != null) return;
        if (mc.options.keyUp.isDown()) mc.options.keySprint.setDown(true);
    }
}
```

Wiring in the entrypoint:

```java
KeyMapping sprintKey = KeyMappingHelper.registerKeyMapping(
        new KeyMapping("key.aleksa04_1.sprint", GLFW.GLFW_KEY_K, category));
ToggleSprintModule sprint = (ToggleSprintModule) ModuleManager.get("sprint");

ClientTickEvents.END_CLIENT_TICK.register(mc -> {
    while (sprintKey.consumeClick()) { sprint.enabled = !sprint.enabled; ModuleManager.save(); }
    sprint.tick(mc);
});
```

The CPS counter works the same way without a key: `ClickTracker.update()` is called once per rendered frame and
edge-detects `keyAttack` / `keyUse`.

### 2.5 Keeping frame times low and input lag out

1. Poll input per frame, not per tick, for anything you display. Ticks are 50 ms apart, which is why CPS here
   is sampled in the HUD callback.
2. Never block the render thread: no file or network I/O in HUD code. Config is read at startup and written only
   when the menu closes.
3. No per-frame allocations in the render path: cache strings and reuse lists.
4. Update slow data on a timer: ping, coordinates and durability can refresh every few ticks and be cached as a string.
5. Keep HUD draw calls few: one `fill` per box, no per-pixel loops, no per-frame texture or font creation.
6. No mixins unless a feature truly needs one. Each one is a compatibility risk with Sodium and friends and a
   breakage risk on every game update.
7. Do not touch vanilla options every frame. Zoom sets the FOV once on press and restores it once on release
   (and on shutdown, so the saved options never keep the zoomed FOV).
8. Run frame-time, not average-FPS, checks: look at 1 percent lows in the F3 profiler or a frametime graph.

Known costs in the current starter, to fix in the next pass:
- `ModuleManager.render` builds small lists every frame, and the text modules call `String.format` twice per
  frame (once for width, once for drawing). Both are cheap but easy to remove: compute lines once per tick and
  cache the layout until module state changes.

---

## 3. Competitive configuration guide for 26.3

### 3.1 HUD layout and positioning

```
+---------------------------------------------------------------+
| FPS                                                           |
| CPS                       (clear)                             |
| Ping                                                          |
|                                                               |
| Potion effects            + crosshair             Armor status|
|                                                               |
|                                                               |
| Direction                  [hotbar]                Keystrokes |
| Coordinates                                                   |
+---------------------------------------------------------------+
```

- Centre third stays empty. Hits, hurt flashes and the crosshair are what you read in a fight.
- Threat info (potions, armor) at the sides, at eye-level, so a glance catches it without moving the aim.
- Keystrokes bottom right, away from the hotbar and the vanilla chat (bottom left).
- GUI scale 2 or 3 at 1080p keeps the HUD readable without covering the arena.
- Vanilla status icons are top right, so nothing from this client is placed there by default.
- Stream or record: turn Coordinates and Direction off.

### 3.2 Keybind setups

These are suggestions. Everything is rebindable in Options -> Controls.

| Action | Suggested key | Reason |
|---|---|---|
| Move | W A S D | |
| Jump | Space | |
| Sprint | Ctrl (or Toggle Sprint, K to flip) | Toggle Sprint covers it while holding W |
| Sneak | Shift or left Alt | |
| Hotbar 1 | Sword | Your default slot after everything |
| Hotbar 2 | Golden apples / food | |
| Hotbar 3 | Fishing rod | Next to the sword so a rod swap is one finger tap |
| Hotbar 4 | Bow or pearl | |
| Hotbar 5 to 9 | Blocks, potions, utility | |
| Swap hand (shield / totem) | F | Default; shield in the off-hand, hold right click to block |
| Drop item | Unbind or move far from your movement keys | Avoids accidental drops mid-fight |
| Inventory | E | |
| Menu | Right Shift | |
| Zoom | C (hold) | |

Mouse side buttons (Mouse 4 / Mouse 5) work well for hotbar slots: you can switch to the rod or a pearl without
moving a finger off WASD.

Technique notes:
- W-tap: tap W off and on right after landing a hit so the next hit counts as a fresh sprint hit and applies full
  knockback. With Toggle Sprint on you only release and re-press W; you do not need to touch the sprint key.
- Rod: right-click throws, so keep the rod on a slot you can reach while holding W. Switch back to the sword
  immediately.
- Shield: right-click blocks, with an axe-wielding opponent shields can be disabled for a few seconds, so
  do not rely on holding it.

### 3.3 Video and rendering settings (max FPS, minimum delay)

Names can vary slightly between vanilla and Sodium's video screen; Sodium's screen replaces vanilla's once installed.

| Setting | Value |
|---|---|
| Max framerate | Unlimited |
| VSync | Off |
| Render distance | 8 to 10 chunks (arenas rarely need more) |
| Simulation distance | 5 or 6 |
| Graphics | Fast |
| Smooth lighting | Off, or minimum |
| Clouds | Off |
| Particles | Minimal or Decreased |
| Entity distance | 75 to 100 percent |
| Entity shadows | Off |
| Biome blend | 1 or off |
| Mipmap levels | 0 or 1 |
| View bobbing | Off |
| Distortion / damage tilt effects | 0 |
| FOV effects (sprint FOV stretch) | 0 |
| Menu / screen blur | Off or minimum |
| Fullscreen | Test both fullscreen and borderless; keep the one with better 1 percent lows |
| Raw mouse input | On |
| Mouse acceleration | Off |
| GUI scale | 2 or 3 |

Method: change one thing at a time on the same spot of the same world and compare 1 percent lows, not just average FPS.

---

## 4. Built today vs. roadmap

| Item | Status |
|---|---|
| Keystrokes, CPS, FPS, Ping, Coordinates, Direction, Armor, Potions | Built |
| FPS graph, Clock, Speed, Dimension, World Day, Weather, Sneak Status | Built |
| Health, hunger, biome, held item, durability warning, memory | Built |
| Toggle Sprint + module keybind | Built |
| Zoom with adjustable FOV and keybind | Built |
| Lunar-style animated control center, top tabs, search, eight themes | Built |
| Per-module RGB and scale, drag editor, chroma, compact/expanded HUD | Built | 
| Movable health and food displays with ten-unit, percent, and bar formats | Built |
| Custom menu RGB palette, dimensions, transparency, and backdrop dimming | Built |
| Performance add-on detection for Sodium, Lithium, Entity Culling, ImmediatelyFast, and FerriteCore | Built |
| Health, food/saturation, biome, held-item, armor-warning, memory modules | Built |
| Animated module-toggle feedback | Built |
| Screenshot gallery, quick reconnect, and attack flash | Built |
| Smooth zoom, scroll levels, custom crosshair shapes and bloom | Built |
| Per-module enable, keybind, and corner placement | Built |
| Reversible Low Effects and Max FPS profiles | Built |
| Mod Menu integration | Built (compile-only dependency) |
| CI that builds the jar | Built |
| Custom crosshair | Built (accent color and adjustable size) |
| Particle multiplier | Roadmap. Needs a hook on particle spawning |
| Fullbright | Roadmap. Needs a lightmap mixin |
| Cosmetics (local only) | Roadmap |
| Cached lines and allocation-free layout | Roadmap, see 2.5 |
