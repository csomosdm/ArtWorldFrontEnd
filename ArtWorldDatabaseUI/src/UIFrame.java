import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;

public class UIFrame extends JFrame implements ActionListener {
	public static final int frameWidth = 1400, frameHeight = 675, queryPanelHeight = 80, otherPanelHeight = 120, resultPanelHeight = 450;
	public static final Font FONT = new Font("TimesRoman", Font.PLAIN,  14);
	public static final Font ERRORFONT = new Font("TimesRoman", Font.BOLD, 30);
	private ArtWorldConnect awc;
	private ArtworkManager artworkManager;
	private ArtistManager artistManager;
	private MuseumManager museumManager;
	private ExhibitManager exhibitManager;
	private JPanel queryPanel, secondPanel, resultPanel;
	private JComboBox secondBox, thirdBox, museumBox, permissions;
	private JTextField[] textFields;
	private JLabel artworkNameLabel, artworkIDLabel, artworkMediumLabel, artworkCategoryLabel, artistNameLabel, artistIDLabel, artistBirthDateLabel, artistDeathDateLabel, museumIDLabel,
	museumNameLabel, museumCityLabel, museumStateLabel, exhibitNameLabel, exhibitIDLabel, otherExhibitNameLabel, exhibitCityLabel, exhibitStateLabel, exhibitStartDateLabel, exhibitEndDateLabel;
	private JLabel museumBoxLabel;
	private JButton executeButton, defaultsButton;
	private int secondBoxIndex, thirdBoxIndex, overallAction, permissionLevel;
	private Object[] museumIDs;
	private String[] attributes, manageExhibit;
	private String username;
	
