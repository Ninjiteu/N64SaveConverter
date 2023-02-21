import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.awt.*;
import java.awt.event.*;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter.HighlightPainter;

public class N64SaveConverter {

    private static final int SIZE_EEP = 2048;
    private static final int SIZE_SRA = 32768;
    private static final int SIZE_FLA = 131072;
    private static final int SIZE_MPK = 131072;
    private static final int SIZE_SRM = 296960;
    private static final int SIZE_SRA_SRM_OFFSET = 133120;
    private static final int SIZE_FLA_SRM_OFFSET = SIZE_SRM - SIZE_FLA;
    private static final int SIZE_MPK_SRM_OFFSET = 2048;
    private static final String EEP_EXT = ".eep";
    private static final String SRA_EXT = ".sra";
    private static final String FLA_EXT = ".fla";
    private static final String MPK_EXT = ".mpk";
    private static final String SRM_EXT = ".srm";
    private static final String EEP_LABEL = "EEPROM (.eep)";
    private static final String SRA_LABEL = "SRAM (.sra)";
    private static final String FLA_LABEL = "FlashRAM (.fla)";
    private static final String MPK_LABEL = "Controller Pak (.mpk)";
    private static final String SRM_LABEL = "Retroarch Save (.srm)";
    private static final String WII_LABEL = "Wii/WiiU/Everdrive64";
    private static final String PJ64_LABEL = "Project64/Mupen64";
    private static final String RA_LABEL = "Retroarch";
    private static final String[] inputTypeList = {"", EEP_LABEL, SRA_LABEL, FLA_LABEL, MPK_LABEL, SRM_LABEL};
    private static final String[] outputTypeList = {"", EEP_LABEL, SRA_LABEL, FLA_LABEL, MPK_LABEL, SRM_LABEL};
    private static final String[] inputSourceList = {"", WII_LABEL, PJ64_LABEL, RA_LABEL};
    private static final String[] outputTargetList = {"", WII_LABEL, PJ64_LABEL, RA_LABEL};
    private static final Map<String, StandardSizeTableEntry> standardSizeTable;
    private static final Map<String, ConversionTableEntry> conversionTable;

