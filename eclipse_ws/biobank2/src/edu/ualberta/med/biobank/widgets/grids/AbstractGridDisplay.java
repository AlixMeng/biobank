package edu.ualberta.med.biobank.widgets.grids;

import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;

import edu.ualberta.med.biobank.common.util.RowColPos;
import edu.ualberta.med.biobank.common.wrappers.ContainerTypeWrapper;
import edu.ualberta.med.biobank.model.Cell;

/**
 * Draw a grid according to specific parameters : total number of rows, total
 * number of columns, width and height of the cell s
 */
public abstract class AbstractGridDisplay extends AbstractContainerDisplay {

    private int cellWidth = 60;

    private int cellHeight = 60;

    protected int gridWidth;

    protected int gridHeight;

    private int rows;

    private int columns;

    /**
     * Height used when legend in under the grid
     */
    public static final int LEGEND_HEIGHT = 20;

    /**
     * width calculated when legend in under the grid
     */
    protected int legendWidth;

    /**
     * Width used when legend is on the side of the grid
     */
    public static final int LEGEND_WIDTH = 70;

    protected boolean hasLegend = false;

    public boolean legendOnSide = false;

    @Override
    protected void paintGrid(PaintEvent e, ContainerDisplayWidget displayWidget) {
        for (int indexRow = 0; indexRow < rows; indexRow++) {
            for (int indexCol = 0; indexCol < columns; indexCol++) {
                int xPosition = cellWidth * indexCol;
                int yPosition = cellHeight * indexRow;
                Rectangle rectangle = new Rectangle(xPosition, yPosition,
                    cellWidth, cellHeight);

                customDraw(e, displayWidget, rectangle, indexRow, indexCol);
                drawRectangle(e, displayWidget, rectangle, indexRow, indexCol);
                String topText = getTopTextForBox(displayWidget.getCells(),
                    indexRow, indexCol);
                if (topText != null) {
                    drawText(e, topText, rectangle, SWT.TOP);
                }
                String middleText = getMiddleTextForBox(
                    displayWidget.getCells(), indexRow, indexCol);
                if (middleText != null) {
                    drawText(e, middleText, rectangle, SWT.CENTER);
                }
                String bottomText = getBottomTextForBox(
                    displayWidget.getCells(), indexRow, indexCol);
                if (bottomText != null) {
                    drawText(e, bottomText, rectangle, SWT.BOTTOM);
                }

            }
        }
    }

    @Override
    public Point computeSize(int wHint, int hHint, boolean changed) {
        if (maxWidth != -1 && maxHeight != -1) {
            cellWidth = maxWidth / columns;
            cellHeight = maxHeight / rows;
            gridWidth = maxWidth;
            gridHeight = maxHeight;
        } else {
            gridWidth = cellWidth * columns;
            gridHeight = cellHeight * rows;
        }
        int width = gridWidth + 10;
        int height = gridHeight + 10;
        if (hasLegend) {
            if (legendOnSide) {
                width = width + LEGEND_WIDTH + 4;
            } else {
                height = height + LEGEND_HEIGHT + 4;
            }
        }
        return new Point(width, height);
    }

