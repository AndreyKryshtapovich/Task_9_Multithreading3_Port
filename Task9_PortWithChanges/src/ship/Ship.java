package ship;

import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;

import port.Berth;
import port.Port;
import port.PortException;
import warehouse.Container;
import warehouse.Warehouse;

public class Ship implements Runnable {

	private final static Logger logger = Logger.getRootLogger();
	private volatile boolean stopThread = false;

	private String name;
	private Port port;
	private Warehouse shipWarehouse;
	
	public Ship(String name, Port port, int shipWarehouseSize) {
		this.name = name;
		this.port = port;
		shipWarehouse = new Warehouse(shipWarehouseSize);
	}

	public void setContainersToWarehouse(List<Container> containerList) {
		shipWarehouse.addContainer(containerList);
	}

	public String getName() {
		return name;
	}

	public void stopThread() {
		stopThread = true;
	}

	public void run() {
		try {
			while (!stopThread) {
				atSea();
				inPort();
			}
		} catch (InterruptedException e) {
			logger.error("С кораблем случилась неприятность и он уничтожен.", e);
		} catch (PortException e) {
			// TODO сообщение переписано
	 logger.error("Порт не смог обслужить корабль. Необходима срочная корректировка работы порта", e);
		}
	}

	private void atSea() throws InterruptedException {
		Thread.sleep(1000);
	}


	private void inPort() throws InterruptedException, PortException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
	
			if (isLockedBerth) {
				berth = port.getBerth(this);
				// TODO 
				// вывод изменен для лучшей отладки кода
				logger.debug("Корабль " + name + " пришвартовался к причалу " + berth.getId() + " НА КОРАБЛЕ " + shipWarehouse.getRealSize());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug("Кораблю " + name + " отказано в швартовке к причалу ");
			} 
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("Корабль " + name + " ОТШВАРТОВАЛСЯ от причала " + berth.getId());
			}
		}
		
	}

	private void executeAction(ShipAction action, Berth berth) throws InterruptedException {
		switch (action) {
		case LOAD_TO_PORT:
 				loadToPort(berth);
			break;
		case LOAD_FROM_PORT:
				loadFromPort(berth);
			break;
		}
	}

	private boolean loadToPort(Berth berth) throws InterruptedException { 

		// TODO  
		/*Изменен вызов метода для выбора из числа имеющихся контейнеров*/
		int containersNumberToMove = conteinersCount(this.shipWarehouse.getRealSize());
		boolean result = false;
		if(containersNumberToMove == 0){
			logger.debug("Корабль " + name + " полностью опустошен и не может ничего выгрузить в порт." );
			return result;
		}

		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров на склад порта.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		// TODO 
		/*Проблема с сообщениями решена изменением метода conteinersCount*/
		if (!result) {
			logger.debug("Недостаточно места на складе порта для выгрузки кораблем "
					+ name + " " + containersNumberToMove + " контейнеров.");
		} else {
			logger.debug("Корабль " + name + " выгрузил " + containersNumberToMove
					+ " контейнеров в порт." + " Теперь в ПОРТУ " + port.getPortWarehouse().getRealSize() );
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException { 
		// TODO выбираем из количесва свободных мест 
		int containersNumberToMove = conteinersCount(this.shipWarehouse.getFreeSize());
		
		boolean result = false;
		if(containersNumberToMove == 0){
			logger.debug("Корабль " + name + "полностью заполнен и не может ничего загрузить из порта." );
			return result;
		}

		logger.debug("Корабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров со склада порта.");
		
		result = berth.get(shipWarehouse, containersNumberToMove); 
		if (result) {
			logger.debug("Корабль " + name + " загрузил " + containersNumberToMove
					+ " контейнеров из порта." + " Теперь в ПОРТУ " + port.getPortWarehouse().getRealSize());
		} else {
			// TODO  Вывод изменен т.к. сигнатура conteinersCount была изменена. Корабль не может запросить больше чем может принять 
			//следовательно false вернется только если у порта запрашивают больше контейнеров чем есть в его хранилище
			logger.debug("Недостаточно котейнеров в порту " 
					+ "для погрузки " + containersNumberToMove + " контейнеров из порта на корабль " +  name + ".");
		}
		
		return result;
	}

	// TODO
	/*Метод изменен, вычисление производится на основе имеющегося количества
	 * котейнеров на корабле. Значение 0 может быть возвращено только если на корабле 0 контейнеров*/
	private int conteinersCount(int amount) {
		Random random = new Random();
		int result = random.nextInt(amount + 1);
		while(result == 0){
			if(amount != 0){
				 result = random.nextInt(amount + 1);
			}else{
				break;
				}
		}
		return result;
	}

	private ShipAction getNextAction() {
		Random random = new Random();
		int value = random.nextInt(4000);
		if (value < 1000) {
			return ShipAction.LOAD_TO_PORT;
		} else if (value < 2000) {
			return ShipAction.LOAD_FROM_PORT;
		}
		return ShipAction.LOAD_TO_PORT;
	}

	enum ShipAction {
		LOAD_TO_PORT, LOAD_FROM_PORT
	}
}
