package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.shippin.controller.utils.CostEstimationInput;
import org.shippin.controller.utils.ExtraOption;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.shippin.dto.Screens.COST_ESTIMATION;

public class CostBreakdownController extends BaseController<CostEstimationInput> implements Initializable {

    @FXML private GridPane  breakdownGrid;
    @FXML private Button    deleteButton;
    @FXML private Button    printPdfButton;
    @FXML private Button    saveButton;

    // Modal overlay injected from FXML
    @FXML private StackPane modalOverlay;
    @FXML private VBox      modalContentHolder;

    // Current grid row index
    private int gridRow = 0;

    // ── PDF mirror ────────────────────────────────────────────────────────────

    private enum PdfRowType { DATA, SEPARATOR, TOTAL_SEPARATOR, TOTAL }

    private record PdfRow(PdfRowType type, String left, String right, boolean bold) {}

    private final List<PdfRow> pdfRows = new ArrayList<>();

    // ── Initializable ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Nothing to initialise statically – all content is driven by onData()
    }

    @Override
    protected Class<CostEstimationInput> getDataType() {
        return CostEstimationInput.class;
    }

    // ── Data rendering ────────────────────────────────────────────────────────

    @Override
    protected void onData(CostEstimationInput data) {
        breakdownGrid.getChildren().clear();
        pdfRows.clear();
        gridRow = 0;

        // ── Row 1: Postal codes ───────────────────────────────────────────────
        String postalValue = data.from() + " \u2013 " + data.destination();
        addRow("Postal codes:", postalValue, "", true, false);

        // ── Row 2: Size ───────────────────────────────────────────────────────
        String sizeValue = data.weight() + " kg";
        if (data.volume() > 0) {
            sizeValue += "  /  " + data.volume() + " m\u00B3";
        }
        addRow("Size:", sizeValue, "", true, false);

        addSeparator();

        // ── Row 3: Fuel surcharge ─────────────────────────────────────────────
        String fuelPct = (int)(data.fuelSurcharge() * 100) + "%";
        addRow("Fuel surcharge:", fuelPct, "", true, false);

        // ── Row 4: Toll ───────────────────────────────────────────────────────
        addRow("Toll:", String.valueOf((int) data.toll()), "", false, false);

        addSeparator();

        // ── Rows: Selected extra options ──────────────────────────────────────
        List<ExtraOption> options = data.options();
        if (options != null && !options.isEmpty()) {
            for (ExtraOption option : options) {
                if (option == ExtraOption.SMALL_PACKAGE || option == ExtraOption.SHIPMENT) continue;
                if (option == ExtraOption.ADDITIONAL_FEES) continue;
                addRow(formatOptionName(option), "", "", true, false);
            }
        }

        addTotalSeparator();
        addTotalRow();
    }

    // ── Grid + mirror helpers ─────────────────────────────────────────────────

    private void addRow(String leftText, String middleText, String rightText,
                        boolean bold, boolean italic) {

        Label left = new Label(leftText);
        left.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(left, Priority.ALWAYS);
        if (bold)   left.getStyleClass().add("cb-bold");
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

        if (middleText != null && !middleText.isEmpty()) {
            left.setText(leftText + "  " + middleText);
        }

        String pdfLeft = (middleText != null && !middleText.isEmpty())
                ? leftText + "  " + middleText : leftText;
        pdfRows.add(new PdfRow(PdfRowType.DATA, pdfLeft, rightText, bold));

        gridRow++;
    }

    private void addSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("cb-separator");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);

        pdfRows.add(new PdfRow(PdfRowType.SEPARATOR, null, null, false));
        gridRow++;
    }

    private void addTotalSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().addAll("cb-separator", "cb-separator-total");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);

        pdfRows.add(new PdfRow(PdfRowType.TOTAL_SEPARATOR, null, null, false));
        gridRow++;
    }

    private void addTotalRow() {
        Label total = new Label("\u2013 \u20AC");   // "– €" — TODO: replace with computed total
        total.getStyleClass().addAll("cb-value", "cb-total");
        GridPane.setHalignment(total, HPos.RIGHT);
        GridPane.setColumnIndex(total, 1);
        GridPane.setRowIndex(total, gridRow);
        breakdownGrid.getChildren().add(total);

        pdfRows.add(new PdfRow(PdfRowType.TOTAL, null, total.getText(), true));
        gridRow++;
    }

    private String formatOptionName(ExtraOption option) {
        return switch (option) {
            case SMALL_PACKAGE   -> "Small package";
            case SHIPMENT        -> "Shipment";
            case ADDITIONAL_FEES -> "Additional fees";
            case ADR             -> "ADR";
            case DOBIERKA        -> "Dobierka";
            case PRIPOISTENIE    -> "Pripoistenie";
            case VRATENIE_EUP    -> "Vratenie EUP";
            case PREMIUM         -> "Premium";
            case FIX             -> "FIX";
            case PREMIUM_10      -> "Premium 10";
            case FIX_10          -> "FIX 10";
            case PREMIUM_13      -> "Premium 13";
            case FIX_13          -> "FIX 13";
        };
    }

    // ── Modal helpers (same pattern as WarehouseManagementController) ─────────

