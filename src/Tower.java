import java.awt.Point;
import javax.swing.*;

public class Tower {
	
	String picName;
	String shellPicName;
	public JLabel label;
	double x, y;
	double angle;
	int price;
	int atk;
	double rate;
	double range;
	public boolean toDestroy = false;
	
	private int timer = 0;
	private Monster obj = null;
	
	Tower(String picName, String shellpicName, double x, double y, int price, int atk, double rate, double range)
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
	public double getX()
	{
		return x;
	}
	public double getY()
	{
		return y;
	}
	public void setX(double _x){
		x = _x;
	}
	public void setY(double _y){
		y = _y;
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
		
		if(obj == null)
			obj = search();
		if(obj != null && timer == 0)
			attack(obj);
	}
	
	void attack(Monster m)
	{
		Shell s = new Shell(shellPicName, x, y, atk, 1000, m);
		TowerDefense.registerShell(s);
		timer += 1000 * rate;
	}
	
	Monster search()
	{
		for(Monster m : TowerDefense.mons){
			if(Point.distance(x, y, m.x, m.y) <= range){
				return m;
			}
		}
		return null;
	}
}
