package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.controller.utils.ExtraOption;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class CostBreakdownController extends BaseController<CostEstimationInput> implements Initializable {

    @FXML private GridPane breakdownGrid;
    @FXML private Button   deleteButton;
    @FXML private Button   printPdfButton;
    @FXML private Button   saveButton;

    // Current grid row index – we build rows one by one in onData()
    private int gridRow = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to initialise statically – all content is driven by onData()
    }

    @Override
    protected Class<CostEstimationInput> getDataType() {
        return CostEstimationInput.class;
    }

    // ── Data rendering ────────────────────────────────────────────────────────

    /**
     * Called automatically by BaseController once the screen receives its data.
     * Builds the breakdown table that matches the design in the screenshot:
     *
     *   Postal codes:  Sklad BA – 14863 (Horná Dolná 28)       (210 km)
     *   Size:          350 kg                                    28,46 €
     *   ─────────────────────────────────────────────────────────────────
     *   Fuel surcharge: 15%                                         5 €
     *   Toll:           5                                          11 €
     *   ─────────────────────────────────────────────────────────────────
     *   Dobierka                                                    2 €
     *   Premium 13                                               2,50 €
     *   FIX 13                                                      3 €
     *   ═════════════════════════════════════════════════════════════════
     *                                                           54,46 €
     */
    @Override
    protected void onData(CostEstimationInput data) {
        breakdownGrid.getChildren().clear();
        gridRow = 0;

        // ── Row 1: Postal codes ───────────────────────────────────────────────
        String postalLabel = "Postal codes:";
        // e.g.  "Sklad BA – 14863 (Horná Dolná 28)"  and distance on the right
        String postalValue = data.from() + " – " + data.destination();
        addRow(postalLabel, postalValue, "", false, false);

        // ── Row 2: Size ───────────────────────────────────────────────────────
        // Weight shown on left, base cost on right (placeholder – compute or pass in)
        String sizeValue = data.weight() + " kg";
        // Volume in parentheses if non-zero
        if (data.volume() > 0) {
            sizeValue += "  /  " + data.volume() + " m³";
        }
        addRow("Size:", sizeValue, "", false, false);

        addSeparator();

        // ── Row 3: Fuel surcharge ─────────────────────────────────────────────
        String fuelPct = (int)(data.fuelSurcharge() * 100) + "%";
        addRow("Fuel surcharge:", fuelPct, "", false, false);

        // ── Row 4: Toll ───────────────────────────────────────────────────────
        addRow("Toll:", String.valueOf((int) data.toll()), "", false, false);

        addSeparator();

        // ── Rows: Selected extra options ──────────────────────────────────────
        List<ExtraOption> options = data.options();
        if (options != null && !options.isEmpty()) {
            for (ExtraOption option : options) {
                // Skip the top-level type toggles – they are shown via Size row
                if (option == ExtraOption.SMALL_PACKAGE || option == ExtraOption.SHIPMENT) {
                    continue;
                }
                // Skip the umbrella checkbox itself; its children are listed individually
                if (option == ExtraOption.ADDITIONAL_FEES) {
                    continue;
                }
                addRow(formatOptionName(option), "", "", false, false);
            }
        }

        addTotalSeparator();

        // ── Total row ─────────────────────────────────────────────────────────
        addTotalRow();
    }

    // ── Grid helpers ──────────────────────────────────────────────────────────

    /**
     * Adds one label+value row to the grid.
     *
     * @param leftText   Left column text (bold when {@code bold} is true)
     * @param middleText Middle annotation text (e.g. distance like "(210 km)")
     * @param rightText  Right-aligned value (e.g. "28,46 €")
     * @param bold       Whether both labels should use a bold style class
     * @param italic     Whether to use an italic style class
     */
    private void addRow(String leftText, String middleText, String rightText,
                        boolean bold, boolean italic) {

        Label left = new Label(leftText);
        left.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(left, Priority.ALWAYS);
        if (bold) left.getStyleClass().add("cb-bold");
        if (italic) left.getStyleClass().add("cb-italic");

        GridPane.setColumnIndex(left, 0);
        GridPane.setRowIndex(left, gridRow);

        Label right = new Label(rightText);
        right.getStyleClass().add("cb-value");
        GridPane.setHalignment(right, HPos.RIGHT);
        GridPane.setColumnIndex(right, 1);
        GridPane.setRowIndex(right, gridRow);
        if (bold) right.getStyleClass().add("cb-bold");

        breakdownGrid.getChildren().addAll(left, right);

        // Optional middle annotation (shown between left and right)
        if (middleText != null && !middleText.isEmpty()) {
            Label mid = new Label(middleText);
            mid.getStyleClass().add("cb-mid");
            GridPane.setColumnIndex(mid, 0);
            GridPane.setRowIndex(mid, gridRow);
            // We place it in the same cell as left but pad via style; alternatively
            // add a third column. Here we append it to the left label text for simplicity.
            left.setText(leftText + "  " + middleText);
        }

        gridRow++;
    }

    /** Adds a thin horizontal separator spanning both columns. */
    private void addSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("cb-separator");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);
        gridRow++;
    }

    /** Adds a bold/double separator before the total row. */
    private void addTotalSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().addAll("cb-separator", "cb-separator-total");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);
        gridRow++;
    }

    /** Adds the bold total row (right column only). Actual value is a placeholder. */
    private void addTotalRow() {
        Label total = new Label("– €");   // TODO: replace with computed total
        total.getStyleClass().addAll("cb-value", "cb-total");
        GridPane.setHalignment(total, HPos.RIGHT);
        GridPane.setColumnIndex(total, 1);
        GridPane.setRowIndex(total, gridRow);
        breakdownGrid.getChildren().add(total);
        gridRow++;
    }

    /**
     * Converts an {@link ExtraOption} enum constant to a human-readable label,
     * matching the names shown in the screenshot (e.g. PREMIUM_13 → "Premium 13").
     */
    private String formatOptionName(ExtraOption option) {
        return switch (option) {
            case SMALL_PACKAGE  -> "Small package";
            case SHIPMENT       -> "Shipment";
            case ADDITIONAL_FEES -> "Additional fees";
            case ADR            -> "ADR";
            case DOBIERKA       -> "Dobierka";
            case PRIPOISTENIE   -> "Pripoistenie";
            case VRATENIE_EUP   -> "Vrátenie EUP";
            case PREMIUM        -> "Premium";
            case FIX            -> "FIX";
            case PREMIUM_10     -> "Premium 10";
            case FIX_10         -> "FIX 10";
            case PREMIUM_13     -> "Premium 13";
            case FIX_13         -> "FIX 13";
        };
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void onDelete() {
        // TODO: delete this estimation from DB, navigate back
    }

    @FXML
    private void onPrintPdf() {
        // TODO: generate and print/save PDF
    }

    @FXML
    private void onSave() {
        // TODO: persist the estimation to DB
    }
}