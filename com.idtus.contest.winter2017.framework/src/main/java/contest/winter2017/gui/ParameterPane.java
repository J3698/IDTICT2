package contest.winter2017.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

/**
 * Pane to control what parameters are used.
 * 
 * @author ICT-2
 */
class ParameterPane extends ScrollPane {
	/**
	 * Content for this ParameterPane.
	 */
	private VBox content = new VBox();

	private CheckBox userDefinedBounds = new CheckBox(
			"Use Custom Parameter Bounds\n(Predefined tests will not be run)");

	/**
	 * Parameter builder for this parameter pane.
	 */
	private FormatBuilder formatBuilder = new FormatBuilder();

	/**
	 * Constructs a ParameterPane with the given test.
	 * 
	 * @param test
	 *            - test for the parameters
	 */
	public ParameterPane() {

		// styling and components
		setFitToWidth(true);
		setContent(this.content);
		this.content.setAlignment(Pos.CENTER);

		// check box for using jar test bounds
		userDefinedBounds.setSelected(false);
		VExternSpace testBoundsBox = new VExternSpace(userDefinedBounds, 20, 0);

		addListeners();

		// add children
		this.content.getChildren().addAll(testBoundsBox, formatBuilder);
	}

	/**
	 * Adds listeners to this component.
	 */
	public void addListeners() {
		this.userDefinedBounds.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					ParameterPane.this.formatBuilder.setVisible(true);
				} else {
					ParameterPane.this.formatBuilder.setVisible(false);
				}
			}
		});
	}

	/**
	 * Returns whether user bounds are to be used.
	 * 
	 * @return true if user bounds are to be used, false if they are not
	 */
	public boolean hasUserTestBounds() {
		return userDefinedBounds.isSelected();
	}

	/**
	 * Returns this parameter pane's parameter builder.
	 * 
	 * @return this parameter pane's parameter builder
	 */
	public FormatBuilder getFormatBuilder() {
		return this.formatBuilder;
	}
}

/**
 * Pane to build custom lists of parameters.
 * 
 * @author ICT-2
 */
class FormatBuilder extends VBox {
	private final String statusOkString = "User Defined Parameters OK!";
	private static final String keyConflictErrorString = "Illegal: Duplicate parameter keys.";

	/**
	 * List of keys
	 */
	private List<String> keys = new ArrayList<String>();

	/**
	 * Box to hold the parameter editors.
	 */
	private VBox params = new VBox();

	/**
	 * Whether parameters should be dynamic or fixed by default.
	 */
	private boolean dynamic = true;

	/**
	 * Text with the status of user defined parameters.
	 */
	private Text statusText = new Text(statusOkString);

	/**
	 * Stack of status errors to display.
	 */
	private LinkedList<String> errors = new LinkedList<String>();

	/**
	 * List of parameter editors.
	 */
	private List<ParameterEditor> parameterEditors = new LinkedList<ParameterEditor>();

