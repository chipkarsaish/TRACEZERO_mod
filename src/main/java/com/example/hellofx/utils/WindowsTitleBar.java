package com.example.hellofx.utils;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef.HWND;
import com.sun.jna.ptr.IntByReference;
import javafx.stage.Stage;

import java.lang.reflect.Method;

public class WindowsTitleBar {

    // DWM API Interface definition for JNA
    public interface DwmApi extends Library {
        DwmApi INSTANCE = Native.load("dwmapi", DwmApi.class);

        int DwmSetWindowAttribute(HWND hwnd, int dwAttribute, Pointer pvAttribute, int cbAttribute);
    }

    // Windows 11 Desktop Window Manager attributes
    // https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/ne-dwmapi-dwmwindowattribute
    private static final int DWMWA_USE_IMMERSIVE_DARK_MODE = 20;
    private static final int DWMWA_CAPTION_COLOR = 35;
    private static final int DWMWA_TEXT_COLOR = 36;
    private static final int DWMWA_BORDER_COLOR = 34;

    /**
     * Converts a standard hex color string (e.g., "#0e1014" or "0e1014")
     * into the ABGR integer format required by the Windows DWM API.
     * Note: Windows uses COLORREF which is 0x00bbggrr
     */
    private static int hexToColorRef(String hex) {
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() == 6) {
            int r = Integer.parseInt(hex.substring(0, 2), 16);
            int g = Integer.parseInt(hex.substring(2, 4), 16);
            int b = Integer.parseInt(hex.substring(4, 6), 16);
            // COLORREF format is 0x00bbggrr
            return (b << 16) | (g << 8) | r;
        }
        return 0; // Default to black if parsing fails
    }

    /**
     * Retrieves the native window handle (HWND) for a JavaFX Stage.
     */
    private static HWND getNativeHandleForStage(Stage stage) {
        try {
            String title = stage.getTitle();
            if (title != null && !title.isEmpty()) {
                // Find window by title using User32
                HWND hwnd = User32.INSTANCE.FindWindow(null, title);
                if (hwnd != null) {
                    return hwnd;
                }
            }
        } catch (Exception e) {
            System.err.println("WindowsTitleBar: Failed to find window handle by title.");
        }
        return null;
    }

    /**
     * Attempts to set the Windows 11 Title Bar colors to match the app theme.
     * MUST be called AFTER stage.show() otherwise the native window handle won't exist yet.
     */
    public static void enableDarkTitleBar(Stage stage) {
        // Fast fail if not on Windows
        if (!System.getProperty("os.name").toLowerCase().contains("win")) {
            return;
        }

        try {
            HWND hwnd = getNativeHandleForStage(stage);
            
            if (hwnd == null) {
                System.err.println("WindowsTitleBar: Could not find HWND for Stage.");
                return;
            }

            // 1. Enable Immersive Dark Mode (ensures window frame & menus follow dark theme)
            IntByReference immersiveDarkMode = new IntByReference(1); // 1 = TRUE
            DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_USE_IMMERSIVE_DARK_MODE,
                    immersiveDarkMode.getPointer(),
                    4
            );

            // 2. Set Caption (Title Bar) Background Color (Win11 only)
            // The theme root background is #0e1014
            int captionColorRef = hexToColorRef("0e1014");
            IntByReference captionColor = new IntByReference(captionColorRef);
            DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_CAPTION_COLOR,
                    captionColor.getPointer(),
                    4
            );

            // 3. Set Text Color (Win11 only)
            // Primary text color is #dde1ef
            int textColorRef = hexToColorRef("dde1ef");
            IntByReference textColor = new IntByReference(textColorRef);
            DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_TEXT_COLOR,
                    textColor.getPointer(),
                    4
            );
            
            // 4. Set Border Color to match the app borders (Win11 only)
            // Top border color in the headers often uses #232736 or similar
            int borderColorRef = hexToColorRef("232736");
            IntByReference borderColor = new IntByReference(borderColorRef);
            DwmApi.INSTANCE.DwmSetWindowAttribute(
                    hwnd,
                    DWMWA_BORDER_COLOR,
                    borderColor.getPointer(),
                    4
            );

            System.out.println("WindowsTitleBar: Applied dark title bar styling via DWM API successfully.");
        } catch (Throwable t) {
            // Use Throwable to catch UnsatisfiedLinkError if JNA isn't fully set up or we are on older Windows
            System.err.println("WindowsTitleBar: Failed to apply DWM attributes for dark mode. Ensure you are on Windows 11 and JNA is loaded correctly.");
            t.printStackTrace();
        }
    }
}
