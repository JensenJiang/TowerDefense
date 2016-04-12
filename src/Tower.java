import java.awt.Point;

import javax.swing.*;

public class Tower {
	
	String picName;
	String shellPicName;
	public JLabel label;
	int x, y;	//Block x, y
	double scx, scy;
	double angle;
	int price;
	int atk;
	double rate;
	double range;
	public boolean toDestroy = false;	// whether this object need to be destroyed
	
	private int timer = 0;
	
	Tower(String picName, String shellpicName, int x, int y, int price, int atk, double rate, double range)
	{
		this.picName = picName;
		this.shellPicName = shellpicName;
		this.x = x;
		this.y = y;
		this.price = price;
		this.atk = atk;
		this.rate = rate;
		this.range = range;
		label = new JLabel(new ImageIcon(picName));
	}
	
	// getters and setters
	public int getX()
	{
		return x;
	}
	public int getY()
	{
		return y;
	}
	public void setX(int _x){
		x = _x;
		Point p = TowerDefense.ui.gridToCoordinate(new Point(x, y));
		scx = p.x;
		scy = p.y;
	}
	public void setY(int _y){
		y = _y;
		Point p = TowerDefense.ui.gridToCoordinate(new Point(x, y));
		scx = p.x;
		scy = p.y;
	}
	public double getAngle()
	{
		return angle;
	}
	
	// functions
	void update()
	{
		if(toDestroy)
			return;
		
		timer -= TowerDefense.deltaTime;
		if(timer < 0)	timer = 0;
		
		if(timer == 0)
		{
			Monster obj = search();
			if(obj != null)
				attack(obj);
		}
	}
	
	// To attack a monster
	void attack(Monster m)
	{
		Shell s = new Shell(shellPicName, scx, scy, atk, 0.4, m);
		TowerDefense.registerShell(s);
		timer += 1000 * rate;
		// System.out.println("Attack!");
	}
	
	// To search for monster in range
	Monster search()
	{
		Monster targM = null;
		double farthest = 0;
		for(Monster m : TowerDefense.mons){
			if(Point.distance(scx, scy, m.x, m.y) <= range && m.lifeDist > farthest){
				targM = m;
				farthest = m.lifeDist;
			}
		}
		return targM;
	}
}
