package eu.europa.ec.fisheries.uvms.rest.security;

/**
 * Defines all known features
 */
public enum UnionVMSFeature {

	/** Viewing both vessels and mobile terminals */
	viewVesselsAndMobileTerminals,
	/** Managing mobile terminals */
	manageMobileTerminals,
	/** Managing vessels */
	manageVessels,

	/** Viewing mobile terminal polls */
	viewMobileTerminalPolls,
	/** Managing mobile terminal polls */
	managePolls,

	mobileTerminalPlugins,
	
    /** Viewing movements */
    viewMovements,
    /** Viewing manual movements */
    viewManualMovements,
    /** Managing movements */
    manageManualMovements,

    /** Viewing exchange */
    viewExchange,
    /** Managing sending queue */
    manageExchangeSendingQueue,
    /** Managing transmission statuses */
    manageExchangeTransmissionStatuses,

    /** Rules */
    viewAlarmRules,
    manageAlarmRules,
    viewAlarmsHoldingTable,
    manageAlarmsHoldingTable,
    viewAlarmsOpenTickets,
    manageAlarmsOpenTickets,
    
    /** Viewing audit */
    viewAudit
}
