import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import org.json.simple.*;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class TowerRegister extends MouseAdapter{
	String picName,shellPicName;
	double rate,range;
	int price,atk;
	public TowerRegister(JSONObject config) {
		/* Parse */
		picName = (String)config.get("picName");
		shellPicName = (String)config.get("shellPicName");
		rate = (Double)config.get("rate");
		range = (Double)config.get("range");
		price = (int)(long)config.get("price");
		atk = (int)(long)config.get("atk");
	}
	/* Only Add to */
	@Override
	public void mouseClicked(MouseEvent e){
		if(TowerDefense.moneyEnough(price))
			TowerDefense.tempTower = new Tower(picName,shellPicName,0,0,price,atk,rate,range);
	}
}

class PlaceRegister extends MouseAdapter{
	int x,y;
	public PlaceRegister(int _x,int _y) {
		x = _x;
		y = _y;
	}
	@Override
	public void mouseClicked(MouseEvent e){
		if(TowerDefense.tempTower != null && TowerDefense.vacant[x][y] == 0){
			TowerDefense.tempTower.setX(x);
			TowerDefense.tempTower.setY(y);
			TowerDefense.towers.add(TowerDefense.tempTower);
			TowerDefense.moneyChange(-TowerDefense.tempTower.price);
			TowerDefense.tempTower = null;
		}
	}
}

class MonsterRegister{
	String picName;
	double speed,x,y;
	int totalHP,killMoney;
	public MonsterRegister(JSONObject config,double _x,double _y){
		/* Parse */
		picName = (String)config.get("picName");
		speed = (Double)config.get("speed");
		totalHP = (int)(long)config.get("totalHP");
		killMoney = (int)(long)config.get("killMoney");
		x = _x;
		y = _y;
	}
	void Register(){
		TowerDefense.mons.add(new Monster(picName,speed,totalHP,killMoney,x,y));
	}
}

class Remainder{
	class Parser{
		JSONObject plot;
		int delay;
		Parser(JSONObject p,int d){
			plot = p;
			delay = d;
		}
		/* unhandled plot error */
		void parse(){
			Object[] startTime,monCount,monList;
			startTime = ((JSONArray)plot.get("start")).toArray();
			monCount = ((JSONArray)plot.get("count")).toArray();
			monList = ((JSONArray)plot.get("list")).toArray();
			int size = startTime.length,j = 0;
			for(int i = 0;i < size;i++){
				int c = (int)(long)monCount[i],time = (int)(long)startTime[i] * 1000;
				for(;c > 0;c--,j++,time += delay){
					TowerDefense.schedule.addMonsterSchedule(TowerDefense.monsterMaker[(int)(long)monList[j]], time);
				}
			}
		}
	}
	Parser plotParser;
	Timer eventTimer;
	int updateDelay;
	Remainder(int u_delay,int g_delay,JSONObject p){
		eventTimer = new Timer();
		updateDelay = u_delay;
		plotParser = new Parser(p,g_delay);
	}
	
	/* maybe wrong ? about internal class */
	private void addMonsterSchedule(MonsterRegister e,int time){
		eventTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				e.Register();
			}
		}, time);
	}
	
	private void updateSchedule(){
		eventTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				/* update Tower */
				for(Iterator<Tower> i = TowerDefense.towers.iterator();i.hasNext();){
					Tower t = i.next();
					if(t.toDestroy){
						i.remove();
						continue;
					}
					t.update();
				}
				
				/* update Shell */
				for(Iterator<Shell> i = TowerDefense.shells.iterator();i.hasNext();){
					Shell t = i.next();
					if(t.toDestroy){
						i.remove();
						continue;
					}
					t.update();
				}
				
				/* update Monster */
				for(Iterator<Monster> i = TowerDefense.mons.iterator();i.hasNext();){
					Monster t = i.next();
					if(t.toDestroy){
						i.remove();
						continue;
					}
					t.update();
				}
				
				/* update UI */
				TowerDefense.ui.update();
				
				/* add to next cycle */
				updateSchedule();
			}
		}, updateDelay);
		
	}
	public void start(){
		plotParser.parse();
		updateSchedule();
	}
}

