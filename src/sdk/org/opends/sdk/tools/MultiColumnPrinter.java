package org.opends.sdk.tools;

import org.opends.server.util.cli.ConsoleApplication;

import java.util.Enumeration;
import java.util.Vector;
import java.util.Map;
import java.util.TreeMap;
import java.util.Iterator;

/**
 * Utility class for printing aligned collumns of text.
 * <P>
 * This class allows you to specify:
 * <UL>
 * <LI>The number of collumns in the output. This will determine
 * the dimension of the string arrays passed to add(String[])
 * or addTitle(String[]).
 * <LI>spacing/gap between columns
 * <LI>character to use for title border (null means no border)
 * <LI>column alignment. Only LEFT/CENTER is supported for now.
 * </UL>
 *
 * <P>
 * Example usage:
 * <PRE>
 *  MyPrinter  mp = new MyPrinter(3, 2, "-");
 *  String    oneRow[] = new String [ 3 ];
 *  oneRow[0] = "User Name";
 *  oneRow[1] = "Email Address";
 *  oneRow[2] = "Phone Number";
 *  mp.addTitle(oneRow);
 *
 *  oneRow[0] = "Bob";
 *  oneRow[1] = "bob@foo.com";
 *  oneRow[2] = "123-4567";
 *  mp.add(oneRow);
 *
 *  oneRow[0] = "John";
 *  oneRow[1] = "john@foo.com";
 *  oneRow[2] = "456-7890";
 *  mp.add(oneRow);
 *  mp.print();
 * </PRE>
 *
 * <P>
 * The above would print:
 * <P>
 * <PRE>
 *  --------------------------------------
 *  User Name  Email Address  Phone Number
 *  --------------------------------------
 *  Bob        bob@foo.com    123-4567
 *  John       john@foo.com   456-7890
 * </PRE>
 *
 *<P>
 * This class also supports multi-row titles and having title
 * strings spanning multiple collumns. Example usage:
 * <PRE>
 *     TestPrinter  tp = new TestPrinter(4, 2, "-");
 *     String    oneRow[] = new String [ 4 ];
 *     int[]    span = new int[ 4 ];
 *
 *     span[0] = 2; // spans 2 collumns
 *     span[1] = 0; // spans 0 collumns
 *     span[2] = 2; // spans 2 collumns
 *     span[3] = 0; // spans 0 collumns
 *
 *     tp.setTitleAlign(CENTER);
 *     oneRow[0] = "Name";
 *     oneRow[1] = "";
 *     oneRow[2] = "Contact";
 *     oneRow[3] = "";
 *     tp.addTitle(oneRow, span);
 *
 *     oneRow[0] = "First";
 *     oneRow[1] = "Last";
 *     oneRow[2] = "Email";
 *     oneRow[3] = "Phone";
 *     tp.addTitle(oneRow);
 *
 *     oneRow[0] = "Bob";
 *     oneRow[1] = "Jones";
 *     oneRow[2] = "bob@foo.com";
 *     oneRow[3] = "123-4567";
 *     tp.add(oneRow);
 *
 *     oneRow[0] = "John";
 *     oneRow[1] = "Doe";
 *     oneRow[2] = "john@foo.com";
 *     oneRow[3] = "456-7890";
 *     tp.add(oneRow);
 *
 *     tp.println();
 * </PRE>
 *
 * <P>
 * The above would print:
 * <P>
 * <PRE>
 *      ------------------------------------
 *          Name             Contact
 *      First  Last      Email       Phone
 *      ------------------------------------
 *      Bob    Jones  bob@foo.com   123-4567
 *      John   Doe    john@foo.com  456-7890
 * </PRE>
 *
 */
public final class MultiColumnPrinter {

  final public static int  LEFT  = 0;
  final public static int  CENTER  = 1;

  private int numCol = 2;
  private int gap = 4;
  private int align = CENTER;
  private int titleAlign = CENTER;
  private String border = null;

