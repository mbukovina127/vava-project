package org.shippin.infrastructure.xml;

import org.shippin.infrastructure.validation.RegionTableValidator;
import org.shippin.services.NavigationService;
import org.shippin.util.Range;
import org.shippin.domain.formatted.RegionTableFormatted;
import org.shippin.domain.formatted.RegionTableRow;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Table;
import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegionTableXmlParser implements XmlParser<RegionTableRow> {

    private static final String SS_NS = "urn:schemas-microsoft-com:office:spreadsheet";

    @Override
    public Table<RegionTableRow> parseFromXml(String text) throws ValidationException {
        RegionTableFormatted table = new RegionTableFormatted();

        // safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        List<List<String>> rows = parseRowsFromXml(text);
        if (rows.size() < 2) {
            return table;
        }

        Map<String, List<String>> rawRegionRanges = new HashMap<>();
        Map<String, List<Range>> regionToRanges = new HashMap<>();
        String currentRegion = null;
        List<String> regionCodesInOrder = new ArrayList<>();   // for duplicate detection

        // load data (skip header row)
        for (int i = 1; i < rows.size(); i++) {
            List<String> fields = rows.get(i);
            if (fields.isEmpty()) continue;

            String firstCell = fields.getFirst().trim();

            // if first cell has a region code, update current region
            if (!firstCell.isEmpty()) {
                currentRegion = firstCell;
                regionCodesInOrder.add(currentRegion);   // record every explicit region code occurrence
            }

            if (currentRegion == null) continue;

            // parse ranges from columns starting at index 2
            List<Range> rangesThisLine = new ArrayList<>();

            for (int j = 2; j < fields.size(); j++) {
                String cell = fields.get(j).trim();
                if (cell.isEmpty()) continue;

                rawRegionRanges.computeIfAbsent(currentRegion, _ -> new ArrayList<>()).add(cell);

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

            regionToRanges.computeIfAbsent(currentRegion, _ -> new ArrayList<>()).addAll(rangesThisLine);
        }

        RegionTableValidator.validate(rawRegionRanges, regionCodesInOrder, NavigationService.getBundle());

        // convert data to table
        for (Map.Entry<String, List<Range>> entry : regionToRanges.entrySet()) {
            RegionTableRow row = new RegionTableRow(entry.getKey());
            row.setRanges(entry.getValue());
            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToXml(Table<RegionTableRow> table) {
        // safety check
        if (!(table instanceof RegionTableFormatted rtf)) {
            return "";
        }

        List<RegionTableRow> rows = rtf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element workbook = doc.createElementNS(SS_NS, "Workbook");
            workbook.setAttribute("xmlns:ss", SS_NS);
            doc.appendChild(workbook);

            Element worksheet = doc.createElementNS(SS_NS, "Worksheet");
            worksheet.setAttributeNS(SS_NS, "ss:Name", "regionTable");
            workbook.appendChild(worksheet);

            Element tableEl = doc.createElementNS(SS_NS, "Table");
            worksheet.appendChild(tableEl);

            // build header
            Element headerRow = createRow(doc);
            headerRow.appendChild(createStringCell(doc, "Rozdelenie PSČ:"));
            tableEl.appendChild(headerRow);

            // build data for each line
            for (RegionTableRow row : rows) {
                String code = row.getRegionCode();
                List<Range> ranges = row.getRanges();

                Element dataRow = createRow(doc);
                dataRow.appendChild(createStringCell(doc, code));

                if (!ranges.isEmpty()) {
                    Element firstRangeCell = createStringCell(doc,
                            ranges.getFirst().getMin() + "-" + ranges.getFirst().getMax());
                    firstRangeCell.setAttributeNS(SS_NS, "ss:Index", "3");
                    dataRow.appendChild(firstRangeCell);

                    for (int i = 1; i < ranges.size(); i++) {
                        Range rg = ranges.get(i);
                        dataRow.appendChild(createStringCell(doc, rg.getMin() + "-" + rg.getMax()));
                    }
                }

                tableEl.appendChild(dataRow);
            }

            return transformToString(doc);
        } catch (Exception e) {
            return "";
        }
    }

    private List<List<String>> parseRowsFromXml(String text) {
        List<List<String>> result = new ArrayList<>();
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8)));

            NodeList rowNodes = doc.getElementsByTagNameNS(SS_NS, "Row");
            for (int i = 0; i < rowNodes.getLength(); i++) {
                Element rowEl = (Element) rowNodes.item(i);
                List<String> cells = parseCells(rowEl);
                result.add(cells);
            }
        } catch (Exception e) {
            // return empty on parse failure
        }
        return result;
    }

    private List<String> parseCells(Element rowEl) {
        List<String> cells = new ArrayList<>();
        NodeList cellNodes = rowEl.getElementsByTagNameNS(SS_NS, "Cell");

        for (int i = 0; i < cellNodes.getLength(); i++) {
            Element cellEl = (Element) cellNodes.item(i);

            // handle ss:Index for skipped cells
            String indexAttr = cellEl.getAttributeNS(SS_NS, "Index");
            if (!indexAttr.isEmpty()) {
                int targetIndex = Integer.parseInt(indexAttr) - 1;
                while (cells.size() < targetIndex) {
                    cells.add("");
                }
            }

            NodeList dataNodes = cellEl.getElementsByTagNameNS(SS_NS, "Data");
            if (dataNodes.getLength() > 0) {
                cells.add(dataNodes.item(0).getTextContent());
            } else {
                cells.add("");
            }
        }
        return cells;
    }

    private Element createRow(Document doc) {
        return doc.createElementNS(SS_NS, "Row");
    }

    private Element createStringCell(Document doc, String value) {
        Element cell = doc.createElementNS(SS_NS, "Cell");
        Element data = doc.createElementNS(SS_NS, "Data");
        data.setAttributeNS(SS_NS, "ss:Type", "String");
        data.setTextContent(value);
        cell.appendChild(data);
        return cell;
    }

    private String transformToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(javax.xml.transform.OutputKeys.OMIT_XML_DECLARATION, "no");
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.toString();
    }
}
