package com.mohaine.brewcontroller.client.layout;

public abstract class BrewHardwareControl extends BreweryComponent {
	private int pin;
	private int duty;
	private boolean hasDuty;

	public int getDuty() {
		return duty;
	}

	public void setDuty(int duty) {
		this.duty = duty;
	}

	public int getPin() {
		return pin;
	}

	public void setPin(int pin) {
		this.pin = pin;
	}

	public boolean isHasDuty() {
		return hasDuty;
	}

	public void setHasDuty(boolean hasDuty) {
		this.hasDuty = hasDuty;
	}

}