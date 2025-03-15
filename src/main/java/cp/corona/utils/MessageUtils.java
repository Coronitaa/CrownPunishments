//MessageUtils.java
package cp.corona.utils;

/**
 * Utility class for handling messages and color formatting.
 */
public class MessageUtils {

    /**
     * Gets a color formatted message, supporting both legacy and RGB color codes.
     * This method uses the {@link ColorUtils#translateRGBColors(String)} method to translate
     * color codes in the message, supporting both legacy color codes (using '&') and RGB hex codes.
     *
     * @param message The message to format with colors.
     * @return The color formatted message.
     */
    public static String getColorMessage(String message){
        return ColorUtils.translateRGBColors(message);
    }
}