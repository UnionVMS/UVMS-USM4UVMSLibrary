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
