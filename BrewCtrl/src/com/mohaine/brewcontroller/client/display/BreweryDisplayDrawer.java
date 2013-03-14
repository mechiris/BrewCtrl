/*
 Copyright 2009-2013 Michael Graessle

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.

 */

package com.mohaine.brewcontroller.client.display;

import java.util.List;

import com.google.inject.Inject;
import com.mohaine.brewcontroller.client.ControllerHardware;
import com.mohaine.brewcontroller.client.Converter;
import com.mohaine.brewcontroller.client.FormatterDefaults;
import com.mohaine.brewcontroller.client.FormatterDefaults.Formatter;
import com.mohaine.brewcontroller.client.UnitConversion;
import com.mohaine.brewcontroller.client.bean.ControlPoint;
import com.mohaine.brewcontroller.client.bean.ControlStep;
import com.mohaine.brewcontroller.client.display.DrawStyle.BColor;
import com.mohaine.brewcontroller.client.display.DrawStyle.BFont;
import com.mohaine.brewcontroller.client.layout.BreweryComponent;
import com.mohaine.brewcontroller.client.layout.HeatElement;
import com.mohaine.brewcontroller.client.layout.Pump;
import com.mohaine.brewcontroller.client.layout.Sensor;
import com.mohaine.brewcontroller.client.layout.Tank;

public class BreweryDisplayDrawer<T> {

	private UnitConversion conversion;
	private ControllerHardware controller;

	private DrawerCanvas<T> canvas;
	private List<BreweryComponentDisplay> displays;
	private int width = 10;
	private int height = 10;

	private Formatter<Number> numberFormat;
	private Formatter<Number> numberFormatWhole;

	public interface RedrawHook<T> {
		void redraw(T context);
	}

	public interface DrawerCanvas<T> {

		void init(RedrawHook<T> hook);

		void updateWidths(int width, int height);

		T getContext();

		void displayContext(T context);

		void addMouseListener(DrawerMouseListener drawerMouseListener);

		void fillRect(T context, int i, int j, int width, int height, BColor background);

		void drawText(T context, String text, BColor textColor, BColor bgColor, boolean alignRight, int left, int top, int width, int height, BFont font);

		Object getWidget();

		void drawPump(T context, int left, int top, int width, int height, BColor backPaint, boolean on);

		void drawTank(T context, int left, int top, int width, int height);
	}

	@SuppressWarnings("unchecked")
	@Inject
	public BreweryDisplayDrawer(ControllerHardware controller, UnitConversion conversion, FormatterDefaults formatters, @SuppressWarnings("rawtypes") DrawerCanvas canvas) {
		this.controller = controller;
		this.conversion = conversion;
		numberFormat = formatters.getDecimalFormatter();
		numberFormatWhole = formatters.getWholeFormatter();

		this.canvas = canvas;
		canvas.init(new RedrawHook<T>() {

			@Override
			public void redraw(T context) {
				redrawAll(context);
			}

		});
	}

	private int getHeight() {
		return height;
	}

	private int getWidth() {
		return width;
	}

	public void setDisplays(List<BreweryComponentDisplay> displays, int width, int height) {
		this.width = width;
		this.height = height;
		canvas.updateWidths(width, height);
		this.displays = displays;
		redrawAll();
	}

	public void redrawBreweryComponent(BreweryComponent component) {
		T context = canvas.getContext();
		for (BreweryComponentDisplay display : displays) {
			BreweryComponent displayComponent = display.getComponent();
			if (displayComponent == component) {
				drawComponent(context, display, true);
				break;
			}
		}
		canvas.displayContext(context);
	}

	public void addMouseListener(final DrawerMouseListener drawerMouseListener) {
		canvas.addMouseListener(drawerMouseListener);
	}

	public void redrawAll() {
		T context = canvas.getContext();
		if (context != null) {
			redrawAll(context);
		}
	}

	private void redrawAll(T context) {
		canvas.fillRect(context, 0, 0, getWidth(), getHeight(), BColor.BACKGROUND);
		for (BreweryComponentDisplay display : displays) {
			drawComponent(context, display, true);
		}
		canvas.displayContext(context);
	}

	public Object getWidget() {
		return canvas.getWidget();
	}

	private void drawComponent(T context2d, BreweryComponentDisplay display, boolean full) {
		BreweryComponent component = display.getComponent();
		if (Tank.TYPE.equals(component.getType())) {
			if (full) {
				drawTank(context2d, display);
			}
		} else if (Pump.TYPE.equals(component.getType())) {
			drawPump(context2d, display);
		} else if (Sensor.TYPE.equals(component.getType())) {
			drawSensor(context2d, display);
		} else if (HeatElement.TYPE.equals(component.getType())) {
			drawHeatElement(context2d, display);
		} else {
			canvas.fillRect(context2d, display.getAbsLeft(), display.getAbsLeft(), display.getWidth(), display.getHeight(), BColor.FOREGROUND);
		}
	}