public class TowerDefense {
	/* Resource */
	static int deltaTime = 150;
	static int row = 7,column = 15;
	static int start_x,start_y;
	static int gold = 100,level,health;
	static HashSet<Tower> towers;
	static HashSet<Monster> mons;
	static HashSet<Shell> shells;
	static int[][] vacant;
	static Tower tempTower;
	static MonsterRegister[] monsterMaker;
	static Remainder schedule;
	
	/* UI */
	static UI ui;
	
	/* Control Parameters */
	static int nextT;	// display for user
	static int towerTypeCount,monsterTypeCount;
	
	/* Register Function */
	static void registerShell(Shell s){
		shells.add(s);
	}
	
	/* Source Change Function */
	static void healthChange(int d){
		health += d;
	}
	
	static boolean moneyEnough(int d){
		return d <= gold;
	}
	
	static void moneyChange(int d){
		gold += d;
	}
	
	/* Init */
	static void init(){
		/* Open Config File */
		JSONParser parser = new JSONParser();
		JSONObject plotConfig,pathConfig;
		JSONArray towerConfig,monsterConfig,mapConfig;
		try{
			towerConfig = (JSONArray)parser.parse(new FileReader("config/towerConfig.json"));
			monsterConfig = (JSONArray)parser.parse(new FileReader("config/monsterConfig.json"));
			plotConfig = (JSONObject)parser.parse(new FileReader("config/plotConfig.json"));
			mapConfig = (JSONArray)parser.parse(new FileReader("config/mapConfig.json"));
			pathConfig = (JSONObject)parser.parse(new FileReader("config/pathConfig.json"));
			/* init parameters */
			towerTypeCount = towerConfig.size();
			monsterTypeCount = monsterConfig.size();
			
			/* init container */
			towers = new HashSet<>();
			mons = new HashSet<>();
			shells = new HashSet<>();
			vacant = new int[row][column];
			for(int i = 0;i < row;i++){
				JSONArray temp_row = (JSONArray)mapConfig.get(i);
				for(int j = 0;j < column;j++) vacant[i][j] = (int)(long)temp_row.get(j);
			}
			
			/* init UI */
			ui = new UI();
			
			/* init path */
			JSONArray path_x = (JSONArray)pathConfig.get("x"),path_y = (JSONArray)pathConfig.get("y");
			int path_size = path_x.size();
			start_x = (int)(long)path_x.get(0);
			start_y = (int)(long)path_y.get(0);
			Monster.path = new Point[path_size];
			for(int i = 0;i < path_size;i++) Monster.path[i] = new Point((int)(long)path_x.get(i), (int)(long)path_y.get(i));
			
			/* Register towerButton Action */
			for(int i = 0;i < towerTypeCount;i++){
				ui.towerButton[i].addMouseListener(new TowerRegister((JSONObject)towerConfig.get(i)));
			}
			
			/* Register mapBlocks Action */
			for(int i = 0;i < row;i++) for(int j = 0;j < column;j++)
				ui.mapBlocks[i][j].addMouseListener(new PlaceRegister(i,j));
			
			/* Register monsterMaker */
			monsterMaker = new MonsterRegister[monsterTypeCount];
			for(int i = 0;i < monsterTypeCount;i++){
				monsterMaker[i] = new MonsterRegister((JSONObject)(monsterConfig.get(i)),(double)start_x,(double)start_y);
			}
			
			/* init global schedule */
			schedule = new Remainder(deltaTime,1500,plotConfig);
			schedule.start();
			
		}catch(FileNotFoundException e){
			System.out.println("Couldn't open json file:");
			System.out.println(e);
		}catch(ParseException e){
			System.out.println("JSON file format error:");
			System.out.println(e);
		}catch(IOException e){
			System.out.println(e);
		}
	}
	
	public static void main(String[] args) {
		init();
	}
}
