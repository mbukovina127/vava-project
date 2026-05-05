package org.shippin.infrastructure.csv;

import org.shippin.infrastructure.validation.PriceListValidator;
import org.shippin.services.NavigationService;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Table;
import org.shippin.util.NumberUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import static org.shippin.infrastructure.validation.ValidationMessages.msg;

public class PriceListCsvParser implements CsvParser<PriceListRow> {

    @Override
    public Table<PriceListRow> parseFromCsv(String text) throws ValidationException {
        PriceListFormatted table = new PriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        String[] lines = text.split("\r?\n");
        if (lines.length < 3) {
            return table;
        }

        //load region codes
        String[] regionLineParts = lines[1].split(";");
        List<String> regionCodes = new ArrayList<>();
        Set<String> seenCodes = new LinkedHashSet<>();
        for (int i = 2; i < regionLineParts.length; i++) {
            String code = regionLineParts[i].trim();
            if (!code.isEmpty()) {
                if (!seenCodes.add(code)) {
                    throw new ValidationException(List.of(
                            msg("csv.price_list.duplicate_zone_column", code)
                    		));
                }
                regionCodes.add(code);
            }
        }

        List<String> hmotnostList = new ArrayList<>();
        List<String> objemList    = new ArrayList<>();
        Map<String, List<String>> regionColumns = new HashMap<>();
        for (String code : regionCodes) regionColumns.put(code, new ArrayList<>());

        //load weight + volume and region prices for each line
        for (int i = 2; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            String[] fields = line.split(";");
            if (fields.length < 2 + regionCodes.size()) continue;

            hmotnostList.add(fields[0].trim());
            objemList.add(fields[1].trim());

            for (int j = 0; j < regionCodes.size(); j++) {
                int colIndex = 2 + j;
                if (colIndex < fields.length) {
                    regionColumns.get(regionCodes.get(j)).add(fields[colIndex].trim());
                }
            }
        }

        PriceListValidator.validate(hmotnostList, objemList, regionColumns, NavigationService.getBundle());

        //build table
        for (int i = 0; i < hmotnostList.size(); i++) {
            float weight = NumberUtils.parseFloat(hmotnostList.get(i));
            float volume = NumberUtils.parseFloat(objemList.get(i));

            PriceListRow row = new PriceListRow(weight, volume);

            for (String code : regionCodes) {
                List<String> prices = regionColumns.get(code);
                if (i < prices.size()) {
                    row.getRegions().put(code, NumberUtils.parseFloat(prices.get(i)));
                }
            }

            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToCsv(Table<PriceListRow> table) {
        //safety check
        if (!(table instanceof PriceListFormatted plf)) {
            return "";
        }

        List<PriceListRow> rows = plf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        // optain regions (from first line)
        PriceListRow firstRow = rows.getFirst();
        List<String> regionCodes = new ArrayList<>(firstRow.getRegions().keySet());
        int numRegions = regionCodes.size();

        StringBuilder sb = new StringBuilder();

        //build header
        sb.append("Hmotnosť do (v kg);Objem do (v m³);Zóny");
        sb.append(";".repeat(Math.max(0, numRegions - 1)));
        sb.append("\r\n");

        //add region codes
        sb.append(";;");
        sb.append(String.join(";", regionCodes));
        sb.append("\r\n");

        //build data for each line
        for (PriceListRow row : rows) {

            String weightStr = NumberUtils.formatFloat(row.getWeight());
            String volumeStr = NumberUtils.formatFloat(row.getVolume());

            List<String> priceStrs = new ArrayList<>();
            for (String reg : regionCodes) {
                Float price = row.getRegions().get(reg);
                priceStrs.add(NumberUtils.formatFloat(price != null ? price : 0f));
            }

            sb.append(weightStr)
                    .append(";")
                    .append(volumeStr)
                    .append(";")
                    .append(String.join(";", priceStrs))
                    .append("\r\n");
        }

        return sb.toString();
    }

}
