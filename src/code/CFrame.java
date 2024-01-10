package code;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;



/*
 * TUTORIAL
 * Step 1. Add countries by dragging your cursor. 
 * Step 2. Add airport connections between countries. 
 * Step 3. Add first person. 
 * Step 4. Run simulation.
 * Note: you may pause the simulation by clicking on the "Run" button in the top-left corner. You may add additional countries and connections when the simulation is paused. 
 * Note: you may reset people but keep the countries and connections. 
 */

public class CFrame extends JPanel implements ActionListener{
	
	final int population = 100; 
	final static int INFECTION_RADIUS = 5;
	final int FPS = 55;
	int timer = 0;
	int countryCount = 0;
	int connectionCount = 0;
	int option;
	int lastConferenceTime = -1;
	
	final double initialInfectionProbability = 0.8;
	final double deceasedProbability = 0.03;
	final double recoverProbability = 0.003;
	
	boolean setAdjacencyList = false;
	boolean simulationRunning = false;
	boolean beginSimulation = false;
	boolean makePeople = false;
	boolean drawingConnection = false;
	boolean drawingCountry = false;
	boolean madeFirstPerson = false;
	
	boolean[][] connectionLookup = new boolean[100][100];
	
	JButton startSimulation;
	JButton addFirstPerson;
	JButton addCountry;
	JButton addConnection;
	JButton resetPeople;
	
	ArrayList<ArrayList<Integer>> adjacencyList = new ArrayList<>(100);
	ArrayList<Person> people = new ArrayList<Person>();
	ArrayList<Country> countries = new ArrayList<Country>();
	ArrayList<Connection> connections = new ArrayList<Connection>();
	
	MouseListener listener = new MouseListener();
		
	public static boolean infectionProximity(double x1, double x2, double y1, double y2) {
		return ((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1) <= INFECTION_RADIUS * INFECTION_RADIUS);
	}
	
	public static int random(int min, int max) {
		return (int)(Math.random() * (max - min + 1) + min);
	}
	
	public static void main(String[] arg) {
		CFrame c = new CFrame();
	}
	
