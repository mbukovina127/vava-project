package org.shippin.controller;
import org.shippin.dto.Screens;

import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;

public abstract class BaseController<T> implements Navigatable {

    private MenuController menuController;

    // package-private
    void setMenuController(MenuController menu) {this.menuController = menu;}

    // toto sa bude overridovat pre spravny typ aby nebol zly cast
    protected Class<T> getDataType() {
        return null;// default
    }

    // prijem dat na aktualnom screen z predosleho
    // kontrola spravnej generickosti
    @Override
    public final void onNavigatedTo(Object data) throws SQLException {
        Class<T> dataType = getDataType();

        if (data == null) {
            onData(null);
            return;
        }

        if (dataType == null) {
            throw new IllegalArgumentException(
                    "This controller does not accept navigation data, but got: "
                            + data.getClass().getSimpleName()
            );
        }

        if (!dataType.isInstance(data)) {
            throw new IllegalArgumentException(
                    "Invalid navigation data. Expected: " + dataType.getSimpleName()
                            + ", got: " + data.getClass().getSimpleName()
            );
        }

        onData(dataType.cast(data));
    }

    // child controllery overridujú toto (receive data from last screen)
    protected void onData(T data) throws SQLException {}

    // child controllery volajú toto (send data to next screen)
    protected void loadScreen(Screens screen) throws IOException {loadScreen(screen, null);}

    // posielanie dat do dalsieho screenu
    protected <D> void loadScreen(Screens screen, D data) throws IOException {
        if (menuController == null)
        {
            throw new IllegalStateException("MenuController not set");
        }
        menuController.loadScreen(screen, data);
        menuController.setActive(screen); //nav bar mark active
    }
    
    protected void showModal(VBox popupContent) {

        if (menuController != null) {
            menuController.showOverlay(popupContent);
        }
    }

    protected void hideModal() {

        if (menuController != null) {
            menuController.hideOverlay();
        }
    }
}