package contest.winter2017.gui;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;

import contest.winter2017.Parameter;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

/**
 * Box for holding a specific format string for a parameter.
 * 
 * @author ICT-2
 */
public class FormatString extends VBox {
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
	private TextField minField = new TextField();

	/**
	 * The text field for max.
	 */
	private TextField maxField = new TextField();

	/**
	 * The text field for the format string.
	 */
	private TextField formatField = new TextField();

	/**
	 * List of errors to stash.
	 */
	private List<String> errorStash = new LinkedList<String>();

	/**
	 * Whether this format string is enabled.
	 */
	private boolean enabled = true;

	/**
	 * The parent parameter editor.
	 */
	private ParameterEditor parameterEditor;

	/**
	 * Box for holding replace-me number bounds.
	 */
	private VBox bounds = new VBox();

	/**
	 * Labeled text field for min bound of replace-me numbers
	 */
	private LabeledNode minLabel = new LabeledNode("Min", minField);

	/**
	 * Labeled text field for max bound of replace-me numbers
	 */
	private LabeledNode maxLabel = new LabeledNode("Max", maxField);

	/**
	 * Constructs a format string with the given parameter editor.
	 * 
	 * @param parameterEditor
	 *            - parameter editor parent for this format string.
	 */
	public FormatString(ParameterEditor parameterEditor) {
		super(3);

		this.parameterEditor = parameterEditor;

		// delete button and text field
		HBox withString = new HBox(2);
		withString.setAlignment(Pos.CENTER);
		Button closeButton = new Button("X");
		this.formatField.setPrefColumnCount(20);

		// min and max fields for number replace-mes
		this.minField.setPrefColumnCount(7);
		this.maxField.setPrefColumnCount(7);

		addHandlers(closeButton);

		// add children
		withString.getChildren().addAll(closeButton, formatField);
		getChildren().addAll(withString, bounds);
	}

	/**
	 * Adds handlers to this component.
	 * 
	 * @param closeButton
	 *            - component to add handlers to
	 */
	public void addHandlers(Button closeButton) {
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
					minField.setText("");
					maxField.setText("");
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

			FormatBuilder builder = parameterEditor.getBuilder();
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
			FormatBuilder builder = parameterEditor.getBuilder();
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