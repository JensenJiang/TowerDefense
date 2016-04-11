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
		// System.out.printf("Run! pos = (%f, %f)\n", x, y);
		if(obj.toDestroy)
			toDestroy = true;
		if(toDestroy)
			return;
		angle = Math.atan2(obj.y - y, obj.x - x);
		x += speed * TowerDefense.deltaTime * Math.cos(angle);
		y += speed * TowerDefense.deltaTime * Math.sin(angle);
		if(Point.distance(x, y, obj.x, obj.y) < 10)
			explode();
	}
	
	public void explode()
	{
		// System.out.println("Boom!");
		if(toDestroy)
			return;
		obj.hurt(damage);
		toDestroy = true;
	}
}
