package lando.systems.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisWindow;
import lando.systems.game.Edge;
import lando.systems.game.Main;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Node extends VisWindow {

    public static class Defaults {
        public static final float NODE_SIZE = 200;
        public static final float PORT_RADIUS = 8;
        public static final float PORT_EDGE_MARGIN = 40;
    }

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
        setSize(Defaults.NODE_SIZE, Defaults.NODE_SIZE);
        setMovable(true);
        setResizable(false);
        setKeepWithinStage(true);
        setKeepWithinParent(true);

        // TODO(brian): 'default' and 'noborder' (and I'm assuming others)
        //  WindowStyles behave differently than others, they can drag but others can't
        //  it likely has something to do with `stageBackground` being set or not
        //  need to get back to it because I'd rather use the 'panel' assets from kenney.nl for nodes
        var def = skin.get("default2", Window.WindowStyle.class);
        var nob = skin.get("noborder", Window.WindowStyle.class);
        var pan = skin.get("panel", Window.WindowStyle.class);
        var mod = skin.get("module-list", Window.WindowStyle.class);
        var style = def;
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
        var textures = Main.get.radioBtnTextures;
        drawPorts(batch, inputs.values(), textures.over(), textures.tick());
        drawPorts(batch, outputs.values(), textures.down(), textures.tickDisabled());
    }

    private void drawPorts(Batch batch, Collection<Port> ports, TextureRegion texture, TextureRegion tick) {
        float radius = Defaults.PORT_RADIUS;
        float size = 2 * radius;
        float x = getX();
        float y = getY();

        for (var port : ports) {
            batch.draw(texture,
                x + port.pos.x - radius,
                y + port.pos.y - radius,
                size, size);
            batch.draw(tick,
                x + port.pos.x - radius,
                y + port.pos.y - radius,
                size, size);
        }
    }

    private void updatePortPositions() {
        for (var edge : Edge.values()) {
            var edgeInputs = inputsByEdge.get(edge);
            var edgeOutputs = outputsByEdge.get(edge);

            // given some margin, calculate even spacing between each port
            float margin = Defaults.PORT_EDGE_MARGIN;
            float inputSpacing = getPortSpacing(edge, margin, edgeInputs.size());
            float outputSpacing = getPortSpacing(edge, margin, edgeOutputs.size());

            // get the center of the edge relative to (0, 0)
            // as the bottom left corner of the node window
            float centerX = getEdgeCenterX(edge);
            float centerY = getEdgeCenterY(edge);

            // position ports along the shared edge, spaced evenly from the center out to margins
            positionPorts(edgeInputs, edge, centerX, centerY, inputSpacing);
            positionPorts(edgeOutputs, edge, centerX, centerY, outputSpacing);
        }
    }

    private void positionPorts(List<Port> ports, Edge edge, float centerX, float centerY, float spacing) {
        int count = ports.size();
        if (count == 0) return;

        // center alignment offset for the ports
        float offset = -(count - 1) * spacing / 2f;

        for (int i = 0; i < count; i++) {
            var port = ports.get(i);
            var space = i * spacing;
            switch (edge) {
                case TOP, BOTTOM -> port.pos.set(centerX + offset + space, centerY);
                case LEFT, RIGHT -> port.pos.set(centerX, centerY + offset + space);
            }
        }
    }

    private float getPortSpacing(Edge edge, float margin, int count) {
        // no spacing if there's only one port
        if (count < 2) return 0;

        return switch (edge) {
            case BOTTOM, TOP -> (getWidth() - 2 * margin) / (count - 1);
            case LEFT, RIGHT -> (getHeight() - 2 * margin) / (count - 1);
        };
    }

    private float getEdgeCenterX(Edge edge) {
        return switch (edge) {
            case TOP, BOTTOM -> getWidth() / 2f;
            case LEFT -> 0;
            case RIGHT -> getWidth();
        };
    }

    private float getEdgeCenterY(Edge edge) {
        return switch (edge) {
            case TOP -> getHeight();
            case BOTTOM -> 0;
            case LEFT, RIGHT -> getHeight() / 2;
        };
    }
}
