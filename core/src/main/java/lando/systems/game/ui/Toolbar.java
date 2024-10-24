package lando.systems.game.ui;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.github.czyzby.lml.vis.ui.VisTabTable;
import com.kotcrab.vis.ui.widget.VisSplitPane;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneAdapter;

public class Toolbar extends VisTable {

    Stage stage;
    Skin skin;
    TabbedPane pane;
    VisTable container;

    public Toolbar(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;
        setSkin(skin);
        build();
    }

    public void build() {
        container = new VisTable();
        container.defaults().pad(5).left().top();

        var originalPaneStyle = skin.get(TabbedPane.TabbedPaneStyle.class);
        pane = new TabbedPane(new TabbedPane.TabbedPaneStyle(originalPaneStyle) {{
            this.draggable = false;
        }});
        pane.getTable().defaults().pad(5).left().top();
        pane.addListener(new TabbedPaneAdapter() {
            @Override
            public void switchedTab(Tab tab) {
                container.clearChildren();
                container.add(tab.getContentTable()).grow();
            }
        });

        // add the tab pane and tab container to the root table (this)

        add(pane.getTable()).growX();
        row();
        add(container).grow();

        // create a vertical split for one of the tabs

        var top = new VisTable();
        top.background(new TextureRegionDrawable(skin.getRegion("vis-blue")));
        top.add("Top Table").growX().row();
        top.add().grow();

        var bottom = new VisTable();
        bottom.background(new TextureRegionDrawable(skin.getRegion("vis-red")));
        bottom.add("Bottom Table").growX().row();
        bottom.add().grow();

        var split = new VisSplitPane(top, bottom, true);
        split.setMinSplitAmount(0.3f);
        split.setMaxSplitAmount(0.7f);
        split.setSplitAmount(0.7f);

        // populate the pane with tabs

        var tab1 = new VisTabTable("Tab One");
        var tab2 = new VisTabTable("Tab Two");
        tab1.getTab().setCloseableByUser(false);
        tab2.getTab().setCloseableByUser(false);

        tab1.add(split).grow();
        tab2.add("This is the second tab").growX().row();
        tab2.add().grow();

        pane.add(tab1.getTab());
        pane.add(tab2.getTab());
        pane.switchTab(tab1.getTab());
    }
}
