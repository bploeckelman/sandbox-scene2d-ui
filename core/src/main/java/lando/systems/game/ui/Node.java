package lando.systems.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.utils.ObjectMap;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import lando.systems.game.Edge;
import lando.systems.game.Main;

import javax.sound.sampled.Port;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node extends VisWindow {

    private static final float DEFAULT_SIZE = 300;

    public static float PORT_RADIUS = 5;

    final Stage stage;
    final Skin skin;
    final Map<String, Port> inputs = new HashMap<>();
    final Map<String, Port> outputs = new HashMap<>();
    final Map<Edge, List<Port>> inputsByEdge = new HashMap<>();
    final Map<Edge, List<Port>> outputsByEdge = new HashMap<>();

    public Node(String title, Stage stage, Skin skin) {
        super(title, true);
        this.stage = stage;
        this.skin = skin;

        // prepopulate edge->port lists
        for (var edge : Edge.values()) {
            inputsByEdge.put(edge, new ArrayList<>());
            outputsByEdge.put(edge, new ArrayList<>());
        }

        setStage(stage);
        setSkin(skin);
        setSize(DEFAULT_SIZE, DEFAULT_SIZE);
        setMovable(true);
        setResizable(false);
        setKeepWithinStage(true);
        setKeepWithinParent(true);

        var def = skin.get("default", Window.WindowStyle.class);
        var nob = skin.get("noborder", Window.WindowStyle.class);
        var pan = skin.get("panel", Window.WindowStyle.class);
        var mod = skin.get("module-list", Window.WindowStyle.class);
        var style = nob;
        setStyle(style);

        build();
    }

    public void build() {
        defaults().pad(0).top().left();

        var content = new VisTable(true);
        content.add("Content").growX().row();

        add(content).grow();
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        updatePortPositions();
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        super.draw(batch, parentAlpha);

        drawPorts(batch);
    }

    public Node addInput(String name, Edge edge) {
        addPort(Port.Type.INPUT, name, edge);
        return this;
    }

    public Node addOutput(String name, Edge edge) {
        addPort(Port.Type.OUTPUT, name, edge);
        return this;
    }

    public record Port(
        Node node,
        Type type,
        Edge edge,
        String name,
        Vector2 pos
    ) {
        public enum Type { INPUT, OUTPUT }

        public Port(Node node, Type type, Edge edge, String name) {
            this(node, type, edge, name, new Vector2());
        }
    }

    public record Connection(
        Port src,
        Port dst
    ) {}

    private void addPort(Port.Type type, String name, Edge edge) {
        var port = new Port(this, type, edge, name);
        if (type == Port.Type.INPUT) {
            inputsByEdge.get(edge).add(port);
            inputs.put(name, port);
        } else {
            outputsByEdge.get(edge).add(port);
            outputs.put(name, port);
        }
    }

    private void drawPorts(Batch batch) {
        TextureRegion texture;
        float x = getX();
        float y = getY();
        float size = 2 * PORT_RADIUS;

        texture = Main.get.radioBtnTextures.normal();
        for (var input : inputs.values()) {
            batch.draw(texture,
                x + input.pos.x - PORT_RADIUS,
                y + input.pos.y - PORT_RADIUS,
                size, size);
        }

        texture = Main.get.radioBtnTextures.down();
        for (var output : outputs.values()) {
            batch.draw(texture,
                x + output.pos.x - PORT_RADIUS,
                y + output.pos.y - PORT_RADIUS,
                size, size);
        }
    }

    private void updatePortPositions() {
        for (var edge : Edge.values()) {
            var edgeInputs = inputsByEdge.get(edge);
            var edgeOutputs = outputsByEdge.get(edge);

            float x, y;
            float margin = 10;
            float inputSpacing = getPortSpacing(edge, margin, edgeInputs.size());
            float outputSpacing = getPortSpacing(edge, margin, edgeOutputs.size());

            x = getPortStartX(edge, margin);
            y = getPortStartY(edge, margin);
            for (var port : edgeInputs) {
                port.pos.set(x, y);

                switch (edge) {
                    case TOP, BOTTOM: x += inputSpacing; break;
                    case LEFT, RIGHT: y += inputSpacing; break;
                }
            }

            x = getPortStartX(edge, margin);
            y = getPortStartY(edge, margin);
            for (var port : edgeOutputs) {
                port.pos.set(x, y);

                switch (edge) {
                    case TOP, BOTTOM: x += outputSpacing; break;
                    case LEFT, RIGHT: y += outputSpacing; break;
                }
            }
        }
    }

    private float getPortSpacing(Edge edge, float margin, int count) {
        return switch (edge) {
            case BOTTOM, TOP -> (getWidth() - 2 * margin) / (count + 1);
            case LEFT, RIGHT -> (getHeight() - 2 * margin) / (count + 1);
        };
    }

    private float getPortStartX(Edge edge, float margin) {
        return switch (edge) {
            case TOP, BOTTOM -> margin;
            case LEFT -> 0;
            case RIGHT -> getWidth();
        };
    }

    private float getPortStartY(Edge edge, float margin) {
        return switch (edge) {
            case TOP -> getHeight();
            case BOTTOM -> 0;
            case LEFT, RIGHT -> margin;
        };
    }
}
