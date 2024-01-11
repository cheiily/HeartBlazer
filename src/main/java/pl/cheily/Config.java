package pl.cheily;

import com.moandjiezana.toml.Toml;

import java.io.File;
import java.util.List;

public class Config {
    public static String token = "";

    public static String selfId = "";

    public static boolean messageCommandsOn;

    public static String prefix = "";

    public static String version = "";

    public static String ownerId = "";

    public static boolean ownerBypass;

    public static List<String> administratorWhitelist = List.of();

    public static List<String> moderatorWhitelist = List.of();


    public static void load() {
        Toml toml = new Toml().read(new File("config.toml"));
        prefix = toml.getString("prefix");
        messageCommandsOn = toml.getBoolean("message_commands_on");
        token = toml.getString("token");
        ownerId = toml.getString("owner");
        ownerBypass = toml.getBoolean("owner_bypass");
        administratorWhitelist = toml.getList("administrators");
        moderatorWhitelist = toml.getList("moderators");
        version = toml.getString("version");
    }
}
