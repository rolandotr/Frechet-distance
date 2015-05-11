package gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;

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

import algorithms.frechet.FrechetCentroideMethod;

import clustering.FixedUtilityCriterion;
import clustering.MyTrajectoryFormat;

import util.Anonymization;
import util.Interpolation;
import wrappers.GPSFormat;
import wrappers.Trajectory;


import distances.Distance;
import distances.EuclideanDistance;
import distances.FrechetDistance;
import distances.FrechetDistanceEuclideanBased;
import distances.FrechetDistanceGPSBased;
import distances.JosepEuclideanDistanceOnTheFly;
import distances.Transformation;
import distances.GPSDistance;
import distances.JosepGPSDistanceOnTheFly;



public class MainFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel jContentPane = null;
	Box centerBox, visualBox, trajectory1Box, trajectory2Box, configurationBox;
	TrajectoryPanel trajectoryPanel;
	TrajectoryPanel anonymizedTrajectoryPanel;
	FreeSpacePanel freeSpacePanel;
	ReachableFreeSpacePanel reachableFreeSpacePanel;
	JFileChooser fileChooser;
	JTextField trajectory1Path;
	JTextField trajectory2Path;
	JButton openTrajectory1Button;
	JButton openTrajectory2Button;
	JTextField tfEpsilon;
	JButton compute;
	
	
	//Distance distance = new GPSDistance();
	
	//trajectories mas cercanas respecto a la distancia Euclideana.
	//File trajectory1File = new File("1672.txt"); 
	//File trajectory2File = new File("1979.txt");

	//trajectories mas cercanas respecto a la distancia Josep.
	//File trajectory1File = new File("1157.txt"); 
	//File trajectory2File = new File("1285.txt");

	//trajectories mas cercanas respecto a la distancia Frechet constrained.
	//File trajectory1File = new File("1442.txt"); 
	//File trajectory2File = new File("1277.txt");

	
	//trajectorias de prueba que son mas o menos cercanas
	//File trajectory1File = new File("1463.txt"); 
	//File trajectory2File = new File("1349.txt");

	//File trajectory1File = new File("1093.txt"); 
	//File trajectory2File = new File("1113.txt");
	
	//File trajectory1File = new File("1449.txt"); 
	//File trajectory2File = new File("1486.txt");
	
	//File trajectory1File = new File("1463.txt");
	//File trajectory2File = new File("1644.txt"); 
	
	//File trajectory1File = new File("P.txt"); 
	//File trajectory2File = new File("Q.txt");
	
	File trajectory1File = new File("tmp1.txt"); 
	File trajectory2File = new File("tmp2.txt"); 
	//File trajectory2File = new File("new_oilrag.txt");
	//File trajectory1File = new File("new_oilrag.txt");
	
	JButton animation;
	JButton doAll;
		
	JButton anonymizeFrechetButton;
	JButton anonymizeNWAButton;
	JButton anonymizePermutationButton;

	Distance distance = new EuclideanDistance();
	//FrechetDistance frechetDistance = new FrechetDistanceEuclideanBased();
	FrechetDistance frechetDistance = new FrechetDistanceGPSBased();
	
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
		
		anonymizedTrajectoryPanel = new TrajectoryPanel();
		anonymizedTrajectoryPanel.setBorder(new TitledBorder("Anonymized Trajectories"));
		
		trajectoryPanel = new TrajectoryPanel();
		trajectoryPanel.setBorder(new TitledBorder("Original trajectories"));
		
		freeSpacePanel = new ConstrainedFreeSpacePanel(frechetDistance);
		freeSpacePanel.setBorder(new TitledBorder("Free Space"));

		reachableFreeSpacePanel = new ReachableFreeSpacePanel();
		reachableFreeSpacePanel.setBorder(new TitledBorder("Reachable Free Space"));
		
		visualBox.add(trajectoryPanel);
		visualBox.add(freeSpacePanel);
		visualBox.add(reachableFreeSpacePanel);
		visualBox.add(anonymizedTrajectoryPanel);
		
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
		fileChooser.setCurrentDirectory(new File(".\\"));
		trajectory1Path = new JTextField(trajectory1File.getPath());
		trajectory2Path = new JTextField(trajectory2File.getPath());
		trajectory1Path.setPreferredSize(new Dimension(300, 20));
		trajectory2Path.setPreferredSize(new Dimension(300, 20));
		
		openTrajectory1Button = new JButton("...");
		trajectoryPanel.setDistance(distance);
		anonymizedTrajectoryPanel.setDistance(distance);
		//trajectoryPanel.setDistance(new GPSDistance());
		trajectoryPanel.setFirstTrajectory(trajectory1File);
		trajectoryPanel.setSecondTrajectory(trajectory2File);
		
		/*Trajectory t1 = trajectoryPanel.getFirstTrajectory();
		Trajectory t2 = trajectoryPanel.getSecondTrajectory();
		for (int i = 47; i <= 100; i++){
			GPSFormat p1 = t1.getPoint(i);
			GPSFormat p2 = t2.getPoint(i);
			if (p2 == null) p2 = Interpolation.interpolate(t2, i);
			if (!p1.equals(p2)) throw new RuntimeException("i = "+i+" p1 = "+p1+", p2 = "+p2);
		}*/
		
		//anonymizedTrajectoryPanel.setFirstTrajectory(trajectory1File);
		//anonymizedTrajectoryPanel.setSecondTrajectory(trajectory2File);
		
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
		JRadioButton euclideanDistance = new JRadioButton("Euclidean distance", true);
		JRadioButton gpsDistance = new JRadioButton("GPS distance", true);
		euclideanDistance.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				distance = new EuclideanDistance();
				trajectoryPanel.setDistance(distance);
				frechetDistance = new FrechetDistanceEuclideanBased();
				anonymizedTrajectoryPanel.setDistance(distance);
				repaint();
				System.out.println("Euclidean");
			}
		});
		gpsDistance.addActionListener(new ActionListener() {			
			@Override
			public void actionPerformed(ActionEvent e) {
				distance = new GPSDistance();
				trajectoryPanel.setDistance(distance);
				frechetDistance = new FrechetDistanceGPSBased();
				anonymizedTrajectoryPanel.setDistance(distance);
				repaint();
				System.out.println("GPS");
			}
		});
		distances.add(euclideanDistance);
		//euclideanDistance.setEnabled(false);
		distances.add(gpsDistance);
		
		//Box radioBox = Box.createVerticalBox();
		
		configurationsPanel.add(euclideanDistance);
		configurationsPanel.add(gpsDistance);
	

		Box epsilonBox = Box.createHorizontalBox();
		
		epsilonBox.add(new JLabel(" Epsilon "));
		tfEpsilon = new JTextField("201");
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
				reachableFreeSpacePanel.computeFreeSpace(freeSpacePanel.getFreeSpace(), 
						frechetDistance);
				animation.setEnabled(true);
				//anonymizeButton.setEnabled(true);
				/*System.out.println(frechetDistance.distance(trajectoryPanel.trajectory1, 
						trajectoryPanel.trajectory2));*/
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
					double[][] trajectories = 
						reachableFreeSpacePanel.computeMonotoneCurve(frechetDistance);
					if (trajectories == null){
	                	JOptionPane.showMessageDialog(null, "The decision problem response is false in this case", 
	                			"Invalid epsilon value", JOptionPane.ERROR_MESSAGE);
	                	return;
					}
					/*System.out.println("Analyzing trajectory 1");
					for (int i = 0; i < trajectories[0].length; i++) {
						System.out.println("time = "+trajectories[0][i]);
						if (trajectoryPanel.trajectory1.timeOutOfInterval(trajectories[0][i]))
							throw new RuntimeException("i = "+i);
					}*/
					/*System.out.println("Analyzing trajectory 2");
					for (int i = 0; i < trajectories[1].length; i++) {
						System.out.println("time = "+trajectories[1][i]);
						if (trajectoryPanel.trajectory2.timeOutOfInterval(trajectories[1][i]))
							throw new RuntimeException("i = "+i);
					}*/
					trajectoryPanel.setAnimation(trajectories);
					trajectoryPanel.setAnimation(true);
				}
			}
		});
		
		anonymizeFrechetButton = new JButton("Frechet-based");
		//anonymizeFrechetButton.setEnabled(false);
		anonymizeFrechetButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				FrechetCentroideMethod algorithmFrechet = new FrechetCentroideMethod("real", frechetDistance);
				List<Trajectory> dataset = new LinkedList<Trajectory>();
				dataset.add(trajectoryPanel.trajectory1);
				dataset.add(trajectoryPanel.trajectory2);
				List<List<Trajectory>> database = new LinkedList<List<Trajectory>>();
				database.add(dataset);
				List<Trajectory> dAnonym = algorithmFrechet.anonymizeAll(database);				
				anonymizedTrajectoryPanel.setFirstTrajectory(dAnonym.get(0));
				anonymizedTrajectoryPanel.setSecondTrajectory(dAnonym.get(1));
				
			}
		});
		
		anonymizeNWAButton = new JButton("Frechet-Based-Centroide");
		anonymizeNWAButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				/*List<Trajectory> dataset = new LinkedList<Trajectory>();
				dataset.add(trajectoryPanel.trajectory1);
				dataset.add(trajectoryPanel.trajectory2);
				NWA algorithmNWA = new NWA();				
				List<Trajectory> dAnonym = algorithmNWA.exeNWA(dataset, 2, 0, 3);
				if (dAnonym == null) return;
				if (dAnonym.size() >= 1)
					anonymizedTrajectoryPanel.setFirstTrajectory(dAnonym.get(0));
				if (dAnonym.size() == 2)
					anonymizedTrajectoryPanel.setSecondTrajectory(dAnonym.get(1));*/
				FrechetCentroideMethod algorithmFrechet = new FrechetCentroideMethod("real", frechetDistance);
				List<Trajectory> dataset = new LinkedList<Trajectory>();
				dataset.add(trajectoryPanel.trajectory1);
				dataset.add(trajectoryPanel.trajectory2);
				List<List<Trajectory>> database = new LinkedList<List<Trajectory>>();
				database.add(dataset);
				List<Trajectory> dAnonym = algorithmFrechet.anonymizeAll(database);
				//List<Trajectory> dAnonym = algorithmFrechet.anonymizesWithoutDistortion(dataset, 2, frechetDistance, false);
				
				anonymizedTrajectoryPanel.setFirstTrajectory(dAnonym.get(0));
				anonymizedTrajectoryPanel.setSecondTrajectory(dAnonym.get(1));

				
			}
		});
		
		anonymizePermutationButton = new JButton("Permutation-based");
		anonymizePermutationButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				Hashtable<String, Trajectory> dataset = new Hashtable<String, Trajectory>();
				
				dataset.put(trajectoryPanel.trajectory1.getIdentifier(), 
						(Trajectory)trajectoryPanel.trajectory1);
				dataset.put(trajectoryPanel.trajectory2.getIdentifier(), 
						(Trajectory)trajectoryPanel.trajectory2);
				
				/*dataset.put(trajectoryPanel.trajectory1.getIdentifier(), 
						(Trajectory)trajectoryPanel.trajectory1.clone());
				dataset.put(trajectoryPanel.trajectory2.getIdentifier(), 
						(Trajectory)trajectoryPanel.trajectory2.clone());*/
				
				List<List<String>> clusters = new LinkedList<List<String>>();
				List<String> cluster = new LinkedList<String>();
				cluster.add(trajectoryPanel.trajectory1.getIdentifier());
				cluster.add(trajectoryPanel.trajectory2.getIdentifier());
				clusters.add(cluster);
				//Distance ourDist = new OurEuclideanDistanceOnTheFly(dataset.size());
				Distance ourDist = new JosepGPSDistanceOnTheFly(dataset.size());

				List<Trajectory> dAnonym = (Anonymization.journalAnonymizationMethod4(dataset, 
						2, new FixedUtilityCriterion(Double.MAX_VALUE, ourDist), ourDist, clusters));
				
				try {
					trajectoryPanel.setSecondTrajectory(trajectory2File);
					trajectoryPanel.setFirstTrajectory(trajectory1File);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (ParserConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SAXException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				if (dAnonym == null) return;
				if (dAnonym.size() >= 1)
					anonymizedTrajectoryPanel.setFirstTrajectory(dAnonym.get(0));
				if (dAnonym.size() == 2)
					anonymizedTrajectoryPanel.setSecondTrajectory(dAnonym.get(1));
				
			}
		});
		
		doAll = new JButton("Compute Frechet distance");
		doAll.setEnabled(true);
		doAll.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				if (trajectoryPanel.isAnimating()){
					trajectoryPanel.setAnimation(false);
				}
				Transformation transformation = frechetDistance.distanceWithTransformationOptimized(trajectoryPanel.trajectory1, 
						trajectoryPanel.trajectory2);
				//Transformation transformation = frechetDistance.distanceWithTransformation(trajectoryPanel.trajectory1, 
						//trajectoryPanel.trajectory2);
				if (transformation == null){
                	JOptionPane.showMessageDialog(null, "The decision problem response is false in this case", 
                			"Invalid epsilon value", JOptionPane.ERROR_MESSAGE);
                	return;
				}
				//tfEpsilon.setText(""+((long)transformation.distance+1));
				tfEpsilon.setText(""+(transformation.distance));
				//tfEpsilon.setText(""+Math.ceil(transformation.distance));
			}
		});
		//configurationPanelBox.add(radioBox);
		
		Box confBox = Box.createHorizontalBox();
		
		
		JPanel executionPanel = new JPanel();
		executionPanel.setBorder(new TitledBorder("Executions"));
		executionPanel.setLayout(new GridLayout(3, 1));
		
		executionPanel.add(compute);
		executionPanel.add(animation);
		executionPanel.add(doAll);
		
		JPanel anonymizePanel = new JPanel();
		anonymizePanel.setBorder(new TitledBorder("Anonymization"));
		anonymizePanel.setLayout(new GridLayout(3, 1));
		
		anonymizePanel.add(anonymizeFrechetButton);
		anonymizePanel.add(anonymizeNWAButton);
		anonymizePanel.add(anonymizePermutationButton);

		confBox.add(trajectories);
		confBox.add(configurationsPanel);
		confBox.add(executionPanel);
		confBox.add(anonymizePanel);
		
		
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
