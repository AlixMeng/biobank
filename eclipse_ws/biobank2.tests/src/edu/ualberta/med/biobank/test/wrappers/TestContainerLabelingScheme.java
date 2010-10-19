package edu.ualberta.med.biobank.test.wrappers;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import edu.ualberta.med.biobank.common.exception.BiobankCheckException;
import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ContainerLabelingSchemeWrapper;
import edu.ualberta.med.biobank.test.TestDatabase;

public class TestContainerLabelingScheme extends TestDatabase {

    private static final Map<Integer, String> CBSR_ALPHA;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(0, "A");
        aMap.put(1, "B");
        aMap.put(2, "C");
        aMap.put(3, "D");
        aMap.put(4, "E");
        aMap.put(5, "F");
        aMap.put(6, "G");
        aMap.put(7, "H");
        aMap.put(8, "J");
        aMap.put(9, "K");
        aMap.put(10, "L");
        aMap.put(11, "M");
        aMap.put(12, "N");
        aMap.put(13, "P");
        aMap.put(14, "Q");
        aMap.put(15, "R");
        aMap.put(16, "S");
        aMap.put(17, "T");
        aMap.put(18, "U");
        aMap.put(19, "V");
        aMap.put(20, "W");
        aMap.put(21, "X");
        aMap.put(22, "Y");
        aMap.put(23, "Z");
        CBSR_ALPHA = Collections.unmodifiableMap(aMap);
    };

    private static final Map<Integer, String> SBS_ALPHA;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(0, "A");
        aMap.put(1, "B");
        aMap.put(2, "C");
        aMap.put(3, "D");
        aMap.put(4, "E");
        aMap.put(5, "F");
        aMap.put(6, "G");
        aMap.put(7, "H");
        aMap.put(8, "I");
        aMap.put(9, "J");
        aMap.put(10, "K");
        aMap.put(11, "L");
        aMap.put(12, "M");
        aMap.put(13, "N");
        aMap.put(14, "O");
        aMap.put(15, "P");
        SBS_ALPHA = Collections.unmodifiableMap(aMap);
    };

    private static final Map<Integer, String> DEWAR_ALPHA;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(0, "A");
        aMap.put(1, "B");
        aMap.put(2, "C");
        aMap.put(3, "D");
        DEWAR_ALPHA = Collections.unmodifiableMap(aMap);
    };

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void TestGetAllLabelingSchemes() throws BiobankCheckException,
        Exception {
        ContainerLabelingSchemeWrapper.getAllLabelingSchemesMap(appService)
            .values();
    }

    @Test
    public void testTwoCharNumeric() throws Exception {
        int totalRows = 5 + r.nextInt(5);
        RowColPos pos = new RowColPos();

        for (int i = 0; i < totalRows; ++i) {
            pos.row = i % totalRows;
            pos.col = i / totalRows;
            String result =
                ContainerLabelingSchemeWrapper.rowColToTwoCharNumeric(pos,
                    totalRows);
            Assert.assertEquals(result.length(), 2);
            Assert.assertEquals(new Integer(pos.row + 1).toString(),
                result.substring(1, 2));
            Assert.assertEquals(pos.col.toString(), result.substring(0, 1));
        }

        for (int i = 0; i < totalRows; ++i) {
            Integer row = i % totalRows + 1;
            Integer col = i / totalRows;
            String label = col.toString() + row.toString();
            pos =
                ContainerLabelingSchemeWrapper.twoCharNumericToRowCol(
                    appService, label, totalRows);
            Assert.assertEquals(pos.row, new Integer(row - 1));
            Assert.assertEquals(pos.col, col);
        }
    }

    @Test
    public void testCbsr() throws Exception {
        int totalRows = 3 + r.nextInt(3);
        int totalCols = 5 + r.nextInt(5);

        String cbsrString;
        RowColPos pos = new RowColPos();

        for (int col = 0; col < totalCols; ++col) {
            for (int row = 0; row < totalRows; ++row) {
                pos.row = row;
                pos.col = col;
                cbsrString =
                    ContainerLabelingSchemeWrapper.rowColToCbsrTwoChar(pos,
                        totalRows, totalCols);
                Assert.assertTrue((cbsrString.length() == 2)
                    || (cbsrString.length() == 3));
                Assert
                    .assertEquals(
                        CBSR_ALPHA.get((row + col * totalRows)
                            % CBSR_ALPHA.size()), cbsrString.substring(1));
                Assert
                    .assertEquals(
                        CBSR_ALPHA.get((row + col * totalRows)
                            / CBSR_ALPHA.size()), cbsrString.substring(0, 1));
            }
        }

        for (int col = 0; col < totalCols; ++col) {
            for (int row = 0; row < totalRows; ++row) {
                cbsrString =
                    CBSR_ALPHA.get((row + col * totalRows) / CBSR_ALPHA.size())
                        + CBSR_ALPHA.get((row + col * totalRows)
                            % CBSR_ALPHA.size());
                pos =
                    ContainerLabelingSchemeWrapper.cbsrTwoCharToRowCol(
                        appService, cbsrString, totalRows, totalCols, "test");
                Assert.assertEquals(new Integer(row), pos.row);
                Assert.assertEquals(new Integer(col), pos.col);
            }
        }

        try {
            pos =
                ContainerLabelingSchemeWrapper.cbsrTwoCharToRowCol(appService,
                    "aa", totalRows, totalCols, "test");
            Assert.fail("should not be allowed to use lower case characters");
        } catch (Exception e) {
            Assert.assertTrue(true);
        }
    }

    @Test
    public void testSbs() throws Exception {
        int totalRows;
        int totalCols;
        String posString;
        RowColPos pos = new RowColPos();

        for (int i = 1; i <= 2; ++i) {
            switch (i) {
            case 1:
                totalRows = 9;
                totalCols = 9;
                break;

            case 2:
            default:
                totalRows = 8;
                totalCols = 12;
                break;
            }

            for (int col = 0; col < totalCols; ++col) {
                for (int row = 0; row < totalRows; ++row) {
                    pos.row = row;
                    pos.col = col;
                    posString = ContainerLabelingSchemeWrapper.rowColToSbs(pos);
                    if (col >= 9) {
                        Assert.assertTrue(posString.length() == 3);
                    } else {
                        Assert.assertTrue(posString.length() == 2);
                    }
                    Assert.assertEquals(SBS_ALPHA.get(row).charAt(0),
                        posString.charAt(0));
                    Assert.assertEquals(col + 1,
                        Integer.valueOf(posString.substring(1)).intValue());
                }
            }

            for (int col = 0; col < totalCols; ++col) {
                for (int row = 0; row < totalRows; ++row) {
                    pos =
                        ContainerLabelingSchemeWrapper
                            .sbsToRowCol(appService, String.format("%s%02d",
                                SBS_ALPHA.get(row), col + 1));
                    Assert.assertEquals(row, pos.row.intValue());
                    Assert.assertEquals(col, pos.col.intValue());
                }
            }
        }
    }

    private static final int DEWAR_MAX_ROWS = 2;

    private static final int DEWAR_MAX_COLS = 2;

    @Test
    public void testDewar() throws Exception {
        String posString;
        RowColPos pos = new RowColPos();

        for (int row = 0; row < DEWAR_MAX_ROWS; ++row) {
            for (int col = 0; col < DEWAR_MAX_COLS; ++col) {
                pos.row = row;
                pos.col = col;
                posString =
                    ContainerLabelingSchemeWrapper.rowColToDewar(pos,
                        DEWAR_MAX_COLS);
                Assert.assertEquals(DEWAR_ALPHA.get(row * DEWAR_MAX_COLS + col)
                    .charAt(0), posString.charAt(0));
                Assert.assertEquals(DEWAR_ALPHA.get(row * DEWAR_MAX_COLS + col)
                    .charAt(0), posString.charAt(1));
            }
        }

        for (int row = 0; row < DEWAR_MAX_ROWS; ++row) {
            for (int col = 0; col < DEWAR_MAX_COLS; ++col) {
                String label = DEWAR_ALPHA.get(row * DEWAR_MAX_COLS + col);
                label += label;
                pos =
                    ContainerLabelingSchemeWrapper.dewarToRowCol(appService,
                        label, DEWAR_MAX_COLS);
                Assert.assertEquals(row, pos.row.intValue());
                Assert.assertEquals(col, pos.col.intValue());
            }
        }

    }

}
