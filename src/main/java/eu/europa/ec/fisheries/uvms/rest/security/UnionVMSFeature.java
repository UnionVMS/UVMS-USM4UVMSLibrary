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
    /** Managing movements */
    manageMovements,
	/** View movement search groups */
	viewMovementGroups,
	/** Manage movement search groups */
	manageMovementGroups,
    /** Accessing movement configuration */
    movementConfig,

    /** Viewing exchange logs */
    viewExchangeLog,
	/** Updating exchange log */
	updateExchangeLog,
    /** Resending exchange logs */
    resendExchangeLog,
    /** Forwarding exchange logs */
    forwardExchangeLog,
    /** Viewing sending queue */
    viewSendingQueue,
    /** Pausing messages in sending queue */
    manageSendingQueue,
    /** Viewing transmission status */
    viewTransmissionStatus,
    /** Starting and stopping transmissions */
    manageTransmissionStatus,
    /** Accessing exchange log configuration */
    exchangeConfig,

    /** Viewing audit logs */
    viewAuditLogs,
    /** Accessing audit log configuration */
    auditLogConfig
}