	/**
	 * Constructs a format builder with the given test.
	 * 
	 * @param test
	 *            - test for this format builder.
	 */
	public FormatBuilder() {
		setVisible(false);
		setAlignment(Pos.CENTER);

		// status of parameters
		statusText.setFill(Color.GREEN);
		VExternSpace paramsOkay = new VExternSpace(statusText, 7, 35);

		// fixed or dynamic parameters
		Text typeText = new Text("Parameter Types");
		ToggleGroup type = new ToggleGroup();
		ToggleButton fixedButton = new ToggleButton("Fixed");
		fixedButton.setToggleGroup(type);
		ToggleButton dynamicButton = new ToggleButton("Dynamic");
		dynamicButton.setToggleGroup(type);
		type.selectToggle(dynamicButton);
		HBox typeBox = new HBox();
		typeBox.getChildren().addAll(fixedButton, dynamicButton);
		typeBox.setAlignment(Pos.CENTER);
		VExternSpace typeBoxSpace = new VExternSpace(typeBox, 0, 8);

		// parameter builder help text
		StringBuffer help = new StringBuffer();
		try {
			InputStream iStream = getClass().getResourceAsStream("FormatBuilderHelp.txt");
			InputStreamReader iStreamReader = new InputStreamReader(iStream);
			BufferedReader bReader = new BufferedReader(iStreamReader);
			String line = "";
			while ((line = bReader.readLine()) != null) {
				help.append(line + "\n");
			}
		} catch (Exception e) {
			help = new StringBuffer("Could not load format builder help.");
		}
		TextArea helpText = new TextArea();
		helpText.setMinHeight(500);
		helpText.setWrapText(true);
		helpText.setEditable(false);
		helpText.setFocusTraversable(false);
		helpText.setText("" + help);
		TitledPane helpPane = new TitledPane("Format Builder Help", helpText);
		helpPane.setExpanded(false);

		Button newParamButton = new Button("Add Parameter");
		VExternSpace newSpacer = new VExternSpace(newParamButton, 10, 0);

		addHandlers(newParamButton, fixedButton, dynamicButton);

		// add children
		getChildren().addAll(paramsOkay, typeText, typeBoxSpace, helpPane, params, newSpacer);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param newParamButton
	 *            - component to add handlers to
	 * @param fixedButton
	 *            - component to add handlers to
	 * @param dynamicButton
	 *            - component to add handlers to
	 */
	public void addHandlers(Button newParamButton, ToggleButton fixedButton, ToggleButton dynamicButton) {
		// add handlers
		newParamButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ParameterEditor newParam = new ParameterEditor(FormatBuilder.this, dynamic);
				newParam.setExpanded(true);
				params.getChildren().add(newParam);
				FormatBuilder.this.parameterEditors.add(newParam);
			}
		});
		fixedButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				FormatBuilder.this.dynamic = false;
				for (Node node : params.getChildren()) {
					if (node instanceof ParameterEditor) {
						((ParameterEditor) node).setDynamic(false);
					}
				}
			}
		});
		dynamicButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				FormatBuilder.this.dynamic = true;
				for (Node node : params.getChildren()) {
					if (node instanceof ParameterEditor) {
						((ParameterEditor) node).setDynamic(true);
					}
				}
			}
		});
	}

	/**
	 * Adds a parameter error string.
	 * 
	 * @param error
	 *            - error to add
	 */
	public void addParameterError(String error) {
		errors.addFirst(error);
		statusText.setFill(Color.RED);
		statusText.setText(errors.peek());
	}

	/**
	 * Removes a parameter error string.
	 * <p>
	 * If no error strings remain, the OK status string is displayed. Otherwise
	 * the next error is displayed.
	 * 
	 * @param error
	 *            - error to remove
	 */
	public void removeParameterError(String error) {
		errors.remove(error);
		if (errors.size() == 0) {
			statusText.setFill(Color.GREEN);
			statusText.setText(this.statusOkString);
		} else {
			statusText.setText(errors.peek());
		}
	}

	/**
	 * Removes a given parameter.
	 * 
	 * @param parameter
	 *            - parameter to remove
	 */
	public void removeParameter(ParameterEditor parameter) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Parameter");
		alert.setContentText("Are you sure you want to delete this parameter? This action cannot be undone.");
		alert.showAndWait();
		if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
			parameter.removeAllErrors();
			this.removeKey(parameter.getRegexKey());
			this.params.getChildren().remove(parameter);
			this.parameterEditors.remove(parameter);
		}
	}

	/**
	 * Adds a key to the key list.
	 * <p>
	 * If the key is already in the key list, sets the status to the appropriate
	 * error.
	 * 
	 * @param key
	 *            - key to add to the key list
	 */
	public void addKey(String key) {
		int insert = Collections.binarySearch(this.keys, key);
		if (insert >= 0) {
			this.addParameterError(keyConflictErrorString);
			this.keys.add(insert, key);
		} else {
			if (insert == this.keys.size()) {
				this.keys.add(key);
			} else {
				this.keys.add(-(1 + insert), key);
			}
		}
	}

	/**
	 * Removes a key from the list of keys.
	 * 
	 * @param key
	 *            - key to remove from the list of keys
	 */
	public void removeKey(String key) {
		this.keys.remove(key);
		if (this.keys.contains(key)) {
			this.removeParameterError(keyConflictErrorString);
		}
	}

	/**
	 * Returns whether this parameter builder has dynamic parameters.
	 * 
	 * @return true if this parameter builder has dynamic parameters or false
	 *         otherwise
	 */
	public boolean isDynamic() {
		return this.dynamic;
	}

	/**
	 * Returns the current error with parameters.
	 * 
	 * @return current error with parameters or null if there are no errors
	 */
	public String getCurrentError() {
		if (this.statusText.getText().equals(statusOkString)) {
			return null;
		} else {
			return this.statusText.getText();
		}
	}

	/**
	 * Returns an unmodifiable list of the parameter editors.
	 * 
	 * @return an unmodifiable list of the parameter editors
	 */
	public List<ParameterEditor> getParameterEditors() {
		return Collections.unmodifiableList(parameterEditors);
	}
}

