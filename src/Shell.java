import java.awt.Point;
import javax.swing.*;

public class Shell {
	
	String picName;
	public JLabel label;
	double x, y;
	double angle;
	int damage;
	double speed;
	Monster obj;
	boolean toDestroy = false;
	
	Shell(String picName, double x, double y, int damage, double speed, Monster obj)
	{
		this.picName = picName;
		this.x = x;
		this.y = y;
		this.damage = damage;
		this.speed = speed;
		this.obj = obj;
		
		angle = Math.atan2(obj.y - y, obj.x - x);
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
	public double getAngle()
	{
		return angle;
	}
	
	// functions
	public void update()
	{
		if(toDestroy)
			return;
		x += speed * TowerDefense.deltaTime * Math.cos(angle);
		y += speed * TowerDefense.deltaTime * Math.sin(angle);
		angle = Math.atan2(obj.y - y, obj.x - x);
		if(Point.distance(x, y, obj.x, obj.y) < 0.01)
			explode();
	}
	
	public void explode()
	{
		if(toDestroy)
			return;
		obj.hurt(damage);
		toDestroy = true;
	}
}
