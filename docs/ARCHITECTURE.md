# Kiến trúc mã

> File này được sinh tự động bởi `tools/gen_architecture.py`. Đừng sửa tay.

- Package gốc: `net.susu609.the8dungeon`
- 38 class/kiểu Java, 2677 dòng, 77 phụ thuộc nội bộ

Mũi tên `A --> B` nghĩa là A có dùng tới B. Khối bo tròn là entry point `@Mod`.

```mermaid
flowchart LR
  subgraph pkg__g_c_["(gốc)"]
    Susu609Mod(["Susu609Mod<br/><small>class · 123 dòng</small>"])
  end
  subgraph pkg_client_animation_definitions["client.animation.definitions"]
    GateAnimation["GateAnimation<br/><small>class · 15 dòng</small>"]
  end
  subgraph pkg_client_gui["client.gui"]
    MiniMapHud["MiniMapHud<br/><small>class · 84 dòng</small>"]
    ScreenUtil["ScreenUtil<br/><small>class · 16 dòng</small>"]
  end
  subgraph pkg_client_gui_screens["client.gui.screens"]
    ModTitleScreen["ModTitleScreen<br/><small>class · 469 dòng</small>"]
    TitleScreenHook["TitleScreenHook<br/><small>class · 24 dòng</small>"]
    TitleScreenInterface["TitleScreenInterface<br/><small>interface · 13 dòng</small>"]
  end
  subgraph pkg_client_model["client.model"]
    GateModel["GateModel<br/><small>class · 70 dòng</small>"]
    PedestalModel["PedestalModel<br/><small>class · 47 dòng</small>"]
  end
  subgraph pkg_client_renderer_entity["client.renderer.entity"]
    GateRenderer["GateRenderer<br/><small>class · 45 dòng</small>"]
    PedestalRenderer["PedestalRenderer<br/><small>class · 42 dòng</small>"]
    PropRenderer["PropRenderer<br/><small>class · 40 dòng</small>"]
  end
  subgraph pkg_dungeon["dungeon"]
    DungeonAutoBuild["DungeonAutoBuild<br/><small>class · 44 dòng</small>"]
    DungeonBuilder["DungeonBuilder<br/><small>class · 172 dòng</small>"]
    DungeonClientState["DungeonClientState<br/><small>class · 27 dòng</small>"]
    DungeonCommand["DungeonCommand<br/><small>class · 60 dòng</small>"]
    DungeonInspector["DungeonInspector<br/><small>class · 159 dòng</small>"]
    DungeonLayout["DungeonLayout<br/><small>class · 399 dòng</small>"]
    DungeonParams["DungeonParams<br/><small>record · 49 dòng</small>"]
    DungeonSavedData["DungeonSavedData<br/><small>class · 55 dòng</small>"]
    DungeonService["DungeonService<br/><small>class · 36 dòng</small>"]
    RoomPreset["RoomPreset<br/><small>enum · 40 dòng</small>"]
  end
  subgraph pkg_init["init"]
    ModBlockEntitiesRegistry["ModBlockEntitiesRegistry<br/><small>class · 16 dòng</small>"]
    ModBlocksRegistry["ModBlocksRegistry<br/><small>class · 49 dòng</small>"]
    ModCreativeTabsRegistry["ModCreativeTabsRegistry<br/><small>class · 31 dòng</small>"]
    ModEntitiesRegistry["ModEntitiesRegistry<br/><small>class · 50 dòng</small>"]
    ModEntityRenderersRegistry["ModEntityRenderersRegistry<br/><small>class · 39 dòng</small>"]
    ModItemsRegistry["ModItemsRegistry<br/><small>class · 42 dòng</small>"]
  end
  subgraph pkg_mixin["mixin"]
    ModBlockBehaviour["ModBlockBehaviour<br/><small>class · 46 dòng</small>"]
  end
  subgraph pkg_network["network"]
    ClientboundDungeonSeedPacket["ClientboundDungeonSeedPacket<br/><small>class · 44 dòng</small>"]
  end
  subgraph pkg_world["world"]
    VoidSpawn["VoidSpawn<br/><small>class · 38 dòng</small>"]
    WorldRandom["WorldRandom<br/><small>class · 68 dòng</small>"]
  end
  subgraph pkg_world_level_block_entity["world.level.block.entity"]
    GateEntity["GateEntity<br/><small>class · 26 dòng</small>"]
    PedestalEntity["PedestalEntity<br/><small>class · 11 dòng</small>"]
    PropEntity["PropEntity<br/><small>class · 94 dòng</small>"]
  end
  subgraph pkg_world_torch["world.torch"]
    ModUnlitTorchBlock["ModUnlitTorchBlock<br/><small>class · 22 dòng</small>"]
    ModUnlitWallTorchBlock["ModUnlitWallTorchBlock<br/><small>class · 22 dòng</small>"]
    TorchBurn["TorchBurn<br/><small>class · 50 dòng</small>"]
  end
  ClientboundDungeonSeedPacket --> DungeonClientState
  ClientboundDungeonSeedPacket --> Susu609Mod
  DungeonAutoBuild --> ClientboundDungeonSeedPacket
  DungeonAutoBuild --> DungeonLayout
  DungeonAutoBuild --> DungeonSavedData
  DungeonAutoBuild --> DungeonService
  DungeonAutoBuild --> Susu609Mod
  DungeonBuilder --> DungeonLayout
  DungeonBuilder --> DungeonParams
  DungeonBuilder --> ModBlocksRegistry
  DungeonBuilder --> RoomPreset
  DungeonBuilder --> TorchBurn
  DungeonBuilder --> WorldRandom
  DungeonClientState --> DungeonLayout
  DungeonClientState --> DungeonParams
  DungeonClientState --> WorldRandom
  DungeonCommand --> ClientboundDungeonSeedPacket
  DungeonCommand --> DungeonLayout
  DungeonCommand --> DungeonService
  DungeonCommand --> Susu609Mod
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
  DungeonService --> DungeonBuilder
  DungeonService --> DungeonLayout
  DungeonService --> DungeonParams
  DungeonService --> DungeonSavedData
  DungeonService --> RoomPreset
  DungeonService --> WorldRandom
  GateEntity --> PropEntity
  GateModel --> GateEntity
  GateRenderer --> GateEntity
  GateRenderer --> GateModel
  MiniMapHud --> Susu609Mod
  ModBlockBehaviour --> ModBlocksRegistry
  ModBlockBehaviour --> TorchBurn
  ModBlockEntitiesRegistry --> Susu609Mod
  ModBlocksRegistry --> ModUnlitTorchBlock
  ModBlocksRegistry --> ModUnlitWallTorchBlock
  ModBlocksRegistry --> Susu609Mod
  ModCreativeTabsRegistry --> ModItemsRegistry
  ModCreativeTabsRegistry --> Susu609Mod
  ModEntitiesRegistry --> GateEntity
  ModEntitiesRegistry --> PedestalEntity
  ModEntitiesRegistry --> Susu609Mod
  ModEntityRenderersRegistry --> GateModel
  ModEntityRenderersRegistry --> GateRenderer
  ModEntityRenderersRegistry --> ModEntitiesRegistry
  ModEntityRenderersRegistry --> PedestalModel
  ModEntityRenderersRegistry --> PedestalRenderer
  ModItemsRegistry --> ModBlocksRegistry
  ModItemsRegistry --> Susu609Mod
  ModTitleScreen --> ScreenUtil
  ModTitleScreen --> Susu609Mod
  ModTitleScreen --> TitleScreenInterface
  PedestalEntity --> PropEntity
  PedestalModel --> PedestalEntity
  PedestalModel --> Susu609Mod
  PedestalRenderer --> PedestalEntity
  PedestalRenderer --> PedestalModel
  PropRenderer --> PropEntity
  Susu609Mod --> ModBlockEntitiesRegistry
  Susu609Mod --> ModBlocksRegistry
  Susu609Mod --> ModCreativeTabsRegistry
  Susu609Mod --> ModEntitiesRegistry
  Susu609Mod --> ModEntityRenderersRegistry
  Susu609Mod --> ModItemsRegistry
  TitleScreenHook --> ModTitleScreen
  TitleScreenHook --> Susu609Mod
  VoidSpawn --> Susu609Mod
```

