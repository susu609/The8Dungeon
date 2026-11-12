# Kiến trúc mã

> File này được sinh tự động bởi `tools/gen_architecture.py`. Đừng sửa tay.

- Package gốc: `net.susu609.the8dungeon`
- 23 class/kiểu Java, 2082 dòng, 48 phụ thuộc nội bộ

Mũi tên `A --> B` nghĩa là A có dùng tới B. Khối bo tròn là entry point `@Mod`.

```mermaid
flowchart LR
  subgraph pkg__g_c_["(gốc)"]
    Susu609Mod(["Susu609Mod<br/><small>class · 119 dòng</small>"])
  end
  subgraph pkg_client_gui["client.gui"]
    ScreenUtil["ScreenUtil<br/><small>class · 16 dòng</small>"]
  end
  subgraph pkg_client_gui_inspector["client.gui.inspector"]
    ScreenInspector["ScreenInspector<br/><small>class · 143 dòng</small>"]
  end
  subgraph pkg_client_gui_screens["client.gui.screens"]
    ModTitleScreen["ModTitleScreen<br/><small>class · 447 dòng</small>"]
    TitleScreenHook["TitleScreenHook<br/><small>class · 24 dòng</small>"]
    TitleScreenInterface["TitleScreenInterface<br/><small>interface · 13 dòng</small>"]
  end
  subgraph pkg_client_model["client.model"]
    ClawdModel["ClawdModel<br/><small>class · 83 dòng</small>"]
  end
  subgraph pkg_client_renderer_entity["client.renderer.entity"]
    ClawdRenderer["ClawdRenderer<br/><small>class · 24 dòng</small>"]
  end
  subgraph pkg_dungeon["dungeon"]
    DungeonBuilder["DungeonBuilder<br/><small>class · 107 dòng</small>"]
    DungeonClientState["DungeonClientState<br/><small>class · 27 dòng</small>"]
    DungeonCommand["DungeonCommand<br/><small>class · 71 dòng</small>"]
    DungeonInspector["DungeonInspector<br/><small>class · 159 dòng</small>"]
    DungeonLayout["DungeonLayout<br/><small>class · 382 dòng</small>"]
    DungeonParams["DungeonParams<br/><small>record · 43 dòng</small>"]
    DungeonSavedData["DungeonSavedData<br/><small>class · 55 dòng</small>"]
    RoomPreset["RoomPreset<br/><small>enum · 40 dòng</small>"]
  end
  subgraph pkg_init["init"]
    ModEntitiesRegistry["ModEntitiesRegistry<br/><small>class · 42 dòng</small>"]
    ModEntityRenderers["ModEntityRenderers<br/><small>class · 34 dòng</small>"]
    ModSoundsRegistry["ModSoundsRegistry<br/><small>class · 35 dòng</small>"]
  end
  subgraph pkg_network["network"]
    ClientboundDungeonSeedPacket["ClientboundDungeonSeedPacket<br/><small>class · 44 dòng</small>"]
  end
  subgraph pkg_world["world"]
    VoidSpawn["VoidSpawn<br/><small>class · 38 dòng</small>"]
    WorldRandom["WorldRandom<br/><small>class · 67 dòng</small>"]
  end
  subgraph pkg_world_entity["world.entity"]
    ClawdEntity["ClawdEntity<br/><small>class · 69 dòng</small>"]
  end
  ClawdEntity --> ModSoundsRegistry
  ClawdModel --> ClawdEntity
  ClawdModel --> Susu609Mod
  ClawdRenderer --> ClawdEntity
  ClawdRenderer --> ClawdModel
  ClawdRenderer --> Susu609Mod
  ClientboundDungeonSeedPacket --> DungeonClientState
  ClientboundDungeonSeedPacket --> Susu609Mod
  DungeonBuilder --> DungeonLayout
  DungeonBuilder --> DungeonParams
  DungeonBuilder --> RoomPreset
  DungeonClientState --> DungeonLayout
  DungeonClientState --> DungeonParams
  DungeonClientState --> WorldRandom
  DungeonCommand --> ClientboundDungeonSeedPacket
  DungeonCommand --> DungeonBuilder
  DungeonCommand --> DungeonLayout
  DungeonCommand --> DungeonParams
  DungeonCommand --> DungeonSavedData
  DungeonCommand --> RoomPreset
  DungeonCommand --> Susu609Mod
  DungeonCommand --> WorldRandom
  DungeonInspector --> DungeonBuilder
  DungeonInspector --> DungeonClientState
  DungeonInspector --> DungeonLayout
  DungeonInspector --> RoomPreset
  DungeonInspector --> Susu609Mod
  DungeonLayout --> DungeonParams
  DungeonLayout --> RoomPreset
  DungeonLayout --> WorldRandom
  DungeonParams --> DungeonLayout
  DungeonParams --> RoomPreset
  ModEntitiesRegistry --> ClawdEntity
  ModEntitiesRegistry --> Susu609Mod
  ModEntityRenderers --> ClawdModel
  ModEntityRenderers --> ClawdRenderer
  ModEntityRenderers --> ModEntitiesRegistry
  ModSoundsRegistry --> Susu609Mod
  ModTitleScreen --> ScreenUtil
  ModTitleScreen --> Susu609Mod
  ModTitleScreen --> TitleScreenInterface
  ScreenInspector --> Susu609Mod
  Susu609Mod --> ModEntitiesRegistry
  Susu609Mod --> ModEntityRenderers
  Susu609Mod --> ModSoundsRegistry
  TitleScreenHook --> ModTitleScreen
  TitleScreenHook --> Susu609Mod
  VoidSpawn --> Susu609Mod
```

