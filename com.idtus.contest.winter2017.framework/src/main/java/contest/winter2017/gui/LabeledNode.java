package contest.winter2017.gui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

/**
 * HBox to hold a label and node. This component steals horizontal space.
 * 
 * @author ICT-2
 */
public class LabeledNode extends HBox {
	/**
	 * Int default space between the label and the node.
	 */
	private static final int DEFAULT_SPACE = 10;

	/**
	 * Label for this labeled node.
	 */
	private Label label;

	/**
	 * Node for this labeled node.
	 */
	private Node node;

	/**
	 * Constructs a labeled node with the specified label and node.
	 * 
	 * @param label
	 *            - label to use
	 * @param node
	 *            - node to label
	 */
	public LabeledNode(String label, Node node) {
		super(DEFAULT_SPACE);
		this.node = node;
		setAlignment(Pos.CENTER);
		this.label = new Label(label);
		getChildren().addAll(this.label, this.node);
	}

	/**
	 * Returns this labeled node's label.
	 * 
	 * @return label of this labeled node
	 */
	public Label getLabel() {
		return this.label;
	}

	/**
	 * Returns this labeled node's node.
	 * 
	 * @return node of this labeled node.
	 */
	public Node getNode() {
		return this.node;
	}
}