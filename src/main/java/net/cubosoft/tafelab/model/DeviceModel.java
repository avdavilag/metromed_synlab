package net.cubosoft.tafelab.model;

import java.util.List;

public class DeviceModel {

	private String id;
	private String name;
	private String temperature;
	private String humidity;
	private String battery;
	private String maxTemperature;
	private String minTemperature;
	private String maxHumidity;
	private String minHumidity;
	private String active;
	private String lastUpdate;
	private String label;
	private String rssi;
	private String maintenance;
	private List<AssetModel> assetsList;
	
	public DeviceModel(String id, String name, String temperature, String humidity, String battery,
			String maxTemperature, String minTemperature, String maxHumidity, String minHumidity, String active, String rssi,
			String lastUpdate, String label,String maintenance,List<AssetModel> assetsList) {
		this.id = id;
		this.name = name;
		this.temperature = temperature;
		this.humidity = humidity;
		this.battery = battery;
		this.maxTemperature = maxTemperature;
		this.minTemperature = minTemperature;
		this.maxHumidity = maxHumidity;
		this.minHumidity = minHumidity;
		this.active = active;
		this.lastUpdate = lastUpdate;
		this.label = label;
		this.rssi = rssi;
		this.maintenance = maintenance;
		this.assetsList=assetsList;
	}



	public String getMaintenance() {
		return maintenance;
	}



	public void setMaintenance(String maintenance) {
		this.maintenance = maintenance;
	}



	public String getRssi() {
		return rssi;
	}



	public void setRssi(String rssi) {
		this.rssi = rssi;
	}



	public String getMaxTemperature() {
		return maxTemperature;
	}



	public void setMaxTemperature(String maxTemperature) {
		this.maxTemperature = maxTemperature;
	}



	public String getMinTemperature() {
		return minTemperature;
	}



	public void setMinTemperature(String minTemperature) {
		this.minTemperature = minTemperature;
	}



	public String getMaxHumidity() {
		return maxHumidity;
	}



	public void setMaxHumidity(String maxHumidity) {
		this.maxHumidity = maxHumidity;
	}



	public String getMinHumidity() {
		return minHumidity;
	}



	public void setMinHumidity(String minHumidity) {
		this.minHumidity = minHumidity;
	}



	public DeviceModel() {
	}
	
	

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getTemperature() {
		return temperature;
	}
	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}
	public String getHumidity() {
		return humidity;
	}
	public void setHumidity(String humidity) {
		this.humidity = humidity;
	}
	public String getBattery() {
		return battery;
	}
	public void setBattery(String battery) {
		this.battery = battery;
	}
	public String getActive() {
		return active;
	}
	public void setActive(String active) {
		this.active = active;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}



	public List<AssetModel> getAssetsList() {
		return assetsList;
	}



	public void setAssetsList(List<AssetModel> assetsList) {
		this.assetsList = assetsList;
	}

	
	
	
}
