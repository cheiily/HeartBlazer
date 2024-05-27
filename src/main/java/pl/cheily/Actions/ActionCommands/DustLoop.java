package pl.cheily.Actions.ActionCommands;

import com.google.gson.JsonObject;
import dustgrain.RunKt;
import dustgrain.model.Model;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import pl.cheily.Actions.Action;
import pl.cheily.Actions.ActionRequestType;
import pl.cheily.Actions.ActionResult;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
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
    public static final String PROP_CMD_EXTRA = "extra";
    public static final String PROP_CMD_HELP = "help";
    public static final String PROP_ARG_WIKI = "wiki";
    public static final String PROP_ARG_CHAR = "character";
    public static final String PROP_ARG_MOVE = "move";
    public static final String PROP_ARG_INPUT = "input";
    public static final String PROP_ARG_COMMAND = "command";
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
        String input;

        SlashCommandInteractionEvent cRequest = (SlashCommandInteractionEvent) request;
        InteractionHook hook = cRequest.deferReply().complete();

        command = cRequest.getSubcommandName();
        character = cRequest.getOption(PROP_ARG_CHAR, OptionMapping::getAsString);
        move = cRequest.getOption(PROP_ARG_MOVE, OptionMapping::getAsString);
        wiki = cRequest.getOption(PROP_ARG_WIKI, DEFAULT_ARG_WIKI, OptionMapping::getAsString);
        input = cRequest.getOption(PROP_ARG_INPUT, OptionMapping::getAsString);

        switch (command) {
            case PROP_CMD_LIST -> {
                if ( character == null ) {
                    hook.editOriginal("Parsing failure - no character. Try verifying your inputs.").queue();
                    return ActionResult.FAILURE("Parsing failure - no character.", null);
                }
                Map<String, List<String>> moves;

                try {
                    moves = Model.INSTANCE.mapOfMoves(wiki, character, null);
                } catch (Exception ex) {
                    hook.editOriginal("Failed to get data from dustloop. Try verifying your inputs or try again later.").queue();
                    return ActionResult.FAILURE("Failed to obtain data from dustloop. (dustgrain exception)", ex);
                }

                EmbedBuilder eb = new EmbedBuilder();
                eb.setTitle("Available moves for " + character + " (" + wiki + ')');
                moves.forEach((key, val) -> {
                    if (!val.isEmpty())
                        eb.addField(key, String.join(", ", val), false);
                });
                Color clr = cRequest.isFromGuild() ? cRequest.getMember().getColor() : Color.WHITE;
                eb.setColor(clr);

                hook.editOriginalEmbeds(eb.build()).queue();
            }
            case PROP_CMD_DATA -> {
                if ( character == null ) {
                    hook.editOriginal("Parsing failure - no character. Try verifying your inputs.").queue();
                    return ActionResult.FAILURE("Parsing failure - no character.", null);
                }
                if (move == null) {
                    hook.editOriginal("Parsing failure - no move. Try verifying your inputs.").queue();
                    return ActionResult.FAILURE("Parsing failure - no move.", null);
                }
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

                hook.editOriginalEmbeds(eb.build()).queue();
            }
//            case PROP_CMD_EXTRA -> {
//                PrintStream sys = System.out;
//                try (ByteArrayOutputStream bostream = new ByteArrayOutputStream(); PrintStream pstream = new PrintStream(bostream)) {
//                    System.setOut(pstream);
//                    input += " -p";
//                    dustgrain.RunKt.main(parseCli(input));
//                    String out = bostream.toString();
//                    EmbedBuilder eb = new EmbedBuilder();
//                    eb.setTitle("Response: " + ' ' + input);
//                    eb.setDescription(out);
//
//                    Color clr = cRequest.isFromGuild() ? cRequest.getMember().getColor() : Color.WHITE;
//                    eb.setColor(clr);
//
//                    eb.setFooter("For help see https://github.com/cheiily/DustGrain/blob/master/README.md");
//
//                    hook.editOriginalEmbeds(eb.build()).queue();
//                } catch (IOException e) {
//                    hook.editOriginal("Failed to get data from dustloop. Try verifying your inputs or see `/dust help`. See https://github.com/cheiily/DustGrain/blob/master/README.md.").queue();
//                } finally {
//                    System.setOut(sys);
//                }
//            }
//            case PROP_CMD_HELP -> {
//                PrintStream sys = System.out;
//                try (ByteArrayOutputStream bostream = new ByteArrayOutputStream(); PrintStream pstream = new PrintStream(bostream)) {
//                    System.setOut(pstream);
//                    input += " -p --help";
//                    dustgrain.RunKt.main(parseCli(input));
//                    String out = bostream.toString();
//                    EmbedBuilder eb = new EmbedBuilder();
//                    eb.setTitle("Response: " + ' ' + input);
//                    eb.setDescription(out);
//
//                    Color clr = cRequest.isFromGuild() ? cRequest.getMember().getColor() : Color.WHITE;
//                    eb.setColor(clr);
//
//                    eb.setFooter("See https://github.com/cheiily/DustGrain/blob/master/README.md");
//
//                    hook.editOriginalEmbeds(eb.build()).queue();
//                } catch (IOException e) {
//                    hook.editOriginal("Failed to get data from dustloop. Try verifying your inputs or see `/dust help`. See https://github.com/cheiily/DustGrain/blob/master/README.md.").queue();
//                } finally {
//                    System.setOut(sys);
//                }
//            }
        }

        return ActionResult.SUCCESS_ACCEPT;
    }

    String[] parseCli(String in) {
        in += ' ';
        List<String> ret = new ArrayList<>();
        String buf = "";

        for(int i = 0; i < in.length(); ++i) {
            char c = in.charAt(i);
            if (c == ' ' && (!buf.contains("\"") || buf.lastIndexOf('"') != buf.indexOf('"'))) {
                buf = buf.replaceAll("\"", "");
                ret.add(buf);
                buf = "";
            } else {
                buf += c;
            }
        }

        return ret.toArray(new String[0]);
    }
}