    protected void drawRectangle(PaintEvent e,
        ContainerDisplayWidget displayWidget, Rectangle rectangle,
        int indexRow, int indexCol) {
        if (displayWidget.getSelection() != null
            && displayWidget.getSelection().row == indexRow
            && displayWidget.getSelection().col == indexCol) {
            Color color = e.display.getSystemColor(SWT.COLOR_YELLOW);
            e.gc.setBackground(color);
            e.gc.fillRectangle(rectangle);
        }
        e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));
        e.gc.drawRectangle(rectangle);
        if (displayWidget.getCells() != null) {
            if (displayWidget.getMultiSelectionManager().isEnabled()) {
                Cell cell = displayWidget.getCells().get(
                    new RowColPos(indexRow, indexCol));
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
        e.gc.setForeground(e.display.getSystemColor(SWT.COLOR_BLACK));

    }

    @SuppressWarnings("unused")
    protected void customDraw(PaintEvent e,
        ContainerDisplayWidget displayWidget, Rectangle rectangle,
        int indexRow, int indexCol) {
    }

    @SuppressWarnings("unused")
    protected String getTopTextForBox(Map<RowColPos, ? extends Cell> cells,
        int indexRow, int indexCol) {
        return null;
    }

    protected String getMiddleTextForBox(Map<RowColPos, ? extends Cell> cells,
        int indexRow, int indexCol) {
        return getDefaultTextForBox(cells, indexRow, indexCol);
    }

    @SuppressWarnings("unused")
    protected String getBottomTextForBox(Map<RowColPos, ? extends Cell> cells,
        int indexRow, int indexCol) {
        return null;
    }

    @Override
    public void setContainerType(ContainerTypeWrapper type) {
        super.setContainerType(type);
        Integer rowCap = containerType.getRowCapacity();
        Integer colCap = containerType.getColCapacity();
        Assert.isNotNull(rowCap, "row capacity is null");
        Assert.isNotNull(colCap, "column capacity is null");
        setStorageSize(rowCap, colCap);
        if (colCap <= 1) {
            // single dimension size
            setCellWidth(120);
            setCellHeight(20);
            setLegendOnSide(true);
        }
    }

    /**
     * Draw the text on the horizontal middle of the rectangle. Vertical
     * alignment depend on the verticalPosition parameter.
     */
    private void drawText(PaintEvent e, String text, Rectangle rectangle,
        int verticalPosition) {
        Font oldFont = e.gc.getFont();
        Font tmpFont = null;
        Point textSize = e.gc.textExtent(text);
        if (textSize.x > rectangle.width) {
            // Try to find a smallest font to see the whole text
            FontData fd = oldFont.getFontData()[0];
            int height = fd.getHeight();
            Point currentTextSize = textSize;
            while (currentTextSize.x > rectangle.width && height > 3) {
                if (tmpFont != null) {
                    tmpFont.dispose();
                }
                height--;
                FontData fd2 = new FontData(fd.getName(), height, fd.getStyle());
                tmpFont = new Font(e.display, fd2);
                e.gc.setFont(tmpFont);
                currentTextSize = e.gc.textExtent(text);
            }
            if (height > 3) {
                textSize = currentTextSize;
            } else {
                e.gc.setFont(oldFont);
            }
        }
        int xTextPosition = (rectangle.width - textSize.x) / 2 + rectangle.x;
        int yTextPosition = 0;
        switch (verticalPosition) {
        case SWT.CENTER:
            yTextPosition = (rectangle.height - textSize.y) / 2 + rectangle.y;
            break;
        case SWT.TOP:
            yTextPosition = rectangle.y + 3;
            break;
        case SWT.BOTTOM:
            yTextPosition = rectangle.y + rectangle.height - textSize.y - 3;
        }
        e.gc.drawText(text, xTextPosition, yTextPosition, true);
        e.gc.setFont(oldFont);
        if (tmpFont != null) {
            tmpFont.dispose();
        }
    }

    protected void drawLegend(PaintEvent e, Color color, int index, String text) {
        e.gc.setBackground(color);
        int width = legendWidth;
        int startx = legendWidth * index;
        int starty = gridHeight + 4;
        if (legendOnSide) {
            width = LEGEND_WIDTH;
            startx = gridWidth + 4;
            starty = LEGEND_HEIGHT * index;
        }
        Rectangle rectangle = new Rectangle(startx, starty, width,
            LEGEND_HEIGHT);
        e.gc.fillRectangle(rectangle);
        e.gc.drawRectangle(rectangle);
        drawText(e, text, rectangle, SWT.CENTER);
    }

    /**
     * Modify only the number of rows and columns of the grid. If no max width
     * and max height has been given to the grid, the default cell width and
     * cell height will be used
     */
    @Override
    public void setStorageSize(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
    }

    public int getCellWidth() {
        return cellWidth;
    }

    public void setCellWidth(int cellWidth) {
        this.cellWidth = cellWidth;
    }

    public int getCellHeight() {
        return cellHeight;
    }

    public void setCellHeight(int cellHeight) {
        this.cellHeight = cellHeight;
    }

    public void setLegendOnSide(boolean onSide) {
        this.legendOnSide = onSide;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return columns;
    }

}