## Class theo package

### (gốc)

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `Susu609Mod` | class | 119 | ModEntitiesRegistry, ModEntityRenderers, ModSoundsRegistry | NetworkEvent, NetworkEvent.Context, TickEvent, TickEvent.Phase, TickEvent.ServerTickEvent |

### client.gui

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ScreenUtil` | class | 16 | - | - |

### client.gui.inspector

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ScreenInspector` | class | 143 | Susu609Mod | ScreenEvent, ScreenEvent.KeyPressed, ScreenEvent.Render |

### client.gui.screens

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModTitleScreen` | class | 447 | ScreenUtil, Susu609Mod, TitleScreenInterface | - |
| `TitleScreenHook` | class | 24 | ModTitleScreen, Susu609Mod | ScreenEvent, ScreenEvent.Opening |
| `TitleScreenInterface` | interface | 13 | - | - |

### client.model

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClawdModel` | class | 83 | ClawdEntity, Susu609Mod | - |

### client.renderer.entity

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClawdRenderer` | class | 24 | ClawdEntity, ClawdModel, Susu609Mod | - |

### dungeon

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `DungeonBuilder` | class | 107 | DungeonLayout, DungeonParams, RoomPreset | - |
| `DungeonClientState` | class | 27 | DungeonLayout, DungeonParams, WorldRandom | - |
| `DungeonCommand` | class | 71 | ClientboundDungeonSeedPacket, DungeonBuilder, DungeonLayout, DungeonParams, DungeonSavedData, RoomPreset, Susu609Mod, WorldRandom | RegisterCommandsEvent |
| `DungeonInspector` | class | 159 | DungeonBuilder, DungeonClientState, DungeonLayout, RoomPreset, Susu609Mod | RegisterGuiOverlaysEvent, RenderLevelStageEvent, RenderLevelStageEvent.Stage |
| `DungeonLayout` | class | 382 | DungeonParams, RoomPreset, WorldRandom | - |
| `DungeonParams` | record | 43 | DungeonLayout, RoomPreset | - |
| `DungeonSavedData` | class | 55 | - | - |
| `RoomPreset` | enum | 40 | - | - |

### init

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModEntitiesRegistry` | class | 42 | ClawdEntity, Susu609Mod | EntityAttributeCreationEvent |
| `ModEntityRenderers` | class | 34 | ClawdModel, ClawdRenderer, ModEntitiesRegistry | EntityRenderersEvent, EntityRenderersEvent.RegisterLayerDefinitions, EntityRenderersEvent.RegisterRenderers |
| `ModSoundsRegistry` | class | 35 | Susu609Mod | SoundEvent, SoundEvent.createVariableRangeEvent |

### network

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClientboundDungeonSeedPacket` | class | 44 | DungeonClientState, Susu609Mod | FMLCommonSetupEvent, NetworkEvent, NetworkEvent.Context |

### world

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `VoidSpawn` | class | 38 | Susu609Mod | LevelEvent, LevelEvent.CreateSpawnPosition |
| `WorldRandom` | class | 67 | - | - |

### world.entity

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClawdEntity` | class | 69 | ModSoundsRegistry | SoundEvent |

## Tài nguyên

- `META-INF/mods.toml`
- `assets/susu609/lang/en_us.json`
- `assets/susu609/sounds/entity/clawd/ambient1.ogg`
- `assets/susu609/sounds/entity/clawd/ambient2.ogg`
- `assets/susu609/sounds/entity/clawd/ambient3.ogg`
- `assets/susu609/sounds/entity/clawd/death.ogg`
- `assets/susu609/sounds/entity/clawd/happy.ogg`
- `assets/susu609/sounds/entity/clawd/hurt.ogg`
- `assets/susu609/sounds/entity/clawd/step1.ogg`
- `assets/susu609/sounds/entity/clawd/step2.ogg`
- `assets/susu609/sounds/entity/clawd/think.ogg`
- `assets/susu609/sounds/entity/clawd/work.ogg`
- `assets/susu609/sounds.json`
- `assets/susu609/textures/entity/clawd_agent.png`
- `assets/susu609/textures/gui/title/mod_title.png`
- `assets/susu609/textures/map/minimap_icons.png`
- `data/susu609/worldgen/world_preset/void.json`
- `pack.mcmeta`
- `susu609.mixins.json`
