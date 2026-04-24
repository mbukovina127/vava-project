package org.shippin.controller;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.net.URL;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

/**
 * Controller for DailyCostsSummaries.fxml
 *
 * Responsibilities:
 *  - Render a navigable month calendar
 *  - Highlight the selected / today date
 *  - Populate the "most recent summaries" list on the right
 */
public class DailyCostsSummariesController implements Initializable {

    // ── FXML injections ──────────────────────────────────────────────────────

    @FXML private ComboBox<String> monthCombo;
    @FXML private ComboBox<Integer> yearCombo;
    @FXML private GridPane dayGrid;
    @FXML private VBox summaryList;

    // ── State ─────────────────────────────────────────────────────────────────

    private YearMonth currentYearMonth;
    private LocalDate selectedDate;

    // ── Sample data  (replace with real DB/service call) ─────────────────────

    /** Map of date-string → total cost for that day */
    private final Map<String, Integer> costData = new LinkedHashMap<>() {{
        put("1.4.2026",  288);
        put("29.3.2026", 211);
        put("28.3.2026", 112);
        put("22.3.2026",  42);
        put("2.3.2026",  153);
    }};

    // ── Initializable ─────────────────────────────────────────────────────────

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        selectedDate    = LocalDate.now();
        currentYearMonth = YearMonth.now();

