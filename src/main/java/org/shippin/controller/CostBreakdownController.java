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
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.shippin.domain.AdditionalService;
import org.shippin.domain.Shipment;
import org.shippin.services.ShipmentService;
import org.shippin.services.UserService;

import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.shippin.dto.Screens.COST_ESTIMATION;
import static org.shippin.dto.Screens.SHIPMENT_DETAIL;

public class CostBreakdownController extends BaseController<Shipment> implements Initializable {

    @FXML private GridPane  breakdownGrid;
    @FXML private Button    deleteButton;
    @FXML private Button    printPdfButton;
    @FXML private Button    saveButton;

    private final ShipmentService shipmentService = new ShipmentService();

    private Shipment shipment;

    private int gridRow = 0;

    // PDF mirror
    private enum PdfRowType { DATA, SEPARATOR, TOTAL_SEPARATOR, TOTAL }
    private record PdfRow(PdfRowType type, String left, String right, boolean bold) {}
    private final List<PdfRow> pdfRows = new ArrayList<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {}

    @Override
    protected Class<Shipment> getDataType() { return Shipment.class; }

    private static String fmt(float value) {
        return String.format("%.2f €", value);
    }

    @Override
    protected void onData(Shipment data) throws SQLException {
        breakdownGrid.getChildren().clear();
        pdfRows.clear();
        gridRow = 0;
        this.shipment = data;

        // Route
        String from = data.getWarehouse() != null ? data.getWarehouse().getName() : "—";
        String dest = data.getDest_region() > 0 ? String.format("%05d", data.getDest_region()) : "—";
        addRow("Route:", from + " – " + dest,"" , true, false);

        // Size
        String size = data.getWeight() + " kg";
        if (data.getVolume() > 0) size += "  /  " + data.getVolume() + " m³";

        float baseCost = ShipmentService.calculateBaseCost(shipment);
        addRow("Size:", size, fmt(baseCost), true, false);

        addSeparator();

        // Fuel surcharge
        String fuelPct = (int)(data.getFuel_payment() * 100) + "%";
        addRow("Fuel surcharge:", fuelPct, fmt(ShipmentService.calculateFuelCost(shipment, baseCost)), true, false);

        // Toll
        String tollPct = (int)(data.getToll() * 100) + "%";
        addRow("Toll:", tollPct, fmt(ShipmentService.calculateTollCost(shipment, baseCost)), true, false);

        addSeparator();


        // Additional services
        if (data.getServices() != null) {
            for (AdditionalService s : data.getServices())
            {
                addRow(s.getName(), "", fmt(ShipmentService.calculateServiceCost(shipment,baseCost,s)), true, false);
            }
        }

        addTotalSeparator();
        addTotalRow();
    }

    // ── Grid helpers ──────────────────────────────────────────────

    private void addRow(String leftText, String middleText, String rightText, boolean bold, boolean italic) {
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
        sep.getStyleClass().add("separator");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);
        pdfRows.add(new PdfRow(PdfRowType.SEPARATOR, null, null, false));
        gridRow++;
    }

    private void addTotalSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().addAll("separator", "separator-total");
        GridPane.setColumnIndex(sep, 0);
        GridPane.setRowIndex(sep, gridRow);
        GridPane.setColumnSpan(sep, 2);
        breakdownGrid.getChildren().add(sep);
        pdfRows.add(new PdfRow(PdfRowType.TOTAL_SEPARATOR, null, null, false));
        gridRow++;
    }

    private void addTotalRow() {
        String totalText = String.format("%.2f €", shipment.getTotalCost());
        Label total = new Label(totalText);
        total.getStyleClass().addAll("cb-value", "cb-total");
        GridPane.setHalignment(total, HPos.RIGHT);
        GridPane.setColumnIndex(total, 1);
        GridPane.setRowIndex(total, gridRow);
        breakdownGrid.getChildren().add(total);
        pdfRows.add(new PdfRow(PdfRowType.TOTAL, null, totalText, true));
        gridRow++;
    }

    // ── Save popup ────────────────────────────────────────────────

    private void showSaveEstimationPopup() {
        VBox popup = createPopupRoot();
        popup.setMaxWidth(400);
        popup.setPrefWidth(400);

        Label title = createPopupTitle("Save shipment");

        Label info = new Label("Save this shipment to the database?");
        info.getStyleClass().add("popup-label");

        HBox buttons = new HBox(18);
        buttons.setAlignment(Pos.CENTER_LEFT);

        Button cancelButton = new Button("Cancel");
        cancelButton.getStyleClass().addAll("popup-button", "popup-secondary-button");
        cancelButton.setPrefSize(150, 42);
        cancelButton.setOnAction(e -> hideModal());

        Button confirmButton = new Button("Save");
        confirmButton.getStyleClass().addAll("popup-button", "popup-primary-button");
        confirmButton.setPrefSize(150, 42);
        confirmButton.setOnAction(e -> {
            hideModal();
            try {
                shipmentService.saveShipment(shipment, UserService.getUser().getId());
                loadScreen(SHIPMENT_DETAIL, shipment);
            } catch (SQLException ex) {
                new Alert(Alert.AlertType.ERROR, "Could not save shipment: " + ex.getMessage()).showAndWait();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        buttons.getChildren().addAll(cancelButton, spacer, confirmButton);

        popup.getChildren().addAll(title, info, buttons);
        showModal(popup);
    }

    // ── Popup helpers ─────────────────────────────────────────────

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

    // ── Button handlers ───────────────────────────────────────────

    @FXML
    private void onDelete() throws IOException {
        loadScreen(COST_ESTIMATION, null);
    }

    @FXML
    private void onSave() {
        showSaveEstimationPopup();
    }

    // ── PDF export ────────────────────────────────────────────────

    @FXML
    private void onPrintPdf() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save Cost Breakdown as PDF");
        chooser.setInitialFileName(
                "cost_breakdown_"
                        + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                        + ".pdf");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF files", "*.pdf"));

        File file = chooser.showSaveDialog(printPdfButton.getScene().getWindow());
        if (file == null) return;

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
            try { openFile(file); } catch (Exception ignored) {}
        });
        worker.setDaemon(true);
        worker.start();
    }

    private void openFile(File file) throws Exception {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("linux")) {
            new ProcessBuilder("xdg-open", file.getAbsolutePath()).inheritIO().start();
        } else if (Desktop.isDesktopSupported()
                && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
            Desktop.getDesktop().open(file);
        }
    }

    private void writePdf(File file) throws Exception {
        PDType1Font fontRegular = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDType1Font fontBold    = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);

        final float PAGE_W   = PDRectangle.A4.getWidth();
        final float PAGE_H   = PDRectangle.A4.getHeight();
        final float MARGIN   = 50f;
        final float COL0_X   = MARGIN;
        final float COL1_X   = PAGE_W - MARGIN;
        final float USABLE_W = PAGE_W - 2 * MARGIN;

        final float TITLE_SZ = 16f;
        final float META_SZ  =  9f;
        final float ROW_SZ   = 11f;
        final float TOTAL_SZ = 13f;
        final float ROW_STEP = 22f;
        final float SEP_STEP = 12f;

        try (PDDocument doc = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            doc.addPage(page);

            try (PDPageContentStream cs = new PDPageContentStream(doc, page)) {
                float y = PAGE_H - MARGIN;

                cs.beginText();
                cs.setFont(fontBold, TITLE_SZ);
                cs.newLineAtOffset(COL0_X, y);
                cs.showText("Shipment costs breakdown");
                cs.endText();
                y -= (TITLE_SZ + 8);

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
