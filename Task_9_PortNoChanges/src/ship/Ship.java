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
			logger.error("— кораблем случилась непри€тность и он уничтожен.", e);
		} catch (PortException e) {
			//TODO
			logger.error("— кораблем случилась непри€тность и он уничтожен.", e);//!!! переписать сообщение
		}
	}

	private void atSea() throws InterruptedException {
		Thread.sleep(1000);
	}


	private void inPort() throws PortException, InterruptedException {

		boolean isLockedBerth = false;
		Berth berth = null;
		try {
			isLockedBerth = port.lockBerth(this);
			
			if (isLockedBerth) {
				berth = port.getBerth(this);
				logger.debug(" орабль " + name + " пришвартовалс€ к причалу " + berth.getId());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug(" ораблю " + name + " отказано в швартовке к причалу ");
			}
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug(" орабль " + name + " отошел от причала " + berth.getId());
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
		/*¬ыбор количесва контейнеров должен осуществл€тьс€ на основе имеющегос€ количества.
		 *  орабль не может запросить больше, чем есть в его хранилище.*/
		int containersNumberToMove = conteinersCount();
		boolean result = false;

		logger.debug(" орабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров на склад порта.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		
		//TODO 
		/*≈сли корабль хочет загрузить в порт больше, чем есть в его
		 * хранилище, то все равно выведетс€ сообщение, что недостаточно места в порту. */
		if (!result) {
			logger.debug("Ќедостаточно места на складе порта дл€ выгрузки кораблем "
					+ name + " " + containersNumberToMove + " контейнеров.");
		} else {
			logger.debug(" орабль " + name + " выгрузил " + containersNumberToMove
					+ " контейнеров в порт.");
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException {
		// TODO
		/*¬ыбор количесва контейнеров должен осуществл€тьс€ на основе количества свободных мест*/
		int containersNumberToMove = conteinersCount();
		
		boolean result = false;

		logger.debug(" орабль " + name + " хочет загрузить " + containersNumberToMove
				+ " контейнеров со склада порта.");
		
		result = berth.get(shipWarehouse, containersNumberToMove);
		
		if (result) {
			logger.debug(" орабль " + name + " загрузил " + containersNumberToMove
					+ " контейнеров из порта.");
		} else {
			logger.debug("Ќедостаточно места на на корабле " + name
					+ " дл€ погрузки " + containersNumberToMove + " контейнеров из порта.");
		}
		
		return result;
	}
	
	//TODO 
	/*ƒл€ предотвращени€ различных ошибок в приложении  данный
	 * метод логично изменить. Ќеобходимо изменить так чтобы корабль 
	 * не мог выгружать больше, чем имеетс€ в его хранилище и запрашивать больше, чем 
	 * есть свободного места.*/
	private int conteinersCount() {
		Random random = new Random();
		return random.nextInt(20) + 1;
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