	public CFrame() {
		JFrame frame = new JFrame("Pandemic Simulation");
		
		startSimulation = new JButton("Run"); 
		addFirstPerson = new JButton("Add First Person");
		addCountry = new JButton("Add Country"); 
		addConnection = new JButton("Add Connection");
		resetPeople = new JButton("Reset People"); 
		

		startSimulation.setBounds(0,0,100,50);
		addFirstPerson.setBounds(100, 0, 200, 50);
		addCountry.setBounds(300, 0, 100, 50);
		addConnection.setBounds(400, 0, 200, 50);
		resetPeople.setBounds(600, 0, 100, 50);
		
		startSimulation.addActionListener(this);
		addFirstPerson.addActionListener(this);
		addCountry.addActionListener(this);
		addConnection.addActionListener(this);
		resetPeople.addActionListener(this);
		
		frame.setSize(1000, 800);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		for(int i = 0; i < 100; i++) {
		    adjacencyList.add(new ArrayList());
		}
		
		Timer t = new Timer(15 , this);
		t.restart();
		
		frame.add(startSimulation);
		frame.add(addFirstPerson);
		frame.add(addConnection);
		frame.add(addCountry);
		frame.add(resetPeople);
		frame.add(this);
		
		frame.setVisible(true);
		
	}

	
	void updatePeople() {
		if(!makePeople) {
			for(int i = 1; i < population; i++) {
				Person newPerson = new Person();
				int countryNumber = random(0, countries.size() - 1);
				
				people.add(new Person());
				people.get(i).countryNumber = countryNumber;
				people.get(i).infectionProbability = initialInfectionProbability;
				people.get(i).timeLastInfected = 0;

				
				int BOX_TOP = countries.get(countryNumber).y1;
				int BOX_BOTTOM = countries.get(countryNumber).y2;
				int BOX_LEFT = countries.get(countryNumber).x1;
				int BOX_RIGHT = countries.get(countryNumber).x2;				

				people.get(i).x = random(BOX_LEFT + 50, BOX_RIGHT - 50);
				people.get(i).y = random(BOX_TOP + 50, BOX_BOTTOM - 50);
				
				people.get(i).BOX_BOTTOM = BOX_BOTTOM;
				people.get(i).BOX_TOP = BOX_TOP;
				people.get(i).BOX_LEFT = BOX_LEFT;
				people.get(i).BOX_RIGHT = BOX_RIGHT;
			}
			makePeople = true;
		}
		
		for(int i = 0; i < people.size(); i++) {
			if(people.get(i).x == 0)
				people.remove(i);
		}
		
		for(int i = 0; i < population; i++) {
			int infectedInCountry = 0, populationInCountry = 0;
			double travelProbability;
			
			travelProbability = (1.0 - percentageHealthy(people.get(i).countryNumber) + 0.20) / 100.0;
			
			if(!people.get(i).travellingToConference && !people.get(i).travelling && adjacencyList.get(people.get(i).countryNumber).size() > 0 && Math.random() <= travelProbability) {

				int newCountryNumber = adjacencyList.get(people.get(i).countryNumber).get(random(0, adjacencyList.get(people.get(i).countryNumber).size() - 1));
				
				if(percentageHealthy(newCountryNumber) < percentageHealthy(people.get(i).countryNumber) - 0.20)
					break;
				
				people.get(i).travelling = true;
				people.get(i).countryNumber = newCountryNumber;
				
				
				int BOX_TOP = countries.get(newCountryNumber).y1;
				int BOX_BOTTOM = countries.get(newCountryNumber).y2;
				int BOX_LEFT = countries.get(newCountryNumber).x1;
				int BOX_RIGHT = countries.get(newCountryNumber).x2;
				
				people.get(i).BOX_BOTTOM = BOX_BOTTOM;
				people.get(i).BOX_TOP = BOX_TOP;
				people.get(i).BOX_LEFT = BOX_LEFT;
				people.get(i).BOX_RIGHT = BOX_RIGHT;
				
				people.get(i).dx = ((BOX_LEFT + BOX_RIGHT) / 2 - people.get(i).x) / 50.0;
				people.get(i).dy = ((BOX_TOP + BOX_BOTTOM) / 2 - people.get(i).y) / 50.0;
			}
		}
		
		for(int i = 0; i < population; i++) {
			if(people.get(i).travelling && people.get(i).x >= people.get(i).BOX_LEFT + 15 && people.get(i).x <= people.get(i).BOX_RIGHT - 15 && people.get(i).y >= people.get(i).BOX_TOP + 15 && people.get(i).y <= people.get(i).BOX_BOTTOM - 15) {
				people.get(i).travelling = false;
				do{
					people.get(i).dx = random(-6, 6);
				} while(people.get(i).dx == 0);
				
				do {
					people.get(i).dy = random(-6, 6);
				} while(people.get(i).dy == 0);
			}
		}
		
		
		if(timer != 0 && timer % (5 * FPS) == 0) {
			int countryHoldingConference = random(0, countryCount);
			countries.get(countryHoldingConference).holdingConference = true;
			for(int i = 0; i < people.size(); i++) {
				if(people.get(i).countryNumber == countryHoldingConference && !people.get(i).travelling) {
					people.get(i).dx = ((people.get(i).BOX_LEFT + people.get(i).BOX_RIGHT) / 2 - people.get(i).x) / 50.0;
					people.get(i).dy = ((people.get(i).BOX_TOP + people.get(i).BOX_BOTTOM) / 2 - people.get(i).y) / 50.0;
					people.get(i).travellingToConference = true;
				}
			}
			lastConferenceTime = timer;
		}
		
		for(int i = 0; i < population; i++) {
			if(people.get(i).travellingToConference) {
				if(Math.abs(people.get(i).x - (people.get(i).BOX_LEFT + people.get(i).BOX_RIGHT) / 2) <= 15 && Math.abs(people.get(i).y - (people.get(i).BOX_TOP + people.get(i).BOX_BOTTOM) / 2) <= 15) {
					people.get(i).travellingToConference = false;
					do{
						people.get(i).dx = random(-6, 6);
					} while(people.get(i).dx == 0);
					
					do {
						people.get(i).dy = random(-6, 6);
					} while(people.get(i).dy == 0);
				}
					
			}
			
		}
		
		
		if(timer >= lastConferenceTime + 0.7 * FPS) {
			for(int i = 0; i < countries.size(); i++) {
				countries.get(i).holdingConference = false;
			}
		}	
		
		for(int i = 0; i < population; i++) {
			for(int j = i + 1; j < population; j++) {
				if(infectionProximity(people.get(i).x, people.get(j).x, people.get(i).y, people.get(j).y) && !people.get(i).travelling && !people.get(j).travelling) {
					if(people.get(i).infected && !people.get(j).infected && Math.random() < people.get(j).infectionProbability) {
						people.get(j).infected = true;
						people.get(j).timeLastInfected = timer;
					}
						
					if(people.get(j).infected && !people.get(i).infected && Math.random() < people.get(i).infectionProbability) {
						people.get(i).infected = true;
						people.get(i).timeLastInfected = timer;
					}
						
				}
			}
		}
		for(int i = 0; i < population; i++) {
			
			if(timer > people.get(i).timeLastInfected + 5 * FPS) {
				
				if(people.get(i).infected == true && Math.random() <= deceasedProbability) {
					people.get(i).deceased = true;
					people.get(i).infected = false;
				}
				
				if(people.get(i).infected == true) {
					if(people.get(i).infected && timer > people.get(i).timeLastInfected + 5 * FPS) {
						people.get(i).infected = false;
						people.get(i).infectionProbability *= 0.9;
					}
				}
			}
		}
		
		for(int i = 0; i < population; i++) {
			people.get(i).x += people.get(i).dx;
			people.get(i).y += people.get(i).dy;
		}
		
		
		int infected = 0, deceased = 0;
		for(int i = 0; i < population; i++) {
			infected += (people.get(i).infected ? 1 : 0);
			deceased += (people.get(i).deceased ? 1 : 0);
		}
		
		if((infected + deceased) * 10 >= population * 8 || infected == 0) { 
			simulationRunning = false;
		}

	}
	
