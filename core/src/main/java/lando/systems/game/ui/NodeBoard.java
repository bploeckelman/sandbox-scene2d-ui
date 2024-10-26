package lando.systems.game.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.utils.Array;
import lando.systems.game.Edge;
import lando.systems.game.Main;
import space.earlygrey.shapedrawer.JoinType;
import space.earlygrey.shapedrawer.ShapeDrawer;

public class NodeBoard extends WidgetGroup {

    final Stage stage;
    final Skin skin;
    final OrthographicCamera camera;

    final Array<Node> nodes = new Array<>();
    final Array<Node.Connection> connections = new Array<>();

    boolean panning = false;

    private final Array<Vector2> path = new Array<>();

    public NodeBoard(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        // NOTE: the board keeps its own camera for pan/zoom independent of the stage
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        this.camera.update();

        setStage(stage);
        build();
    }

    public void build() {
        clearChildren();

        var node1 = new Node("Node 1", stage, skin);
        var node2 = new Node("Node 2", stage, skin);

        float spacing = 100;
        float x1 = 600;
        float x2 = x1 + node1.getWidth() + spacing;
        float y1 = (stage.getHeight() - node1.getHeight()) / 2f;
        float y2 = (stage.getHeight() - node2.getHeight()) / 2f;
        node1.setPosition(x1, y1);
        node2.setPosition(x2, y2);

        buildTestPorts(node1, node2);

        nodes.addAll(node1, node2);
        nodes.forEach(this::addActor);

        addListener(panZoomListener);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        // NOTE: this is needed so that input events are processed by the board
        setBounds(0, 0, stage.getWidth(), stage.getHeight());
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        var shapes = Main.get.shapes;

        // apply the board's camera consistently to everything drawn by the board
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.getBatch().setProjectionMatrix(camera.combined);

        drawGrid(batch, shapes);
        // NOTE: super.draw(batch, parentAlpha) applies the stage's camera
        //  which causes problems keeping the board and its contents independent
        //  of the other ui in the stage that's suppose to remain fixed
        //  relative to the stage's camera, not the board's camera,
        //  so we'll draw the children 'manually' instead.
        //super.draw(batch, parentAlpha);
        for (var child : getChildren()) {
            if (child.isVisible()) {
                child.draw(batch, parentAlpha);
            }
        }
        drawConnections(batch, shapes);

        // restore the stage's camera for the rest of the ui
        var stageCamera = getStage().getCamera();
        shapes.getBatch().setProjectionMatrix(stageCamera.combined);
        batch.setProjectionMatrix(stageCamera.combined);
    }

    private void drawGrid(Batch batch, ShapeDrawer shapes) {
        var color = Color.DARK_GRAY;
        float lineWidth = 1f;
        float grid = 50;
        float left = camera.position.x - camera.viewportWidth / 2 * camera.zoom;
        float right = camera.position.x + camera.viewportWidth / 2 * camera.zoom;
        float bottom = camera.position.y - camera.viewportHeight / 2 * camera.zoom;
        float top = camera.position.y + camera.viewportHeight / 2 * camera.zoom;
        float xOffset = left % grid;
        float yOffset = bottom % grid;

        float startX = left - xOffset;
        float endX = right;
        float startY = bottom - yOffset;
        float endY = top;

        shapes.getBatch().begin();
        for (float x = startX; x < endX; x += grid) shapes.line(x, bottom, x, top, color, lineWidth);
        for (float y = startY; y < endY; y += grid) shapes.line(left, y, right, y, color, lineWidth);
        shapes.getBatch().end();
    }

    private void drawConnections(Batch batch, ShapeDrawer shapes) {
        shapes.getBatch().begin();
        for (var connection : connections) {
            shapes.setColor(1, 0, 1, 1);
            shapes.path(path, 2, JoinType.SMOOTH, true);
            shapes.setColor(1, 1, 1, 1);
        }
        shapes.getBatch().end();
    }

    private void buildTestPorts(Node node1, Node node2) {
        for (var edge : Edge.values()) {
            int count;

            // inputs
            if (edge == Edge.LEFT || edge == Edge.TOP) {
                count = MathUtils.random(1, 5);
                for (int i = 1; i <= count; i++) {
                    node1.addInput("in_%s_%d".formatted(edge, i), edge);
                }
                count = MathUtils.random(1, 5);
                for (int i = 1; i <= count; i++) {
                    node2.addInput("in_%s_%d".formatted(edge, i), edge);
                }
            }

            // outputs
            if (edge == Edge.BOTTOM || edge == Edge.RIGHT) {
                count = MathUtils.random(1, 5);
                for (int i = 1; i <= count; i++) {
                    node1.addOutput("out_%s_%d".formatted(edge, i), edge);
                }
                count = MathUtils.random(1, 5);
                for (int i = 1; i <= count; i++) {
                    node2.addOutput("out_%s_%d".formatted(edge, i), edge);
                }
            }
        }
    }

    // NOTE: zooming has to be handled separately because the 'scrolled' event
    //  doesn't trigger with a handler method in this listener, so it gets
    //  dispatched from the Stage's listener instead.
    //  Not sure why, but the workaround gets the job done for now.
    private final InputListener panZoomListener = new InputListener() {

        private final Vector2 touchStart = new Vector2();

        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            Gdx.app.log("Board", "touched: p=%d|b=%d @ (%.0f, %.0f)".formatted(pointer, button, x, y));
            touchStart.set(x, y);
            return true;
        }

        @Override
        public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
            super.touchUp(event, x, y, pointer, button);
            if (panning) {
                Gdx.app.log("Board", "drag stopped");
            }
            panning = false;
        }

        @Override
        public void touchDragged(InputEvent event, float x, float y, int pointer) {
            if (!panning) {
                panning = true;
                // log once per drag
                Gdx.app.log("Board", "drag started");
            }

            float dx = touchStart.x - x;
            float dy = touchStart.y - y;
            touchStart.set(x, y);

            camera.position.add(dx, dy, 0);
            camera.update();
        }
    };

    public void zoomBy(float amount) {
        // TODO(brian): scale amount relative to current zoom level and extents
        camera.zoom += amount * 0.05f;
        camera.zoom = MathUtils.clamp(camera.zoom, 0.1f, 2f);
        camera.update();
    }
}