//    private void showModal(VBox popupContent) {
//        modalContentHolder.getChildren().setAll(popupContent);
//        modalContentHolder.setManaged(true);
//        modalContentHolder.setVisible(true);
//
//        modalOverlay.setManaged(true);
//        modalOverlay.setVisible(true);
//    }
//
//    private void hideModal() {
//        modalContentHolder.getChildren().clear();
//        modalContentHolder.setVisible(false);
//        modalContentHolder.setManaged(false);
//
//        modalOverlay.setVisible(false);
//        modalOverlay.setManaged(false);
//    }

    // ── Save estimation popup ─────────────────────────────────────────────────

    private void showSaveEstimationPopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(440);
        popup.setPrefWidth(440);

        Label title = createPopupTitle("Save cost estimation to daily summary");

        GridPane formGrid = new GridPane();
        formGrid.setHgap(16);
        formGrid.setVgap(14);

        ColumnConstraints labelColumn = new ColumnConstraints();
        labelColumn.setPrefWidth(55);

        ColumnConstraints fieldColumn = new ColumnConstraints();
        fieldColumn.setHgrow(Priority.ALWAYS);

        formGrid.getColumnConstraints().addAll(labelColumn, fieldColumn);

        Label titleLabel = createFormLabel("Title:");
        TextField titleField = createPopupTextField("Value");

        formGrid.add(titleLabel, 0, 0);
        formGrid.add(titleField, 1, 0);

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(150, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button confirmButton = new Button("Save estimation");
        confirmButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        confirmButton.setPrefSize(170, 42);
        confirmButton.setOnAction(e -> {
            String estimationTitle = titleField.getText().trim();
            // TODO: persist estimationTitle + current breakdown data to DB / daily summary
            hideModal();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        buttons.getChildren().addAll(cancelButton, spacer, confirmButton);

        popup.getChildren().addAll(title, formGrid, buttons);

        showModal(popup);
    }

    // ── Popup builder helpers (mirrors WarehouseManagementController) ─────────

    private VBox createPopupRoot() {
        VBox root = new VBox(24);
        root.setPadding(new Insets(28, 30, 24, 30));
        root.setAlignment(Pos.TOP_LEFT);
        root.getStyleClass().add("popup-root");
        return root;
    }

    private Label createPopupTitle(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-title");
        return label;
    }

    private Label createFormLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("popup-label");
        return label;
    }

    private TextField createPopupTextField(String prompt) {
        TextField textField = new TextField();
        textField.setPromptText(prompt);
        textField.getStyleClass().add("popup-text-field");
        textField.setPrefHeight(38);
        return textField;
    }

    // ── Button handlers ───────────────────────────────────────────────────────

    @FXML
    private void onDelete() throws IOException {
        loadScreen(COST_ESTIMATION, null);
    }

    @FXML
    private void onSave() {
        showSaveEstimationPopup();
    }

    // ── PDF export ────────────────────────────────────────────────────────────

    /**
     * Exports the current breakdown to a user-chosen PDF file.
     *
     * Dependency — add to build.gradle:
     *   implementation("org.apache.pdfbox:pdfbox:3.0.2")
     *
     * PDFBox is Apache 2.0 licensed (no AGPL).
     */
    @FXML
    private void onPrintPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Cost Breakdown as PDF");
        chooser.setInitialFileName(
                "cost_breakdown_"
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + ".pdf");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        File file = chooser.showSaveDialog(printPdfButton.getScene().getWindow());
        if (file == null) return;   // user cancelled

        Thread worker = new Thread(() -> {
            try {
                writePdf(file);
            } catch (Exception e) {
                e.printStackTrace();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("PDF export failed");
                    alert.setHeaderText("Could not write PDF");
                    alert.setContentText(e.getMessage());
                    alert.showAndWait();
                });
                return;
            }

            try {
                openFile(file);
            } catch (Exception ignored) { }
        });
        worker.setDaemon(true);
        worker.start();
    }

    /**
     * Opens {@code file} in the system default application.
     * On Linux uses {@code xdg-open} directly to avoid the blocking
     * {@link Desktop} implementation bundled with some JDKs.
     */
    private void openFile(File file) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            new ProcessBuilder("xdg-open", file.getAbsolutePath())
                    .inheritIO()
                    .start();
        } else if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file);
        }
    }

    /**
     * Renders {@link #pdfRows} into an A4 PDF using Apache PDFBox 3.x.
     *
     * Layout (all values in PDF points; 1 pt = 1/72 inch):
     *   Page     595 × 842 pt  (A4 portrait)
     *   Margin   50 pt all sides
     *   Col 0    left x = 50       label text, left-aligned
     *   Col 1    right x = 545     value text, right-aligned
     */
    private void writePdf(File file) throws Exception {

        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        final float PAGE_W    = PDRectangle.A4.getWidth();    // 595
        final float PAGE_H    = PDRectangle.A4.getHeight();   // 842
        final float MARGIN    = 50f;
        final float COL0_X    = MARGIN;
        final float COL1_X    = PAGE_W - MARGIN;              // right edge for right-aligned text
        final float USABLE_W  = PAGE_W - 2 * MARGIN;

        final float TITLE_SZ  = 16f;
        final float META_SZ   =  9f;
        final float ROW_SZ    = 11f;
        final float TOTAL_SZ  = 13f;
        final float ROW_STEP  = 22f;   // vertical advance per data row
        final float SEP_STEP  = 12f;   // vertical advance after a separator

        try (PDDocument doc = new PDDocument()) {

            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {

                float y = PAGE_H - MARGIN;

                // ── Title ──────────────────────────────────────────────────
                cs.beginText();
                cs.setFont(fontBold, TITLE_SZ);
                cs.newLineAtOffset(COL0_X, y);
                cs.showText("Shipment costs breakdown");
                cs.endText();
                y -= (TITLE_SZ + 8);

                // ── Generated date ─────────────────────────────────────────
                String dateLine = "Generated: "
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("dd.MM.yyyy"));
                cs.beginText();
                cs.setFont(fontRegular, META_SZ);
                cs.setNonStrokingColor(0.5f, 0.5f, 0.5f);
                cs.newLineAtOffset(COL0_X, y);
                cs.showText(dateLine);
                cs.endText();
                cs.setNonStrokingColor(0f, 0f, 0f);
                y -= (META_SZ + 18);

                // ── Table rows ─────────────────────────────────────────────
                for (PdfRow row : pdfRows) {
                    switch (row.type()) {

                        case DATA -> {
                            PDType1Font lf = row.bold() ? fontBold : fontRegular;
                            PDType1Font rf = row.bold() ? fontBold : fontRegular;

                            if (row.left() != null && !row.left().isBlank()) {
                                cs.beginText();
                                cs.setFont(lf, ROW_SZ);
                                cs.newLineAtOffset(COL0_X, y);
                                cs.showText(row.left());
                                cs.endText();
                            }

                            if (row.right() != null && !row.right().isBlank()) {
                                float tw = rf.getStringWidth(row.right()) / 1000f * ROW_SZ;
                                cs.beginText();
                                cs.setFont(rf, ROW_SZ);
                                cs.newLineAtOffset(COL1_X - tw, y);
                                cs.showText(row.right());
                                cs.endText();
                            }

                            y -= ROW_STEP;
                        }

                        case SEPARATOR -> {
                            y -= 4;
                            cs.setStrokingColor(0.847f, 0.753f, 0.682f);
                            cs.setLineWidth(0.75f);
                            cs.moveTo(COL0_X, y);
                            cs.lineTo(COL0_X + USABLE_W, y);
                            cs.stroke();
                            cs.setStrokingColor(0f, 0f, 0f);
                            y -= SEP_STEP;
                        }

                        case TOTAL_SEPARATOR -> {
                            y -= 4;
                            cs.setStrokingColor(0.12f, 0.12f, 0.12f);
                            cs.setLineWidth(1f);
                            cs.moveTo(COL0_X, y);
                            cs.lineTo(COL0_X + USABLE_W, y);
                            cs.stroke();
                            cs.moveTo(COL0_X, y - 3.5f);
                            cs.lineTo(COL0_X + USABLE_W, y - 3.5f);
                            cs.stroke();
                            cs.setStrokingColor(0f, 0f, 0f);
                            y -= (SEP_STEP + 4);
                        }

                        case TOTAL -> {
                            if (row.right() != null && !row.right().isBlank()) {
                                float tw = fontBold.getStringWidth(row.right()) / 1000f * TOTAL_SZ;
                                cs.beginText();
                                cs.setFont(fontBold, TOTAL_SZ);
                                cs.setNonStrokingColor(0.365f, 0.247f, 0.145f);
                                cs.newLineAtOffset(COL1_X - tw, y);
                                cs.showText(row.right());
                                cs.endText();
                                cs.setNonStrokingColor(0f, 0f, 0f);
                            }
                            y -= ROW_STEP;
                        }
                    }
                }
            }

            doc.save(file);
        }
    }
}