  private Vector titleTable = null;
  private Vector titleSpanTable = null;
  private int curLength[];

  private final ConsoleApplication app;

  /**
   * Creates a new MultiColumnPrinter class.
   *
   * @param numCol number of columns
   * @param gap gap between each column
   * @param border character used to frame the titles
   * @param align type of alignment within columns
   */
  public MultiColumnPrinter(int numCol, int gap, String border,
                            int align, ConsoleApplication app) {

    titleTable = new Vector();
    titleSpanTable = new Vector();
    curLength = new int[numCol];

    this.numCol = numCol;
    this.gap = gap;
    this.border = border;
    this.align = align;
    this.titleAlign = LEFT;

    this.app = app;
  }

  /**
   * Creates a sorted new MultiColumnPrinter class using LEFT alignment.
   *
   * @param numCol number of columns
   * @param gap gap between each column
   * @param border character used to frame the titles
   */
  public MultiColumnPrinter(int numCol, int gap, String border,
                            ConsoleApplication app) {
    this(numCol, gap, border, LEFT, app);
  }

  /**
   * Creates a sorted new MultiColumnPrinter class using LEFT alignment
   * and with no title border.
   *
   * @param numCol number of columns
   * @param gap gap between each column
   */
  public MultiColumnPrinter(int numCol, int gap, ConsoleApplication app) {
    this(numCol, gap, null, LEFT, app);
  }

  /**
   * Adds to the row of strings to be used as the title for the table.
   *
   * @param row Array of strings to print in one row of title.
   */
  public void addTitle(String[] row) {
    if (row == null)
      return;

    int[] span = new int [ row.length ];
    for (int i = 0; i < row.length; i++) {
      span[i] = 1;
    }

    addTitle(row, span);
  }

  /**
   * Adds to the row of strings to be used as the title for the table.
   * Also allows for certain title strings to span multiple collumns
   * The span parameter is an array of integers which indicate how
   * many collumns the corresponding title string will occupy.
   * For a row that is 4 collumns wide, it is possible to have some
   * title strings in a row to 'span' multiple collumns:
   *
   * <P>
   * <PRE>
   * ------------------------------------
   *     Name             Contact
   * First  Last      Email       Phone
   * ------------------------------------
   * Bob    Jones  bob@foo.com   123-4567
   * John   Doe    john@foo.com  456-7890
   * </PRE>
   *
   * In the example above, the title row has a string 'Name' that
   * spans 2 collumns. The string 'Contact' also spans 2 collumns.
   * The above is done by passing in to addTitle() an array that
   * contains:
   *
   * <PRE>
   *    span[0] = 2; // spans 2 collumns
   *    span[1] = 0; // spans 0 collumns, ignore
   *    span[2] = 2; // spans 2 collumns
   *    span[3] = 0; // spans 0 collumns, ignore
   * </PRE>
   * <P>
   * A span value of 1 is the default.
   * The method addTitle(String[] row) basically does:
   *
   * <PRE>
   *   int[] span = new int [ row.length ];
   *   for (int i = 0; i < row.length; i++) {
   *       span[i] = 1;
   *   }
   *   addTitle(row, span);
   * </PRE>
   *
   * @param row Array of strings to print in one row of title.
   * @param span Array of integers that reflect the number of collumns
   * the corresponding title string will occupy.
   */
  public void addTitle(String[] row, int span[]) {
    // Need to create a new instance of it, otherwise the new values will
    // always overwrite the old values.

    String[] rowInstance = new String[(row.length)];
    for (int i = 0; i < row.length; i++) {
      rowInstance[i] = row[i];
    }
    titleTable.addElement(rowInstance);

    titleSpanTable.addElement(span);
  }

  /**
   * Set alignment for title strings
   *
   * @param titleAlign
   */
  public void setTitleAlign(int titleAlign)  {
    this.titleAlign = titleAlign;
  }

