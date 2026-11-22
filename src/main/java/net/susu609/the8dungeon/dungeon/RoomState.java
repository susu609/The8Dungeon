package net.susu609.the8dungeon.dungeon;

// Server chỉ lưu COMBAT và CLEARED; HIDDEN/SEEN do client tự suy ra từ phòng kề.
public enum RoomState {HIDDEN, SEEN, COMBAT, CLEARED}