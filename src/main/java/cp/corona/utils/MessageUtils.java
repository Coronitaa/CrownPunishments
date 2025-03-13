package cp.corona.utils;

import org.bukkit.ChatColor;

public class MessageUtils {

    public static String getColorMessage(String message){
        return ChatColor.translateAlternateColorCodes('&',message);
    }
}
