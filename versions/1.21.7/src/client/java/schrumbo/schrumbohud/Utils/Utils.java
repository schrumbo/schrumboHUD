package schrumbo.schrumbohud.Utils;

import schrumbo.schrumbohud.clickgui.ClickGuiScreen;
import schrumbo.schrumbohud.clickgui.categories.Category;
import schrumbo.schrumbohud.clickgui.widgets.ColorPickerWidget;
import schrumbo.schrumbohud.clickgui.widgets.Widget;

import java.util.List;

public class Utils {
    /**
     * Checks if any ColorPicker popup is currently open in any category.
     * @return true if any popup is open, false otherwise
     */
    public static boolean isInColorPickerWidget() {
        List<Category> categories = ClickGuiScreen.categories;
        for (Category category : categories) {
            if (!category.isCollapsed()) {
                for (Widget widget : category.widgets) {
                    if (widget instanceof ColorPickerWidget colorPicker) {
                        if (colorPicker.isPopupOpen()) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }
}