	public UIFrame (ArtWorldConnect awc) {
		this.awc = awc;
		this.artworkManager = new ArtworkManager(awc, this);
		this.artistManager = new ArtistManager(awc, this);
		this.museumManager = new MuseumManager(awc, this);
		this.exhibitManager = new ExhibitManager(awc, this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(UIFrame.frameWidth, UIFrame.frameHeight);
		this.setLocationRelativeTo(null);
		this.setTitle("Art World Front End");
		this.setLayout(null);
		this.initializeTextAndLabels();
		permissionLevel = 1;
		username = "username";

		//query panel
		queryPanel = new JPanel();
		queryPanel.setBackground(Color.darkGray);
		queryPanel.setBounds(0,0,UIFrame.frameWidth, UIFrame.queryPanelHeight);
		this.initializeQueryPanel();
		
		//other panels
		secondPanel = new JPanel();
		secondPanel.setBackground(Color.orange);
		secondPanel.setBounds(0, UIFrame.queryPanelHeight, UIFrame.frameWidth, UIFrame.otherPanelHeight);
		this.setSearchArtwork();
		
		resultPanel = new JPanel();
		resultPanel.setBounds(0, UIFrame.queryPanelHeight + UIFrame.otherPanelHeight, UIFrame.frameWidth, UIFrame.frameHeight - (UIFrame.queryPanelHeight + UIFrame.otherPanelHeight));
		
		this.add(queryPanel);
		this.add(secondPanel);
		this.add(resultPanel);
		this.setVisible(true);		
	}

	private void initializeQueryPanel() {
		//Generate the strings for the JComboBoxs
		String[] actions = {"Search For", "Staff Search For", "Create",  "Update", "Delete", "Manage Exhibit"};
		String[] attributes = {"Artwork", "Artist", "Museum", "Exhibit"};
		Object[] museums = museumManager.getMuseums();
		museumIDs = museumManager.getMuseumIDs();
		
		//Create Action Box and Label
		JLabel secondBoxLabel = new JLabel("     ");
		secondBoxLabel.setFont(UIFrame.FONT);
		secondBoxLabel.setForeground(Color.white);
		secondBox = new JComboBox(actions);
		secondBox.setFont(UIFrame.FONT);
		secondBox.addActionListener(this);
		secondBoxIndex = 0;
		
		//Create Attribute Box and Label
		JLabel thirdBoxLabel = new JLabel("     ");
		thirdBoxLabel.setFont(UIFrame.FONT);
		thirdBoxLabel.setForeground(Color.white);
		thirdBox = new JComboBox(attributes);
		thirdBox.setFont(UIFrame.FONT);
		thirdBox.addActionListener(this);
		
		//Create Museum Box and Label
		museumBoxLabel = new JLabel("Current Museum:");
		museumBoxLabel.setFont(UIFrame.FONT);
		museumBoxLabel.setForeground(Color.white);
		museumBox = new JComboBox(museums);
		museumBox.setFont(UIFrame.FONT);
		museumBox.addActionListener(this);
		
		//Create Execute Button
		executeButton = new JButton("Execute");
		executeButton.setFont(UIFrame.FONT);
		executeButton.addActionListener(this);
		
		defaultsButton = new JButton("Fill With Defaults");
		defaultsButton.setFont(UIFrame.FONT);
		defaultsButton.addActionListener(this);
		
		//Adds all boxes and labels to the panel
		
		//Used for Testing
		String[] permissionList = {"1", "2", "3", "4", "5"};
		permissions = new JComboBox(permissionList);
		permissions.addActionListener(this);
		queryPanel.add(permissions);
		
		queryPanel.add(secondBoxLabel);
		queryPanel.add(secondBox);
		queryPanel.add(thirdBoxLabel);
		queryPanel.add(thirdBox);
		queryPanel.add(new JLabel("   "));
		queryPanel.add(executeButton);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == permissions) {
			if (permissionLevel != (permissionLevel = permissions.getSelectedIndex() + 1)) {
				this.setsecondBox(permissionLevel);
				secondBoxIndex = secondBox.getSelectedIndex();
				this.setThirdBox(secondBoxIndex, permissionLevel);
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		} else if (e.getSource() == secondBox) {
			if (permissionLevel != (permissionLevel = permissions.getSelectedIndex() + 1) || secondBoxIndex != (secondBoxIndex = secondBox.getSelectedIndex())) {
				this.setThirdBox(secondBoxIndex, permissionLevel);
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		} else if (e.getSource() == thirdBox) {
			if (permissionLevel != (permissionLevel = permissions.getSelectedIndex() + 1) || secondBoxIndex != (secondBoxIndex = secondBox.getSelectedIndex()) || thirdBoxIndex != (thirdBoxIndex = thirdBox.getSelectedIndex())) {
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		}else if (e.getSource() == executeButton) {
			this.executeAction();
		} else if (e.getSource() == defaultsButton) {
			this.fillDefaults();
		}
		this.repaint();
		this.setVisible(true);
	}

	private void setThirdBox(int action, int pLevel) {
		this.disableActionListeners();
		switch (action) {
		case (0):
			boolean[] temp = {true, true, true, true};
			this.setThridBox(temp);
			break;
		case (1):
			boolean[] temp2 = {true, true, true, true};
			this.setThridBox(temp2);
			break;
		case (2):
			if (pLevel == 2) {
				boolean[] temp1 = {false, false, false, true};
				this.setThridBox(temp1);
			} else if (pLevel == 3 || pLevel == 4) {
				boolean[] temp1 = {true, true, false, true};
				this.setThridBox(temp1);
			} else {
				boolean[] temp1 = {true, true, true, true};
				this.setThridBox(temp1);
			}
			break;
		case (3):
			if (pLevel == 2) {
				boolean[] temp1 = {false, false, false, true};
				this.setThridBox(temp1);
			} else if (pLevel == 3) {
				boolean[] temp1 = {true, true, false, true};
				this.setThridBox(temp1);
			} else {
				boolean[] temp1 = {true, true, true, true};
				this.setThridBox(temp1);
			}
			break;
		case (4):
			if (pLevel == 2) {
				boolean[] temp1 = {false, false, false, true};
				this.setThridBox(temp1);
			} else if (pLevel == 3) {
				boolean[] temp1 = {true, true, false, true};
				this.setThridBox(temp1);
			} else {
				boolean[] temp1 = {true, true, true, true};
				this.setThridBox(temp1);
			}
			break;
		case (5):
			thirdBox.removeAllItems();
			thirdBox.addItem("List Artwork");
			thirdBox.addItem("Add Artwork");
			thirdBox.addItem("Remove Artwork");
			System.out.println("Second Box Selected: " + action);
			break;
		case (6):
			thirdBox.removeAllItems();
			thirdBox.addItem("List Staff");
			if (pLevel > 3) {
				thirdBox.addItem("Add Staff");
				thirdBox.addItem("Update Staff");
				thirdBox.addItem("Remove Staff");
			}
			System.out.println("Second Box Selected: " + action);
			break;
		default:
			System.out.println("What the fuck are you doing here");
		}
		this.enableActionListeners();
		thirdBox.setSelectedIndex(0);
	}

	private void setThridBox(boolean[] temp) {
		thirdBox.removeAllItems();
		if (temp[0]) {
			thirdBox.addItem("Artwork");
		}
		if (temp[1]) {
			thirdBox.addItem("Artist");
		}
		if (temp[2]) {
			thirdBox.addItem("Museum");
		}
		if (temp[3]) {
			thirdBox.addItem("Exhibit");
		}
	}

	private void setsecondBox(int pLevel) {
		this.disableActionListeners();
		switch (pLevel) {
		case(1) :
//			System.out.println("Permission 1");
			this.setQueryPanelBasic();
			break;
		case(2) :
//			System.out.println("Permission 2");
			this.setQueryPanelExhibitStaff();
			break;
		case(3) :
//			System.out.println("Permission 3");
			this.setQueryPanelGeneralStaff();
			break;
		case(4) :
			this.setQueryPanelGeneralStaff();
//			System.out.println("Permission 4");
			break;
		case(5) :
			this.setQueryPanelGeneralStaff();
//			System.out.println("Permission 5");
			break;
		default:
			System.out.println("Fuck, you shouldn't be here");
		}
		this.enableActionListeners();
		secondBox.setSelectedIndex(0);
	}

	private void setQueryPanelGeneralStaff() {
		secondBox.removeAllItems();
		String[] temp = {"Search For", "Staff Search For", "Create",  "Update", "Delete", "Manage Exhibit", "Manage Staff"};
		for(String s : temp) {
			secondBox.addItem(s);
		}
	}

	private void setQueryPanelExhibitStaff() {
		secondBox.removeAllItems();
		String[] temp = {"Search For", "Staff Search For", "Create",  "Update", "Delete", "Manage Exhibit"};
		for(String s : temp) {
			secondBox.addItem(s);
		}
	}

	private void setQueryPanelBasic() {
		secondBox.removeAllItems();
		secondBox.addItem((String) "Search For");
	}

	private void enableActionListeners() {
		permissions.addActionListener(this);
		secondBox.addActionListener(this);
		thirdBox.addActionListener(this);
	}

	private void disableActionListeners() {
		permissions.removeActionListener(this);
		secondBox.removeActionListener(this);
		thirdBox.removeActionListener(this);
	}

	private int calculateAction() {
		int action;
		String temp = (String) secondBox.getSelectedItem();
		if (temp.equals("Search For")) {
			action = 0;
		} else if (temp.equals("Staff Search For")) {
			action = 10;
		} else if (temp.equals("Create")) {
			action = 20;
		} else if (temp.equals("Update")) {
			action = 30;
		} else if (temp.equals("Delete")){
			action = 40;
		} else if (temp.equals("Manage Exhibit")){
			temp = (String) thirdBox.getSelectedItem();
			if (temp.equals("Remove Artwork")) {
				return 52;
			} else if (temp.equals("Add Artwork")) {
				return 51;
			} else {
				return 50;
			}
		} else {
			temp = (String) thirdBox.getSelectedItem();
			if (temp.equals("Remove Staff")) {
				return 63;
			} else if (temp.equals("Update Staff")) {
				return 62;
			} else if (temp.equals("Add Staff")) {
				return 61;
			} else {
				return 60;
			}
		}
		temp = (String) thirdBox.getSelectedItem();
		if (temp.equals("Artwork")) {
			action += 0;
		} else if (temp.equals("Artist")) {
			action += 1;
		} else if (temp.equals("Museum")) {
			action += 2;
		} else {
			action += 3;
		}
		return action;
	}

	private void fillDefaults() {
		switch (overallAction) {
		case (30):
			artworkManager.fillDefaults(textFields[0].getText(), username);
			break;
		case (31):
			artistManager.fillDefaults(textFields[0].getText(), username);
			break;
		case (32):
			museumManager.fillDefaults(museumIDs[museumBox.getSelectedIndex()], username);
			break;
		case (33):
			exhibitManager.fillDefaults(textFields[0].getText(), username);
			break;
		default:
			System.out.println("this code shouldn't run");
		}
	}

	private void executeAction() {
		switch (overallAction) {
		case (0):
			artworkManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText());
			break;
		case (1):
			artistManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (2):
			museumManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText());
			break;
		case (3):
			exhibitManager.basicSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText());
			break;
		case (10):
			artworkManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (11):
			artistManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (12):
			museumManager.staffSearch(museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (13):
			exhibitManager.staffSearch(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (20):
			artworkManager.createArtwork(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (21): 
			artistManager.createArtist(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (22):
			museumManager.createMuseum(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (23):
			exhibitManager.createExhibit(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (30):
			artworkManager.updateArtwork(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), textFields[5].getText(), username);
			break;
		case (31):
			artistManager.updateArtist(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), username);
			break;
		case (32):
			museumManager.updateMuseum(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), museumIDs[museumBox.getSelectedIndex()], username);
			break;
		case (33):
			exhibitManager.updateExhibit(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), textFields[4].getText(), username);
			break;
		case (40):
			artworkManager.deleteArtwork(textFields[0].getText(), textFields[1].getText(), username);
			break;
		case (41):
			artistManager.deleteArtist(textFields[0].getText(), textFields[1].getText(), username);
			break;
		case (42):
			museumManager.deleteMuseum(textFields[0].getText(), museumIDs[museumBox.getSelectedIndex()], username);
			break;
		case (43):
			exhibitManager.deleteExhibit(textFields[0].getText(), textFields[1].getText(), username);
			break;
		default:
			System.out.println("execution not yet implemented");
		}
		this.repaint();
	}

	private void updateSecondPanel() {
		switch (overallAction) {
		case (0):
			this.setSearchArtwork();
			break;
		case (1):
			this.setSearchArtist();
			break;
		case (2):
			this.setSearchMuseum();
			break;
		case (3):
			this.setSearchExhibit();
			break;
		case (10):
			this.setStaffSearchArtwork();
			break;
		case (11):
			this.setStaffSearchArtist();
			break;
		case (12):
			this.setStaffSearchMuseum();
			break;
		case (13):
			this.setStaffSearchExhibit();
			break;
		case (20):
			this.setInsertArtwork();
			break;
		case (21):
			this.setInsertArtist();
			break;
		case (22):
			this.setInsertMuseum();
			break;
		case (23):
			this.setInsertExhibit();
			break;
		case (30):
			this.setArtworkUpdate();
			break;
		case (31):
			this.setArtistUpdate();
			break;
		case (32):
			this.setMuseumUpdate();
			break;
		case (33):
			this.setExhibitUpdate();
			break;
		case (40):
			this.setDeleteArtwork();
			break;
		case (41):
			this.setDeleteArtist();
			break;
		case (42):
			this.setDeleteMuseum();
			break;
		case (43):
			this.setDeleteExhibit();
			break;
		default:
			System.out.println("OverallAction: " + overallAction);
		}
		this.repaint();
	}

	private void setDeleteExhibit() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[1]);		
	}

