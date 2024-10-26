package lando.systems.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
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
    public OrthographicCamera windowCamera;
    public OrthographicCamera uiCamera;
    public InputMultiplexer inputMux;

    Color backgroundColor;
    TextureAtlas atlas;
    Texture gdx;
    Texture pixel;
    TextureRegion pixelRegion;

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
    Toolbar tools;
    NodeBoard board;

    public Main() {
        Main.get = this;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapes = new ShapeDrawer(batch);
        inputMux = new InputMultiplexer();

        windowCamera = new OrthographicCamera();
        windowCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        windowCamera.update();

        uiCamera = new OrthographicCamera();
        uiCamera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.update();

        backgroundColor = new Color(0.15f, 0.15f, 0.2f, 1f);
        gdx = new Texture("libgdx.png");

        // create a single pixel texture and associated region
        var pixmap = new Pixmap(2, 2, Pixmap.Format.RGBA8888);
        {
            pixmap.setColor(Color.WHITE);
            pixmap.drawPixel(0, 0);
            pixmap.drawPixel(1, 0);
            pixmap.drawPixel(0, 1);
            pixmap.drawPixel(1, 1);

            pixel = new Texture(pixmap);
            pixelRegion = new TextureRegion(pixel);
        }
        pixmap.dispose();
        shapes.setTextureRegion(pixelRegion);

        var skinFile = "ui/skin-talos/uiskin";
        atlas = new TextureAtlas(Gdx.files.internal(skinFile + ".atlas"));
        radioBtnTextures = new RadioButtonTextures(
            atlas.findRegion("vis-radio"),
            atlas.findRegion("vis-radio-over"),
            atlas.findRegion("vis-radio-down"),
            atlas.findRegion("vis-radio-tick"),
            atlas.findRegion("vis-radio-tick-disabled"),
            atlas.findRegion("border-circle"),
            atlas.findRegion("border-circle-error")
        );

        skin = new Skin(Gdx.files.internal(skinFile + ".json"), atlas);
        skin.addRegions(atlas);
        VisUI.load(skin);
        VisUI.setDefaultTitleAlign(Align.center);

        stage = new Stage(new ScreenViewport(uiCamera));
        //stage.setDebugTableUnderMouse(Table.Debug.all);

        // NOTE: the board is independent of the rest of the ui,
        //  and needs to be added first so it behaves like a background canvas
        board = new NodeBoard(stage, skin);
        stage.addActor(board);

        root = new VisTable(true);
        root.setFillParent(true);
        stage.addActor(root);

        // NOTE: workaround for the board's input listener not receiving the 'scrolled' event
        stage.addListener(new InputListener() {
            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (board.hit(x, y, true) != null) {
                    board.zoomBy(amountY);
                    return true;
                }
                return false;
            }
        });

        setDefaults();
        populateRoot();

        inputMux.addProcessor(stage);
        Gdx.input.setInputProcessor(inputMux);
    }

    public void setDefaults() {
        root.defaults().top().left();
    }

    public void populateRoot() {
        root.clearChildren();

        menu = new MainMenu(stage, skin);
        workspace = new VisTable(true);
        tools = new Toolbar(stage, skin);

        var toolsWidth = Value.percentWidth(1 / 4f, root);
        var emptyWidth = Value.percentWidth(3 / 4f, root);
        workspace.add(tools).width(toolsWidth).growY();
        workspace.add().width(emptyWidth).grow();

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

        windowCamera.update();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        update(dt);

        ScreenUtils.clear(backgroundColor);

        float margin = 50;
        float x = windowCamera.viewportWidth - gdx.getWidth() - margin;

        batch.setProjectionMatrix(windowCamera.combined);
        batch.begin();
        batch.draw(gdx, x, margin);
        batch.end();

        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        batch.dispose();
        gdx.dispose();
        pixel.dispose();
        atlas.dispose();
        VisUI.dispose();
    }
}
