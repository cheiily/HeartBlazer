package pl.cheily.Actions.ActionCommands;

import model.Model;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import pl.cheily.Actions.Action;
import pl.cheily.Actions.ActionRequestType;
import pl.cheily.Actions.ActionResult;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class DustLoop extends Action {
    public DustLoop() {
        this.name = PROP_NAME;
        this.acceptedRequestTypes = Set.of(
                ActionRequestType.SLASH_COMMAND
        );
    }

    public static final String PROP_NAME = "dust";
    public static final String PROP_CMD_LIST = "list";
    public static final String PROP_CMD_DATA = "data";
    public static final String PROP_ARG_WIKI = "wiki";
    public static final String PROP_ARG_CHAR = "character";
    public static final String PROP_ARG_MOVE = "move";
    public static final String DEFAULT_ARG_WIKI = "GBVSR";

    private static DustLoop _instance;

    public static DustLoop instance() {
        if ( _instance == null ) _instance = new DustLoop();
        return _instance;
    }

    @Override
    public ActionResult invoke(GenericEvent request, ActionRequestType requestType) {
        String command;
        String wiki;
        String character;
        String move;

        SlashCommandInteractionEvent cRequest = (SlashCommandInteractionEvent) request;
        InteractionHook hook = cRequest.deferReply().complete();

        command = cRequest.getSubcommandName();
        character = cRequest.getOption(PROP_ARG_CHAR, OptionMapping::getAsString);
        move = cRequest.getOption(PROP_ARG_MOVE, OptionMapping::getAsString);
        wiki = cRequest.getOption(PROP_ARG_WIKI, DEFAULT_ARG_WIKI, OptionMapping::getAsString);

        if ( command == null ) return ActionResult.FAILURE("Parsing failure - no command.", null);
        if ( character == null ) return ActionResult.FAILURE("Parsing failure - no character.", null);

        if ( command.equals(PROP_CMD_LIST) ) {
            List<String> moves;

            try {
                moves = Model.INSTANCE.listMoves(wiki, character);
            } catch (Exception ex) {
                hook.editOriginal("Failed to get data from dustloop. Try verifying your inputs.").queue();
                return ActionResult.FAILURE("Failed to obtain data from dustloop. (dustgrain exception)", ex);
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle("Available moves for " + character + " (" + wiki + ')');
            eb.setDescription(String.join(", ", moves));
            Color clr = cRequest.isFromGuild() ? cRequest.getMember().getColor() : Color.WHITE;
            eb.setColor(clr);

            hook.editOriginalEmbeds( eb.build() ).queue();
        } else if ( command.equals(PROP_CMD_DATA) ) {
            if ( move == null ) return ActionResult.FAILURE("Parsing failure - no move.", null);
            Map<String, String> data;

            try {
                data = Model.INSTANCE.getData(wiki, character, move);
            } catch (Exception ex) {
                hook.editOriginal("Failed to get data from dustloop. Try verifying your inputs or see `/dust list`.").queue();
                return ActionResult.FAILURE("Failed to obtain data from dustloop. (dustgrain exception)", ex);
            }

            EmbedBuilder eb = new EmbedBuilder();
            eb.setTitle(character + ' ' + data.get("input") + " (" + wiki + ')');
            data.forEach((key, val) -> {
                if (!key.equals("input"))
                    eb.addField(key, val, true);
            });

            Color clr = cRequest.isFromGuild() ? cRequest.getMember().getColor() : Color.WHITE;
            eb.setColor(clr);

            hook.editOriginalEmbeds( eb.build() ).queue();
        }

        return ActionResult.SUCCESS_ACCEPT;
    }
}
