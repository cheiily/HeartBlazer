package pl.cheily;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;
import pl.cheily.Actions.Action;
import pl.cheily.Actions.ActionHandler;

import java.util.Objects;
import java.util.stream.Collectors;

public class HeartBlazer {
    public final static Logger logger = LoggerFactory.getLogger("HeartBlazer-Log-main-v" + Config.version);
    public final static Marker mBotFeedback = MarkerFactory.getIMarkerFactory().getMarker("BOT-FEEDBACK");
    public final static ActionHandler actionHandler = new ActionHandler();


    public static void main(String[] args) {
        Config.load();
        actionHandler.initialize();
        startup();
    }

    public static void startup() {
        FirstEventListener client = new FirstEventListener();
        JDA jda = JDABuilder.createDefault(Config.token)
                .addEventListeners(client)
                .build();

        var actionConfigs = actionHandler.loadedActions.stream()
                .map(Action::getContextCommandLoadConfiguration)
                .filter(Objects::nonNull)
                .toList();

        jda.updateCommands().addCommands(actionConfigs).queue(
                cmds -> logger.debug(mBotFeedback, "Configured context commands for: " + cmds.toString())
        );

        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            onStartupFailure(e);
        }

        onStartupSuccess(jda);
    }

    public static void onStartupSuccess(JDA jda) {
        logger.info(mBotFeedback, "----- NEW SESSION -----");
        logger.debug(mBotFeedback, "Gateway ping: " + jda.getGatewayPing() + "ms.");
        Config.selfId = jda.getSelfUser().getId();
        logger.info(mBotFeedback, "Connected to guilds: " + jda.getGuilds().stream().map(g -> g.getName() + " (" + g.getId() + ")").collect(Collectors.joining(", ")));
    }

    public static void onStartupFailure(Exception ex) {
        logger.error(mBotFeedback, "Startup failure!", ex);
    }

}