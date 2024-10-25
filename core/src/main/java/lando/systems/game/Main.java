package lando.systems.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.PopupMenu;
import com.kotcrab.vis.ui.widget.VisTable;
import lando.systems.game.ui.MainMenu;
import lando.systems.game.ui.NodeBoard;
import lando.systems.game.ui.Toolbar;
import space.earlygrey.shapedrawer.ShapeDrawer;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    public static Main get;

    public SpriteBatch batch;
    public ShapeDrawer shapes;
    public OrthographicCamera camera;

    Color backgroundColor;
    TextureAtlas atlas;
    Texture gdx;

    // NOTE: for convenience to group all these uiskin atlas textures
    public record RadioButtonTextures(
        TextureRegion normal,
        TextureRegion over,
        TextureRegion down,
        TextureRegion tick,
        TextureRegion tickDisabled,
        TextureRegion focusBorder,
        TextureRegion errorBorder
    ) {}
    public RadioButtonTextures radioBtnTextures;

    Skin skin;
    Stage stage;

    VisTable root;
    MainMenu menu;
    VisTable workspace;
    Toolbar toolbar;
    NodeBoard board;

    public Main() {
        Main.get = this;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeDrawer(batch);
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        backgroundColor = new Color(0.15f, 0.15f, 0.2f, 1f);
        atlas = new TextureAtlas(Gdx.files.internal("ui/skin-talos/uiskin.atlas"));
        gdx = new Texture("libgdx.png");
        radioBtnTextures = new RadioButtonTextures(
            atlas.findRegion("vis-radio"),
            atlas.findRegion("vis-radio-over"),
            atlas.findRegion("vis-radio-down"),
            atlas.findRegion("vis-radio-tick"),
            atlas.findRegion("vis-radio-tick-disabled"),
            atlas.findRegion("border-circle"),
            atlas.findRegion("border-circle-error")
        );

        skin = new Skin(Gdx.files.internal("ui/skin-talos/uiskin.json"), atlas);
        skin.addRegions(atlas);
        VisUI.load(skin);
        VisUI.setDefaultTitleAlign(Align.center);

        root = new VisTable(true);
        root.setFillParent(true);

        stage = new Stage(new ScreenViewport());
        stage.addActor(root);
        stage.setDebugTableUnderMouse(Table.Debug.all);
        Gdx.input.setInputProcessor(stage);

        setDefaults();
        populateRoot();
    }

    public void setDefaults() {
        root.defaults().top().left();
    }

    public void populateRoot() {
        root.clearChildren();

        menu = new MainMenu(stage, skin);
        workspace = new VisTable(true);

        toolbar = new Toolbar(stage, skin);
        board = new NodeBoard(stage, skin);

        workspace.add(toolbar).width(Value.percentWidth(0.25f, root)).growY();
        workspace.add(board).width(Value.percentWidth(0.75f, root)).grow();

        root.add(menu).growX();
        root.row();
        root.add(workspace).grow();
    }

    @Override
    public void resize(int width, int height) {
        super.resize(width, height);

        PopupMenu.removeEveryMenu(stage);

        stage.getViewport().update(width, height, true);

        var resizeEvent = new Event();
        for (var actor : stage.getActors()) {
            actor.fire(resizeEvent);
        }
    }

    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
        }
        stage.act(dt);
        camera.update();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        update(dt);

        ScreenUtils.clear(backgroundColor);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(gdx, 140, 210);
        batch.draw(atlas.findRegion("window"), 200, 200, 200, 200);
        batch.end();

        stage.draw();
    }

    @Override
    public void dispose() {
        batch.dispose();
        gdx.dispose();
        atlas.dispose();
        VisUI.dispose();
    }
}