	double percentageHealthy(int countryID) {
		int healthy = 0, total = 0;
		for(int i = 0; i < population; i++) {
			if(people.get(i).countryNumber == countryID && !people.get(i).deceased) {
				total++;
				if(!people.get(i).infected)
					healthy++;
			}
		}
		
		if(total == 0)
			return 1.0;
		
		return (double)healthy/(double)total;
	}

	
	void drawCountry() {
		option = 1;
		addMouseListener(listener);
        addMouseMotionListener(listener);
        Country newCountry = new Country();
        countries.add(newCountry);
	}
	
	
	void drawConnection() {
		option = 2;
        addMouseListener(listener);
        addMouseMotionListener(listener);
        Connection newConnection = new Connection();
        connections.add(newConnection);

	}
	
	
	void drawFirstPerson() {
		option = 3;
        addMouseListener(listener);
        addMouseMotionListener(listener);
		Person newPerson = new Person();
		people.add(new Person());
	}
	

	
	class MouseListener extends MouseAdapter {
		
        public void mousePressed(MouseEvent e) {
        	if(option == 1) {
        		countries.get(countryCount).x1 = e.getX();
            	countries.get(countryCount).y1 = e.getY();
            	drawingCountry = true;
        	} else if(option == 2) {
        		connections.get(connectionCount).x1 = e.getX();
                connections.get(connectionCount).y1 = e.getY();
                drawingConnection = true;
        	} 
        }
        
        public void mouseDragged(MouseEvent e) {
        	if(option == 1) {
        		countries.get(countryCount).x2 = e.getX();
        		if(countries.get(countryCount).x2 < 0)
        			countries.get(countryCount).x2 = 0;
        		else if(countries.get(countryCount).x2 > 1000)
        			countries.get(countryCount).x2 = 1000;
        		
            	countries.get(countryCount).y2 = e.getY();
            	if(countries.get(countryCount).y2 < 50)
        			countries.get(countryCount).y2 = 50;
        		else if(countries.get(countryCount).y2 > 800)
        			countries.get(countryCount).y2 = 800;
            	
                repaint();
        	} else if(option == 2) {
        		connections.get(connectionCount).x2 = e.getX();
                connections.get(connectionCount).y2 = e.getY();
            	repaint();
        	}
        	
        }
        
