package fr.eisbm.GRAPHML2SBGNML;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

public class App extends Application {

	/**
	 * This enum describes the 2 possible directions of convertion.
	 */
	public enum ConvertionChoice {
		GRAPHML2SBGN, SBGN2GRAPHML;

		@Override
		public String toString() {
			switch (this) {
			case GRAPHML2SBGN:
				return "GraphML -> SBGN-ML";
			case SBGN2GRAPHML:
				return "SBGN-ML -> GraphML";
			}
			throw new IllegalArgumentException("No valid enum was given");
		}
	}

	String szFolderName = "";
	boolean bSingleFileOption = true;
	List<String> results = new ArrayList<String>();

	@SuppressWarnings({ "restriction", "rawtypes" })
	@Override
	public void start(Stage primaryStage) {

		primaryStage.setTitle("The ySBGN Dual Converter");

		VBox vbox = new VBox(10);
		vbox.setPadding(new Insets(10, 10, 10, 10));

		GridPane grid = new GridPane();
		grid.setAlignment(Pos.CENTER);
		grid.setHgap(10);
		grid.setVgap(10);
		vbox.getChildren().add(grid);

		// --- 0th row --- //
		Label directionLabel = new Label("Convertion Direction:");
		grid.add(directionLabel, 0, 0);

		ChoiceBox directionChoice = new ChoiceBox<>(FXCollections.observableArrayList(
				ConvertionChoice.GRAPHML2SBGN.toString(), ConvertionChoice.SBGN2GRAPHML.toString()));
		grid.add(directionChoice, 1, 0);
		directionChoice.getSelectionModel().selectFirst(); // set first as default

		// --- 1st row --- //
		Label inputFileLabel = new Label("Input File:");
		grid.add(inputFileLabel, 0, 1);

		TextField inputFileText = new TextField();
		grid.add(inputFileText, 1, 1);
		TextField inputFolderText = new TextField();

		FileChooser inputFileChooser = new FileChooser();

		Button inputFileOpenButton = new Button("Choose file");

		grid.add(inputFileOpenButton, 2, 1);

		inputFileOpenButton.setOnAction(e -> {
			File file = inputFileChooser.showOpenDialog(primaryStage);
			if (file != null) {
				inputFileText.setDisable(false);
				inputFileText.setText(file.getAbsolutePath());
				szFolderName = file.getParentFile().getAbsolutePath();
				inputFolderText.setText("");
				inputFolderText.setDisable(true);
				bSingleFileOption = true;
			}

		});

		// --- 1st row --- //
		Label inputFolderLabel = new Label("Input Folder:");
		grid.add(inputFolderLabel, 0, 2);

		grid.add(inputFolderText, 1, 2);

		Button inputFolderOpenButton = new Button("Choose folder");

		grid.add(inputFolderOpenButton, 2, 2);

		DirectoryChooser inputFolderChooser = new DirectoryChooser();
		inputFolderChooser.setTitle("Open Resource File");

		inputFolderOpenButton.setOnAction(e -> {

			File folder = inputFolderChooser.showDialog(primaryStage);
			if (folder != null) {
				inputFolderText.setDisable(false);
				inputFolderText.setText(folder.getAbsolutePath());
				szFolderName = folder.getAbsolutePath();

				File[] files = folder.listFiles();

				for (File file : files) {
					if (file.isFile()) {
						results.add(file.getAbsolutePath());
					}
				}

				inputFileText.setText("");
				inputFileText.setDisable(true);
				bSingleFileOption = false;
			}

		});

		// --- final row --- //
		final Label infoLabel = new Label();
		Button convertButton = new Button("Convert");
		grid.add(convertButton, 1, 4, 3, 1);
		convertButton.setOnAction(e -> {

			// check arguments
			if (inputFileText.getText().isEmpty() && inputFolderText.getText().isEmpty()) {
				infoLabel.setText("No input provided.");
				return;
			}

			if (directionChoice.getValue().equals(ConvertionChoice.GRAPHML2SBGN.toString())) {
				System.out.println("Convert button clicked, launch script");
				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(() -> {
							infoLabel.setText("Running...");
						});

						if (bSingleFileOption) {
							GraphML2SBGNML.convert(inputFileText.getText());
						} else {
							for (String fileName : results) {
								if (fileName.contains(".graphml")) {
									GraphML2SBGNML.convert(fileName);
								}
							}
						}

						Platform.runLater(() -> {
							infoLabel.setText("Done");
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							alert.setTitle("The output folder");
							alert.setHeaderText("The output is available in the following folder: ");
							alert.setContentText(szFolderName);
							alert.show();
						});
						return null;
					}
				};
				new Thread(task).start();

			} else if (directionChoice.getValue().equals(ConvertionChoice.SBGN2GRAPHML.toString())) {
				System.out.println("Convert button clicked, launch script");
				Task task = new Task<Void>() {
					@Override
					public Void call() {
						Platform.runLater(() -> {
							infoLabel.setText("Running...");
						});

						if (bSingleFileOption) {
							SBGNML2GraphML.convert(inputFileText.getText());
						} else {
							for (String fileName : results) {
								if ((fileName.contains(".sbgn") || fileName.contains(".xml"))) {
									SBGNML2GraphML.convert(fileName);
								}
							}
						}

						Platform.runLater(() -> {
							infoLabel.setText("Done");
							Alert alert = new Alert(Alert.AlertType.INFORMATION);
							alert.setTitle("The output folder");
							alert.setHeaderText("The output is available in the following folder: ");
							alert.setContentText(szFolderName);
							alert.show();
						});
						return null;
					}
				};
				new Thread(task).start();

			} else {
				throw new RuntimeException("That shouldn't happen.");
			}

		});

		Button closeButton = new Button("Close");
		grid.add(closeButton, 2, 4, 3, 1);

		closeButton.setOnAction(e -> {

			System.exit(0);

		});

		// info row
		grid.add(infoLabel, 1, 5);

		Scene scene = new Scene(vbox, 800, 400);
		primaryStage.setScene(scene);

		primaryStage.show();
	}

	public class TextOutputStream extends OutputStream {
		TextArea textArea;

		public TextOutputStream(TextArea textArea) {
			this.textArea = textArea;
		}

		@Override
		public void write(int b) throws IOException {
			// redirects data to the text area
			textArea.appendText(String.valueOf((char) b));
			// scrolls the text area to the end of data
			textArea.positionCaret(textArea.getText().length());
		}
	}
}
