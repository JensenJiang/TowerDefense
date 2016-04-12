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
			TowerDefense.vacant[x][y] = 1;
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

class Reminder{
	class Parser{
		int delay;
		private Object[] startTime,monCount,monList;
		Parser(JSONObject p,int d){
			startTime = ((JSONArray)p.get("start")).toArray();
			monCount = ((JSONArray)p.get("count")).toArray();
			monList = ((JSONArray)p.get("list")).toArray();
			delay = d;
		}
		/* unhandled plot error */
		void parse(){
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
	int updateDelay,start_health,start_gold;
	int[][] mapBlock;
	TowerRegister[] towerRegisters;
	PlaceRegister[][] placeRegisters;
	Reminder(int u_delay,int g_delay,JSONObject p,int m[][],int g,int h,TowerRegister[] _tr,PlaceRegister[][] _pr){
		updateDelay = u_delay;
		plotParser = new Parser(p,g_delay);
		mapBlock = m;
		start_gold = g;
		start_health = h;
		towerRegisters = _tr;
		placeRegisters = _pr;
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
	
	private void initRes(){
		TowerDefense.mons = new HashSet<>();
		TowerDefense.towers = new HashSet<>();
		TowerDefense.shells =  new HashSet<>();
		TowerDefense.gold = start_gold;
		TowerDefense.health = start_health;
		TowerDefense.tempTower = null;
		for(int i = 0;i < TowerDefense.row;i++) for(int j = 0;j < TowerDefense.column;j++)
			TowerDefense.vacant[i][j] = mapBlock[i][j];
		TowerDefense.ui.Init();
		for(int i = 0;i < TowerDefense.towerTypeCount;i++)
			TowerDefense.ui.towerButton[i].addMouseListener(towerRegisters[i]);
		for(int i = 0;i < TowerDefense.row;i++) for(int j = 0;j < TowerDefense.column;j++)
			TowerDefense.ui.mapBlocks[i][j].addMouseListener(placeRegisters[i][j]);
	}
	
	private void updateSchedule(){
		eventTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				/* see if game over */
				if(TowerDefense.health <= 0)
				{
					TowerDefense.gameOver();
					eventTimer.cancel();
					return;
				}
				
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
		/// System.out.println("Start!");
		initRes();
		for(int i = 0;i < TowerDefense.row;i++){
			for(int j = 0;j < TowerDefense.column;j++) System.out.printf("%d ",mapBlock[i][j]);
			System.out.println("");
		}
		eventTimer = new Timer();
		plotParser.parse();
		updateSchedule();
	}
}

public class TowerDefense {
	/* Resource */
	static int deltaTime = 50;
	static int row = 7, column = 15;
	static int start_x,start_y;
	static int gold, level, health;
	static HashSet<Tower> towers;
	static HashSet<Monster> mons;
	static HashSet<Shell> shells;
	static int[][] vacant;	// -1 = Road, 0 = Empty ground, 1 = Built ground
	static Tower tempTower;
	static MonsterRegister[] monsterMaker;
	static Reminder schedule;
	
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
	
	
	static public void startGame(){
		/// System.out.println("startGame()");
		schedule.start();
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
			
			ui = new UI();
			
			int[][] m = new int[row][column];
			vacant = new int[row][column];
			for(int i = 0;i < row;i++){
				JSONArray temp_row = (JSONArray)mapConfig.get(i);
				for(int j = 0;j < column;j++) vacant[i][j] = m[i][j] = (int)(long)temp_row.get(j);
			}
			/* init path */
			JSONArray path_x = (JSONArray)pathConfig.get("x"),path_y = (JSONArray)pathConfig.get("y");
			int path_size = path_x.size();
			start_x = (int)(long)path_x.get(0);
			start_y = (int)(long)path_y.get(0);
			Monster.path = new Point[path_size];
			for(int i = 0;i < path_size;i++) Monster.path[i] = new Point((int)(long)path_x.get(i), (int)(long)path_y.get(i));
			
			/* produce towerButton Action Register */
			TowerRegister[] tr = new TowerRegister[towerTypeCount];
			for(int i = 0;i < towerTypeCount;i++){
				tr[i] = new TowerRegister((JSONObject)towerConfig.get(i));
			}
			
			/* Register mapBlocks Action */
			PlaceRegister[][] pr = new PlaceRegister[row][column];
			for(int i = 0;i < row;i++) for(int j = 0;j < column;j++)
				pr[i][j] = new PlaceRegister(i,j);
			
			/* Register monsterMaker */
			monsterMaker = new MonsterRegister[monsterTypeCount];
			for(int i = 0;i < monsterTypeCount;i++){
				monsterMaker[i] = new MonsterRegister((JSONObject)(monsterConfig.get(i)),(double)start_x,(double)start_y);
			}
			
			/* init global schedule */
			schedule = new Reminder(deltaTime,1500,plotConfig,m,100,10,tr,pr);
			
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
	
	public static void gameOver()
	{
		ui.showGameOver();
	}
	
	public static void main(String[] args) {
		init();
		startGame();
	}
}
