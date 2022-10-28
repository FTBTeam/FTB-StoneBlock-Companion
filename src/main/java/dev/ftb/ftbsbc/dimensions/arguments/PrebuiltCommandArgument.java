package dev.ftb.ftbsbc.dimensions.arguments;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import dev.ftb.ftbsbc.dimensions.kubejs.StoneBlockDataKjs;
import dev.ftb.ftbsbc.dimensions.prebuilt.PrebuiltStructure;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.concurrent.CompletableFuture;

public class PrebuiltCommandArgument implements ArgumentType<PrebuiltStructure> {
    private static final DynamicCommandExceptionType START_NOT_FOUND = new DynamicCommandExceptionType(object -> new TextComponent("Prebuilt Structure '" + object + "' not found!"));

    public static PrebuiltCommandArgument create() {
        return new PrebuiltCommandArgument();
    }

    public static PrebuiltStructure get(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
        return context.getArgument(name, PrebuiltStructure.class);
    }

    private PrebuiltCommandArgument() {
    }

    @Override
    public PrebuiltStructure parse(StringReader reader) throws CommandSyntaxException {
        int i = reader.getCursor();

        while (reader.canRead() && ResourceLocation.isAllowedInResourceLocation(reader.peek())) {
            reader.skip();
        }

        String s = reader.getString().substring(i, reader.getCursor());
        PrebuiltStructure instance = StoneBlockDataKjs.PREBUILT_STRUCTURES.get(s);

        if (instance != null) {
            return instance;
        }

        throw START_NOT_FOUND.createWithContext(reader, s);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder builder) {
        if (commandContext.getSource() instanceof SharedSuggestionProvider) {
            return SharedSuggestionProvider.suggest(StoneBlockDataKjs.PREBUILT_STRUCTURES.keySet(), builder);
        }

        return Suggestions.empty();
    }
}
