package lando.systems.game.ui;

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

public class NodeBoard extends WidgetGroup {

    final Stage stage;
    final Skin skin;

    final Array<Node> nodes = new Array<>();
    final Array<Node.Connection> connections = new Array<>();

    private final Array<Vector2> path = new Array<>();

    public NodeBoard(Stage stage, Skin skin) {
        this.stage = stage;
        this.skin = skin;

        setStage(stage);
        build();

//        addPanAndZoomListeners();
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

        // setup a bunch of ports for testing
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

        nodes.addAll(node1, node2);
        nodes.forEach(this::addActor);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        drawGrid(batch);

        super.draw(batch, parentAlpha);

        for (var connection : connections) {
            var shapes = Main.get.shapes;
            shapes.setColor(1, 0, 1, 1);
            shapes.path(path, 2, JoinType.SMOOTH, true);
            shapes.setColor(1, 1, 1, 1);
        }
    }

    private void drawGrid(Batch batch) {
        var shapes = Main.get.shapes;
        var camera = (OrthographicCamera) stage.getCamera();
        var color = Color.DARK_GRAY;
        var lineWidth = 1f;

        shapes.getBatch().setProjectionMatrix(stage.getCamera().combined);
        shapes.getBatch().begin();

        float gridSize = 50;
        float left = camera.position.x - camera.viewportWidth / 2 * camera.zoom;
        float right = camera.position.x + camera.viewportWidth / 2 * camera.zoom;
        float bottom = camera.position.y - camera.viewportHeight / 2 * camera.zoom;
        float top = camera.position.y + camera.viewportHeight / 2 * camera.zoom;
        float xOffset = left % gridSize;
        float yOffset = bottom % gridSize;

        for (float x = left - xOffset; x < right; x += gridSize) {
            shapes.line(x, bottom, x, top, color, lineWidth);
        }
        for (float y = bottom - yOffset; y < top; y += gridSize) {
            shapes.line(left, y, right, y, color, lineWidth);
        }
        shapes.getBatch().end();
    }

    private void addPanAndZoomListeners() {
        addListener(new InputListener() {
            private final Vector2 lastTouch = new Vector2();
            private boolean isDraggingNode = false;

            @Override
            public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
                var hitActor = stage.hit(x, y, true);
                isDraggingNode = (hitActor instanceof Node);
                if (!isDraggingNode) {
                    lastTouch.set(x, y);
                }
                return true;
            }

            @Override
            public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
                isDraggingNode = false;
            }

            @Override
            public void touchDragged(InputEvent event, float x, float y, int pointer) {
                if (!isDraggingNode) {
                    var camera = (OrthographicCamera) stage.getCamera();
                    camera.position.add(lastTouch.x - x, lastTouch.y - y, 0);
                    lastTouch.set(x, y);
                }
            }

            @Override
            public boolean scrolled(InputEvent event, float x, float y, float amountX, float amountY) {
                if (!isDraggingNode) {
                    var camera = (OrthographicCamera) stage.getCamera();
                    camera.position.add(lastTouch.x - x, lastTouch.y - y, 0);
                    camera.zoom += amountY * 0.1f; // Zoom factor
                    camera.zoom = MathUtils.clamp(camera.zoom, 0.5f, 2f); // Limit zoom range
                }
                return true;
            }
        });
    }
}
