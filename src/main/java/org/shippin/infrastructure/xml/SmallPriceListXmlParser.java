package org.shippin.infrastructure.xml;

import org.shippin.infrastructure.validation.SmallPriceListValidator;
import org.shippin.domain.formatted.SmallPriceListFormatted;
import org.shippin.domain.formatted.SmallPriceListRow;
import org.shippin.exception.ValidationException;
import org.shippin.domain.Table;
import org.shippin.util.NumberUtils;
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
import java.util.List;


public class SmallPriceListXmlParser implements XmlParser<SmallPriceListRow> {

    private static final String SS_NS = "urn:schemas-microsoft-com:office:spreadsheet";

    @Override
    public Table<SmallPriceListRow> parseFromXml(String text) throws ValidationException {
        SmallPriceListFormatted table = new SmallPriceListFormatted();

        //safety check
        if (text == null || text.trim().isEmpty()) {
            return table;
        }

        List<List<String>> rows = parseRowsFromXml(text);
        if (rows.size() < 2) {
            return table;
        }

        List<String> header = rows.getFirst();
        String headerCol0 = header.size() > 0 ? header.get(0).trim() : "";
        String headerCol1 = header.size() > 1 ? header.get(1).trim() : "";

        List<String> weightDescs = new ArrayList<>();
        List<String> priceStrs   = new ArrayList<>();

        // skip header row
        for (int i = 1; i < rows.size(); i++) {
            List<String> fields = rows.get(i);
            if (fields.size() < 2) continue;

            weightDescs.add(fields.get(0).trim());
            priceStrs.add(fields.get(1).trim());
        }

        SmallPriceListValidator.validate(headerCol0, headerCol1, weightDescs, priceStrs);

        for (int i = 0; i < weightDescs.size(); i++) {
            String weightDesc = weightDescs.get(i);
            String costStr    = priceStrs.get(i);

            // parse the upper weight limit from "do X kg"
            float weightLimit = 0f;
            if (weightDesc.startsWith("do ")) {
                String numPart = weightDesc.substring(3).replace(" kg", "").trim();
                try {
                    weightLimit = Float.parseFloat(numPart.replace(',', '.'));
                } catch (NumberFormatException ignored) {
                    continue;
                }
            }

            float cost = parseNumber(costStr);

            SmallPriceListRow row = new SmallPriceListRow(weightLimit, cost);
            table.addRow(row);
        }

        return table;
    }

    @Override
    public String exportToXml(Table<SmallPriceListRow> table) {
        //safety check
        if (!(table instanceof SmallPriceListFormatted splf)) {
            return "";
        }

        List<SmallPriceListRow> rows = splf.getRows();
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
            worksheet.setAttributeNS(SS_NS, "ss:Name", "smallPriceList");
            workbook.appendChild(worksheet);

            Element tableEl = doc.createElementNS(SS_NS, "Table");
            worksheet.appendChild(tableEl);

            // Header
            Element headerRow = createRow(doc);
            headerRow.appendChild(createStringCell(doc, "Hmotnosť"));
            headerRow.appendChild(createStringCell(doc, "Cena"));
            tableEl.appendChild(headerRow);

            for (SmallPriceListRow row : rows) {
                // Format weight as "do X kg"
                String weightStr = "do " + NumberUtils.formatFloat(row.getWeight()) + " kg";

                Element dataRow = createRow(doc);
                dataRow.appendChild(createStringCell(doc, weightStr));
                dataRow.appendChild(createNumberCell(doc, row.getCost()));
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
