package edu.ualberta.med.biobank.widgets.grids;

import java.util.ArrayList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.model.Cell;
import edu.ualberta.med.biobank.model.ContainerCell;
import edu.ualberta.med.biobank.model.ContainerStatus;

/**
 * Drawer 36 display.
 */
public class Drawer36Display extends AbstractContainerDisplay {

    public static final int SQUARE_CELL_WIDTH = 70;

    public static final int RECTANGLE_CELL_WIDTH = SQUARE_CELL_WIDTH * 2;

    public static final int RECTANGLE_CELL_HEIGHT = 45;

    private static final int GRID_WIDTH = 8 * SQUARE_CELL_WIDTH;

    private static final int GRID_HEIGHT = 3 * SQUARE_CELL_WIDTH + 3
        * RECTANGLE_CELL_HEIGHT;

    public static final int WIDTH = GRID_WIDTH + 10;

    public static final int HEIGHT = GRID_HEIGHT + 10;

    private Boolean hasLegend = false;

    private ArrayList<ContainerStatus> legendStatus;

    private static final int DRAWER_SIZE = 36;

    public static int LEGEND_WIDTH = 70;

    public static int LEGEND_HEIGHT = 20;

    @Override
    public void initLegend() {
        hasLegend = true;
        legendStatus = new ArrayList<ContainerStatus>();
        legendStatus.add(ContainerStatus.NOT_INITIALIZED);
        legendStatus.add(ContainerStatus.INITIALIZED);
    }

    @Override
    public Point getSizeToApply() {
        int fullHeight = HEIGHT;
        if (hasLegend) {
            fullHeight += LEGEND_HEIGHT;
        }
        return new Point(WIDTH, fullHeight);
    }

    @Override
    protected void paintGrid(PaintEvent e, ContainerDisplayWidget displayWidget) {
        if (legendStatus != null) {
            LEGEND_WIDTH = WIDTH / legendStatus.size();
        }
        GC gc = e.gc;
        int currentX = 0;
        int rectYTotal = 0;
        int squareYTotal = 0;
        int squareXTotal = 0;
        for (int boxIndex = 1; boxIndex <= DRAWER_SIZE; boxIndex++) {
            int width = SQUARE_CELL_WIDTH;
            int height = SQUARE_CELL_WIDTH;
            int rectXPosition = squareXTotal * SQUARE_CELL_WIDTH;
            if (boxIndex % 3 == 0) {
                // rectangle (ex: 03, 06, 09, 12, 15...)
                rectYTotal++;
                width = RECTANGLE_CELL_WIDTH;
                height = RECTANGLE_CELL_HEIGHT;
                currentX = 0;
            } else {
                if (currentX == 1) {
                    // second square (ex: 02, 05, 08, 11, 14...)
                    rectXPosition += SQUARE_CELL_WIDTH;
                } else {
                    // first square (ex: 01, 04, 07, 10, 13...)
                    squareYTotal++;
                }
                currentX++;
            }
            int rectYPosition = GRID_HEIGHT
                - (squareYTotal * SQUARE_CELL_WIDTH + rectYTotal
                    * RECTANGLE_CELL_HEIGHT);
            Rectangle rectangle = new Rectangle(rectXPosition, rectYPosition,
                width, height);

            gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
            gc.setBackground(getStatus(displayWidget.getCells(), boxIndex)
                .getColor());
            gc.fillRectangle(rectangle);
            if (displayWidget.getSelection() != null
                && (displayWidget.getSelection().row + 1) == boxIndex) {
                gc.setBackground(e.display.getSystemColor(SWT.COLOR_YELLOW));
                gc.fillRectangle(rectangle);
            }
            gc.drawRectangle(rectangle);

            String text = getDefaultTextForBox(displayWidget.getCells(),
                boxIndex - 1, 0);
            if (text != null) {
                drawTextOnCenter(gc, text, rectangle);
            }
            if (boxIndex % 9 == 0) {
                // one column of 9 boxes done
                squareXTotal += 2;
                squareYTotal = 0;
                rectYTotal = 0;
            }

            if (displayWidget.getCells() != null) {
                if (displayWidget.getMultiSelectionManager().isEnabled()) {
                    Cell cell = displayWidget.getCells().get(
                        new RowColPos(boxIndex - 1, 0));
                    if (cell != null && cell.isSelected()) {
                        Rectangle rect = new Rectangle(rectangle.x + 5,
                            rectangle.y + 5, rectangle.width - 10,
                            rectangle.height - 10);
                        Color color = e.display.getSystemColor(SWT.COLOR_BLUE);
                        e.gc.setForeground(color);
                        e.gc.drawRectangle(rect);
                    }
                }
            }
        }
        if (hasLegend) {
            for (int i = 0; i < legendStatus.size(); i++) {
                ContainerStatus status = legendStatus.get(i);
                drawLegend(e, status.getColor(), i, status.getLegend());
            }
        }
    }

    private ContainerStatus getStatus(Map<RowColPos, ? extends Cell> cells,
        int boxIndex) {
        ContainerStatus status = null;
        if (cells != null) {
            status = ((ContainerCell) cells.get(new RowColPos(boxIndex - 1, 0)))
                .getStatus();
        }
        if (status == null)
            status = ContainerStatus.NOT_INITIALIZED;
        return status;
    }

    /**
     * Draw the text on the middle of the rectangle
     */
    public void drawTextOnCenter(GC gc, String text, Rectangle rectangle) {
        Point textSize = gc.textExtent(text);
        int xTextPosition = (rectangle.width - textSize.x) / 2 + rectangle.x;
        int yTextPosition = (rectangle.height - textSize.y) / 2 + rectangle.y;
        gc.drawText(text, xTextPosition, yTextPosition, true);
    }

    @Override
    public Cell getObjectAtCoordinates(ContainerDisplayWidget displayWidget,
        int x, int y) {
        if (displayWidget.getCells() == null) {
            return null;
        }
        int gridCellWidth = RECTANGLE_CELL_WIDTH;
        int gridCellHeight = SQUARE_CELL_WIDTH + RECTANGLE_CELL_HEIGHT;
        // get high level position
        int xGrid = x / (gridCellWidth);
        int yGrid = y / (gridCellHeight);
        int cellNum;
        // get subcell position
        if (y % gridCellHeight < RECTANGLE_CELL_HEIGHT)
            cellNum = 9;
        else if (x % gridCellWidth > SQUARE_CELL_WIDTH)
            cellNum = 8;
        else
            cellNum = 7;
        // convert subcell to real cell
        int xGridCellNumOffset = 9;
        int yGridCellNumOffset = 3;
        return displayWidget.getCells().get(
            new RowColPos(cellNum + xGrid * xGridCellNumOffset - yGrid
                * yGridCellNumOffset - 1, 0));
    }

    protected void drawLegend(PaintEvent e, Color color, int index, String text) {
        e.gc.setBackground(color);
        int width = LEGEND_WIDTH;
        int startx = LEGEND_WIDTH * index;
        int starty = GRID_HEIGHT + 4;

        Rectangle rectangle = new Rectangle(startx, starty, width,
            LEGEND_HEIGHT);
        e.gc.fillRectangle(rectangle);
        e.gc.drawRectangle(rectangle);
        drawTextOnCenter(e.gc, text, rectangle);
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        int fullHeight = HEIGHT;
        if (hasLegend) {
            fullHeight += LEGEND_HEIGHT;
        }
        return new Point(WIDTH, fullHeight);
    }

}