## Class theo package

### (gốc)

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `Susu609Mod` | class | 123 | ModBlockEntitiesRegistry, ModBlocksRegistry, ModCreativeTabsRegistry, ModEntitiesRegistry, ModEntityRenderersRegistry, ModItemsRegistry | NetworkEvent, NetworkEvent.Context, TickEvent, TickEvent.Phase, TickEvent.ServerTickEvent |

### client.animation.definitions

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `GateAnimation` | class | 15 | - | - |

### client.gui

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `MiniMapHud` | class | 84 | Susu609Mod | RenderGuiEvent, RenderGuiEvent.Post |
| `ScreenUtil` | class | 16 | - | - |

### client.gui.screens

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModTitleScreen` | class | 469 | ScreenUtil, Susu609Mod, TitleScreenInterface | - |
| `TitleScreenHook` | class | 24 | ModTitleScreen, Susu609Mod | ScreenEvent, ScreenEvent.Opening |
| `TitleScreenInterface` | interface | 13 | - | - |

### client.model

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `GateModel` | class | 70 | GateEntity | - |
| `PedestalModel` | class | 47 | PedestalEntity, Susu609Mod | - |

### client.renderer.entity

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `GateRenderer` | class | 45 | GateEntity, GateModel | - |
| `PedestalRenderer` | class | 42 | PedestalEntity, PedestalModel | - |
| `PropRenderer` | class | 40 | PropEntity | - |

### dungeon

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `DungeonAutoBuild` | class | 44 | ClientboundDungeonSeedPacket, DungeonLayout, DungeonSavedData, DungeonService, Susu609Mod | PlayerEvent, PlayerEvent.PlayerLoggedInEvent |
| `DungeonBuilder` | class | 172 | DungeonLayout, DungeonParams, ModBlocksRegistry, RoomPreset, TorchBurn, WorldRandom | - |
| `DungeonClientState` | class | 27 | DungeonLayout, DungeonParams, WorldRandom | - |
| `DungeonCommand` | class | 60 | ClientboundDungeonSeedPacket, DungeonLayout, DungeonService, Susu609Mod | RegisterCommandsEvent |
| `DungeonInspector` | class | 159 | DungeonBuilder, DungeonClientState, DungeonLayout, RoomPreset, Susu609Mod | RegisterGuiOverlaysEvent, RenderLevelStageEvent, RenderLevelStageEvent.Stage |
| `DungeonLayout` | class | 399 | DungeonParams, RoomPreset, WorldRandom | - |
| `DungeonParams` | record | 49 | DungeonLayout, RoomPreset | - |
| `DungeonSavedData` | class | 55 | - | - |
| `DungeonService` | class | 36 | DungeonBuilder, DungeonLayout, DungeonParams, DungeonSavedData, RoomPreset, WorldRandom | - |
| `RoomPreset` | enum | 40 | - | - |

### init

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModBlockEntitiesRegistry` | class | 16 | Susu609Mod | - |
| `ModBlocksRegistry` | class | 49 | ModUnlitTorchBlock, ModUnlitWallTorchBlock, Susu609Mod | - |
| `ModCreativeTabsRegistry` | class | 31 | ModItemsRegistry, Susu609Mod | - |
| `ModEntitiesRegistry` | class | 50 | GateEntity, PedestalEntity, Susu609Mod | EntityAttributeCreationEvent |
| `ModEntityRenderersRegistry` | class | 39 | GateModel, GateRenderer, ModEntitiesRegistry, PedestalModel, PedestalRenderer | EntityRenderersEvent, EntityRenderersEvent.RegisterLayerDefinitions, EntityRenderersEvent.RegisterRenderers |
| `ModItemsRegistry` | class | 42 | ModBlocksRegistry, Susu609Mod | BuildCreativeModeTabContentsEvent |

