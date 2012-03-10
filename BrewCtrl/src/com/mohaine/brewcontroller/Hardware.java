/*
    Copyright 2009-2011 Michael Graessle

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

package com.mohaine.brewcontroller;

import java.util.List;

import com.mohaine.brewcontroller.bean.ControlPoint;
import com.mohaine.brewcontroller.bean.HardwareControl;
import com.mohaine.brewcontroller.bean.HardwareSensor;
import com.mohaine.brewcontroller.bean.HardwareStatus;
import com.mohaine.event.HandlerRegistration;
import com.mohaine.event.StatusChangeHandler;

public interface Hardware {

	public List<HardwareSensor> getSensors();

	public HardwareStatus getHardwareStatus();

	public void setHardwareControl(HardwareControl hc);

	public HandlerRegistration addStatusChangeHandler(StatusChangeHandler handler);

	public void fireStateChangeHandlers();

	public String getStatus();

	public Double getSensorTemp(String tunSensor);

	public List<ControlPoint> getControlPoints();

}
