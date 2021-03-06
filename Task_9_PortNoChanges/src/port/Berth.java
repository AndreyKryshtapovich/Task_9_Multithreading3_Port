package port;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

import warehouse.Container;
import warehouse.Warehouse;

public class Berth {

	private int id;
	private Warehouse portWarehouse;

	public Berth(int id, Warehouse warehouse) {
		this.id = id;
		portWarehouse = warehouse;
	}

	public int getId() {
		return id;
	}

	public boolean add(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();
		boolean portLock = false;

		try {
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				int newConteinerCount = portWarehouse.getRealSize() + numberOfConteiners;
				//TODO
				/*некорректное сравнение со значением portWarehouse.getFreeSize()
				 * новое количество высчитывается как старое + добавка, необходимо сравнивать 
				 * с общим местом в хранилище порта. Необходимо portWarehouse.getSize()*/
				if (newConteinerCount <= portWarehouse.getFreeSize()) {
					result = doMoveFromShip(shipWarehouse, numberOfConteiners);
				}
			}
		} finally {
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}

	private boolean doMoveFromShip(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;

		try {
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				if (shipWarehouse.getRealSize() >= numberOfConteiners) {
					List<Container> containers = shipWarehouse.getContainer(numberOfConteiners);
					portWarehouse.addContainer(containers);
					return true;
				}
			}
		} finally {
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}

		return false;
	}

	public boolean get(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		boolean result = false;
		Lock portWarehouseLock = portWarehouse.getLock();
		boolean portLock = false;

		try {
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				if (numberOfConteiners <= portWarehouse.getRealSize()) {
					result = doMoveFromPort(shipWarehouse, numberOfConteiners);
				}
			}
		} finally {
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}

	private boolean doMoveFromPort(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException {
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;

		try {
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				int newConteinerCount = shipWarehouse.getRealSize() + numberOfConteiners;
				//TODO 
				/*Высчитанное новое количество должно сравниваться не со 
				 * свободным, а с общим местом в хранилище корабля.
				 * Необходимо shipWarehouse.getSize()*/
				if (newConteinerCount <= shipWarehouse.getFreeSize()) {
					List<Container> containers = portWarehouse.getContainer(numberOfConteiners);
					shipWarehouse.addContainer(containers);
					return true;
				}
			}
		} finally {
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}

		return false;
	}
}
