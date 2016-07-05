/*
﻿Developed with the contribution of the European Commission - Directorate General for Maritime Affairs and Fisheries
© European Union, 2015-2016.

This file is part of the Integrated Fisheries Data Management (IFDM) Suite. The IFDM Suite is free software: you can
redistribute it and/or modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or any later version. The IFDM Suite is distributed in
the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more details. You should have received a
copy of the GNU General Public License along with the IFDM Suite. If not, see <http://www.gnu.org/licenses/>.
 */
package eu.europa.ec.fisheries.uvms.rest.security;

/**
 * Defines all known modules
 */
public enum UnionVMSModule {
	//We shouldn't limit the modules within an Enum, because if a new module is created, we have to do modifications in usm4uvms in order to have
	//the new module security working
	Audit,
	Config,
	Exchange,
	MobileTerminal,
	Movement,
	Reporting,
	Rules,
	Spatial,
	User,
	Vessel
}