/**
 * Pane to edit a specific parameter.
 * 
 * @author ICT-2
 */
class ParameterEditor extends TitledPane {
	private static final String keyErrorString = "Illegal: Key regex invalid.";

	/**
	 * Whether there is an error in the key regex.
	 */
	private boolean keyError = false;

	/**
	 * The string regex key for this parameter.
	 */
	private String regexKey = "";

	/**
	 * Format builder for this ParameterEditor.
	 */
	private FormatBuilder formatBuilder;

	/**
	 * Box for editor settings of this parameter.
	 */
	private VBox content = new VBox(10);

	/**
	 * Whether this parameter is fixed or not.
	 */
	private boolean isDynamic;

	/**
	 * Whether this parameter is optional or not.
	 */
	private boolean isOptional = false;

	/**
	 * List of format strings for this paramter.
	 */
	private List<FormatString> formatStrings = new LinkedList<FormatString>();

	/**
	 * Components to hide if this parameter is fixed.
	 */
	private List<Node> dynamicSettings = new ArrayList<Node>();

	/**
	 * Components to hide if this parameter is fixed.
	 */
	private HBox initialSettings = new HBox(45);

	/**
	 * Constructs a parameter editor with the given parameter builder and
	 * dynamic option.
	 * 
	 * @param formatBuilder
	 *            - parameter builder for this parameter editor
	 * @param dynamic
	 *            - whether this parameter is dynamic or fixed
	 */
	public ParameterEditor(FormatBuilder formatBuilder, boolean dynamic) {
		this.formatBuilder = formatBuilder;
		this.isDynamic = dynamic;

		setExpanded(false);
		setText("Parameter");

		// for stacking delete button on other settings
		StackPane container = new StackPane();

		// pane for exit button
		AnchorPane exitPane = new AnchorPane();
		exitPane.setPickOnBounds(false);
		Button exitButton = new Button("X");
		AnchorPane.setTopAnchor(exitButton, 0.0);
		AnchorPane.setLeftAnchor(exitButton, 0.0);
		exitPane.getChildren().add(exitButton);

		// style editor box
		this.content.setAlignment(Pos.CENTER);

		// add dynamic settings
		TextField keyField = new TextField();
		keyField.setPrefColumnCount(7);
		this.formatBuilder.addKey(this.regexKey);
		LabeledNode key = new LabeledNode("Key", keyField);
		CheckBox optional = new CheckBox("Optional");
		this.initialSettings.setAlignment(Pos.CENTER);
		if (this.isDynamic) {
			initialSettings.getChildren().addAll(optional, key);
		}
		dynamicSettings.add(optional);
		dynamicSettings.add(key);
		VExternSpace initialSpacer = new VExternSpace(initialSettings, 0, 25);

		Button newFormatString = new Button("Add Format String");

		addHandlers(newFormatString, exitButton, optional, keyField);

		// add children
		content.getChildren().addAll(initialSpacer, newFormatString);
		container.getChildren().addAll(content, exitPane);
		setContent(container);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param newFormatString
	 *            - component to add handlers to
	 * @param exitButton
	 *            - component to add handlers to
	 * @param optional
	 *            - component to add handlers to
	 * @param keyField
	 *            - component to add handlers to
	 */
	public void addHandlers(Button newFormatString, Button exitButton, CheckBox optional, TextField keyField) {
		// handlers
		newFormatString.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				FormatString newFormat = new FormatString(ParameterEditor.this);
				content.getChildren().add(newFormat);
				ParameterEditor.this.formatStrings.add(newFormat);
			}
		});

		exitButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ParameterEditor thisParameter = ParameterEditor.this;
				thisParameter.formatBuilder.removeParameter(thisParameter);
			}
		});

		optional.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				ParameterEditor.this.isOptional = newValue;
			}
		});

		keyField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				ParameterEditor.this.formatBuilder.removeKey(oldValue);
				ParameterEditor.this.formatBuilder.addKey(newValue);

				ParameterEditor.this.regexKey = newValue;
				try {
					Pattern.compile(newValue);
					if (ParameterEditor.this.keyError) {
						ParameterEditor.this.keyError = false;
						formatBuilder.removeParameterError(keyErrorString);
					}
				} catch (PatternSyntaxException e) {
					if (!ParameterEditor.this.keyError) {
						ParameterEditor.this.keyError = true;
						formatBuilder.addParameterError(keyErrorString);
					}
				}
			}
		});
	}

	/**
	 * Removes all status errors associated with this editor and its children.
	 */
	public void removeAllErrors() {
		if (this.keyError) {
			this.formatBuilder.removeParameterError(keyErrorString);
			this.keyError = false;
		}

		for (FormatString formatString : this.formatStrings) {
			formatString.removeAllErrors();
		}
	}

	/**
	 * Removes a given format string.
	 * 
	 * @param toRemove
	 *            - format string to remove
	 */
	public void removeFormatString(FormatString toRemove) {
		Alert alert = new Alert(AlertType.CONFIRMATION);
		alert.setTitle("Delete Format String");
		alert.setContentText("Are you sure you want to delete this format string? This action cannot be undone.");
		alert.showAndWait();
		if (alert.getResult().getButtonData() == ButtonData.OK_DONE) {
			toRemove.removeAllErrors();
			this.content.getChildren().remove(toRemove);
			this.formatStrings.remove(toRemove);
		}
	}

	/**
	 * Sets whether this parameter is dynamic or set.
	 * <p>
	 * If this is set to true, dynamic options are shown. Otherwise dynamic
	 * settings are hidden.
	 * 
	 * @param dynamic
	 *            - whether this parameter is dynamic or not
	 */
	public void setDynamic(boolean dynamic) {
		if (this.isDynamic != dynamic) {
			if (dynamic) {
				// add dynamic only settings
				for (Node node : this.dynamicSettings) {
					initialSettings.getChildren().add(node);
				}
				// enable all format strings
				for (Node node : this.content.getChildren()) {
					if (node instanceof FormatString) {
						((FormatString) node).enable();
						System.out.println("Stuff Enabled.");
					}
				}
			} else {
				for (Node node : this.dynamicSettings) {
					initialSettings.getChildren().remove(node);
				}
				// disable all but first formst strings
				boolean first = true;
				for (Node node : this.content.getChildren()) {
					if (node instanceof FormatString) {
						if (first) {
							first = false;
						} else {
							((FormatString) node).disable();
							System.out.println("Stuff Disabled.");
						}
					}
				}
			}
		}
		this.isDynamic = dynamic;
	}

	/**
	 * Returns whether this parameter is optional or not.
	 * 
	 * @return true if this parameter is optional or false if it is not.
	 */
	public boolean isOptional() {
		return this.isOptional;
	}

	/**
	 * Returns the regex key for this parameter.
	 * <p>
	 * A key for a parameter is a previous parameter passed to an application
	 * which signifies that the new parameter is now eligible for use.
	 * 
	 * @return the regex key for this parameter
	 */
	public String getRegexKey() {
		return this.regexKey;
	}

	/**
	 * Returns this parameter editor's parameter builder.
	 * 
	 * @return this parameter editor's parameter builder
	 */
	public FormatBuilder getBuilder() {
		return formatBuilder;
	}

	/**
	 * Returns this parameter editor's format strings.
	 * 
	 * @return this parameter editor's format strings
	 */
	public List<FormatString> getFormatStrings() {
		return this.formatStrings;
	}
}
