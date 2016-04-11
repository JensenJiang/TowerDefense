import java.awt.*;
import java.util.*;
import javax.swing.*;

public class UI {
	JFrame frame;
	JPanel panel;
	
	JPanel statusBar;
	JLayeredPane gameMap;
	JPanel controlPanel;
	
	JLabel goldLabel, levelLabel, nextTLabel, healthLabel;

	int placingTower;
	JLabel[] towerButton;
	JLabel[][] mapBlocks;
	
	private static Integer GROUND_LAYER = 10;
	private static Integer TOWER_LAYER = 20;
	private static Integer MONSTER_LAYER = 30;
	private static Integer SHELL_LAYER = 40;
	private static Integer UI_LAYER = 90;
	private int ROW = 7;
	private int COL = 15;
	private int GRID_SIZE = 60;
	private int UNIT_SIZE = 50;
	private int GAP_SIZE = 15;
	private int SHELL_SIZE = 15;

	// return the center coordinate of grid(p(x, y));
	public Point gridToCoordinate(Point p)
	{
		int x = p.y * GRID_SIZE + GRID_SIZE / 2;
		int y = p.x * GRID_SIZE + GRID_SIZE / 2;
		return new Point(x, y);
	}
	
	public void update() {
		// Clear all the game objects
		final Integer[] volatileLayers = {TOWER_LAYER, MONSTER_LAYER, SHELL_LAYER, UI_LAYER - 1, UI_LAYER, UI_LAYER + 1};
		
		Component[] toRefresh;
		for(int i = 0; i < volatileLayers.length; i++)
		{
			toRefresh = gameMap.getComponentsInLayer(volatileLayers[i]);
			for(int j = 0; j < toRefresh.length; j++)
			{
				gameMap.remove(toRefresh[j]);
			}
		}
		
		// Show Towers
		for(Iterator<Tower> it = TowerDefense.towers.iterator(); it.hasNext();)
		{
			Tower t = it.next();
			Point _pos = gridToCoordinate(new Point(t.getX(), t.getY()));
			gameMap.add(t.label, TOWER_LAYER);
			t.label.setBounds(
					_pos.x - UNIT_SIZE / 2, 
					_pos.y - UNIT_SIZE / 2, 
					UNIT_SIZE, 
					UNIT_SIZE
			);
			t.label.setOpaque(true);
		}
		
		// Show Monsters
		for(Iterator<Monster> it = TowerDefense.mons.iterator(); it.hasNext();)
		{
			Monster t = it.next();
			gameMap.add(t.label, MONSTER_LAYER);
			t.label.setBounds(
					(int)t.x - UNIT_SIZE / 2, 
					(int)t.y - UNIT_SIZE / 2, 
					UNIT_SIZE, 
					UNIT_SIZE
			);
			t.label.setOpaque(true);
			/*
			JLabel totalHPLabel = new JLabel();
			gameMap.add(totalHPLabel, UI_LAYER);
			totalHPLabel.setBackground(Color.gray);
			totalHPLabel.setBounds(
					(int)t.x - UNIT_SIZE / 2, 
					(int)t.y - UNIT_SIZE / 2 - GAP_SIZE * 2, 
					UNIT_SIZE, 
					GAP_SIZE
			);
			totalHPLabel.setOpaque(true);
			*/
			JLabel curHPLabel = new JLabel();
			gameMap.add(curHPLabel, (Integer)(UI_LAYER + 1));
			curHPLabel.setBackground(Color.red);
			curHPLabel.setBounds(
					(int)t.x - UNIT_SIZE / 2, 
					(int)t.y - UNIT_SIZE / 2 - GAP_SIZE * 2, 
					UNIT_SIZE * t.getCurHP() / t.getTotalHP(), 
					GAP_SIZE
			);
			curHPLabel.setOpaque(true);
		}
		
		//Show Shells
		for(Iterator<Shell> it = TowerDefense.shells.iterator(); it.hasNext();)
		{
			Shell t = it.next();
			gameMap.add(t.label, MONSTER_LAYER);
			t.label.setBounds(
					(int)t.x - SHELL_SIZE / 2, 
					(int)t.y - SHELL_SIZE / 2, 
					SHELL_SIZE, 
					SHELL_SIZE
			);
			t.label.setOpaque(true);
		}
		
		// Update labels
		goldLabel.setText("Gold : " + TowerDefense.gold);
		levelLabel.setText("Level : " + TowerDefense.level);
		nextTLabel.setText("Next Wave in : " + TowerDefense.nextT + "secs");
		healthLabel.setText("Health : " + TowerDefense.health);
		
		// Placing Tower
		if(placingTower > 0)
		{
			for(int i = 0; i < ROW; i++)
				for(int j = 0; j < COL; j++)
				{
					if(TowerDefense.vacant[i][j] == 0)
					{
						Point cen = gridToCoordinate(new Point(i, j));
						JLabel here = new JLabel(new ImageIcon("placeHere.png"));
						gameMap.add(here, (Integer)(UI_LAYER - 1));
						here.setBounds(cen.x - UNIT_SIZE / 2, cen.y - UNIT_SIZE / 2, UNIT_SIZE, UNIT_SIZE);
						here.setOpaque(true);
					}
				}
		}
		gameMap.repaint();
	}
	
