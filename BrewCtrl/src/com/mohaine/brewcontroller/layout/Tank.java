package com.mohaine.brewcontroller.layout;

public class Tank extends BreweryComponent {

	public static final String TYPE = "Tank";

	@Override
	public String getType() {
		return TYPE;
	}

	private Sensor sensor;
	private HeatElement heater;

	public Sensor getSensor() {
		return sensor;
	}

	public void setSensor(Sensor sensor) {
		this.sensor = sensor;
	}

	public HeatElement getHeater() {
		return heater;
	}

	public void setHeater(HeatElement heater) {
		this.heater = heater;
	}

}
