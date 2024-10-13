//package net.ss.sudungeon.command;
//
//import com.mojang.brigadier.CommandDispatcher;
//import com.mojang.brigadier.builder.LiteralArgumentBuilder;
//import com.mojang.brigadier.context.CommandContext;
//import com.mojang.brigadier.exceptions.CommandSyntaxException;
//import net.minecraft.commands.CommandSourceStack;
//import net.minecraft.commands.Commands;
//import net.minecraft.commands.arguments.EntityArgument;
//import net.minecraft.network.chat.Component;
//import net.minecraft.server.level.ServerPlayer;
//import net.minecraft.world.entity.Entity;
//import net.minecraft.world.entity.LivingEntity;
//import net.minecraft.world.entity.ai.attributes.Attributes;
//import net.minecraftforge.event.RegisterCommandsEvent;
//import net.minecraftforge.eventbus.api.SubscribeEvent;
//import net.minecraftforge.fml.common.Mod;
////import net.ss.sudungeon.world.entity.EntityScoring;
//
//import java.util.Objects;
//
//@Mod.EventBusSubscriber
//public class InfoEntityCommand {
//
//    @SubscribeEvent
//    public static void register (RegisterCommandsEvent event) {
//        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();
//        LiteralArgumentBuilder<CommandSourceStack> infoTargetCommand = Commands.literal("info_entity")
//                .requires((source) -> source.hasPermission(2))
//                .then(Commands.argument("target", EntityArgument.entity())
//                        .executes(InfoEntityCommand::execute));
//
//        dispatcher.register(infoTargetCommand);
//    }
//
//    private static int execute (CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
//        ServerPlayer player = context.getSource().getPlayerOrException();
//        Entity entity = EntityArgument.getEntity(context, "target");
//
//        if (entity instanceof LivingEntity livingEntity) {
//            // Hiển thị thông tin về thực thể
//            displayEntityInfo(player, livingEntity);
//        } else {
//            player.sendSystemMessage(Component.literal("Thực thể không hợp lệ. Hãy chọn một sinh vật."));
//        }
//
//        return 1;
//    }
//
//    private static void displayEntityInfo (ServerPlayer player, LivingEntity entity) {
//        // Tính toán và hiển thị thông tin
//
//        double health = entity.getHealth();
//        double maxHealth = Objects.requireNonNull(entity.getAttribute(Attributes.MAX_HEALTH)).getValue();
////        double score = EntityScoring.calculateEntityScore(entity);
//
//
//        player.sendSystemMessage(Component.literal("Thông tin của " + entity.getName().getString() + ":"));
//        player.sendSystemMessage(Component.literal(" - Máu: " + health + "/" + maxHealth));
////        player.sendSystemMessage(Component.literal(" - Điểm số: " + score));
//
//        // Hiển thị thêm các thông tin khác nếu cần (ví dụ: loại entity, hiệu ứng đang có, ...)
//    }
//}
