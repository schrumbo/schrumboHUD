package schrumbo.schrumbohud.clickgui.categories;
import schrumbo.schrumbohud.SchrumboHUDClient;
import schrumbo.schrumbohud.clickgui.widgets.ButtonWidget;
import schrumbo.schrumbohud.config.ConfigManager;



public class PresetsCategory extends Category {


    public PresetsCategory() {
        super("Presets");
    }

    @Override
    public void initializeWidgets(int startX, int startY, int width) {

        int currentY = startY;

        ButtonWidget catppucinMocha = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Catppucin Mocha")
                .onClick(()->{
                    SchrumboHUDClient.config.loadCatppuccinMocha();
                    ConfigManager.save();
                })
                .build();
        widgets.add(catppucinMocha);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ButtonWidget gruvbox = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Gruvbox")
                .onClick(()->{
                    SchrumboHUDClient.config.loadGruvbox();
                    ConfigManager.save();
                })
                .build();
        widgets.add(gruvbox);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ButtonWidget monokai = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Monokai")
                .onClick(()->{
                    SchrumboHUDClient.config.loadMonokai();
                    ConfigManager.save();
                })
                .build();
        widgets.add(monokai);

        currentY += widgets.get(widgets.size() - 1).getHeight() + WIDGET_SPACING;

        ButtonWidget dracula = ButtonWidget.builder()
                .y(currentY)
                .width(width)
                .label("Dracula")
                .onClick(()->{
                    SchrumboHUDClient.config.loadDracula();
                    ConfigManager.save();
                })
                .build();
        widgets.add(dracula);
        updateWidgetPositions(startX, startY);
    }

}
