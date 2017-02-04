package contest.winter2017.gui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

/**
 * Class to space a Node vertically.
 * 
 * @author ICT-2
 */
class VExternSpace extends VBox {
	/**
	 * Child to space.
	 */
	private Node child;

	/**
	 * Constructs a VExternSpace with the specefied child and space.
	 * 
	 * @param child
	 *            - child to space
	 * @param space
	 *            - space to surround child with
	 */
	public VExternSpace(Node child, double spaceTop, double spaceBottom) {
		getChildren().add(child);
		setAlignment(Pos.CENTER);
		setPadding(new Insets(spaceTop, 0, spaceBottom, 0));
	}

	/**
	 * Returns this spacers child.
	 * 
	 * @return this spacers child
	 */
	public Node getChild() {
		return this.child;
	}
}