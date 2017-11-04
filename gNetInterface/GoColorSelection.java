package gNetInterface;

import gNetUtil.*;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.*;

public class GoColorSelection extends JDialog implements ActionListener {
    public boolean isOKSelected = false;
    public Color[] newColors = new Color[4];

    private Color[] oldColors;
    private String  okStr = "OK";
    private String  cancelStr = "Cancel";
    private JTable  colorTable = null;
    
    public GoColorSelection(Frame fOwner, String str, boolean modal, Color[] oldColors) {
        super(fOwner, str, modal);
        this.oldColors = oldColors;

        GoColorTableModel goModel = new GoColorTableModel();
        colorTable = new JTable(goModel);
        colorTable.setPreferredScrollableViewportSize(new Dimension(300, 88));
        colorTable.setRowHeight(22);

        JScrollPane scrollPane = new JScrollPane(colorTable);

        setUpColorRenderer();
        setUpColorEditor();

        JButton okButton = new JButton(okStr);
        okButton.setFont(GoFont.buttonFont);
        okButton.addActionListener(this);
        
        JButton cancelButton = new JButton(cancelStr);
        cancelButton.setFont(GoFont.buttonFont);
        cancelButton.addActionListener(this);
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(2, 0, 2, 0));
        buttonPanel.add(okButton);
        buttonPanel.add(new JPanel());
        buttonPanel.add(cancelButton);
                
        getContentPane().add(scrollPane, BorderLayout.CENTER);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private void setUpColorRenderer() {
        colorTable.setDefaultRenderer(Color.class, new GoColorRenderer());
    }

    private void setUpColorEditor() {
        final JButton button = new JButton("");
        button.setBackground(Color.white);
        button.setBorderPainted(false);
        button.setMargin(new Insets(0, 0, 0, 0));

        final GoColorEditor goColorEditor = new GoColorEditor(button);
        colorTable.setDefaultEditor(Color.class, goColorEditor);

        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                button.setBackground(goColorEditor.currentColor);
                Color newColor = JColorChooser.showDialog(button, "Pick a Color", goColorEditor.currentColor);
                if (newColor != null) {
                    goColorEditor.currentColor = newColor;
                }
            }
        });
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals(okStr)) {
            isOKSelected = true;
            TableModel model = colorTable.getModel();
            for (int i = 0; i < 4; i++) newColors[i] = (Color)model.getValueAt(i, 1);
            setVisible(false);
        }
        else if (e.getActionCommand().equals(cancelStr)) {
            isOKSelected = false;
            setVisible(false);
        }
    }
    
    class GoColorRenderer extends JLabel implements TableCellRenderer {
        Border unselectedBorder = null;
        Border selectedBorder = null;

        public GoColorRenderer() {
            super();
            setOpaque(true); //MUST do this for background to show up.
        }

        public Component getTableCellRendererComponent(JTable table, Object color, 
                                boolean isSelected, boolean hasFocus, int row, int column) {
            setBackground((Color)color);
            if (isSelected) {
                if (selectedBorder == null) {
                    selectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getSelectionBackground());
                }
                setBorder(selectedBorder);
            } else {
                if (unselectedBorder == null) {
                    unselectedBorder = BorderFactory.createMatteBorder(2, 5, 2, 5, table.getBackground());
                }
                setBorder(unselectedBorder);
            }
            return this;
        }
    }
    
    class GoColorEditor extends DefaultCellEditor {
        Color currentColor = null;
        //JComponent editorComponent; which inherited from the superclass DefaultCellEditor

        public GoColorEditor(JButton b) {
            super(new JCheckBox());  //No JButton Constructors.
            editorComponent = b;
            setClickCountToStart(1);

            //Must do this so that editing stops when appropriate.
            b.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                }
            });
        }

        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }

        public Object getCellEditorValue() {
            return currentColor;
        }

        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            currentColor = (Color)value;
            return editorComponent;
        }
    }

    class GoColorTableModel extends AbstractTableModel {
        final String[] columnNames = {"Text Type", "Favorite Color"};
        final Object[][] data = {
            {"Texts Written", oldColors[0]},
            {"Texts Sent", oldColors[1]},
            {"Texts Received", oldColors[2]},
            {"Selection Color", oldColors[3]}
        };

        public int getColumnCount() {
            return columnNames.length;
        }
        
        public int getRowCount() {
            return data.length;
        }

        public String getColumnName(int col) {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col) {
            return data[row][col];
        }
        
        public Class getColumnClass(int c) {
            return getValueAt(0, c).getClass();
        }
        
        public boolean isCellEditable(int row, int col) {
            if (col < 1) return false;
            else return true;
        }

        public void setValueAt(Object value, int row, int col) {
            data[row][col] = value;
            fireTableCellUpdated(row, col);
        }
    }
}