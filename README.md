#TowerDefense
##一、简介
一款塔防游戏。地图上一定时刻会出怪，玩家可以在空地上建造防御塔，以攻击怪物，击破怪物可以获得金币，用于建造更多的塔。
如果怪物逃出地图，则会减少玩家的生命值，减少到0则游戏结束（失败）。如果玩家在所有怪物出现之后成功存活，则胜利。
##二、一些特色
1. 怪物头上有血量条，可以显示该怪物剩余生命值比例
2. 建造塔的按钮在玩家金币不足的时候会变灰（利用setEnable）
3. 将鼠标放置在塔上时，会提示该塔的信息（利用setToolTipText）
4. 随时显示怪物当前的进攻轮数（level）以及到下一波的时间（next wave time）
5. 胜利或失败之后可以重新开始游戏
##三、设计的类及其关系
一共设计了5个类：
1. TowerDefense：主类，负责游戏流程控制。开始时从外部JSON文件读取各个塔、怪物、炮弹的属性值。内部有各个游戏对象的注册表，每隔一定时间调用所有游戏对象的update()。使用Timer进行调度，来实现定时的功能。
2. UI：负责显示和交互的类。每次update()从TowerDefense的注册表中读取所有待显示的游戏对象，然后依次显示之。
3. Tower：塔类。每次update()搜索怪物并尝试攻击。攻击动作即为注册一个以目标怪物为目标的炮弹（Shell）。
4. Monster：怪物类。每次update()如果生命值<=0则死亡，否则向前移动，如果跑到最后一个路径节点则消失并对玩家造成伤害。
5. Shell：炮弹类。每次update()向前移动，如果距离目标怪物小于一定距离则对该怪物造成伤害，然后消失。
注：游戏对象消失时，将自身的toDestroy标记位置为true，由主流程遍历注册表时从注册表中清除。（替代析构函数）