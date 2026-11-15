// Gửi cho mọi người chơi

Susu609Mod.PACKET_HANDLER.send(PacketDistributor.ALL.noArg(),
new ClientboundDungeonSeedPacket(true, seed));

// Gửi cho một người chơi

Susu609Mod.PACKET_HANDLER.send(PacketDistributor.PLAYER.with(() -> player),
new ClientboundDungeonSeedPacket(present, seed));