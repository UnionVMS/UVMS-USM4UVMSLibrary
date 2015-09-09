package eu.europa.ec.fisheries.uvms.rest.security;

/**
 * Defines all known features
 */
public enum UnionVMSFeature {
    /** View Vessels */
    getVesselList,
    getVesselById,
    /** Create vessels */
    createVessel,
    /** Update vessels */
    updateVessel,
    /** Access vessel history */
    vesselHistory,
    /** View vessel groups */
    viewVesselGroups,
    /** Manage vessel groups */
    manageVesselGroups,
    /** Accessing vessel configuration */
    vesselConfig,

    /** Viewing mobile terminals */
    viewMobileTerminals,
    /** Managing mobile terminals */
    manageMobileTerminals,
    /** Mobile terminal plugins */
    mobileTerminalPlugins,
    /** Accessing mobile terminal configuration */
    mobileTerminalConfig,

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

    /** Viewing polling logs */
    viewPollingLogs,
    /** Managing program polls */
    manageProgramPolls,
    /** Creating new polls */
    managePolls,
    /** Viewing pollable channels */
    viewPollableChannels,

    /** Viewing audit logs */
    viewAuditLogs,
    /** Accessing audit log configuration */
    auditLogConfig
}
