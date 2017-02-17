package contest.winter2017.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Class to add vertical padding space to a node. This is most helpful when
 * working with a VBox.
 * 
 * @author ICT-2
 */
class VExternSpace extends VBox {
	/**
	 * Node to space.
	 */
	private Node node;

	/**
	 * Constructs a VExternSpace with the specified node and space.
	 * 
	 * @param node
	 *            - child to space
	 * @param space
	 *            - space to surround node with
	 */
	public VExternSpace(Node node, double spaceTop, double spaceBottom) {
		getChildren().add(node);
		setAlignment(Pos.CENTER);
		setPadding(new Insets(spaceTop, 0, spaceBottom, 0));
	}

	/**
	 * Returns this spacers node.
	 * 
	 * @return this spacers node
	 */
	public Node getNode() {
		return this.node;
	}
}