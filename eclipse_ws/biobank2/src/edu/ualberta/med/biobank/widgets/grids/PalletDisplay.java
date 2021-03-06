package edu.ualberta.med.biobank.widgets.grids;

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;

import edu.ualberta.med.biobank.common.util.StringUtil;
import edu.ualberta.med.biobank.common.wrappers.ContainerLabelingSchemeWrapper;
import edu.ualberta.med.biobank.model.type.LabelingLayout;
import edu.ualberta.med.biobank.model.util.RowColPos;
import edu.ualberta.med.biobank.util.SbsLabeling;
import edu.ualberta.med.biobank.widgets.grids.well.AbstractUIWell;
import edu.ualberta.med.biobank.widgets.grids.well.SpecimenCell;

/**
 * Specific widget to draw a pallet for scan features
 */
public class PalletDisplay extends AbstractGridDisplay {

    public static final int SAMPLE_WIDTH = 50;

    public static final int PALLET_WIDTH = SAMPLE_WIDTH * SbsLabeling.COL_DEFAULT;

    public static final int PALLET_HEIGHT = SAMPLE_WIDTH * SbsLabeling.ROW_DEFAULT;

    public static final int PALLET_HEIGHT_AND_LEGEND = PALLET_HEIGHT + LEGEND_HEIGHT + 4;

    public PalletDisplay(int rows, int cols,
        ContainerLabelingSchemeWrapper containerLabelingScheme, LabelingLayout labelingLayout) {
        super(PalletDisplay.class.getSimpleName());
        setCellWidth(SAMPLE_WIDTH);
        setCellHeight(SAMPLE_WIDTH);
        setDefaultStorageSize(rows, cols);
        setLabelingScheme(containerLabelingScheme);
        setLabelingLayout(labelingLayout);
    }

    public void setDefaultStorageSize() {
        setStorageSize(SbsLabeling.ROW_DEFAULT, SbsLabeling.COL_DEFAULT);
    }

    public void setDefaultStorageSize(int rows, int cols) {
        setStorageSize(rows, cols);
    }

    @Override
    protected String getMiddleTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow,
        int indexCol) {
        if (cells != null) {
            SpecimenCell cell = (SpecimenCell) cells.get(new RowColPos(indexRow,
                indexCol));
            if (cell != null)
                return cell.getTitle();
        }
        return StringUtil.EMPTY_STRING;
    }

    @Override
    protected String getTopTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow, int indexCol) {
        return getDefaultTextForBox(cells, indexRow, indexCol);
    }

    @Override
    protected String getBottomTextForBox(
        Map<RowColPos, ? extends AbstractUIWell> cells, int indexRow,
        int indexCol) {
        if (cells != null) {
            SpecimenCell cell = (SpecimenCell) cells.get(new RowColPos(indexRow,
                indexCol));
            if (cell != null)
                return cell.getTypeString();
        }
        return StringUtil.EMPTY_STRING;
    }

    @SuppressWarnings("nls")
    @Override
    protected Color getDefaultBackgroundColor(
        Display display,
        Map<RowColPos, ? extends AbstractUIWell> cells,
        Rectangle rectangle,
        int indexRow,
        int indexCol) {
        if (cells == null) {
            throw new IllegalArgumentException("cells is null");
        }

        if (!cells.isEmpty()) {
            SpecimenCell cell = (SpecimenCell) cells.get(new RowColPos(indexRow, indexCol));
            if ((cell != null) && (cell.getStatus() != null)) {
                return cell.getStatus().getColor();
            }
        }
        return super.getDefaultBackgroundColor(display, cells, rectangle, indexRow, indexCol);
    }

    @Override
    protected void drawRectangle(
        Display display,
        GC gc,
        Rectangle rectangle,
        int indexRow,
        int indexCol,
        Color defaultBackgroundColor,
        RowColPos selection) {

        gc.setBackground(defaultBackgroundColor);
        gc.fillRectangle(rectangle);
        gc.setForeground(display.getSystemColor(SWT.COLOR_BLACK));
        gc.drawRectangle(rectangle);
    }

}
