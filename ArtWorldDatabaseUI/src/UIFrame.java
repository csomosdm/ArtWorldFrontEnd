import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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
	public static final int frameWidth = 1400, frameHeight = 675, queryPanelHeight = 80, otherPanelHeight = 80, resultPanelHeight = 450;
	public static final Font FONT = new Font("TimesRoman", Font.PLAIN,  14);
	public static final Font ERRORFONT = new Font("TimesRoman", Font.BOLD, 30);
	private ArtWorldConnect awc;
	private ArtworkManager artworkManager;
	private ArtistManager artistManager;
	private MuseumManager museumManager;
	private ExhibitManager exhibitManager;
	private StaffManager staffManager;
	private JPanel queryPanel, secondPanel, resultPanel;
	private JComboBox secondBox, thirdBox, museumBox, staffRoles;
	private JTextField[] textFields;
	private JLabel artworkNameLabel, artworkIDLabel, artworkMediumLabel, artworkCategoryLabel, artistNameLabel, artistIDLabel, artistBirthDateLabel, artistDeathDateLabel, museumIDLabel,
	museumNameLabel, museumBoxLabel, museumCityLabel, museumStateLabel, exhibitNameLabel, exhibitIDLabel, otherExhibitNameLabel, exhibitCityLabel, exhibitStateLabel, exhibitStartDateLabel, 
	exhibitEndDateLabel, staffNameLabel, staffUsernameLabel, staffPasswordLabel, staffPositionLabel, permissionLabel;
	private JButton executeButton, defaultsButton;
	private int secondBoxIndex, thirdBoxIndex, overallAction, permissionLevel;
	private Object[] museumIDs, permissionLevels;
	private String[] attributes, manageExhibit;
	private String username;
	private JCheckBox filterByRoles, newUser;
	private boolean update, admin, guest;
	
	public UIFrame (ArtWorldConnect awc, String username, boolean admin) {
		this.awc = awc;
		this.artworkManager = new ArtworkManager(awc, this);
		this.artistManager = new ArtistManager(awc, this);
		this.museumManager = new MuseumManager(awc, this);
		this.exhibitManager = new ExhibitManager(awc, this);
		this.staffManager = new StaffManager(awc, this);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setSize(UIFrame.frameWidth, UIFrame.frameHeight);
		this.setLocationRelativeTo(null);
		this.setTitle("Art World Front End");
		this.setLayout(null);
		this.initializeTextAndLabels();
		guest = true;
		update = false;
		this.admin = admin;

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
		if (username == null) {
			this.initializeMuseumInfo();
			this.setsecondBox(1);
		} else {
			this.username = username;
			guest = false;
			this.initializeMuseumInfo();
			permissionLevel = (int) permissionLevels[0];
			this.setPermissionLabel();
			this.setsecondBox(permissionLevel);
			secondBoxIndex = secondBox.getSelectedIndex();
			this.setThirdBox(secondBoxIndex, permissionLevel);
			overallAction = calculateAction();
			this.updateSecondPanel();
		}
		//Adds all boxes and labels to the panel
		queryPanel.add(secondBox);
		JLabel thirdBoxLabel = new JLabel("     ");
		queryPanel.add(thirdBoxLabel);
		queryPanel.add(thirdBox);
		queryPanel.add(new JLabel("     "));
		queryPanel.add(executeButton);
		this.setVisible(true);		
	}

	private void initializeMuseumInfo() {
		museumBoxLabel = new JLabel("Current Museum:");
		museumBoxLabel.setFont(UIFrame.FONT);
		museumBoxLabel.setForeground(Color.white);
		Object[][] museumInfo = museumManager.getMuseumInformation(username, guest);
		if (museumInfo[0].length == 0) {
			this.setsecondBox(1);
		} else {
			museumBox = new JComboBox(museumInfo[0]);
			museumBox.setFont(UIFrame.FONT);
			museumBox.addActionListener(this);
			museumIDs = museumInfo[1];
			permissionLevels = museumInfo[2];
			for (int i = 0; i < museumInfo[0].length; i++) {
//				System.out.println("Museum Name: " + museumInfo[0][i] + " Museum ID: " + museumInfo[1][i] + " Museum Permission Level: " + museumInfo[2][i]);
			}
			if (guest) return;
			queryPanel.add(museumBox);
			permissionLabel = new JLabel();
			permissionLabel.setFont(UIFrame.FONT);
			permissionLabel.setForeground(Color.white);
			this.setPermissionLabel();
			queryPanel.add(permissionLabel);
		}
//		this.setsecondBox(5);		
	}

	private void setPermissionLabel() {
		int i = (int) permissionLevels[museumBox.getSelectedIndex()];
		System.out.println("permissionLevel: " + i);
		switch (i) {
		case (2):
			permissionLabel.setText("  Role: Exhibit Staff          ");
			break;
		case (3):
			permissionLabel.setText("  Role: General Staff          ");
			break;
		case (4):
			permissionLabel.setText("  Role: Owner          ");
			break;
		case (5):
			permissionLabel.setText("  Role: Admin          ");
			break;
		}
	}

	private void initializeQueryPanel() {
		//Generate the strings for the JComboBoxs
		String[] actions = {"Search For", "Staff Search For", "Create",  "Update", "Delete", "Manage Exhibit"};
		String[] attributes = {"Artwork", "Artist", "Museum", "Exhibit"};		
		
		//Create Action Box and Label
		secondBox = new JComboBox(actions);
		secondBox.setFont(UIFrame.FONT);
		secondBox.addActionListener(this);
		secondBoxIndex = 0;
		
		//Create Attribute Box and Label
		thirdBox = new JComboBox(attributes);
		thirdBox.setFont(UIFrame.FONT);
		thirdBox.addActionListener(this);
		
		//Create Execute Button
		executeButton = new JButton("Execute");
		executeButton.setFont(UIFrame.FONT);
		executeButton.addActionListener(this);
		
		defaultsButton = new JButton("Fill With Defaults");
		defaultsButton.setFont(UIFrame.FONT);
		defaultsButton.addActionListener(this);
		
		newUser = new JCheckBox("Is A New User");
		newUser.setFont(UIFrame.FONT);
		newUser.setHorizontalTextPosition(SwingConstants.LEFT);
		newUser.setBackground(Color.orange);
		newUser.addActionListener(this);
		
		filterByRoles = new JCheckBox("   Filter By Role");
		filterByRoles.setFont(UIFrame.FONT);
		filterByRoles.setHorizontalTextPosition(SwingConstants.LEFT);
		filterByRoles.setBackground(Color.orange);
		
		String[] positions = {"Exhibit Staff", "General Staff", "Museum Owner"};
		staffRoles = new JComboBox(positions);
		staffRoles.setFont(UIFrame.FONT);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == museumBox) {
			if (permissionLevel != (permissionLevel = (int) permissionLevels[museumBox.getSelectedIndex()])) {
				System.out.println("Actual Permission Level: " + permissionLevel);
				this.setPermissionLabel();
				this.setsecondBox(permissionLevel);
				secondBoxIndex = secondBox.getSelectedIndex();
				this.setThirdBox(secondBoxIndex, permissionLevel);
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		} else if (e.getSource() == secondBox) {
			if (permissionLevel != (permissionLevel = (int) permissionLevels[museumBox.getSelectedIndex()]) || secondBoxIndex != (secondBoxIndex = secondBox.getSelectedIndex())) {
				this.setThirdBox(secondBoxIndex, permissionLevel);
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		} else if (e.getSource() == thirdBox) {
			if (permissionLevel != (permissionLevel = (int) permissionLevels[museumBox.getSelectedIndex()]) || secondBoxIndex != (secondBoxIndex = secondBox.getSelectedIndex()) || thirdBoxIndex != (thirdBoxIndex = thirdBox.getSelectedIndex())) {
				overallAction = calculateAction();
				this.updateSecondPanel();
			}
		}else if (e.getSource() == executeButton) {
			this.executeAction();
		} else if (e.getSource() == defaultsButton) {
			this.fillDefaults();
		} else if (e.getSource() == newUser) {
			if (newUser.isSelected()) {
				secondPanel.add(staffPasswordLabel);
				secondPanel.add(textFields[2]);
			} else {
				secondPanel.remove(staffPasswordLabel);
				secondPanel.remove(textFields[2]);
			}
		}
		this.repaint();
		this.setVisible(true);
	}

	private void setThirdBox(int action, int pLevel) {
		this.disableActionListeners();
		if (update) {
			update = false;
			queryPanel.remove(defaultsButton);
		}
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
			if (!update) {
				update = true;
				queryPanel.remove(executeButton);
				queryPanel.add(defaultsButton);
				queryPanel.add(executeButton);
			}
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
			break;
		case (6):
			thirdBox.removeAllItems();
			thirdBox.addItem("List Staff");
			if (pLevel > 3) {
				thirdBox.addItem("Add Staff");
				thirdBox.addItem("Update Staff");
				thirdBox.addItem("Remove Staff");
			}
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
		secondBox.addActionListener(this);
		thirdBox.addActionListener(this);
	}

	private void disableActionListeners() {
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
		case (62):
			staffManager.fillDefaults(textFields[0].getText(), museumIDs[museumBox.getSelectedIndex()], username);
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
			artworkManager.createArtwork(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), textFields[3].getText(), museumIDs[museumBox.getSelectedIndex()], username);
			break;
		case (21): 
			artistManager.createArtist(textFields[0].getText(), textFields[1].getText(), textFields[2].getText());
			break;
		case (22):
			museumManager.createMuseum(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), username);
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
		case (50):
			artworkManager.listArtworkInExhibit(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), username);
			break;
		case (51):
			exhibitManager.addArtworkToExhibit(textFields[0].getText(), textFields[1].getText(), username);
			break;
		case (52):
			exhibitManager.removeArtworkFromExhibit(textFields[0].getText(), textFields[1].getText(), username);
			break;
		case (60):
			staffManager.listStaff(textFields[0].getText(), filterByRoles.isSelected(), staffRoles.getSelectedIndex(), username, museumIDs[museumBox.getSelectedIndex()], (admin || ((int) permissionLevels[museumBox.getSelectedIndex()] == 4)));
			break;
		case (61):
			staffManager.addStaff(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), staffRoles.getSelectedIndex(), newUser.isSelected(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (62):
			staffManager.updateStaff(textFields[0].getText(), textFields[1].getText(), textFields[2].getText(), staffRoles.getSelectedIndex(), museumIDs[museumBox.getSelectedIndex()]);
			break;
		case (63):
			staffManager.deleteStaff(textFields[0].getText(), textFields[1].getText(), museumIDs[museumBox.getSelectedIndex()]);
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
		case (50):
			this.setListArtworkInExhibit();
			break;
		case (51):
			this.setAddArtworkToExhibit();
			break;
		case (52):
			this.setAddArtworkToExhibit();
			break;
		case (60):
			if (update) {
				update = false;
				queryPanel.remove(defaultsButton);
			}
			this.setListStaff();
			break;
		case (61):
			if (update) {
				update = false;
				queryPanel.remove(defaultsButton);
			}
			this.setAddStaff();
			break;
		case (62):
			if (!update) {
			update = true;
				queryPanel.remove(executeButton);
				queryPanel.add(defaultsButton);
				queryPanel.add(executeButton);
			}
			this.setUpdateStaff();
			break;
		case (63):
			if (update) {
				update = false;
				queryPanel.remove(defaultsButton);
			}
			setRemoveStaff();
			break;
		default:
			System.out.println("OverallAction: " + overallAction);
		}
		this.repaint();
	}

	private void setRemoveStaff() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(staffUsernameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(staffNameLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setUpdateStaff() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(staffUsernameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(staffNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(staffPositionLabel);
		this.secondPanel.add(staffRoles);
		this.secondPanel.add(staffPasswordLabel);
		this.secondPanel.add(textFields[2]);
	}

	private void setAddStaff() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(staffUsernameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(staffNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(staffPositionLabel);
		this.secondPanel.add(staffRoles);
		this.secondPanel.add(newUser);
//		this.secondPanel.add(staffPasswordLabel);
//		this.secondPanel.add(textFields[2]);
	}

	private void setListStaff() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(staffNameLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(filterByRoles);
		this.secondPanel.add(staffPositionLabel);
		this.secondPanel.add(staffRoles);
	}

	private void setAddArtworkToExhibit() {
		this.resetTextFields(2);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkIDLabel);
		this.secondPanel.add(textFields[1]);
	}

	private void setListArtworkInExhibit() {
		this.resetTextFields(3);
		this.secondPanel.removeAll();
		this.secondPanel.add(exhibitIDLabel);
		this.secondPanel.add(textFields[0]);
		this.secondPanel.add(artworkNameLabel);
		this.secondPanel.add(textFields[1]);
		this.secondPanel.add(artistNameLabel);
		this.secondPanel.add(textFields[2]);
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
		staffNameLabel = new JLabel("Staff Name:");
		staffNameLabel.setFont(UIFrame.FONT);
		staffUsernameLabel = new JLabel("Staff Username:");
		staffUsernameLabel.setFont(UIFrame.FONT);
		staffPasswordLabel = new JLabel("Staff Password:");
		staffPasswordLabel.setFont(UIFrame.FONT);
		staffPositionLabel = new JLabel("Staff Role:");
		staffPositionLabel.setFont(UIFrame.FONT);
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

	public JComboBox getStaffRolesBox() {
		return staffRoles;
	}
}