import java.awt.*;
import javax.swing.*;

public class Monster {
	static Point[] path;	// monsters go along the path
	
	String picName;
	public JLabel label;
	double speed;
	int totalHP;
	int curHP;
	int killMoney;
	double x, y;
	double angle;
	public boolean toDestroy = false;
	public double lifeDist = 0;
	
	int destID;		// going to path[destID]
	
	Monster(String picName, double speed, int totalHP, int killMoney, double x, double y)
	{
		this.picName = picName;
		this.speed = speed;
		this.totalHP = totalHP;
		curHP = totalHP;
		this.killMoney = killMoney;
		this.x = x;
		this.y = y;
		
		label = new JLabel(new ImageIcon(picName));
	}
	
	// getters and setters
	/*
	public void setSpeed(double speed)
	{
		this.speed = speed;
	}
	public double getSpeed()
	{
		return speed;
	}
	*/
	public int getCurHP()
	{
		return curHP;
	}
	public int getTotalHP()
	{
		return totalHP;
	}
	public double getX()
	{
		return x;
	}
	public double getY()
	{
		return y;
	}
	public double getAngle()
	{
		return angle;
	}
	
	// functions
	public void update()
	{
		if(toDestroy)
			return;
		
		if(curHP <= 0)
		{
			die();
			return;
		}
		x += speed * TowerDefense.deltaTime * Math.cos(angle);
		y += speed * TowerDefense.deltaTime * Math.sin(angle);
		lifeDist += speed * TowerDefense.deltaTime;
		
		if(Point.distance(x, y, path[destID].getX(), path[destID].getY()) < 0.01)
			destID++;
		if(destID >= path.length)
		{
			escape();
			return;
		}
		angle = Math.atan2(path[destID].getY() - y, path[destID].getX() - x);
	}
	
	// To take damage
	public void hurt(int damage)
	{
		curHP -= damage;
		if(curHP <= 0)
			die();
	}
	
	// To escape from the map
	void escape()
	{
		if(toDestroy)
			return;
		TowerDefense.healthChange(-1);
		toDestroy = true;
	}
	
	// To die
	void die()
	{
		if(toDestroy)
			return;
		TowerDefense.moneyChange(killMoney);
		toDestroy = true;
	}
}