	private void setDeleteMuseum() {
		this.resetTextFields(1);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
	}

	private void setDeleteArtist() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setDeleteArtwork() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setExhibitUpdate() {
		this.resetTextFields(5);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitStartDateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(new JLabel("                                                "));
		this.secondPanel.add(exhibitEndDateLabel);
		this.secondPanel.add(textFields[3]);
		JLabel temp = new JLabel("Host Museum ID:");
		temp.setFont(UIFrame.FONT);
		this.secondPanel.add(temp);
		this.secondPanel.add(textFields[4]);
	}

	private void setMuseumUpdate() {	
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setArtistUpdate() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistBirthDateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistDeathDateLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setArtworkUpdate() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(new JLabel("            "));
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(museumIDLabel);
		this.secondPanel.add(textFields[5]);
	}

	private void setStaffSearchExhibit() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setStaffSearchMuseum() {
		this.secondPanel.removeAll();
	}

	private void setStaffSearchArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setStaffSearchArtwork() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[5]);
		this.setVisible(true);		
	}

	private void setInsertExhibit() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(otherExhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(exhibitStartDateLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitEndDateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertMuseum() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artistBirthDateLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistDeathDateLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setInsertArtwork() {
		this.resetTextFields(4);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistIDLabel);
		this.secondPanel.add(textFields[3]);
	}

	private void setSearchExhibit() {
		this.resetTextFields(5);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(exhibitCityLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(exhibitStateLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[4]);
	}

	private void setSearchMuseum() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(museumCityLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumStateLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[5]);
	}

	private void setSearchArtist() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[2]);
	}
	
	private void setSearchArtwork() {
		this.resetTextFields(6);
		this.secondPanel.removeAll();
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkMediumLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artworkCategoryLabel);
		this.secondPanel.add(textFields[2]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[3]);
		this.secondPanel.add(museumNameLabel);
		this.secondPanel.add(textFields[4]);
		this.secondPanel.add(exhibitNameLabel);
		this.secondPanel.add(textFields[5]);
	}
	
	public JPanel getResultPanel() {
		return resultPanel;
	}

	private void initializeTextAndLabels() {
		textFields = new JTextField[7];
		for (int i = 0; i < 7; i++) {
			textFields[i] = new JTextField(20);
			textFields[i].setFont(UIFrame.FONT);
		}
		
		artworkNameLabel = new JLabel("Artwork Name:");
		artworkNameLabel.setFont(UIFrame.FONT);
		artworkIDLabel = new JLabel("Artwork ID:");
		artworkIDLabel.setFont(UIFrame.FONT);
		artworkMediumLabel = new JLabel("Artwork Medium:");
		artworkMediumLabel.setFont(UIFrame.FONT);
		artworkCategoryLabel = new JLabel("Artwork Category:");
		artworkCategoryLabel.setFont(UIFrame.FONT);
		artistNameLabel = new JLabel("Artist Name:");
		artistNameLabel.setFont(UIFrame.FONT);
		artistIDLabel = new JLabel("Artist ID:");
		artistIDLabel.setFont(UIFrame.FONT);
		artistBirthDateLabel = new JLabel("Artist Birth Date (year)");
		artistBirthDateLabel.setFont(UIFrame.FONT);
		artistDeathDateLabel = new JLabel("Artist Death Date (year)");
		artistDeathDateLabel.setFont(UIFrame.FONT);
		museumIDLabel = new JLabel("Museum ID: ");
		museumIDLabel.setFont(UIFrame.FONT);
		museumNameLabel = new JLabel("Museum Name:");
		museumNameLabel.setFont(UIFrame.FONT);
		museumCityLabel = new JLabel("Museum City:");
		museumCityLabel.setFont(UIFrame.FONT);
		museumStateLabel = new JLabel("Museum State (AL, AK, ...)");
		museumStateLabel.setFont(UIFrame.FONT);
		exhibitNameLabel = new JLabel("Active Exhibit Name:");
		exhibitNameLabel.setFont(UIFrame.FONT);
		otherExhibitNameLabel = new JLabel("Exhibit Name:");
		otherExhibitNameLabel.setFont(UIFrame.FONT);
		exhibitIDLabel = new JLabel("Exhibit ID:");
		exhibitIDLabel.setFont(UIFrame.FONT);
		exhibitCityLabel = new JLabel("Exhibit City:");
		exhibitCityLabel.setFont(UIFrame.FONT);
		exhibitStateLabel = new JLabel("Exhibit State (AL, AK, ...)");
		exhibitStateLabel.setFont(UIFrame.FONT);
		exhibitStartDateLabel = new JLabel("Exhibit Start Date (YYYY-MM-DD)");
		exhibitStartDateLabel.setFont(UIFrame.FONT);
		exhibitEndDateLabel = new JLabel("Exhibit End Date (YYYY-MM-DD)");
		exhibitEndDateLabel.setFont(UIFrame.FONT);
	}
	
	private void resetTextFields(int index) {
		for (int i = 0; i < index; i++) {
			textFields[i].setText("");
		}
	}

	public void createErrorMessage(String errorMessage) {
		this.resultPanel.removeAll();
		JLabel temp = new JLabel(errorMessage);
		temp.setFont(UIFrame.ERRORFONT);
		temp.setForeground(Color.red);
		this.resultPanel.add(temp);
	}
	
	public void createSuccessMessage(String errorMessage) {
		this.resultPanel.removeAll();
		JLabel temp = new JLabel(errorMessage);
		temp.setFont(UIFrame.ERRORFONT);
		this.resultPanel.add(temp);
	}

	public JTextField[] getTextFields() {
		return textFields;
	}
}
