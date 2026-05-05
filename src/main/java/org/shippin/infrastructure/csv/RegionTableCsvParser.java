package org.shippin.infrastructure.csv;

import org.shippin.infrastructure.validation.RegionTableValidator;
import org.shippin.util.Range;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Table;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegionTableCsvParser implements CsvParser<RegionTableRow> {

    @Override
    public Table<RegionTableRow> parseFromCsv(String text) throws ValidationException {
        RegionTableFormatted table = new RegionTableFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        String[] lines = text.split("\r?\n");
        if (lines.length < 2) {
            return table;
        }

        Map<String, List<String>> rawRegionRanges = new HashMap<>();
        Map<String, List<Range>> regionToRanges = new HashMap<>();
        //load data
        for (int i = 1; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || line.startsWith(";")) continue;

            String[] fields = line.split(";");
            if (fields.length < 1) continue;

            String regionCode = fields[0].trim();
            if (regionCode.isEmpty()) continue;

            List<Range> rangesThisLine = new ArrayList<>();

            for (int j = 2; j < fields.length; j++) {
                String cell = fields[j].trim();
                if (cell.isEmpty()) continue;

                rawRegionRanges.computeIfAbsent(regionCode, _ -> new ArrayList<>()).add(cell);

                if (cell.contains("-")) {
                    String[] parts = cell.split("-", 2);
                    if (parts.length == 2) {
                        try {
                            int start = Integer.parseInt(parts[0].trim());
                            int end   = Integer.parseInt(parts[1].trim());
                            rangesThisLine.add(new Range(start, end));
                        } catch (NumberFormatException e) {
                            // ignore invalid range
                        }
                    }
                } else {
                    try {
                        int code = Integer.parseInt(cell.trim());
                        rangesThisLine.add(new Range(code, code));
                    } catch (NumberFormatException ignored) {}
                }
            }

            regionToRanges.computeIfAbsent(regionCode, _ -> new ArrayList<>()).addAll(rangesThisLine);
        }

        RegionTableValidator.validate(rawRegionRanges);

        // convert data to table
        for (Map.Entry<String, List<Range>> entry : regionToRanges.entrySet()) {
            RegionTableRow row = new RegionTableRow(entry.getKey());
            row.setRanges(entry.getValue());
            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToCsv(Table<RegionTableRow> table) {
        //safety chcek
        if (!(table instanceof RegionTableFormatted rtf)) {
            return "";
        }

        List<RegionTableRow> rows = rtf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        //build header
        sb.append("Rozdelenie PSČ:;;;;;;\n");

        //build data for each line
        for (RegionTableRow row : rows) {
            String code = row.getRegionCode();
            List<Range> ranges = row.getRanges();

            sb.append(code).append(";;");

            if (!ranges.isEmpty()) {
                List<String> rangeStrs = new ArrayList<>();
                for (Range rg : ranges) {
                    rangeStrs.add(rg.getMin() + "-" + rg.getMax());
                }
                sb.append(String.join(";", rangeStrs));
            }

            int approxColsAfterCode = 6;
            int used = 2 + ranges.size();
            int trailing = Math.max(0, approxColsAfterCode - used + 1);
            sb.append(";".repeat(trailing));
            sb.append("\n");
        }

        return sb.toString();
    }
}
