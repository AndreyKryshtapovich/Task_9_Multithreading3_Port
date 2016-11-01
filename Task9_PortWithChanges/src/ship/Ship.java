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
			logger.error("� �������� ��������� ������������ � �� ���������.", e);
		} catch (PortException e) {
			// TODO ��������� ����������
	 logger.error("���� �� ���� ��������� �������. ���������� ������� ������������� ������ �����", e);
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
				// ����� ������� ��� ������ ������� ����
				logger.debug("������� " + name + " �������������� � ������� " + berth.getId() + " �� ������� " + shipWarehouse.getRealSize());
				ShipAction action = getNextAction();
				executeAction(action, berth);
			} else {
				logger.debug("������� " + name + " �������� � ��������� � ������� ");
			} 
		} finally {
			if (isLockedBerth){
				port.unlockBerth(this);
				logger.debug("������� " + name + " ������������� �� ������� " + berth.getId());
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
		/*������� ����� ������ ��� ������ �� ����� ��������� �����������*/
		int containersNumberToMove = conteinersCount(this.shipWarehouse.getRealSize());
		boolean result = false;
		if(containersNumberToMove == 0){
			logger.debug("������� " + name + " ��������� ��������� � �� ����� ������ ��������� � ����." );
			return result;
		}

		logger.debug("������� " + name + " ����� ��������� " + containersNumberToMove
				+ " ����������� �� ����� �����.");

		result = berth.add(shipWarehouse, containersNumberToMove);
		// TODO 
		/*�������� � ����������� ������ ���������� ������ conteinersCount*/
		if (!result) {
			logger.debug("������������ ����� �� ������ ����� ��� �������� �������� "
					+ name + " " + containersNumberToMove + " �����������.");
		} else {
			logger.debug("������� " + name + " �������� " + containersNumberToMove
					+ " ����������� � ����." + " ������ � ����� " + port.getPortWarehouse().getRealSize() );
			
		}
		return result;
	}

	private boolean loadFromPort(Berth berth) throws InterruptedException { 
		// TODO �������� �� ��������� ��������� ���� 
		int containersNumberToMove = conteinersCount(this.shipWarehouse.getFreeSize());
		
		boolean result = false;
		if(containersNumberToMove == 0){
			logger.debug("������� " + name + "��������� �������� � �� ����� ������ ��������� �� �����." );
			return result;
		}

		logger.debug("������� " + name + " ����� ��������� " + containersNumberToMove
				+ " ����������� �� ������ �����.");
		
		result = berth.get(shipWarehouse, containersNumberToMove); 
		if (result) {
			logger.debug("������� " + name + " �������� " + containersNumberToMove
					+ " ����������� �� �����." + " ������ � ����� " + port.getPortWarehouse().getRealSize());
		} else {
			// TODO  ����� ������� �.�. ��������� conteinersCount ���� ��������. ������� �� ����� ��������� ������ ��� ����� ������� 
			//������������� false �������� ������ ���� � ����� ����������� ������ ����������� ��� ���� � ��� ���������
			logger.debug("������������ ���������� � ����� " 
					+ "��� �������� " + containersNumberToMove + " ����������� �� ����� �� ������� " +  name + ".");
		}
		
		return result;
	}

	// TODO
	/*����� �������, ���������� ������������ �� ������ ���������� ����������
	 * ���������� �� �������. �������� 0 ����� ���� ���������� ������ ���� �� ������� 0 �����������*/
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