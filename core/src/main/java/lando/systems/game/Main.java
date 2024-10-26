package lando.systems.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Event;
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
    Stage boardStage;

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
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        backgroundColor = new Color(0.15f, 0.15f, 0.2f, 1f);
        gdx = new Texture("libgdx.png");

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

        // the board is its own independent actor, acting as the background of the stage
        // with pan/zoom capabilities and node interaction independent of the rest of the UI
        boardStage = new Stage(new ScreenViewport());
        board = new NodeBoard(boardStage, skin);
        boardStage.addActor(board);

        root = new VisTable(true);
        root.setFillParent(true);
        stage = new Stage(new ScreenViewport());
        stage.addActor(root);

//        stage.setDebugTableUnderMouse(Table.Debug.all);
//        stage.setDebugAll(true);

        var inputMux = new InputMultiplexer(stage, boardStage);
        Gdx.input.setInputProcessor(inputMux);

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

        boardStage.getViewport().update(width, height, true);
        board.setBounds(0, 0, stage.getWidth(), stage.getHeight());

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
        boardStage.act(dt);

        camera.update();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();
        update(dt);

        ScreenUtils.clear(backgroundColor);

        float margin = 50;
        float x = camera.viewportWidth - gdx.getWidth() - margin;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(gdx, x, margin);
        batch.end();

        boardStage.draw();
        stage.draw();
    }

    @Override
    public void dispose() {
        boardStage.dispose();
        stage.dispose();
        batch.dispose();
        gdx.dispose();
        atlas.dispose();
        VisUI.dispose();
    }
}
