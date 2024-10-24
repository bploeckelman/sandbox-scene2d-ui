package lando.systems.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.kotcrab.vis.ui.VisUI;
import lando.systems.game.ui.MainMenu;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    OrthographicCamera camera;
    SpriteBatch batch;
    Color backgroundColor;
    Texture gdx;

    TextureAtlas atlas;
    Skin skin;

    Viewport viewport;
    Stage stage;
    DragAndDrop dragAndDrop;

    Table root;
    MainMenu mainMenu;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch = new SpriteBatch();
        backgroundColor = new Color(0.15f, 0.15f, 0.2f, 1f);
        gdx = new Texture("libgdx.png");

        atlas = new TextureAtlas(Gdx.files.internal("ui/skin-talos/uiskin.atlas"));
        skin = new Skin(Gdx.files.internal("ui/skin-talos/uiskin.json"), atlas);
        skin.addRegions(atlas);
        VisUI.load(skin);

        viewport = new ScreenViewport();
        stage = new Stage(viewport);
        Gdx.input.setInputProcessor(stage);
        dragAndDrop = new DragAndDrop();

        root = new Table();
        root.setFillParent(true);
        stage.addActor(root);

        defaults();
        populate();
    }

    public void defaults() {
        root.top().left();
    }

    public void populate() {
        root.clearChildren();

        mainMenu = new MainMenu(stage, skin);
        root.add(mainMenu).growX();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height);
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
