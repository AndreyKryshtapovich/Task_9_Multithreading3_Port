package port;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;


import org.apache.log4j.Logger;

import ship.Ship;
import warehouse.Container;
import warehouse.Warehouse;

public class Port {
	private final static Logger logger = Logger.getRootLogger();
	
	private BlockingQueue<Berth> berthList; // ������� ��������
	private Warehouse portWarehouse; // ��������� �����
	
	// TODO ��������  Map �� ConcurrentMap
	private ConcurrentMap<Ship, Berth> usedBerths; // ����� ������� � ������ ������� �����

	public Port(int berthSize, int warehouseSize) {
		portWarehouse = new Warehouse(warehouseSize); // ������� ������ ���������
		berthList = new ArrayBlockingQueue<Berth>(berthSize); // ������� ������� ��������
		for (int i = 0; i < berthSize; i++) { // ��������� ������� �������� ��������������� ������ ���������
			berthList.add(new Berth(i, portWarehouse));
		}
		// TODO ����������  HashMap �� ConcurrentHashMap
		usedBerths = new ConcurrentHashMap<Ship, Berth>(); // ������� ������, ������� �����
		// ������� ����� ����� �������� � ��������
		logger.debug("���� ������.");
	}
	
	
	 //TODO ����� ��������
		public Warehouse getPortWarehouse() {
			return portWarehouse;
		}
		
	
	public void setContainersToWarehouse(List<Container> containerList){
		portWarehouse.addContainer(containerList);
	}

	public boolean lockBerth(Ship ship) {
		Berth berth;
		try {
			berth = berthList.take();
			usedBerths.put(ship, berth);
			return true;
		} catch (InterruptedException e) {
			logger.debug("������� " + ship.getName() + " �������� � ���������.");
			return false;
		}
		
	}
	
	public boolean unlockBerth(Ship ship) {
		Berth berth = usedBerths.get(ship); 
		
		try {
			berthList.put(berth);
			usedBerths.remove(ship);
		} catch (InterruptedException e) {
			logger.debug("������� " + ship.getName() + " �� ���� ��������������.");
			return false;
		}		
		return true;
	}
	
	public Berth getBerth(Ship ship) throws PortException {
		
		Berth berth = usedBerths.get(ship);
		if (berth == null){
			throw new PortException("Try to use Berth without blocking.");
		}
		return berth;		
	}

   
}
