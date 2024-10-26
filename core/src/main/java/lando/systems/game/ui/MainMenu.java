package lando.systems.game.ui;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.kotcrab.vis.ui.util.dialog.Dialogs;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;
import com.kotcrab.vis.ui.widget.VisTable;

public class MainMenu extends VisTable {

    Stage stage;
    MenuBar menuBar;
    Menu menuFile;
    Menu menuHelp;
    MenuItem helpAbout;

    public MainMenu(Stage stage, Skin skin) {
        super(true);
        this.stage = stage;

        setSkin(skin);
        setBackground(skin.getDrawable("button-main-menu"));

        build();
    }

    public void build() {
        clearChildren();

        // create widgets

        menuBar = new MenuBar();

        menuFile = new Menu("File");

        menuHelp = new Menu("Help");
        helpAbout = new MenuItem("About");
        menuHelp.addItem(helpAbout);

        menuBar.addMenu(menuFile);
        menuBar.addMenu(menuHelp);

        // add event listeners

        menuBar.setMenuListener(new MenuBar.MenuBarListener() {
            @Override
            public void menuOpened(Menu menu) {
                //Dialogs.showOKDialog(stage, "Menu", "Opened: " + menu.getTitle());
            }

            @Override
            public void menuClosed(Menu menu) {
                //Dialogs.showOKDialog(stage, "Menu", "Closed: " + menu.getTitle());
            }
        });

        helpAbout.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Dialogs.showOKDialog(stage, "About", "Sandbox: Scene2d UI");
            }
        });

        add(menuBar.getTable()).left().grow();
    }

}