    static {
        standardSizeTable = new HashMap<>();
        standardSizeTable.put(EEP_LABEL, new StandardSizeTableEntry(SIZE_EEP, EEP_EXT));
        standardSizeTable.put(SRA_LABEL, new StandardSizeTableEntry(SIZE_SRA, SRA_EXT));
        standardSizeTable.put(FLA_LABEL, new StandardSizeTableEntry(SIZE_FLA, FLA_EXT));
        standardSizeTable.put(MPK_LABEL, new StandardSizeTableEntry(SIZE_MPK, MPK_EXT));
        standardSizeTable.put(SRM_LABEL, new StandardSizeTableEntry(SIZE_SRM, SRM_EXT));

        conversionTable = new HashMap<>();
        conversionTable.put(WII_LABEL+"-"+EEP_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_EEP, SIZE_SRM, 0, false, SRM_EXT));
        conversionTable.put(WII_LABEL+"-"+SRA_LABEL+"-"+PJ64_LABEL+"-"+SRA_LABEL, new ConversionTableEntry(SIZE_SRA, SIZE_SRA, 0, true, SRA_EXT));
        conversionTable.put(WII_LABEL+"-"+SRA_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_SRA, SIZE_SRM, SIZE_SRA_SRM_OFFSET, true, SRM_EXT));
        conversionTable.put(WII_LABEL+"-"+FLA_LABEL+"-"+PJ64_LABEL+"-"+FLA_LABEL, new ConversionTableEntry(SIZE_FLA, SIZE_FLA, 0, true, FLA_EXT));
        conversionTable.put(WII_LABEL+"-"+FLA_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_FLA, SIZE_SRM, SIZE_FLA_SRM_OFFSET, true, SRM_EXT));
        conversionTable.put(WII_LABEL+"-"+MPK_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_MPK, SIZE_SRM, SIZE_MPK_SRM_OFFSET, false, SRM_EXT));
        conversionTable.put(PJ64_LABEL+"-"+EEP_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_EEP, SIZE_SRM, 0, false, SRM_EXT));
        conversionTable.put(PJ64_LABEL+"-"+SRA_LABEL+"-"+WII_LABEL+"-"+SRA_LABEL, new ConversionTableEntry(SIZE_SRA, SIZE_SRA, 0, true, SRA_EXT));
        conversionTable.put(PJ64_LABEL+"-"+SRA_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_SRA, SIZE_SRM, SIZE_SRA_SRM_OFFSET, false, SRM_EXT));
        conversionTable.put(PJ64_LABEL+"-"+FLA_LABEL+"-"+WII_LABEL+"-"+FLA_LABEL, new ConversionTableEntry(SIZE_FLA, SIZE_FLA, 0, true, FLA_EXT));
        conversionTable.put(PJ64_LABEL+"-"+FLA_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_FLA, SIZE_SRM, SIZE_FLA_SRM_OFFSET, false, SRM_EXT));
        conversionTable.put(PJ64_LABEL+"-"+MPK_LABEL+"-"+RA_LABEL+"-"+SRM_LABEL, new ConversionTableEntry(SIZE_MPK, SIZE_SRM, SIZE_MPK_SRM_OFFSET, false, SRM_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+WII_LABEL+"-"+EEP_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_EEP, 0, false, EEP_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+WII_LABEL+"-"+SRA_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_SRA, -SIZE_SRA_SRM_OFFSET, true, SRA_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+WII_LABEL+"-"+FLA_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_FLA, -SIZE_FLA_SRM_OFFSET, true, FLA_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+WII_LABEL+"-"+MPK_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_MPK, -SIZE_MPK_SRM_OFFSET, false, MPK_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+PJ64_LABEL+"-"+EEP_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_EEP, 0, false, EEP_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+PJ64_LABEL+"-"+SRA_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_SRA, -SIZE_SRA_SRM_OFFSET, false, SRA_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+PJ64_LABEL+"-"+FLA_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_FLA, -SIZE_FLA_SRM_OFFSET, false, FLA_EXT));
        conversionTable.put(RA_LABEL+"-"+SRM_LABEL+"-"+PJ64_LABEL+"-"+MPK_LABEL, new ConversionTableEntry(SIZE_SRM, SIZE_MPK, -SIZE_MPK_SRM_OFFSET, false, MPK_EXT));
    }

    private static JFrame mainFrame;
    private static JFrame readmeFrame;
    private static JFrame lookupFrame;
    private static Image appIcon;
    private static HighlightPainter painter;
    private static JButton inputPathButton;
    private static JTextField inputPathTextField;
    private static JCheckBox standardSizeCheckbox;
    private static JLabel inputSourceLabel;
    private static JComboBox inputSource;
    private static JLabel inputTypeLabel;
    private static JComboBox inputType;
    private static JLabel outputTargetLabel;
    private static JComboBox outputTarget;
    private static JLabel outputTypeLabel;
    private static JComboBox outputType;
    private static JButton convertButton;

    private static String prevSearchText = "";
    private static int prevSearchTextIndex = 0;
       
    public static void main(String[] args) throws Exception {
        appIcon = ImageIO.read(N64SaveConverter.class.getResourceAsStream("N64Logo.png"));
        painter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);
        
        inputPathButton = new JButton("Select an N64 save file");
        inputPathButton.setFocusable(false);
        inputPathButton.addActionListener(inputPathButtonListener());
        inputPathTextField = new JTextField();
        inputPathTextField.setPreferredSize(new Dimension(500, 30));
        inputPathTextField.getDocument().addDocumentListener(inputPathTextFieldListener());
        standardSizeCheckbox = new JCheckBox();
        standardSizeCheckbox.setText("Pad/trim to standard file type size. (No conversion)");
        standardSizeCheckbox.setFocusable(false);
        standardSizeCheckbox.addActionListener(standardSizeCheckboxListener());
        inputSourceLabel = new JLabel("Save file source:");
        inputSource = new JComboBox(inputSourceList);
        inputSource.setFocusable(false);
        inputSource.addActionListener(inputSourceListener());
        inputTypeLabel = new JLabel("Save file source type:");
        inputType = new JComboBox(new String[0]);
        inputType.setFocusable(false);
        inputType.setEnabled(false);
        inputType.addActionListener(inputTypeListener());
        outputTargetLabel = new JLabel("Save file target:");
        outputTarget = new JComboBox(new String[0]);
        outputTarget.setFocusable(false);
        outputTarget.setEnabled(false);
        outputTarget.addActionListener(outputTargetListener());
        outputTypeLabel = new JLabel("Save file target type:");
        outputType = new JComboBox(new String[0]);
        outputType.setFocusable(false);
        outputType.setEnabled(false);
        convertButton = new JButton("Convert save file");
        convertButton.setFocusable(false);
        convertButton.setEnabled(false);
        convertButton.addActionListener(convertButtonListener());

        JPanel groupPanel = new JPanel();
        GroupLayout groupLayout = new GroupLayout(groupPanel);
        groupPanel.setLayout(groupLayout);
        groupLayout.setAutoCreateGaps(true);
        groupLayout.setHorizontalGroup(
            groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
                .addComponent(inputSourceLabel)
                .addComponent(inputTypeLabel)
                .addComponent(outputTargetLabel)
                .addComponent(outputTypeLabel))
            .addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
                .addComponent(inputSource)
                .addComponent(inputType)
                .addComponent(outputTarget)
                .addComponent(outputType))
            .addComponent(standardSizeCheckbox)
        );
        groupLayout.setVerticalGroup(
            groupLayout.createSequentialGroup()
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(inputSourceLabel)
                .addComponent(inputSource))
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(inputTypeLabel)
                .addComponent(inputType)
                .addComponent(standardSizeCheckbox))
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(outputTargetLabel)
                .addComponent(outputTarget))
            .addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
                .addComponent(outputTypeLabel)
                .addComponent(outputType))
        );

        JMenuBar menuBar = new JMenuBar();
        JMenu help = new JMenu("Help");
        JMenuItem readme = new JMenuItem("Readme");
        readme.addActionListener(readmeListener());
        JMenuItem lookup = new JMenuItem("Lookup");
        lookup.addActionListener(lookupListener());
        help.add(readme);
        help.add(lookup);
        menuBar.add(help);

        JPanel inputPathPanel = new JPanel();
        inputPathPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        inputPathPanel.add(inputPathButton);
        inputPathPanel.add(inputPathTextField);
        JPanel selectionPanel = new JPanel();
        selectionPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        selectionPanel.add(groupPanel);
        JPanel convertPanel = new JPanel();
        convertPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
        convertPanel.add(convertButton);
        
        mainFrame = new JFrame();
        mainFrame.setIconImage(appIcon);
        mainFrame.setTitle("N64 Save File Converter");
        mainFrame.setResizable(false);
        mainFrame.setLayout(new BoxLayout(mainFrame.getContentPane(), BoxLayout.Y_AXIS));
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setJMenuBar(menuBar);
        mainFrame.add(inputPathPanel);
        mainFrame.add(selectionPanel);
        mainFrame.add(convertPanel);
        mainFrame.setVisible(true);
        mainFrame.pack();
        mainFrame.setLocationRelativeTo(null);
    }

    public static ActionListener readmeListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    JTextArea readmeText = new JTextArea(new String(N64SaveConverter.class.getResourceAsStream("Readme.txt").readAllBytes()));
                    readmeText.setMargin(new Insets(10,10,10,10));
                    readmeText.setLineWrap(true);
                    readmeText.setEditable(false);
                    JScrollPane readmeScrollPane = new JScrollPane(readmeText);
                    readmeFrame = new JFrame();
                    readmeFrame.setIconImage(appIcon);
                    readmeFrame.setTitle("N64 Save Converter Readme");
                    readmeFrame.setSize(1000, 1000);
                    readmeFrame.add(readmeScrollPane);
                    readmeFrame.setVisible(true);
                    readmeFrame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            mainFrame.setEnabled(true);
                            readmeFrame.dispose();
                        }
                    });
                    mainFrame.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public static ActionListener lookupListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                try {
                    JTextArea lookupText = new JTextArea(new String(N64SaveConverter.class.getResourceAsStream("Lookup.txt").readAllBytes()));
                    lookupText.setMargin(new Insets(10,10,10,10));
                    lookupText.setLineWrap(true);
                    lookupText.setEditable(false);
                    JScrollPane lookupScrollPane = new JScrollPane(lookupText);

                    JTextField searchTextField = new JTextField();
                    searchTextField.setPreferredSize(new Dimension(500, 30));
                    JButton searchTextButton = new JButton("Search for text");
                    searchTextButton.setFocusable(false);
                    searchTextButton.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent event) {
                            try {
                                String currSearchText = searchTextField.getText();
                                int firstSearchTextIndex = lookupText.getText().toUpperCase().indexOf(currSearchText.toUpperCase());
                                if(firstSearchTextIndex == -1) {
                                    JOptionPane.showMessageDialog(null, "Search Value Not Found");
                                } else {
                                    lookupText.getHighlighter().removeAllHighlights();
                                    int currSearchTextIndex = 0;
                                    if(prevSearchText.equals(currSearchText)) {
                                        currSearchTextIndex = lookupText.getText().toUpperCase().indexOf(currSearchText.toUpperCase(), prevSearchTextIndex + prevSearchText.length());
                                        if(currSearchTextIndex == -1) {
                                            currSearchTextIndex = firstSearchTextIndex;
                                        }
                                    } else {
                                        currSearchTextIndex = firstSearchTextIndex;
                                    }
                                    lookupText.select(currSearchTextIndex, currSearchTextIndex + currSearchText.length());
                                    lookupText.getHighlighter().addHighlight(currSearchTextIndex, currSearchTextIndex + currSearchText.length(), painter);
                                    prevSearchText = currSearchText;
                                    prevSearchTextIndex = currSearchTextIndex;
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    JPanel searchPanel = new JPanel();
                    searchPanel.setLayout(new FlowLayout(FlowLayout.LEADING));
                    searchPanel.add(searchTextField);
                    searchPanel.add(searchTextButton);
                    
                    lookupFrame = new JFrame();
                    lookupFrame.setIconImage(appIcon);
                    lookupFrame.setTitle("N64 Save Type Lookup");
                    lookupFrame.setSize(1000, 1000);
                    lookupFrame.setLayout(new BoxLayout(lookupFrame.getContentPane(), BoxLayout.Y_AXIS));
                    lookupFrame.add(searchPanel);
                    lookupFrame.add(lookupScrollPane);
                    lookupFrame.setVisible(true);
                    lookupFrame.addWindowListener(new WindowAdapter() {
                        public void windowClosing(WindowEvent e) {
                            mainFrame.setEnabled(true);
                            lookupFrame.dispose();
                            prevSearchText = "";
                            prevSearchTextIndex = 0;
                        }
                    });
                    mainFrame.setEnabled(false);
                } catch (Exception e) {
                    e.printStackTrace();
                }                
            }
        };
    }

    public static ActionListener inputPathButtonListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setCurrentDirectory(new File(System.getProperty("user.dir")));
                fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("N64 Save Files (*.eep; *.sra; *.fla; *.mpk; *.srm)", "eep", "sra", "fla", "mpk", "srm"));
                fileChooser.setAcceptAllFileFilterUsed(true);
                fileChooser.setDialogTitle("Select a save file");
                if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
                    inputPathTextField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                }
                convertButtonCheck();
            }
        };
    }

    public static DocumentListener inputPathTextFieldListener() {
        return new DocumentListener() {
            public void changedUpdate(DocumentEvent e) {
            }
            public void removeUpdate(DocumentEvent e) {
                convertButtonCheck();
            }
            public void insertUpdate(DocumentEvent e) {
                convertButtonCheck();
            }
        };
    }

    public static ActionListener standardSizeCheckboxListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if(standardSizeCheckbox.isSelected()) {
                    inputSource.setSelectedItem("");
                    inputType.removeAllItems();
                    for(String newInputTypeEntry : Arrays.asList(inputTypeList)) {
                        if(!newInputTypeEntry.isEmpty()) {
                            inputType.addItem(newInputTypeEntry);
                        }
                    }
                    inputSource.setEnabled(false);
                    inputType.setEnabled(true);
                    outputTarget.setEnabled(false);
                    outputType.setEnabled(false);
                    convertButton.setText("Resize save file");
                } else {
                    inputType.removeAllItems();
                    inputSource.setEnabled(true);
                    inputType.setEnabled(false);
                    convertButton.setText("Convert save file");
                }
                convertButtonCheck();
            }
        };
    }

    public static ActionListener inputSourceListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Set<String> validInputTypeOptions = new HashSet<>();
                for(String key : conversionTable.keySet()) {
                    String[] keyParts = key.split("-");
                    if(inputSource.getSelectedItem() != null && inputSource.getSelectedItem().equals(keyParts[0])) {
                        validInputTypeOptions.add(keyParts[1]);
                    }
                }
                List<String> newInputTypeList = new ArrayList<>(Arrays.asList(inputTypeList)); 
                newInputTypeList.retainAll(validInputTypeOptions);
                inputType.removeAllItems();
                for(String newInputTypeEntry : newInputTypeList) {
                    inputType.addItem(newInputTypeEntry);
                }
                convertButtonCheck();
                if(inputSource.getSelectedItem() != null && !inputSource.getSelectedItem().equals("")) {
                    inputType.setEnabled(true);
                    outputTarget.setEnabled(true);
                    outputType.setEnabled(true);
                } else {
                    inputType.setEnabled(false);
                    outputTarget.setEnabled(false);
                    outputType.setEnabled(false);
                }
            }
        };
    }

    public static ActionListener inputTypeListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Set<String> validOutputTargetOptions = new HashSet<>();
                for(String key : conversionTable.keySet()) {
                    String[] keyParts = key.split("-");
                    if(inputSource.getSelectedItem() != null && inputSource.getSelectedItem().equals(keyParts[0]) 
                        && inputType.getSelectedItem() != null && inputType.getSelectedItem().equals(keyParts[1])) {
                        validOutputTargetOptions.add(keyParts[2]);
                    }
                }
                List<String> newOutputTargetList = new ArrayList<>(Arrays.asList(outputTargetList)); 
                newOutputTargetList.retainAll(validOutputTargetOptions);
                outputTarget.removeAllItems();
                for(String newOutputTargetEntry : newOutputTargetList) {
                    outputTarget.addItem(newOutputTargetEntry);
                }
            }
        };
    }

    public static ActionListener outputTargetListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Set<String> validOutputTypeOptions = new HashSet<>();
                for(String key : conversionTable.keySet()) {
                    String[] keyParts = key.split("-");
                    if(inputSource.getSelectedItem() != null && inputSource.getSelectedItem().equals(keyParts[0]) 
                        && inputType.getSelectedItem() != null && inputType.getSelectedItem().equals(keyParts[1]) 
                        && outputTarget.getSelectedItem() != null && outputTarget.getSelectedItem().equals(keyParts[2])) {
                        validOutputTypeOptions.add(keyParts[3]);
                    }
                }
                List<String> newOutputTypeList = new ArrayList<>(Arrays.asList(outputTypeList)); 
                newOutputTypeList.retainAll(validOutputTypeOptions);
                outputType.removeAllItems();
                for(String newOutputTypeEntry : newOutputTypeList) {
                    outputType.addItem(newOutputTypeEntry);
                }
            }
        };
    }

    public static ActionListener convertButtonListener() {
        return new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                byte[] inputSaveFile = readBytes(inputPathTextField.getText());
                if (inputSaveFile != null){
                    if(standardSizeCheckbox.isSelected()) {
                        StandardSizeTableEntry sizeInfo = standardSizeTable.get(inputType.getSelectedItem());
                        byte[] outputSaveFile = byteResize(inputSaveFile, sizeInfo.getStandardSize(), 0);
                        String outputFileName = newFilename(inputPathTextField.getText(), sizeInfo.getExtension());
                        if(writeBytes(outputSaveFile, outputFileName)) {
                            JOptionPane.showMessageDialog(null, "Created new N64 save file: " + outputFileName, "Save resize success!", JOptionPane.INFORMATION_MESSAGE);
                        }  
                    } else {
                        String conversionKey = inputSource.getSelectedItem()+"-"+inputType.getSelectedItem()+"-"+outputTarget.getSelectedItem()+"-"+outputType.getSelectedItem();
                        ConversionTableEntry conversionInfo = conversionTable.get(conversionKey);
                        byte[] outputSaveFile = byteResize(inputSaveFile, conversionInfo.getInputSize(), 0);
                        if(!SRM_LABEL.equals(inputType.getSelectedItem()) && conversionInfo.isByteswap()) {
                            outputSaveFile = byteSwap(outputSaveFile);
                        }
                        if(!Objects.equals(inputType.getSelectedItem(), outputType.getSelectedItem())) {
                            outputSaveFile = byteResize(outputSaveFile, conversionInfo.getOutputSize(), conversionInfo.getOffset());
                        }
                        if(SRM_LABEL.equals(inputType.getSelectedItem()) && conversionInfo.isByteswap()) {
                            outputSaveFile = byteSwap(outputSaveFile);
                        }
                        String outputFileName = newFilename(inputPathTextField.getText(), conversionInfo.getExtension());
                        if(writeBytes(outputSaveFile, outputFileName)) {
                            JOptionPane.showMessageDialog(null, "Created new N64 save file: " + outputFileName, "Save conversion success!", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        };
    }

    private static void convertButtonCheck() {
        if((inputPathTextField.getText() != null && !inputPathTextField.getText().isBlank()) 
            && ((inputSource.getSelectedItem() != null && !inputSource.getSelectedItem().equals(""))
            || standardSizeCheckbox.isSelected())) {
                convertButton.setEnabled(true);
        } else {
            convertButton.setEnabled(false);
        }
    }

    private static byte[] readBytes(String location) {
        byte[] fileBytes = null;
        try {
            File file = new File(location);
            fileBytes = Files.readAllBytes(file.toPath());
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Could not read file: " + location, "File read error", JOptionPane.ERROR_MESSAGE);
        }
        return fileBytes;
    }

    private static boolean writeBytes(byte[] output, String location) {
        boolean success = false;
        if (output != null) {
            try {
                Path path = Paths.get(location);
                Files.write(path, output);
                success = true;
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Could not write file: " + location, "File write error", JOptionPane.ERROR_MESSAGE);
            }
        }
        return success;
    }

    private static String newFilename(String filename, String extension) {
        int lastIndexOfDot = filename.lastIndexOf(".");
        if(lastIndexOfDot == -1) {
            return filename + extension;
        } else {
            String currentExtention = filename.substring(lastIndexOfDot);
            return filename.substring(0, lastIndexOfDot) + (Objects.equals(extension, currentExtention) ? "#" : "") + extension;
        }
    }

    private static byte[] byteResize(byte[] input, int toSize, int offset) {
        byte[] output = new byte[toSize];
        for(int i = 0; i < output.length; i++) {
            output[i] = (i >= offset && i - offset < input.length) ? input[i - offset] : 0;
        }
        return output;
    }

    private static byte[] byteSwap(byte[] input) {
        byte[] output = new byte[input.length];
        for(int i = 0; i < input.length; i++) {
            if(i < input.length) {
                if(i % 4 == 0) {
                    if(i + 3 < input.length) {
                        output[i] = input[i + 3];
                        output[i + 1] = input[i + 2];
                        output[i + 2] = input[i + 1];
                        output[i + 3] = input[i];
                    } else if(i + 2 < input.length) {
                        output[i] = input[i + 2];
                        output[i + 1] = input[i + 1];
                        output[i + 2] = input[i];
                    } else if(i + 1 < input.length) {
                        output[i] = input[i + 1];
                        output[i + 1] = input[i];
                    } else {
                        output[i] = input[i];
                    }
                }
            }
        }
        return output;
    }

    private static class StandardSizeTableEntry {
        private int standardSize;
        private String extension;

        public StandardSizeTableEntry(int standardSize, String extension) {
            this.standardSize = standardSize;
            this.extension = extension;
        }
        public int getStandardSize() {
            return standardSize;
        }
        public String getExtension() {
            return extension;
        }
    }

    private static class ConversionTableEntry {
        private int inputSize;
        private int outputSize;
        private int offset;
        private boolean byteswap;
        private String extension;

        public ConversionTableEntry(int inputSize, int outputSize, int offset, boolean byteswap, String extension) {
            this.inputSize = inputSize;
            this.outputSize = outputSize;
            this.offset = offset;
            this.byteswap = byteswap;
            this.extension = extension;
        }
        public int getInputSize() {
            return inputSize;
        }
        public int getOutputSize() {
            return outputSize;
        }
        public int getOffset() {
            return offset;
        }
        public boolean isByteswap() {
            return byteswap;
        }
        public String getExtension() {
            return extension;
        }
    }
}


