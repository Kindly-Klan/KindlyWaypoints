package kindly.klan.kkwaypoints;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.concurrent.CompletableFuture;

@Mod.EventBusSubscriber(modid = KindlyWaypoints.MODID)
public class WaypointCommands {

    private static final SuggestionProvider<CommandSourceStack> TEXTURE_SUGGESTIONS = (context, builder) -> {
        builder.suggest("red_point");
        builder.suggest("blue_point");
        return builder.buildFuture();
    };

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        CommandDispatcher<CommandSourceStack> dispatcher = event.getDispatcher();

        dispatcher.register(Commands.literal("waypoint")
            .then(Commands.argument("name", StringArgumentType.string())
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                    .then(Commands.argument("texture", StringArgumentType.string())
                        .suggests(TEXTURE_SUGGESTIONS)
                        .executes(context -> {
                            String name = StringArgumentType.getString(context, "name");
                            BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
                            String texture = StringArgumentType.getString(context, "texture");
                            String world = context.getSource().getLevel().dimension().location().toString();
                            Waypoint waypoint = new Waypoint(name, pos.getX(), pos.getY(), pos.getZ(), world, texture, true);
                            WaypointManager.addWaypoint(waypoint);
                            context.getSource().sendSuccess(() -> Component.literal("Waypoint added: " + name), true);
                            return 1;
                        })))));
    }
}