        initMonthCombo();
        initYearCombo();
        buildCalendar();
        buildSummaryList();
    }

    // ── ComboBox setup ────────────────────────────────────────────────────────

    private void initMonthCombo() {
        for (Month m : Month.values()) {
            String name = m.getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            // Capitalize first letter only (e.g. "Sep")
            monthCombo.getItems().add(name.substring(0, 1).toUpperCase()
                    + name.substring(1).toLowerCase());
        }
        monthCombo.getSelectionModel().select(currentYearMonth.getMonthValue() - 1);
    }

    private void initYearCombo() {
        int thisYear = LocalDate.now().getYear();
        for (int y = thisYear - 5; y <= thisYear + 5; y++) {
            yearCombo.getItems().add(y);
        }
        yearCombo.getSelectionModel().select((Integer) currentYearMonth.getYear());
    }

    // ── Navigation handlers ───────────────────────────────────────────────────

    @FXML
    private void onPrevMonth() {
        currentYearMonth = currentYearMonth.minusMonths(1);
        syncCombos();
        buildCalendar();
    }

    @FXML
    private void onNextMonth() {
        currentYearMonth = currentYearMonth.plusMonths(1);
        syncCombos();
        buildCalendar();
    }

    @FXML
    private void onMonthChanged() {
        int idx = monthCombo.getSelectionModel().getSelectedIndex();
        if (idx >= 0) {
            currentYearMonth = YearMonth.of(currentYearMonth.getYear(), idx + 1);
            buildCalendar();
        }
    }

    @FXML
    private void onYearChanged() {
        Integer y = yearCombo.getSelectionModel().getSelectedItem();
        if (y != null) {
            currentYearMonth = YearMonth.of(y, currentYearMonth.getMonth());
            buildCalendar();
        }
    }

    /** Keep ComboBoxes in sync after prev/next navigation */
    private void syncCombos() {
        monthCombo.getSelectionModel().select(currentYearMonth.getMonthValue() - 1);
        yearCombo.getSelectionModel().select((Integer) currentYearMonth.getYear());
    }

    // ── Calendar grid builder ─────────────────────────────────────────────────

    private void buildCalendar() {
        dayGrid.getChildren().clear();
        dayGrid.getRowConstraints().clear();

        LocalDate firstOfMonth = currentYearMonth.atDay(1);
        // Sunday = 0 offset; DayOfWeek: MON=1..SUN=7 → map SUN to 0
        int startCol = firstOfMonth.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentYearMonth.lengthOfMonth();

        // Previous month trailing days (muted)
        YearMonth prevYM = currentYearMonth.minusMonths(1);
        int prevLen = prevYM.lengthOfMonth();
        for (int i = startCol - 1; i >= 0; i--) {
            int day = prevLen - i;
            Button btn = makeDayButton(String.valueOf(day), "dcs-day-btn-muted", null);
            int col = startCol - 1 - i;
            GridPane.setHalignment(btn, HPos.CENTER);
            dayGrid.add(btn, col, 0);
        }

        // Current month days
        int col = startCol;
        int row = 0;
        LocalDate today = LocalDate.now();

        for (int d = 1; d <= daysInMonth; d++) {
            LocalDate date = currentYearMonth.atDay(d);
            boolean isSelected = date.equals(selectedDate);
            boolean isToday    = date.equals(today);

            String styleClass = (isSelected || isToday)
                    ? "dcs-day-btn-selected"
                    : "dcs-day-btn";

            Button btn = makeDayButton(String.valueOf(d), styleClass, date);

            GridPane.setHalignment(btn, HPos.CENTER);
            dayGrid.add(btn, col, row);

            col++;
            if (col > 6) { col = 0; row++; }
        }

        // Next month leading days (muted, fill remaining cells)
        int nextDay = 1;
        while (col <= 6) {
            Button btn = makeDayButton(String.valueOf(nextDay++), "dcs-day-btn-muted", null);
            GridPane.setHalignment(btn, HPos.CENTER);
            dayGrid.add(btn, col, row);
            col++;
        }

        // Row constraints — equal height for each week row
        int totalRows = row + 1;
        for (int r = 0; r < totalRows; r++) {
            RowConstraints rc = new RowConstraints(38);
            rc.setValignment(javafx.geometry.VPos.CENTER);
            dayGrid.getRowConstraints().add(rc);
        }
    }

    /** Creates a calendar day button with the given style class and optional click date */
    private Button makeDayButton(String text, String styleClass, LocalDate date) {
        Button btn = new Button(text);
        btn.getStyleClass().add(styleClass);
        btn.setMaxWidth(Double.MAX_VALUE);

        if (date != null) {
            btn.setOnAction(e -> onDaySelected(date));
        }
        return btn;
    }

    private void onDaySelected(LocalDate date) {
        selectedDate = date;
        buildCalendar();  // re-render to move the selection highlight
        // TODO: load cost details for this date and display them
    }

    // ── Summary list builder ──────────────────────────────────────────────────

    /**
     * Builds the right-hand "most recent summaries" list.
     * Replace costData with a real service/repository call.
     */
    private void buildSummaryList() {
        summaryList.getChildren().clear();

        for (Map.Entry<String, Integer> entry : costData.entrySet()) {
            HBox row = buildSummaryRow(entry.getKey(), entry.getValue());
            summaryList.getChildren().add(row);
        }
    }

    /**
     * Creates a single summary row card:
     *   [ date label ]   [ amount label ]
     */
    private HBox buildSummaryRow(String dateStr, int amount) {
        Label dateLabel = new Label(dateStr);
        dateLabel.getStyleClass().add("dcs-row-date");

        Label amountLabel = new Label(amount + "€");
        amountLabel.getStyleClass().add("dcs-row-amount");

        // Spacer pushes amount to the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(dateLabel, spacer, amountLabel);
        row.getStyleClass().add("dcs-summary-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        // Click → open detail for this date
        row.setOnMouseClicked(e -> onSummaryRowClicked(dateStr));

        return row;
    }

    private void onSummaryRowClicked(String dateStr) {
        // TODO: navigate to / open the daily detail view for dateStr
        System.out.println("Opening summary for: " + dateStr);
    }

    // ── Public API (call from parent controller if needed) ───────────────────

    /**
     * Refresh the summary list with fresh data from the database.
     * @param data ordered map of dateString → totalCost
     */
    public void loadSummaries(Map<String, Integer> data) {
        costData.clear();
        costData.putAll(data);
        buildSummaryList();
    }

    /**
     * Jump the calendar to a specific month/year programmatically.
     */
    public void navigateTo(YearMonth ym) {
        currentYearMonth = ym;
        syncCombos();
        buildCalendar();
    }
}