### mixin

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModBlockBehaviour` | class | 46 | ModBlocksRegistry, TorchBurn | - |

### network

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClientboundDungeonSeedPacket` | class | 44 | DungeonClientState, Susu609Mod | FMLCommonSetupEvent, NetworkEvent, NetworkEvent.Context |

### world

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `VoidSpawn` | class | 38 | Susu609Mod | LevelEvent, LevelEvent.CreateSpawnPosition |
| `WorldRandom` | class | 68 | - | - |

### world.level.block.entity

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `GateEntity` | class | 26 | PropEntity | - |
| `PedestalEntity` | class | 11 | PropEntity | - |
| `PropEntity` | class | 94 | - | - |

### world.torch

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModUnlitTorchBlock` | class | 22 | - | - |
| `ModUnlitWallTorchBlock` | class | 22 | - | - |
| `TorchBurn` | class | 50 | - | - |

## Tài nguyên

- `META-INF/mods.toml`
- `assets/susu609/blockstates/torch_off.json`
- `assets/susu609/blockstates/wall_torch_off.json`
- `assets/susu609/lang/en_us.json`
- `assets/susu609/lang/vi_vn.json`
- `assets/susu609/models/block/torch_off.json`
- `assets/susu609/models/block/wall_torch_off.json`
- `assets/susu609/models/item/torch.json`
- `assets/susu609/models/item/torch_off.json`
- `assets/susu609/sounds.json`
- `assets/susu609/textures/block/torch_off.png`
- `assets/susu609/textures/entity/clawd_agent.png`
- `assets/susu609/textures/gui/title/mod_title.png`
- `assets/susu609/textures/map/minimap_icons.png`
- `data/susu609/loot_tables/blocks/torch_dim.json`
- `data/susu609/loot_tables/blocks/torch_off.json`
- `data/susu609/loot_tables/blocks/wall_torch_dim.json`
- `data/susu609/loot_tables/blocks/wall_torch_off.json`
- `data/susu609/worldgen/world_preset/void.json`
- `pack.mcmeta`
- `susu609.mixins.json`
