# PvPClient 1.4.0

A client-side Fabric mod for Minecraft 26.3 by aleksa04_1, with a searchable animated menu, customizable HUD, utility modules, and reversible performance options. It does not modify reach, aim, hitboxes, or attack behavior.

## Modules

| Module | What it shows | Default |
|---|---|---|
| Keystrokes | WASD, mouse buttons, CPS, and jump | On |
| CPS | Left and right clicks per second | On |
| FPS | Current frame rate | On |
| Ping | Server latency in milliseconds | On |
| Coordinates | Player XYZ position | On |
| Direction | Cardinal direction and yaw | On |
| Armor Status | Equipped armor, durability numbers, and bars | On |
| Potion Effects | Active effects and timers with expiry colors | On |
| Toggle Sprint | Sprint while moving forward | On; K toggles it |
| FPS Graph | Rolling frame-rate graph | Off |
| Sneak Status | Whether the player is crouching | Off |
| Clock / Speed / Dimension / World Day / Weather | World and session readouts | Off |
| Health / Food / Biome | Health, absorption, hunger, saturation, and biome | Off |
| Held Item | Main- and off-hand stacks and durability | Off |
| Armor Warning | Warns when an armor piece is at 20% durability or less | Off |
| Memory Usage | Current and maximum JVM heap | Off |
| Hit Color Flash | Brief custom-color flash on entity attack input | Off |
| Target Distance | Read-only distance to the entity under the crosshair | Off |
| Custom Crosshair | Cross, dot, or ring, with adjustable gap and attack bloom | Off |
| Performance Profile | Reversible low-effects graphics preset | Off |
| Zoom | Smooth hold-to-zoom; scroll while held to change zoom | Hold C |

## Control center

Press **Right Shift** or click **PVP CLIENT** on the pause screen to open the control center. The pause-screen button sits between the advancement/social buttons and Options. The menu includes smooth transitions, top category tabs, live search, eight accent themes, module toggles, per-module keybinds, and a gear page for RGB color and scale. Global settings include HUD transparency and scale, compact or expanded cards, animated chroma, crosshair shape and bloom, zoom FOV, and global keybinds. Changes are saved in `config/aleksa04_1.json`.

Use **Edit HUD** to drag active widgets directly or drag entries from the widget library onto the screen. Left-click a library entry to toggle it, scroll while dragging to resize, turn on optional grid snapping, or reset all custom positions. The menu transparency and HUD background opacity are adjustable in Settings. The Screenshot Gallery previews captures, copies an image to the system clipboard, and asks for confirmation before deleting. A **Quick Reconnect** button is available on multiplayer pause and death screens and in the control center.

The built-in Performance Profile has **Low Effects** and **Max FPS** modes. It lowers render/simulation distance, entity distance, particles, clouds, weather detail, ambient occlusion, and entity shadows; disabling it restores the previous settings. Actual gains depend on hardware and world. The hit flash is visual feedback on attack input and does not confirm damage.

The **Style** tab edits menu accent, panel, border, and text colors with quick color swatches or RGB sliders. It also has draggable width and height sliders and backdrop dimming. All menu, gallery, and HUD editor controls use left-click. The Performance tab reports loaded optimization mods. This JAR bundles **Lithium**, **ImmediatelyFast**, and **FerriteCore** as nested Fabric mods; their license texts and corresponding source archives are included under `META-INF`. In each Health and Food & Saturation gear page, set **Vanilla Bar** to ON to restore Minecraft's original row and hide that custom widget. These two switches work independently.

The Health and Food & Saturation widgets can replace the vanilla heart and hunger rows when enabled. They are movable from **Edit HUD** and their gear pages let you switch between a normalized `10/10` display, percentage, and a compact bar. With either widget disabled, its vanilla HUD row remains unchanged.

## Install

Use Minecraft 26.3 with Fabric Loader 0.19.5+ and Fabric API 0.160.5+26.3. Copy `aleksa04_1-1.4.0.jar` into the instance's `mods` folder. Lithium, ImmediatelyFast, and FerriteCore are included in that file; remove separate copies of those three from the instance's `mods` folder to avoid duplicate mod IDs. Mod Menu 21.0.0 is optional and adds a Configure button. For additional performance, install compatible Fabric releases of [Sodium](https://modrinth.com/mod/sodium) and [Entity Culling](https://modrinth.com/mod/entityculling) separately. The title screen, HUD, and menu display the client credit and detected runtime versions.

## Build

Requires JDK 25 and Gradle 9.5.0. Run `gradle build`; the mod JAR is written to `build/libs/aleksa04_1-1.4.0.jar`.