	UI()
	{
		frame = new JFrame();
		frame.setSize(900, 600);
		// frame.setBackground(Color.YELLOW);

		panel = (JPanel)frame.getContentPane();
		//panel.setOpaque(false);
		//frame.getContentPane().setBackground(Color.BLACK);
		panel.setSize(900, 600);
		panel.setOpaque(true);
		panel.setLayout(null);
		panel.setBackground(Color.WHITE);

		statusBar = new JPanel();
		statusBar.setLayout(null);
		statusBar.setBackground(new Color(200, 200, 200));
		statusBar.setBounds(0, 0, 900, 60);
		statusBar.setOpaque(true);

		controlPanel = new JPanel();
		controlPanel.setLayout(null);
		controlPanel.setBackground(new Color(200, 200, 200));
		controlPanel.setBounds(0, 480, 900, 200);
		controlPanel.setOpaque(true);

		int ttc = TowerDefense.towerTypeCount;
		towerButton = new JLabel[ttc];
		for(int i = 0; i < ttc; i++)
		{
			towerButton[i] = new JLabel("TOWER" + i);
			controlPanel.add(towerButton[i]);
			towerButton[i].setBounds((2 * i + 1) * 900 / (2 * ttc + 1), 20, 900 / (2 * ttc + 1), 50);
			towerButton[i].setBackground(new Color(150, 150, 150));
			towerButton[i].setOpaque(true);
		}

		gameMap = new JLayeredPane();
		gameMap.setLayout(null);
		gameMap.setBackground(new Color(100, 100, 100));
		gameMap.setBounds(0, 60, 900, 420);
		gameMap.setOpaque(true);

		panel.add(statusBar);
		panel.add(gameMap);
		panel.add(controlPanel);

		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		goldLabel = new JLabel("Gold : " + TowerDefense.gold);
		statusBar.add(goldLabel);
		goldLabel.setBounds(60, 0, 100, 50);

		levelLabel = new JLabel("Level : " + TowerDefense.level);
		statusBar.add(levelLabel);
		levelLabel.setBounds(160, 0, 100, 50);

		nextTLabel = new JLabel("Next Wave in : " + TowerDefense.nextT + "secs");
		statusBar.add(nextTLabel);
		nextTLabel.setBounds(260, 0, 100, 50);

		healthLabel = new JLabel("Health : " + TowerDefense.health);
		statusBar.add(healthLabel);
		healthLabel.setBounds(360, 0, 100, 50);

		mapBlocks = new JLabel[ROW+1][COL+1];

		for(int i = 0; i < ROW; ++i)
		for(int j = 0; j < COL; ++j) {
			JLabel tmpLb = new JLabel();
			mapBlocks[i][j] = tmpLb;
			if(TowerDefense.vacant[i][j] == -1)
				tmpLb.setIcon(new ImageIcon("resources/Road.jpg"));
			else
				tmpLb.setIcon(new ImageIcon("resources/Ground.jpg"));
			// tmpLb.setBackground(new Color((i + j) * 5 + 50, (i + j) * 5 + 50, (i + j) * 5 + 50));
			gameMap.add(tmpLb, GROUND_LAYER);
			tmpLb.setBounds(j * 60, i * 60, 60, 60);
			tmpLb.setOpaque(true);
		}
	}

}