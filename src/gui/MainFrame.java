package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;


import distances.Distance;
import distances.EuclideanDistance;
import distances.GPSDistance;



public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	Box centerBox, visualBox, trajectory1Box, trajectory2Box, configurationBox;
	TrajectoryPanel trajectoryPanel;
	FreeSpacePanel freeSpacePanel;
	ReachableFreeSpacePanel reachableFreeSpacePanel;
	JFileChooser fileChooser;
	JTextField trajectory1Path;
	JTextField trajectory2Path;
	JButton openTrajectory1Button;
	JButton openTrajectory2Button;
	JTextField tfEpsilon;
	JButton compute;
	Distance distance = new GPSDistance();
	File trajectory1File = new File("P.txt"); 
	File trajectory2File = new File("Q.txt"); 
	JButton animation;
	/**
	 * This is the default constructor
	 */
	public MainFrame() {
		super();
		try {
			initialize();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SAXException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 * @throws SAXException 
	 * @throws ParserConfigurationException 
	 * @throws IOException 
	 */
	private void initialize() throws IOException, ParserConfigurationException, SAXException {
		this.setSize(700, 400);
		this.setLocation(200, 100);
		this.setContentPane(getJContentPane());
		// TrajectoryPanel simulator.simulator.enviroment = new RectangleVisualEnviroment(
		// (RectangleRFIDEnvironment) simulator.simulator.getEnviroment());
		// getContentPane().add(simulator.simulator.enviroment);
		centerBox = Box.createVerticalBox();
		//centerPane = new JPanel(new BoxLayout(null, BoxLayout.Y_AXIS));
		//algorithmPane = new JPanel(new BoxLayout(null, BoxLayout.X_AXIS));
		visualBox = Box.createHorizontalBox();
		
		trajectoryPanel = new TrajectoryPanel();
		trajectoryPanel.setBorder(new TitledBorder("Trajectories View"));
		
		freeSpacePanel = new ConstrainedFreeSpacePanel();
		freeSpacePanel.setBorder(new TitledBorder("Free Space"));

		reachableFreeSpacePanel = new ReachableFreeSpacePanel();
		reachableFreeSpacePanel.setBorder(new TitledBorder("Reachable Free Space"));
		
		visualBox.add(trajectoryPanel);
		visualBox.add(freeSpacePanel);
		visualBox.add(reachableFreeSpacePanel);
		
		centerBox.add(visualBox);
		
		JPanel trajectories = new JPanel();
		trajectories.setBorder(new TitledBorder("Loaded Trajectories"));
		Box trajectoeisPanelBox =Box.createHorizontalBox();
		trajectories.add(trajectoeisPanelBox);
		
		trajectory1Box = Box.createVerticalBox();
		trajectory2Box = Box.createVerticalBox();
		
		trajectoeisPanelBox.add(trajectory1Box);
		trajectoeisPanelBox.add(trajectory2Box);
		
		fileChooser = new JFileChooser();
		trajectory1Path = new JTextField(trajectory1File.getPath());
		trajectory2Path = new JTextField(trajectory2File.getPath());
		trajectory1Path.setPreferredSize(new Dimension(300, 20));
		trajectory2Path.setPreferredSize(new Dimension(300, 20));
		
		openTrajectory1Button = new JButton("...");
		trajectoryPanel.setDistance(new GPSDistance());
		trajectoryPanel.setFirstTrajectory(trajectory1File);
		trajectoryPanel.setSecondTrajectory(trajectory2File);
		openTrajectory2Button = new JButton("...");
		
		fileChooser.setFileFilter(new FileFilter() {
			
			@Override
			public String getDescription() {
				return "txt format";
			}
			
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) return true;
				if (f.getName().length() < 4) return false;
				return f.getName().substring(f.getName().length()-4).equals(".txt");
			}
		});
		openTrajectory1Button.addActionListener(new OpenTrajectoryAction(trajectory1Path, this));
		openTrajectory2Button.addActionListener(new OpenTrajectoryAction(trajectory2Path, this));
		
		trajectory1Box.add(trajectory1Path);
		trajectory1Box.add(trajectory2Path);
		
		trajectory2Box.add(openTrajectory1Button);
		trajectory2Box.add(openTrajectory2Button);
		
		JPanel configurationsPanel = new JPanel();
		configurationsPanel.setLayout(new GridLayout(3, 1));
		configurationsPanel.setBorder(new TitledBorder("Configuration"));
		//Box configurationPanelBox = Box.createHorizontalBox();
		//configurationsPanel.add(configurationPanelBox);
		
		ButtonGroup distances = new ButtonGroup();
		JRadioButton euclideanDistance = new JRadioButton("Euclidean distance");
		JRadioButton gpsDistance = new JRadioButton("GPS distance", true);
		euclideanDistance.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				distance = new EuclideanDistance();
				trajectoryPanel.setDistance(distance);
				repaint();
				System.out.println("Euclidean");
			}
		});
		gpsDistance.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				distance = new GPSDistance();
				trajectoryPanel.setDistance(distance);
				repaint();
				System.out.println("GPS");
			}
		});
		distances.add(euclideanDistance);
		distances.add(gpsDistance);
		
		//Box radioBox = Box.createVerticalBox();
		
		configurationsPanel.add(euclideanDistance);
		configurationsPanel.add(gpsDistance);
	

		Box epsilonBox = Box.createHorizontalBox();
		
		epsilonBox.add(new JLabel(" Epsilon "));
		tfEpsilon = new JTextField("2.01");
		//epsilon.setSize(new Dimension(20, 20));
		tfEpsilon.setPreferredSize(new Dimension(10, 20));
		epsilonBox.add(tfEpsilon);
		
		configurationsPanel.add(epsilonBox);
		
		compute = new JButton("Compute Free Space");
		compute.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				double epsilon = 0;
				try{
					epsilon = Double.parseDouble(tfEpsilon.getText());
				}catch(NumberFormatException exc){
					JOptionPane.showMessageDialog((JButton)e.getSource(), "A valid epsilon value is required", "Error message", 
							JOptionPane.ERROR_MESSAGE);
				}
				freeSpacePanel.computeFreeSpace(trajectoryPanel.trajectory1, 
						trajectoryPanel.trajectory2, epsilon);
				reachableFreeSpacePanel.computeFreeSpace(freeSpacePanel.getFreeSpace());
				animation.setEnabled(true);
			}
		});
		
		//animationPanel.setVisible(false);
		//animationPanel.setBackground(Color.black);
		
		
		animation = new JButton("Animate/Stop");
		animation.setEnabled(false);
		animation.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trajectoryPanel.isAnimating()){
					trajectoryPanel.setAnimation(false);
				}
				else{
					double[][] trajectories = reachableFreeSpacePanel.computeMonotoneCurve();
					trajectoryPanel.setAnimation(trajectories);
					trajectoryPanel.setAnimation(true);
				}
			}
		});
		//configurationPanelBox.add(radioBox);
		
		Box confBox = Box.createHorizontalBox();
		
		
		JPanel executionPanel = new JPanel();
		executionPanel.setBorder(new TitledBorder("Executions"));
		executionPanel.setLayout(new GridLayout(2, 1));
		
		executionPanel.add(compute);
		executionPanel.add(animation);
		
		confBox.add(trajectories);
		confBox.add(configurationsPanel);
		confBox.add(executionPanel);
		
		centerBox.add(confBox);
		
		getContentPane().add(centerBox);
		this.setTitle("JFrame");
	}

	class OpenTrajectoryAction implements ActionListener{
		JTextField path;
		JFrame frame;
		
		public OpenTrajectoryAction(JTextField path, JFrame frame){
			this.path = path;
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			
            int returnVal = fileChooser.showOpenDialog(frame);
            

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                path.setText(file.getPath());
                try{
	                if (e.getSource().equals(openTrajectory1Button))
	                	trajectoryPanel.setFirstTrajectory(file);
	                else trajectoryPanel.setSecondTrajectory(file);
                }catch(Exception exc){
                	JOptionPane.showMessageDialog(frame, exc.toString(), 
                			"Error opening trajectory file", JOptionPane.ERROR_MESSAGE);
                }
            }
            
		}
		
	}
	/**
	 * This method initializes jContentPane
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getJContentPane() {
		if (jContentPane == null) {
			jContentPane = new JPanel();
			jContentPane.setLayout(new BorderLayout());
		}
		return jContentPane;
	}

}
