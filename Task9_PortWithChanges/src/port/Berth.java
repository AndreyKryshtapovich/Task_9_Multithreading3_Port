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

		try{
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				int newConteinerCount = portWarehouse.getRealSize()	+ numberOfConteiners; 
				// TODO изменено на portWarehouse.getSize()
				if (newConteinerCount <= portWarehouse.getSize()) { 
					result = doMoveFromShip(shipWarehouse, numberOfConteiners);
				}
			}
		} finally{
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}
	
	private boolean doMoveFromShip(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException{
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;
		
		try{
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				if(shipWarehouse.getRealSize() >= numberOfConteiners){ // если то сколько мы хотим выгрузить не больше реального кол-ва груза а корабле
					List<Container> containers = shipWarehouse.getContainer(numberOfConteiners); 
					portWarehouse.addContainer(containers);  
					return true;
				}
			}
		}finally{
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

		try{
			portLock = portWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (portLock) {
				if (numberOfConteiners <= portWarehouse.getRealSize()) { // если то что мы хотим загрузить не превышает то что имеется в наличии в порту
					result = doMoveFromPort(shipWarehouse, numberOfConteiners);	
				}
			}
		} finally{
			if (portLock) {
				portWarehouseLock.unlock();
			}
		}

		return result;
	}
	
	private boolean doMoveFromPort(Warehouse shipWarehouse, int numberOfConteiners) throws InterruptedException{
		Lock shipWarehouseLock = shipWarehouse.getLock();
		boolean shipLock = false;
		
		try{
			shipLock = shipWarehouseLock.tryLock(30, TimeUnit.SECONDS);
			if (shipLock) {
				int newConteinerCount = shipWarehouse.getRealSize() + numberOfConteiners;
				// TODO исправлено с shipWarehouse.getFreeSize() на shipWarehouse.getSize()
				if(newConteinerCount <= shipWarehouse.getSize()){ 
					List<Container> containers = portWarehouse.getContainer(numberOfConteiners); 
					shipWarehouse.addContainer(containers); 
					return true;
				}
			}
		}finally{
			if (shipLock) {
				shipWarehouseLock.unlock();
			}
		}
		
		return false;		
	}
}