	private void drawHeatElement(T g, BreweryComponentDisplay display) {
		HeatElement heater = (HeatElement) display.getComponent();
		int duty = heater.getDuty();
		String text = null;
		BColor color = BColor.FOREGROUND;

		ControlStep selectedStep = controller.getSelectedStep();
		if (selectedStep != null) {

			ControlPoint controlPointForPin = selectedStep.getControlPointForPin(heater.getPin());
			if (controlPointForPin != null) {
				int cpDuty = controlPointForPin.getDuty();

				if (controlPointForPin.isAutomaticControl()) {
					color = BColor.INACTIVE;
				}

				if (selectedStep.isActive()) {
					if (controlPointForPin.isAutomaticControl()) {
						text = Integer.toString(duty) + "%";
					} else {
						if (duty != cpDuty) {
							color = BColor.PENDING;
						}
						text = Integer.toString(cpDuty) + "%";
					}

				} else {
					if (controlPointForPin.isAutomaticControl()) {
						text = "Auto";
					} else {
						text = Integer.toString(cpDuty) + "%";
					}
				}
			}
		}

		if (text == null) {
			text = Integer.toString(duty) + "%";
		}
		drawText(g, display, text, color, BColor.TANK, true);
	}

	private void drawText(T g, BreweryComponentDisplay display, String tempDisplay, BColor textColor, BColor bgColor, boolean alignRight) {
		drawText(g, tempDisplay, textColor, bgColor, alignRight, display.getAbsLeft(), display.getAbsTop(), display.getWidth(), display.getHeight(), BFont.TEMP_FONT);
	}

	private void drawText(T context, String text, BColor textColor, BColor bgColor, boolean alignRight, int left, int top, int width, int height, BFont font) {
		canvas.drawText(context, text, textColor, bgColor, alignRight, left, top, width, height, font);
	}

	private void drawSensor(T g, BreweryComponentDisplay display) {
		Sensor sensor = (Sensor) display.getComponent();
		Double tempatureC = sensor.getTempatureC();
		if (tempatureC != null) {
			final Converter<Double, Double> tempDisplayConveter = conversion.getTempDisplayConveter();
			String tempDisplay = numberFormat.format(tempDisplayConveter.convertFrom(tempatureC)) + "\u00B0";

			BColor textColor;
			if (sensor.isReading()) {
				textColor = BColor.FOREGROUND;
			} else {
				textColor = BColor.ERROR;
			}
			drawText(g, tempDisplay, textColor, BColor.TANK, false, display.getAbsLeft(), display.getAbsTop(), display.getWidth(), 30, BFont.TEMP_FONT);

			boolean clearText = true;
			ControlStep selectedStep = controller.getSelectedStep();
			if (selectedStep != null) {
				ControlPoint cp = selectedStep.getControlPointForAddress(sensor.getAddress());
				if (cp != null && cp.isAutomaticControl()) {
					// if (selectedStep.isActive()) {
					// if (cp.getTargetTemp() != sensor.getTargetTemp()) {
					// textColor = Color.PENDING;
					// }
					// }
					clearText = false;
					tempDisplay = numberFormatWhole.format(tempDisplayConveter.convertFrom(cp.getTargetTemp())) + "\u00B0";
					drawText(g, "(" + tempDisplay + ")", textColor, BColor.TANK, false, display.getAbsLeft(), display.getAbsTop() + 30, display.getWidth(), 30, BFont.TEMP_TARGET_FONT);
				}
			}

			if (clearText) {
				drawText(g, "", textColor, BColor.TANK, false, display.getAbsLeft(), display.getAbsTop() + 30, display.getWidth(), 30, BFont.TEMP_TARGET_FONT);
			}
		}
	}

	// private void drawName(T context, BreweryComponentDisplay display) {
	// String name = display.getComponent().getName();
	//
	// canvas.drawText(context, name, BColor.FOREGROUND, BColor.BACKGROUND,
	// true, display.getAbsLeft(), display.getAbsTop(), width, height,
	// BFont.TEMP_TARGET_FONT);
	// }

	private void drawPump(T g, BreweryComponentDisplay display) {
		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight() - 15;

		// drawG.setColor(Color.BACKGROUND);
		// drawG.fillRect(0, 0, width, height);

		Pump pump = (Pump) display.getComponent();
		boolean on = pump.isOn();

		BColor backPaint = null;

		ControlStep selectedStep = controller.getSelectedStep();
		if (selectedStep != null) {
			ControlPoint controlPointForPin = selectedStep.getControlPointForPin(pump.getPin());
			if (controlPointForPin != null) {
				int cpDuty = controlPointForPin.getDuty();

				if (controlPointForPin.isAutomaticControl()) {
					backPaint = BColor.INACTIVE;
				}

				if (selectedStep.isActive()) {

					if (controlPointForPin.isAutomaticControl()) {

					} else {
						if (on != cpDuty > 0) {
							on = cpDuty > 0;
							backPaint = BColor.PENDING;
						}
					}
				} else {
					on = cpDuty > 0;
				}
			}
		}

		if (backPaint == null) {
			backPaint = (on ? BColor.PUMP_ON : BColor.PUMP_OFF);
		}

		canvas.drawPump(g, left, top, width, height, backPaint, on);

	}

	private void drawTank(T g, BreweryComponentDisplay display) {
		int top = display.getTop();
		int left = display.getLeft();
		int width = display.getWidth();
		int height = display.getHeight();
		canvas.drawTank(g, left, top, width, height);
	}

}