  /**
   * Clears title strings.
   */
  public void clearTitle()  {
    titleTable.clear();
    titleSpanTable.clear();
  }

  /**
   * Prints the table title
   */
  public void printTitle()
  {
    // Get the longest string for each column and store in curLength[]

    // Scan through title rows
    Enumeration elm = titleTable.elements();
    Enumeration spanEnum = titleSpanTable.elements();
    while (elm.hasMoreElements()) {
      String[] row = (String[])elm.nextElement();
      int[] curSpan = (int[])spanEnum.nextElement();

      for (int i = 0; i < numCol; i++) {
        // None of the fields should be null, but if it
        // happens to be so, replace it with "-".
        if (row[i] == null)
          row[i] = "-";

        int len = row[i].length();

        /*
        * If a title string spans multiple collumns, then
        * the space it occupies in each collumn is at most
        * len/span (since we have gap to take into account
        * as well).
        */
        int span = curSpan[i], rem = 0;
        if (span > 1)  {
          rem = len % span;
          len = len/span;
        }

        if (curLength[i] < len)  {
          curLength[i] = len;

          if ((span > 1) && ((i+span) <= numCol))  {
            for (int j=i+1; j<(i+span); ++j)  {
              curLength[j] = len;
            }

            /*
            * Add remainder to last collumn in span
            * to avoid round-off errors.
            */
            curLength[(i+span)-1] += rem;
          }
        }
      }
    }

    printBorder();
      elm = titleTable.elements();
      spanEnum = titleSpanTable.elements();

      while (elm.hasMoreElements()) {
        String[] row = (String[])elm.nextElement();
        int[] curSpan = (int[])spanEnum.nextElement();

        for (int i = 0; i < numCol; i++) {
          int availableSpace = 0, span = curSpan[i];

          if (span == 0)
            continue;

          availableSpace = curLength[i];

          if ((span > 1) && ((i+span) <= numCol))  {
            for (int j=i+1; j<(i+span); ++j)  {
              availableSpace += gap;
              availableSpace += curLength[j];
            }
          }

          if (titleAlign == CENTER)  {
            int space_before, space_after;
            space_before = (availableSpace-row[i].length())/2;
            space_after = availableSpace-row[i].length() - space_before;

            printSpaces(space_before);
            app.getOutputStream().print(row[i]);
            printSpaces(space_after);
            if (i < numCol-1) printSpaces(gap);
          } else  {
            app.getOutputStream().print(row[i]);
            if (i < numCol-1) printSpaces(availableSpace-row[i].length()+gap);
          }

        }
        app.getOutputStream().println("");
      }
    printBorder();
  }

  /**
   * Adds one row of text to output.
   *
   * @param row Array of strings to print in one row.
   */
  public void printRow(String... row) {
    for (int i = 0; i < numCol; i++) {
      if (align == CENTER)  {
        int space1, space2;
        space1 = (curLength[i]-row[i].length())/2;
        space2 = curLength[i]-row[i].length() - space1;

        printSpaces(space1);
        app.getOutputStream().print(row[i]);
        printSpaces(space2);
        if (i < numCol-1) printSpaces(gap);
      } else  {
        app.getOutputStream().print(row[i]);
        if (i < numCol-1) printSpaces(curLength[i]-row[i].length()+gap);
      }
    }
    app.getOutputStream().println("");
  }

  private void printSpaces(int count)  {
    for (int i = 0; i < count; ++i)  {
      app.getOutputStream().print(" ");
    }
  }

  private void printBorder() {

    int colNum = 1;
    if (border == null) return;

    // For the value in each column
    for (int i = 0; i < numCol; i++) {
      for (int j = 0; j < curLength[i]; j++) {
        app.getOutputStream().print(border);
      }
    }

    // For the gap between each column
    for (int i = 0; i < numCol-1; i++) {
      for (int j = 0; j < gap; j++) {
        app.getOutputStream().print(border);
      }
    }
    app.getOutputStream().println("");
  }
}