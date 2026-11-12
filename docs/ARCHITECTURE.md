# Kiến trúc mã

> File này được sinh tự động bởi `tools/gen_architecture.py`. Đừng sửa tay.

- Package gốc: `net.susu609.the8dungeon`
- 17 class/kiểu Java, 1793 dòng, 33 phụ thuộc nội bộ

Mũi tên `A --> B` nghĩa là A có dùng tới B. Khối bo tròn là entry point `@Mod`.

```mermaid
flowchart LR
  subgraph pkg__g_c_["(gốc)"]
    Susu609Mod(["Susu609Mod<br/><small>class · 117 dòng</small>"])
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
  subgraph pkg_network["network"]
    ClientboundDungeonSeedPacket["ClientboundDungeonSeedPacket<br/><small>class · 44 dòng</small>"]
  end
  subgraph pkg_world["world"]
    VoidSpawn["VoidSpawn<br/><small>class · 38 dòng</small>"]
    WorldRandom["WorldRandom<br/><small>class · 67 dòng</small>"]
  end
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
  ModTitleScreen --> ScreenUtil
  ModTitleScreen --> Susu609Mod
  ModTitleScreen --> TitleScreenInterface
  ScreenInspector --> Susu609Mod
  TitleScreenHook --> ModTitleScreen
  TitleScreenHook --> Susu609Mod
  VoidSpawn --> Susu609Mod
```

## Class theo package

### (gốc)

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `Susu609Mod` | class | 117 | - | NetworkEvent, NetworkEvent.Context, TickEvent, TickEvent.Phase, TickEvent.ServerTickEvent |

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

### network

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ClientboundDungeonSeedPacket` | class | 44 | DungeonClientState, Susu609Mod | FMLCommonSetupEvent, NetworkEvent, NetworkEvent.Context |

### world

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `VoidSpawn` | class | 38 | Susu609Mod | LevelEvent, LevelEvent.CreateSpawnPosition |
| `WorldRandom` | class | 67 | - | - |

## Tài nguyên

- `META-INF/mods.toml`
- `assets/susu609/lang/en_us.json`
- `assets/susu609/textures/gui/title/mod_title.png`
- `assets/susu609/textures/map/minimap_icons.png`
- `data/susu609/worldgen/world_preset/void.json`
- `pack.mcmeta`
- `susu609.mixins.json`
