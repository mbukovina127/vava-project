import org.shippin.infrastructure.csv.PriceListCsvParser;
import org.shippin.infrastructure.csv.RegionTableCsvParser;
import org.shippin.infrastructure.formatted.*;
import org.shippin.infrastructure.table.Row;

public class ParserTest {

    public static void main(String[] args) {


        // PriceListCsvParser test

        String priceCsv  = """
Hmotnosť do (v kg);Objem do (v m³);Zóny;;;;;
;;BA1;BA2;BA3;ZA;ZV;KE
50;0,2;15,66;16,80;17,35;17,57;17,57;18,52
100;0,4;16,64;18,46;19,10;20,25;20,26;22,07
150;0,6;18,20;20,14;21,80;22,34;22,36;24,80
200;0,8;19,77;21,80;23,67;24,48;24,51;27,65
250;1,0;21,32;23,49;26,00;26,99;27,03;30,87
300;1,2;22,86;25,16;28,10;29,46;29,51;34,12
350;1,4;23,85;26,83;30,21;31,83;31,89;37,41
400;1,6;24,87;27,88;32,30;33,83;33,87;40,17
450;1,8;26,41;29,54;34,37;35,84;35,90;42,81
500;2,0;27,40;31,21;35,89;37,91;37,95;46,32
600;2,4;30,51;35,19;39,50;43,74;43,80;56,02
700;2,8;34,18;39,77;46,76;51,59;51,69;65,88
800;3,2;37,32;43,12;52,24;56,86;56,99;73,23
900;3,6;39,30;46,44;56,12;61,61;61,77;80,08
1000;4,0;41,28;49,16;60,79;66,56;66,71;86,78
1250;5,0;47,67;55,93;72,13;78,64;78,82;103,24
1500;6,0;54,84;63,55;82,62;89,94;90,15;119,15
1750;7,0;59,79;68,73;91,61;100,20;100,47;134,05
2000;8,0;66,22;73,90;101,36;110,64;110,93;149,31
2250;9,0;70,43;78,27;110,35;120,56;120,92;163,99
2500;10,0;75,37;84,25;117,76;130,29;130,68;178,56
3500;14,0;110,82;126,73;169,62;187,01;187,52;252,07
5000;20,0;135,67;151,64;211,97;234,52;235,24;321,42
""";


        PriceListCsvParser priceParser  = new PriceListCsvParser();
        PriceListFormatted priceListTable = (PriceListFormatted) priceParser.parseFromCsv(priceCsv);

        System.out.println("=== PRICE LIST PARSER ===");
        System.out.println("Loaded " + priceListTable.getRows().size() + " rows");

        PriceListRow firstPriceRow = (PriceListRow) priceListTable.getRows().getFirst();
        System.out.println("First row: " + firstPriceRow.getWeight() + " kg, " + firstPriceRow.getVolume() + " m");
        System.out.println("BA1 price: " + firstPriceRow.getRegions().get("BA1"));


        String exportedPrice = priceParser.exportToCsv(priceListTable);
        System.out.println("\n=== ExportedPrice CSV ===");
        String[] priceLines = exportedPrice.split("\n");
        for (String line : priceLines) {
            System.out.println(line);
        }
        System.out.println("total " + priceLines.length + " lines");



        // RegionTableCsvParser test

        String regionCsv = """
Rozdelenie PSČ:;;;;;;
BA1;;81000-85999;;;;
BA2;;90001-91099;91700-92099;92242-93399;94000-95499;
BA3;;01831-01857;01901-01999;91100-91699;92100-92241;95500-95999
;;97100-97399;;;;
ZA;;01000-01826;01861-01864;02000-03999;;
ZV;;93400-93999;96000-96999;97400-99399;;
;;04901-04901;04913-04918;04961-04964;;
;;05001-05001;;;;
KE;;04000-04900;04902-04912;04919-04960;;
;;04965-05000;05002-06599;06600-09599;;
""";

        RegionTableCsvParser regionParser  = new RegionTableCsvParser();
        RegionTableFormatted regionTable = (RegionTableFormatted) regionParser.parseFromCsv(regionCsv);

        System.out.println("\n=== REGION TABLE PARSER ===");
        System.out.println("Loaded " + regionTable.getRows().size() + " regions");

        RegionTableRow firstRegionRow = (RegionTableRow) regionTable.getRows().getFirst();
        System.out.println("First row region code: " + firstRegionRow.getRegionCode());
        System.out.println("First row Min Range: " + firstRegionRow.getRanges().getFirst().getMin() + ", and Max Range:" + firstRegionRow.getRanges().getFirst().getMax());

        //EXAMPLE: Postal code check/lookup

        int testPsc = 82105;
        System.out.println("\nTesting postal code " + testPsc + ":");
        boolean found = false;
        for (Row row : regionTable.getRows()) {
            RegionTableRow r = (RegionTableRow) row;
            for (Range rg : r.getRanges()) {
                if (rg.contains(testPsc)) {
                    System.out.println("→ belongs to region " + r.getRegionCode() +
                            " (range " + rg.getMin() + "-" + rg.getMax() + ")");
                    found = true;
                    break;
                }
            }
            if (found) break;
        }
        if (!found) {
            System.out.println("→ not found in any region");
        }

        String exportedRegion = regionParser.exportToCsv(regionTable);
        System.out.println("\nExported region table:");
        String[] regionLines = exportedRegion.split("\n");
        for (String line : regionLines) {
            System.out.println(line);
        }
        System.out.println("total " + regionLines.length + " lines");



    }
}