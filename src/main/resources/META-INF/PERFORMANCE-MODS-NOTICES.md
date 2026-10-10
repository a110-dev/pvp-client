# Bundled performance mods

PvPClient includes the following unmodified Fabric mods as nested JARs for Minecraft 26.3. The nested JARs remain separate archives and are loaded by Fabric Loader.

| Mod | Bundled version | License | Source archive included in this JAR |
|---|---|---|---|
| Lithium | 0.26.1 | LGPL-3.0-only | `META-INF/sources/lithium-fabric-mc26.3-0.26.1-source.zip` |
| ImmediatelyFast | 1.17.1 | LGPL-3.0-or-later | `META-INF/sources/ImmediatelyFast-v1.17.1-source.zip` |
| FerriteCore | 9.0.0 | MIT | `META-INF/sources/FerriteCore-build-9.0.0-source.zip` |

The corresponding license texts are included in `META-INF/licenses/`. These upstream JARs have not been modified. Their source and license files are included so each component can be inspected and replaced independently.

Upstream project pages:

- Lithium: https://modrinth.com/mod/lithium
- ImmediatelyFast: https://modrinth.com/mod/immediatelyfast
- FerriteCore: https://modrinth.com/mod/ferrite-core

Sodium and Entity Culling are not bundled. Sodium is distributed under PolyForm Shield 1.0.0, whose non-compete terms require a product-specific determination for this client. Entity Culling's project page says not to redistribute its JAR outside Modrinth- or CurseForge-hosted modpacks. Install those two separately from their official project pages if desired.
