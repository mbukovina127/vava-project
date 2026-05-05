
package org.shippin.infrastructure.xml;

import org.shippin.infrastructure.validation.PriceListValidator;
import org.shippin.services.NavigationService;
import org.shippin.domain.formatted.PriceListFormatted;
import org.shippin.domain.formatted.PriceListRow;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class PriceListXmlParser implements XmlParser<PriceListRow> {

    private static final String SS_NS = "urn:schemas-microsoft-com:office:spreadsheet";

    @Override
    public Table<PriceListRow> parseFromXml(String text) throws ValidationException {
        PriceListFormatted table = new PriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        List<List<String>> rows = parseRowsFromXml(text);
        if (rows.size() < 3) {
            return table;
        }

        //load region codes
        List<String> regionLine = rows.get(1);
        List<String> regionCodes = new ArrayList<>();
        Set<String> seenCodes = new LinkedHashSet<>();
        for (int i = 2; i < regionLine.size(); i++) {
            String code = regionLine.get(i).trim();
            if (!code.isEmpty()) {
                if (!seenCodes.add(code)) {
                    throw new ValidationException(List.of("Duplicate zone column in header: " + code));
                }
                regionCodes.add(code);
            }
        }

        List<String> hmotnostList = new ArrayList<>();
        List<String> objemList    = new ArrayList<>();
        Map<String, List<String>> regionColumns = new HashMap<>();
        for (String code : regionCodes) regionColumns.put(code, new ArrayList<>());

        //load weight + volume and region prices for each line
        for (int i = 2; i < rows.size(); i++) {
            List<String> fields = rows.get(i);
            if (fields.size() < 2 + regionCodes.size()) continue;

            hmotnostList.add(fields.get(0).trim());
            objemList.add(fields.get(1).trim());

            for (int j = 0; j < regionCodes.size(); j++) {
                int colIndex = 2 + j;
                if (colIndex < fields.size()) {
                    regionColumns.get(regionCodes.get(j)).add(fields.get(colIndex).trim());
                }
            }
        }

        PriceListValidator.validate(hmotnostList, objemList, regionColumns, NavigationService.getBundle());

        //build table
        for (int i = 0; i < hmotnostList.size(); i++) {
            float weight = parseNumber(hmotnostList.get(i));
            float volume = parseNumber(objemList.get(i));

            PriceListRow row = new PriceListRow(weight, volume);

            for (String code : regionCodes) {
                List<String> prices = regionColumns.get(code);
                if (i < prices.size()) {
                    row.getRegions().put(code, parseNumber(prices.get(i)));
                }
            }

            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToXml(Table<PriceListRow> table) {
        //safety check
        if (!(table instanceof PriceListFormatted plf)) {
            return "";
        }

        List<PriceListRow> rows = plf.getRows();
        if (rows.isEmpty()) {
            return "";
        }

        // obtain regions (from first line)
        PriceListRow firstRow = rows.getFirst();
        List<String> regionCodes = new ArrayList<>(firstRow.getRegions().keySet());

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.newDocument();

            Element workbook = doc.createElementNS(SS_NS, "Workbook");
            workbook.setAttribute("xmlns:ss", SS_NS);
            doc.appendChild(workbook);

            Element worksheet = doc.createElementNS(SS_NS, "Worksheet");
            worksheet.setAttributeNS(SS_NS, "ss:Name", "priceList");
            workbook.appendChild(worksheet);

            Element tableEl = doc.createElementNS(SS_NS, "Table");
            worksheet.appendChild(tableEl);

            //build header row
            Element headerRow = createRow(doc);
            headerRow.appendChild(createStringCell(doc, "Hmotnosť do (v kg)"));
            headerRow.appendChild(createStringCell(doc, "Objem do (v m³)"));
            headerRow.appendChild(createStringCell(doc, "Zóny"));
            tableEl.appendChild(headerRow);

            //add region codes row
            Element regionRow = createRow(doc);
            Element firstRegionCell = createStringCell(doc, regionCodes.getFirst());
            firstRegionCell.setAttributeNS(SS_NS, "ss:Index", "3");
            regionRow.appendChild(firstRegionCell);
            for (int i = 1; i < regionCodes.size(); i++) {
                regionRow.appendChild(createStringCell(doc, regionCodes.get(i)));
            }
            tableEl.appendChild(regionRow);

            //build data for each line
            for (PriceListRow row : rows) {
                Element dataRow = createRow(doc);
                dataRow.appendChild(createNumberCell(doc, row.getWeight()));
                dataRow.appendChild(createNumberCell(doc, row.getVolume()));

                for (String reg : regionCodes) {
                    Float price = row.getRegions().get(reg);
                    dataRow.appendChild(createNumberCell(doc, price != null ? price : 0f));
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

    private float parseNumber(String value) {
        try {
            return Math.round(Float.parseFloat(value) * 100f) / 100f;
        } catch (NumberFormatException e) {
            return 0f;
        }
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

    private Element createNumberCell(Document doc, float value) {
        Element cell = doc.createElementNS(SS_NS, "Cell");
        Element data = doc.createElementNS(SS_NS, "Data");
        data.setAttributeNS(SS_NS, "ss:Type", "Number");
        data.setTextContent(String.valueOf(value));
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
