package contest.winter2017.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Pos;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Pane to change parameters.
 */
class ParameterPane extends ScrollPane {
	/**
	 * Content for this pane.
	 */
	private VBox content;
	private GUITestPackage test;

	/**
	 * Constructs a ParameterPane.
	 */
	public ParameterPane(GUITestPackage test) {
		this.test = test;

		// styling and components
		setFitToWidth(true);
		this.content = new VBox();
		setContent(this.content);
		this.content.setAlignment(Pos.CENTER);

		ParameterBuilder parameterBuilder = new ParameterBuilder(this.test);

		CheckBox box = new CheckBox("Use Parameter Test Bounds from Jar");
		box.setSelected(true);
		box.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (!newValue) {
					parameterBuilder.setVisible(true);
				} else {
					parameterBuilder.setVisible(false);
				}
			}
		});

		VExternSpace testBoundsBox = new VExternSpace(box, 20, 0);
		this.content.getChildren().add(testBoundsBox);
		this.content.getChildren().add(parameterBuilder);
	}
}

class ParameterBuilder extends VBox {
	private GUITestPackage test;

	public ParameterBuilder(GUITestPackage test) {
		this.test = test;
		setVisible(false);

		Text text = new Text("User Defined Parameters OK!");
		text.setFill(Color.GREEN);
		VExternSpace paramsOkay = new VExternSpace(text, 20, 20);

		StringBuffer help = new StringBuffer();
		try {
			InputStream iStream = getClass().getResourceAsStream("ParameterBuilderHelp.txt");
			InputStreamReader iStreamReader = new InputStreamReader(iStream);
			BufferedReader bReader = new BufferedReader(iStreamReader);
			String line = "";
			while ((line = bReader.readLine()) != null) {
				help.append(line + "\n");
			}
		} catch (Exception e) {
			help = new StringBuffer("Could not load parameter builder help.");
		}

		TextArea helpText = new TextArea();
		// helpText.setPrefHeight(600);
		helpText.setWrapText(true);
		helpText.setEditable(false);
		// helpText.setMouseTransparent(true);
		helpText.setFocusTraversable(false);

		helpText.setText("" + help);
		TitledPane helpPane = new TitledPane("Parameter Builder Help", helpText);
		helpPane.setExpanded(false);

		getChildren().addAll(paramsOkay, helpPane);
	}

	/*
	 * Button b = new Button("New Parameter"); b.setFont(new Font(15));
	 * VExternSpace addParamButton = new VExternSpace(b, 20, 20);
	 * this.content.getChildren().add(addParamButton);
	 */
}
