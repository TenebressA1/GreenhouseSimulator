package greenhousesimulator.ui;

import greenhousesimulator.*;
import javax.swing.*;
import java.awt.*;
import java.util.List;

public class LoadGameDialog extends JDialog {
    private JList<GreenhouseDatabase.SaveInfo> savesList;
    private DefaultListModel<GreenhouseDatabase.SaveInfo> listModel;
    private boolean loaded = false;
    private GreenhouseDatabase.SaveData loadedData;
    private String playerName;
    
    public LoadGameDialog(JFrame parent) {
        super(parent, "Загрузить теплицу", true);
        setupUI();
        loadSaves();
    }
    
    private void setupUI() {
        setLayout(new BorderLayout());
        setSize(500, 400);
        setLocationRelativeTo(getParent());
        
        JLabel titleLabel = new JLabel("Выберите сохранение:", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        listModel = new DefaultListModel<>();
        savesList = new JList<>(listModel);
        savesList.setFont(new Font("Monospaced", Font.PLAIN, 14));
        savesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(savesList);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        
        JButton loadButton = new JButton("Загрузить");
        loadButton.addActionListener(e -> loadSelected());
        
        JButton deleteButton = new JButton("Удалить");
        deleteButton.addActionListener(e -> deleteSelected());
        
        JButton cancelButton = new JButton("Отмена");
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(loadButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadSaves() {
        listModel.clear();
        List<GreenhouseDatabase.SaveInfo> saves = GreenhouseDatabase.getSavesList();
        for (GreenhouseDatabase.SaveInfo save : saves) {
            listModel.addElement(save);
        }
        
        if (saves.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Нет сохраненных игр. Начните новую игру и сохранитесь!",
                "Информация",
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    private void loadSelected() {
        GreenhouseDatabase.SaveInfo selected = savesList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                "Выберите сохранение для загрузки",
                "Ошибка",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        loadedData = GreenhouseDatabase.loadGame(selected.id);
        if (loadedData != null) {
            playerName = loadedData.playerName;
            loaded = true;
            dispose();
        } else {
            JOptionPane.showMessageDialog(this,
                "Не удалось загрузить сохранение",
                "Ошибка",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void deleteSelected() {
        GreenhouseDatabase.SaveInfo selected = savesList.getSelectedValue();
        if (selected == null) return;
        
        int result = JOptionPane.showConfirmDialog(this,
            "Удалить сохранение \"" + selected.saveName + "\"?",
            "Подтверждение",
            JOptionPane.YES_NO_OPTION);
        
        if (result == JOptionPane.YES_OPTION) {
            GreenhouseDatabase.deleteSave(selected.id);
            loadSaves();
        }
    }
    
    public boolean isLoaded() {
        return loaded;
    }
    
    public GreenhouseDatabase.SaveData getLoadedData() {
        return loadedData;
    }
    
    public String getPlayerName() {
        return playerName;
    }
}