        public void mouseReleased(MouseEvent e) {
        	if(option == 1) {
        		countries.get(countryCount).x2 = e.getX();
            	countries.get(countryCount).y2 = e.getY();
            	
            	int x1 = Math.min(countries.get(countryCount).x1, countries.get(countryCount).x2);
            	int x2 = Math.max(countries.get(countryCount).x1, countries.get(countryCount).x2);
            	int y1 = Math.min(countries.get(countryCount).y1, countries.get(countryCount).y2);
            	int y2 = Math.max(countries.get(countryCount).y1, countries.get(countryCount).y2);

            	countries.get(countryCount).x1 = Math.max(0, x1);
            	countries.get(countryCount).y1 = Math.max(50, y1);
            	countries.get(countryCount).x2 = Math.min(1000, x2);
            	countries.get(countryCount).y2 = Math.min(800, y2);
            	

            	boolean valid = true;
            	
            	if(Math.abs(countries.get(countryCount).x1 - countries.get(countryCount).x2) <= 50 || Math.abs(countries.get(countryCount).y1 - countries.get(countryCount).y2) < 50) {
            		valid = false;
            		
            	} 
            	
            	for(int i = 0; i < countries.size() - 1; i++) {
            		if(countries.get(i).x1 < countries.get(countryCount).x2 && countries.get(countryCount).x1 < countries.get(i).x2 && countries.get(i).y1 < countries.get(countryCount).y2 && countries.get(countryCount).y1 < countries.get(i).y2) {
            			valid = false;
            		}
            	}
            	
            	if(valid) {
            		countryCount++;
            	} else {
            		countries.remove(countryCount);
            		repaint();
            		return;
            	}
            	
        	} else if(option == 2) {
        		connections.get(connectionCount).x2 = e.getX();
                connections.get(connectionCount).y2 = e.getY();
                int country1 = getCountryID(connections.get(connectionCount).x1, connections.get(connectionCount).y1);
                int country2 = getCountryID(connections.get(connectionCount).x2, connections.get(connectionCount).y2);
                if(country1 != -1 && country2 != -1 && country1 != country2 && !connectionLookup[country1][country2] && !connectionLookup[country2][country1]) {
                	connectionCount++;
                	connectionLookup[country1][country2] = true;
                	connectionLookup[country2][country1] = true;
                	adjacencyList.get(country1).add(country2);
                	adjacencyList.get(country2).add(country1);
                } else {
                	connections.remove(connectionCount);
                }
                drawingConnection = false;
                repaint();
        	} else if(option == 3) {
        		people.get(0).x = e.getX();
        		people.get(0).y = e.getY();

        		int countryNumber = getCountryID(e.getX(), e.getY());
        		
        		if(countryNumber != -1) {
        			people.get(0).countryNumber = countryNumber;

            		int BOX_TOP = countries.get(countryNumber).y1;
            		int BOX_BOTTOM = countries.get(countryNumber).y2;
            		int BOX_LEFT = countries.get(countryNumber).x1;
            		int BOX_RIGHT = countries.get(countryNumber).x2;				

            		people.get(0).BOX_BOTTOM = BOX_BOTTOM;
            		people.get(0).BOX_TOP = BOX_TOP;
            		people.get(0).BOX_LEFT = BOX_LEFT;
            		people.get(0).BOX_RIGHT = BOX_RIGHT;
            		people.get(0).infected = true;
            		people.get(0).source = true;
            		madeFirstPerson = true;
            		repaint();
        		} else {
        			people.remove(0);
        		}
        		drawingCountry = false;
        	}
        }
    }
	
	public int getCountryID(int x, int y) {
		for(int i = 0; i < countries.size(); i++) {
			if(x >= countries.get(i).x1 && x <= countries.get(i).x2 && y >= countries.get(i).y1 && y <= countries.get(i).y2)
				return i;
		}
		return -1;
	}
	
	boolean stronglyConnected(ArrayList<Country> countries, ArrayList<ArrayList<Integer>> adjacencyList) {
		
		boolean[] visited = new boolean[countries.size()];
		int[] queue = new int[countries.size()];
		queue[0] = 0;
		int next = 1;
		for(int i = 0; i < queue.length; i++) {
			visited[queue[i]] = true;
			for(int x = 0; x < adjacencyList.get(queue[i]).size(); x++) {
				if(!visited[adjacencyList.get(queue[i]).get(x)]) {
					queue[next++] = adjacencyList.get(queue[i]).get(x);
					visited[adjacencyList.get(queue[i]).get(x)] = true; 
				}
			}
		}
		
		boolean ok = true;
		for(int i = 0; i < visited.length; i++) {
			ok &= visited[i];
		}
		return ok;
	}
	

	public void paint(Graphics g) {
		for(Country c : countries) {
			c.display(g);
		}
		
		for(Connection c : connections) {
			c.display(g);
		}
		
		if(madeFirstPerson && !beginSimulation) {
			people.get(0).display(g);
		}
		
		if(beginSimulation) {
			if(!drawingConnection && !drawingCountry) {
				updatePeople();
				timer++;
			}
			for(Person p : people) {
				p.display(g);
			}
		}
		
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		if(simulationRunning) 
			repaint();
		
		if(e.getSource() == startSimulation) {
			if(stronglyConnected(countries, adjacencyList) && madeFirstPerson) {
				simulationRunning ^= true;
				
				beginSimulation = true;
			}
		}
		
		if(e.getSource() == addCountry) {
			if(!beginSimulation && countries.size() < 100) {
				drawCountry();
				repaint(); 
			}
		}
		
		if(e.getSource() == addFirstPerson) {
			if(!simulationRunning && people.size() == 0)
				drawFirstPerson();
			repaint();
		}
		
		if(e.getSource() == addConnection) {
			if(!beginSimulation)
				drawConnection();
			repaint();
		}
		
		if(e.getSource() == resetPeople) {
			while(people.size() > 0)
				people.remove(0);
			
			makePeople = false;
			madeFirstPerson = false;
			beginSimulation = false;
			simulationRunning = false;
			repaint();
		}
	}
}

