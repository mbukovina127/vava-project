package org.shippin.util.io;

import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Window;


public class FilePicker {

    /**
     * Opens a JavaFX file picker dialog and returns the selected file.
     *
     * @param ownerWindow the current window/stage
     * @return selected File, or null if the user cancels
     */
	public static File pickFile(Window ownerWindow, FileChooser.ExtensionFilter... filters) {
	    FileChooser fileChooser = new FileChooser();
	    fileChooser.setTitle("Choose a file");

	    if (filters.length > 0) {
	        fileChooser.getExtensionFilters().addAll(filters);
	    }

	    return fileChooser.showOpenDialog(ownerWindow);
	}
}