package contest.winter2017.gui;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import contest.winter2017.Parameter;
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
	private VBox content;

	private CheckBox userDefinedBounds;

	/**
	 * Parameter builder for this parameter pane.
	 */
	private ParameterBuilder parameterBuilder;

	/**
	 * Constructs a ParameterPane with the given test.
	 * 
	 * @param test
	 *            - test for the parameters
	 */
	public ParameterPane() {

		// styling and components
		setFitToWidth(true);
		this.content = new VBox();
		setContent(this.content);
		this.content.setAlignment(Pos.CENTER);

		// parameter builder
		parameterBuilder = new ParameterBuilder();

		// check box for using jar test bounds
		userDefinedBounds = new CheckBox("Use Custom Parameter Bounds\n(Predefined tests will not be run)");
		userDefinedBounds.setSelected(false);
		userDefinedBounds.selectedProperty().addListener(new ChangeListener<Boolean>() {
			@Override
			public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
				if (newValue) {
					parameterBuilder.setVisible(true);
				} else {
					parameterBuilder.setVisible(false);
				}
			}
		});
		VExternSpace testBoundsBox = new VExternSpace(userDefinedBounds, 20, 0);

		// add children
		this.content.getChildren().addAll(testBoundsBox, parameterBuilder);
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
	public ParameterBuilder getParameterBuilder() {
		return this.parameterBuilder;
	}
}

/**
 * Pane to build custom lists of parameters.
 * 
 * @author ICT-2
 */
class ParameterBuilder extends VBox {
	private final String statusOkString = "User Defined Parameters OK!";

	/**
	 * Box to hold the parameter editors.
	 */
	private VBox params;

	/**
	 * Whether parameters should be dynamic or fixed by default.
	 */
	private boolean dynamic = true;

	/**
	 * Text with the status of user defined parameters.
	 */
	private Text statusText;

	/**
	 * Stack of status errors to display.
	 */
	private LinkedList<String> errors;

	/**
	 * List of parameter editors.
	 */
	private List<ParameterEditor> parameterEditors;

