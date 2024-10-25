package lando.systems.game.ui;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.Vector2;
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
    }

    public void build() {
        clearChildren();

        var node1 = new Node("Node 1", stage, skin);
        var node2 = new Node("Node 2", stage, skin);

        float spacing = 100;
        float x1 = 200;
        float x2 = x1 + node1.getWidth() + spacing;
        float y1 = (stage.getHeight() - node1.getHeight()) / 2f;
        float y2 = (stage.getHeight() - node2.getHeight()) / 2f;
        node1.setPosition(x1, y1);
        node2.setPosition(x2, y2);

        node1.addOutput("Output 1", Edge.RIGHT);
        node2.addInput("Input 1", Edge.LEFT);

        nodes.add(node1);
        nodes.add(node2);
        nodes.forEach(this::addActor);
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        for (var connection : connections) {
            var shapes = Main.get.shapes;
            shapes.setColor(1, 0, 1, 1);
            shapes.path(path, 2, JoinType.SMOOTH, true);
            shapes.setColor(1, 1, 1, 1);
        }

        super.draw(batch, parentAlpha);
    }
}
