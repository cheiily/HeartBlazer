package pl.cheily;


import net.dv8tion.jda.api.events.interaction.command.MessageContextInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import static pl.cheily.HeartBlazer.actionHandler;

public class FirstEventListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        actionHandler.accept(event);
    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        actionHandler.accept(event);
    }

    @Override
    public void onMessageContextInteraction(@NotNull MessageContextInteractionEvent event) {
        actionHandler.accept(event);
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        String[] words = event.getMessage().getContentRaw().split(" ");
        if ( words.length == 0 ) return;
        String fWord = words[ 0 ];
        if ( !fWord.startsWith(Config.prefix) ) return;

        fWord = fWord.substring(1);
        actionHandler.accept(event, fWord);
    }
}