	/**
	 * Constructs a ParameterBuilder with the given test.
	 * 
	 * @param test
	 *            - test for this ParameterBuilder.
	 */
	public ParameterBuilder() {
		setVisible(false);
		setAlignment(Pos.CENTER);

		errors = new LinkedList<String>();
		parameterEditors = new LinkedList<ParameterEditor>();

		// status of parameters
		statusText = new Text(statusOkString);
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
		helpText.setMinHeight(500);
		helpText.setWrapText(true);
		helpText.setEditable(false);
		helpText.setFocusTraversable(false);
		helpText.setText("" + help);
		TitledPane helpPane = new TitledPane("Parameter Builder Help", helpText);
		helpPane.setExpanded(false);

		// box to hold parameter editors
		this.params = new VBox();
		Button newParamButton = new Button("Add Parameter");
		VExternSpace newSpacer = new VExternSpace(newParamButton, 10, 0);

		// add handlers
		newParamButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ParameterEditor newParam = new ParameterEditor(ParameterBuilder.this, dynamic);
				newParam.setExpanded(true);
				params.getChildren().add(newParam);
				ParameterBuilder.this.parameterEditors.add(newParam);
			}
		});
		fixedButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				ParameterBuilder.this.dynamic = false;
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
				ParameterBuilder.this.dynamic = true;
				for (Node node : params.getChildren()) {
					if (node instanceof ParameterEditor) {
						((ParameterEditor) node).setDynamic(true);
					}
				}
			}
		});

		// add children
		getChildren().addAll(paramsOkay, typeText, typeBoxSpace, helpPane, params, newSpacer);
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
			this.params.getChildren().remove(parameter);
			this.parameterEditors.remove(parameter);
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
	private boolean keyError;

	/**
	 * The string regex key for this parameter.
	 */
	private String regexKey;

	/**
	 * ParameterBuilder for this ParameterEditor.
	 */
	private ParameterBuilder parameterBuilder;

	/**
	 * Box for editor settings of this parameter.
	 */
	private VBox content;

	/**
	 * Whether this parameter is fixed or not.
	 */
	private boolean isDynamic;

	/**
	 * Whether this parameter is optional or not.
	 */
	private boolean isOptional;

	/**
	 * List of format strings for this paramter.
	 */
	private List<FormatString> formatStrings;

	/**
	 * Components to hide if this parameter is fixed.
	 */
	private List<Node> dynamicSettings;

	/**
	 * Components to hide if this parameter is fixed.
	 */
	private HBox initialSettings;

	/**
	 * Constructs a parameter editor with the given parameter builder and
	 * dynamic option.
	 * 
	 * @param parameterBuilder
	 *            - parameter builder for this parameter editor
	 * @param dynamic
	 *            - whether this parameter is dynamic or fixed
	 */
	public ParameterEditor(ParameterBuilder parameterBuilder, boolean dynamic) {
		this.parameterBuilder = parameterBuilder;

		setExpanded(false);
		setText("Parameter");

		this.formatStrings = new LinkedList<FormatString>();
		this.keyError = false;

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
		this.content = new VBox(10);
		this.content.setAlignment(Pos.CENTER);

		// add dynamic settings
		TextField keyField = new TextField();
		keyField.setPrefColumnCount(7);
		this.regexKey = "";
		LabeledNode key = new LabeledNode("Key", keyField);
		CheckBox optional = new CheckBox("Optional");
		this.isOptional = false;
		this.initialSettings = new HBox(45);
		this.initialSettings.setAlignment(Pos.CENTER);
		this.isDynamic = dynamic;
		if (this.isDynamic) {
			initialSettings.getChildren().addAll(optional, key);
		}
		dynamicSettings = new ArrayList<Node>();
		dynamicSettings.add(optional);
		dynamicSettings.add(key);
		VExternSpace initialSpacer = new VExternSpace(initialSettings, 0, 25);

		Button newFormatString = new Button("Add Format String");

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
				thisParameter.parameterBuilder.removeParameter(thisParameter);
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
				ParameterEditor.this.regexKey = newValue;
				try {
					Pattern.compile(newValue);
					if (ParameterEditor.this.keyError) {
						ParameterEditor.this.keyError = false;
						parameterBuilder.removeParameterError(keyErrorString);
					}
				} catch (PatternSyntaxException e) {
					if (!ParameterEditor.this.keyError) {
						ParameterEditor.this.keyError = true;
						parameterBuilder.addParameterError(keyErrorString);
					}
				}
			}
		});

		// add children
		content.getChildren().addAll(initialSpacer, newFormatString);
		container.getChildren().addAll(content, exitPane);
		setContent(container);
	}

	/**
	 * Removes all status errors associated with this editor and its children.
	 */
	public void removeAllErrors() {
		if (this.keyError) {
			this.parameterBuilder.removeParameterError(keyErrorString);
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
	public ParameterBuilder getBuilder() {
		return parameterBuilder;
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

/**
 * Box for holding a specific format string for a parameter.
 * 
 * @author ICT-2
 */
class FormatString extends VBox {
	// constants which represent different errors
	private static final String minErrorString = "Illegal: Min bound could not be parsed.";
	private static final String maxErrorString = "Illegal: Max bound could not be parsed.";
	private static final String replaceMeErrorString = "Illegal: Two replace-me numbers in one format string.";
	private static final String overlapErrorString = "Illegal: Min and max overlap.";

	/**
	 * Whether there is an error in the min field.
	 */
	private boolean minError = false;

	/**
	 * Whether there is an error in the max field.
	 */
	private boolean maxError = false;

	/**
	 * Whether the min and max fields overlap.
	 */
	private boolean overlapError = false;

	/**
	 * Whether the field has two number replace-mes.
	 */
	private boolean replaceMeError = false;

	/**
	 * Min value for replace-me number.
	 */
	private BigDecimal min = null;

	/**
	 * Max value for replace-me number.
	 */
	private BigDecimal max = null;

	/**
	 * The format string.
	 */
	private String formatString;

	/**
	 * The text field for min.
	 */
	private TextField minField;

	/**
	 * The text field for max.
	 */
	private TextField maxField;

	/**
	 * The text field for the format string.
	 */
	private TextField formatField;

	/**
	 * List of errors to stash.
	 */
	private List<String> errorStash;

	/**
	 * Whether this format string is enabled.
	 */
	private boolean enabled;

	/**
	 * The parent parameter editor.
	 */
	private ParameterEditor parameterEditor;

	/**
	 * Constructs a format string with the given parameter editor.
	 * 
	 * @param parameterEditor
	 *            - parameter editor parent for this format string.
	 */
	public FormatString(ParameterEditor parameterEditor) {
		super(3);

		this.parameterEditor = parameterEditor;
		this.errorStash = new LinkedList<String>();
		this.enabled = true;

		// delete button and text field
		HBox withString = new HBox(2);
		withString.setAlignment(Pos.CENTER);
		Button closeButton = new Button("X");
		formatField = new TextField();
		formatField.setPrefColumnCount(20);

		// min and max fields for number replace-mes
		VBox bounds = new VBox();
		minField = new TextField();
		minField.setPrefColumnCount(7);
		LabeledNode minLabel = new LabeledNode("Min", minField);
		maxField = new TextField();
		maxField.setPrefColumnCount(7);
		LabeledNode maxLabel = new LabeledNode("Max", maxField);

		// handlers
		closeButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				parameterEditor.removeFormatString(FormatString.this);
			}
		});
		minField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// clear errors if number blank
				if (newValue.equals("")) {
					FormatString.this.min = null;
					if (FormatString.this.minError) {
						FormatString.this.minError = false;
						parameterEditor.getBuilder().removeParameterError(minErrorString);
					}
				} else {
					// attempt to parse number
					try {
						min = new BigDecimal(newValue);
						if (FormatString.this.minError) {
							FormatString.this.minError = false;
							parameterEditor.getBuilder().removeParameterError(minErrorString);
						}
					} catch (NumberFormatException nfe) {
						FormatString.this.min = null;
						if (!FormatString.this.minError) {
							FormatString.this.minError = true;
							parameterEditor.getBuilder().addParameterError(minErrorString);
						}
					}
				}

				// determine if there is an overlap error
				if (min == null || max == null) {
					if (FormatString.this.overlapError) {
						FormatString.this.overlapError = false;
						parameterEditor.getBuilder().removeParameterError(overlapErrorString);
					}
				} else {
					if (min.compareTo(max) > 0) {
						if (!FormatString.this.overlapError) {
							FormatString.this.overlapError = true;
							parameterEditor.getBuilder().addParameterError(overlapErrorString);
						}
					} else {
						if (FormatString.this.overlapError) {
							FormatString.this.overlapError = false;
							parameterEditor.getBuilder().removeParameterError(overlapErrorString);
						}
					}
				}
			}
		});
		maxField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// clear errors if number blank
				if (newValue.equals("")) {
					FormatString.this.max = null;
					if (FormatString.this.maxError) {
						FormatString.this.maxError = false;
						parameterEditor.getBuilder().removeParameterError(maxErrorString);
					}
				} else {
					// attempt to parse number
					try {
						max = new BigDecimal(newValue);
						if (FormatString.this.maxError) {
							FormatString.this.maxError = false;
							parameterEditor.getBuilder().removeParameterError(maxErrorString);
						}
					} catch (NumberFormatException nfe) {
						FormatString.this.max = null;
						if (!FormatString.this.maxError) {
							FormatString.this.maxError = true;
							parameterEditor.getBuilder().addParameterError(maxErrorString);
						}
					}
				}

				// determine if there is an overlap error
				if (FormatString.this.min == null || FormatString.this.max == null) {
					if (FormatString.this.overlapError) {
						FormatString.this.overlapError = false;
						parameterEditor.getBuilder().removeParameterError(overlapErrorString);
					}
				} else {
					if (FormatString.this.min.compareTo(FormatString.this.max) > 0) {
						if (!FormatString.this.overlapError) {
							FormatString.this.overlapError = true;
							parameterEditor.getBuilder().addParameterError(overlapErrorString);
						}
					} else {
						if (FormatString.this.overlapError) {
							FormatString.this.overlapError = false;
							parameterEditor.getBuilder().removeParameterError(overlapErrorString);
						}
					}
				}
			}
		});
		formatField.textProperty().addListener(new ChangeListener<String>() {
			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				FormatString.this.formatString = newValue;

				// count replace-me numbers
				int nums = 0;
				for (String string : Parameter.REPLACABLES) {
					if (!string.equals(Parameter.REPLACE_STRING)) {
						int index = 0;
						while ((index = newValue.indexOf(string, index)) != -1) {
							index++;
							nums++;
						}
					}
				}

				// hide bounds if there are no replace-me numbers
				if (nums == 0) {
					removeAllErrors();
					((TextField) minLabel.getNode()).setText("");
					((TextField) maxLabel.getNode()).setText("");
					bounds.getChildren().removeAll(minLabel, maxLabel);
				} else {
					if (!bounds.getChildren().contains(minLabel)) {
						bounds.getChildren().addAll(minLabel, maxLabel);
					}
				}

				// give error if there are multiple replace-me numbers
				if (nums > 1) {
					if (!FormatString.this.replaceMeError) {
						parameterEditor.getBuilder().addParameterError(replaceMeErrorString);
						FormatString.this.replaceMeError = true;
					}
				} else {
					if (FormatString.this.replaceMeError) {
						parameterEditor.getBuilder().removeParameterError(replaceMeErrorString);
						FormatString.this.replaceMeError = false;
					}
				}
			}
		});

		// add children
		withString.getChildren().addAll(closeButton, formatField);
		getChildren().addAll(withString, bounds);
	}

	/**
	 * Unregisters any errors associated with this format string.
	 */
	public void removeAllErrors() {
		if (FormatString.this.minError) {
			parameterEditor.getBuilder().removeParameterError(minErrorString);
			FormatString.this.minError = false;
		}
		if (FormatString.this.maxError) {
			parameterEditor.getBuilder().removeParameterError(maxErrorString);
			FormatString.this.maxError = false;
		}
		if (FormatString.this.overlapError) {
			parameterEditor.getBuilder().removeParameterError(replaceMeErrorString);
			FormatString.this.overlapError = false;
		}
	}

	/**
	 * Enables this format field.
	 * <p>
	 * Adds back error statuses from error stash.
	 */
	public void enable() {
		if (!this.enabled) {
			this.enabled = true;

			minField.setDisable(false);
			maxField.setDisable(false);
			formatField.setDisable(false);

			ParameterBuilder builder = parameterEditor.getBuilder();
			if (this.minError) {
				builder.addParameterError(minErrorString);
			}
			if (this.maxError) {
				builder.addParameterError(maxErrorString);
			}
			if (this.replaceMeError) {
				builder.addParameterError(replaceMeErrorString);
			}
			if (this.overlapError) {
				builder.addParameterError(overlapErrorString);
			}
		}
	}

	/**
	 * Disables this format field.
	 * <p>
	 * Rescinds error statuses and stashes them.
	 */
	public void disable() {
		if (this.enabled) {
			this.enabled = false;

			minField.setDisable(true);
			maxField.setDisable(true);
			formatField.setDisable(true);

			this.errorStash.clear();
			ParameterBuilder builder = parameterEditor.getBuilder();
			if (this.minError) {
				this.errorStash.add(minErrorString);
				builder.removeParameterError(minErrorString);
			}
			if (this.maxError) {
				this.errorStash.add(maxErrorString);
				builder.removeParameterError(maxErrorString);
			}
			if (this.replaceMeError) {
				this.errorStash.add(replaceMeErrorString);
				builder.removeParameterError(replaceMeErrorString);
			}
			if (this.overlapError) {
				this.errorStash.add(overlapErrorString);
				builder.removeParameterError(overlapErrorString);
			}
		}
	}

	/**
	 * Returns the min for the replace-me number.
	 * 
	 * @return the min for the replace-me number
	 */
	public Number getMin() {
		return this.min;
	}

	/**
	 * Returns the max for the replace-me number.
	 * 
	 * @return the max for the replace-me number
	 */
	public Number getMax() {
		return this.max;
	}

	/**
	 * Returns the format string.
	 * 
	 * @return the format string
	 */
	public String getFormatString() {
		return this.formatString;
	}

}
