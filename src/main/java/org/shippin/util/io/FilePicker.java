package org.shippin.util.io;

import lombok.extern.log4j.Log4j2;
import java.io.File;
import javafx.stage.FileChooser;
import javafx.stage.Window;


@Log4j2
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
	
	public static File saveFile(Window ownerWindow, FileChooser.ExtensionFilter... filters) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save file as");

        if (filters.length > 0) {
            fileChooser.getExtensionFilters().addAll(filters);
        }

        File file = fileChooser.showSaveDialog(ownerWindow);

        if (file != null && !file.exists()) {
            try {
                file.createNewFile();
            } catch (Exception e) {
                log.error("Failed to create file", e);
                return null;
            }
        }

        return file;
    }
}