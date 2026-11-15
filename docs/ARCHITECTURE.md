# Kiến trúc mã

> File này được sinh tự động bởi `tools/gen_architecture.py`. Đừng sửa tay.

- Package gốc: `net.susu609.the8dungeon`
- 15 class/kiểu Java, 981 dòng, 22 phụ thuộc nội bộ

Mũi tên `A --> B` nghĩa là A có dùng tới B. Khối bo tròn là entry point `@Mod`.

```mermaid
flowchart LR
  subgraph pkg__g_c_["(gốc)"]
    Susu609Mod(["Susu609Mod<br/><small>class · 123 dòng</small>"])
  end
  subgraph pkg_client_gui["client.gui"]
    ScreenUtil["ScreenUtil<br/><small>class · 16 dòng</small>"]
  end
  subgraph pkg_client_gui_screens["client.gui.screens"]
    ModTitleScreen["ModTitleScreen<br/><small>class · 469 dòng</small>"]
    TitleScreenHook["TitleScreenHook<br/><small>class · 24 dòng</small>"]
    TitleScreenInterface["TitleScreenInterface<br/><small>interface · 13 dòng</small>"]
  end
  subgraph pkg_init["init"]
    ModBlockEntitiesRegistry["ModBlockEntitiesRegistry<br/><small>class · 16 dòng</small>"]
    ModBlocksRegistry["ModBlocksRegistry<br/><small>class · 49 dòng</small>"]
    ModCreativeTabsRegistry["ModCreativeTabsRegistry<br/><small>class · 31 dòng</small>"]
    ModEntitiesRegistry["ModEntitiesRegistry<br/><small>class · 32 dòng</small>"]
    ModEntityRenderersRegistry["ModEntityRenderersRegistry<br/><small>class · 30 dòng</small>"]
    ModItemsRegistry["ModItemsRegistry<br/><small>class · 42 dòng</small>"]
  end
  subgraph pkg_mixin["mixin"]
    ModBlockBehaviour["ModBlockBehaviour<br/><small>class · 45 dòng</small>"]
  end
  subgraph pkg_world_torch["world.torch"]
    ModUnlitTorchBlock["ModUnlitTorchBlock<br/><small>class · 22 dòng</small>"]
    ModUnlitWallTorchBlock["ModUnlitWallTorchBlock<br/><small>class · 22 dòng</small>"]
    TorchBurn["TorchBurn<br/><small>class · 47 dòng</small>"]
  end
  ModBlockBehaviour --> ModBlocksRegistry
  ModBlockBehaviour --> TorchBurn
  ModBlockEntitiesRegistry --> Susu609Mod
  ModBlocksRegistry --> ModUnlitTorchBlock
  ModBlocksRegistry --> ModUnlitWallTorchBlock
  ModBlocksRegistry --> Susu609Mod
  ModCreativeTabsRegistry --> ModItemsRegistry
  ModCreativeTabsRegistry --> Susu609Mod
  ModEntitiesRegistry --> Susu609Mod
  ModItemsRegistry --> ModBlocksRegistry
  ModItemsRegistry --> Susu609Mod
  ModTitleScreen --> ScreenUtil
  ModTitleScreen --> Susu609Mod
  ModTitleScreen --> TitleScreenInterface
  Susu609Mod --> ModBlockEntitiesRegistry
  Susu609Mod --> ModBlocksRegistry
  Susu609Mod --> ModCreativeTabsRegistry
  Susu609Mod --> ModEntitiesRegistry
  Susu609Mod --> ModEntityRenderersRegistry
  Susu609Mod --> ModItemsRegistry
  TitleScreenHook --> ModTitleScreen
  TitleScreenHook --> Susu609Mod
```

## Class theo package

### (gốc)

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `Susu609Mod` | class | 123 | ModBlockEntitiesRegistry, ModBlocksRegistry, ModCreativeTabsRegistry, ModEntitiesRegistry, ModEntityRenderersRegistry, ModItemsRegistry | NetworkEvent, NetworkEvent.Context, TickEvent, TickEvent.Phase, TickEvent.ServerTickEvent |

### client.gui

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ScreenUtil` | class | 16 | - | - |

### client.gui.screens

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModTitleScreen` | class | 469 | ScreenUtil, Susu609Mod, TitleScreenInterface | - |
| `TitleScreenHook` | class | 24 | ModTitleScreen, Susu609Mod | ScreenEvent, ScreenEvent.Opening |
| `TitleScreenInterface` | interface | 13 | - | - |

### init

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModBlockEntitiesRegistry` | class | 16 | Susu609Mod | - |
| `ModBlocksRegistry` | class | 49 | ModUnlitTorchBlock, ModUnlitWallTorchBlock, Susu609Mod | - |
| `ModCreativeTabsRegistry` | class | 31 | ModItemsRegistry, Susu609Mod | - |
| `ModEntitiesRegistry` | class | 32 | Susu609Mod | EntityAttributeCreationEvent |
| `ModEntityRenderersRegistry` | class | 30 | - | EntityRenderersEvent, EntityRenderersEvent.RegisterLayerDefinitions, EntityRenderersEvent.RegisterRenderers |
| `ModItemsRegistry` | class | 42 | ModBlocksRegistry, Susu609Mod | BuildCreativeModeTabContentsEvent |

### mixin

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModBlockBehaviour` | class | 45 | ModBlocksRegistry, TorchBurn | - |

### world.torch

| Class | Loại | Dòng | Dùng tới | Event Forge |
|---|---|---|---|---|
| `ModUnlitTorchBlock` | class | 22 | - | - |
| `ModUnlitWallTorchBlock` | class | 22 | - | - |
| `TorchBurn` | class | 47 | - | - |

## Tài nguyên

- `META-INF/mods.toml`
- `assets/susu609/blockstates/torch_off.json`
- `assets/susu609/blockstates/wall_torch_off.json`
- `assets/susu609/lang/en_us.json`
- `assets/susu609/lang/vi_vn.json`
- `assets/susu609/models/block/torch_off.json`
- `assets/susu609/models/block/wall_torch_off.json`
- `assets/susu609/models/item/clawd_spawn_egg.json`
- `assets/susu609/models/item/torch.json`
- `assets/susu609/models/item/torch_off.json`
- `assets/susu609/sounds.json`
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
