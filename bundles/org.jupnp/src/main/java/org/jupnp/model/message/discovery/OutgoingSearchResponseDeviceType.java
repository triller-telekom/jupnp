/**
 * Copyright (C) 2014 4th Line GmbH, Switzerland and others
 *
 * The contents of this file are subject to the terms of the
 * Common Development and Distribution License Version 1 or later
 * ("CDDL") (collectively, the "License"). You may not use this file
 * except in compliance with the License. See LICENSE.txt for more
 * information.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 */

package org.jupnp.model.message.discovery;

import org.jupnp.model.Location;
import org.jupnp.model.message.IncomingDatagramMessage;
import org.jupnp.model.message.header.DeviceTypeHeader;
import org.jupnp.model.message.header.DeviceUSNHeader;
import org.jupnp.model.message.header.UpnpHeader;
import org.jupnp.model.meta.LocalDevice;

/**
 * @author Christian Bauer
 */
public class OutgoingSearchResponseDeviceType extends OutgoingSearchResponse {

    public OutgoingSearchResponseDeviceType(IncomingDatagramMessage request,
                                            Location location,
                                            LocalDevice device) {
        super(request, location, device);

        getHeaders().add(UpnpHeader.Type.ST, new DeviceTypeHeader(device.getType()));
        getHeaders().add(UpnpHeader.Type.USN, new DeviceUSNHeader(device.getIdentity().getUdn(), device.getType()));
    }

}