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

package org.jupnp.test.gena;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jupnp.mock.MockUpnpService;
import org.jupnp.mock.MockUpnpServiceConfiguration;
import org.jupnp.model.gena.CancelReason;
import org.jupnp.model.gena.LocalGENASubscription;
import org.jupnp.model.message.StreamRequestMessage;
import org.jupnp.model.message.gena.IncomingEventRequestMessage;
import org.jupnp.model.message.gena.OutgoingEventRequestMessage;
import org.jupnp.model.meta.LocalDevice;
import org.jupnp.model.meta.LocalService;
import org.jupnp.model.meta.RemoteDevice;
import org.jupnp.model.meta.RemoteService;
import org.jupnp.model.state.StateVariableValue;
import org.jupnp.transport.impl.GENAEventProcessorImpl;
import org.jupnp.transport.spi.GENAEventProcessor;
import org.jupnp.test.data.SampleData;
import org.testng.annotations.Test;


public class EventXMLProcessingTest {

    public static final String EVENT_MSG =
        "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\"?>" +
            "<e:propertyset xmlns:e=\"urn:schemas-upnp-org:event-1-0\">" +
            "<e:property>" +
            "<Status>0</Status>" +
            "</e:property>" +
            "<e:property>" +
            "<SomeVar></SomeVar>" +
            "</e:property>" +
            "</e:propertyset>";

    @Test
    public void writeReadRequest() throws Exception {
        MockUpnpService upnpService = new MockUpnpService(new MockUpnpServiceConfiguration(){
            @Override
            public GENAEventProcessor getGenaEventProcessor() {
                return new GENAEventProcessorImpl();
            }
        });
        writeReadRequest(upnpService);
    }

    public void writeReadRequest(MockUpnpService upnpService) throws Exception {

        LocalDevice localDevice = GenaSampleData.createTestDevice(GenaSampleData.LocalTestService.class);
        LocalService localService = localDevice.getServices()[0];

        List<URL> urls = new ArrayList() {{
            add(SampleData.getLocalBaseURL());
        }};
        
        LocalGENASubscription subscription =
                new LocalGENASubscription(localService, 1800, urls) {
                    public void failed(Exception ex) {
                        throw new RuntimeException("TEST SUBSCRIPTION FAILED: " + ex);
                    }

                    public void ended(CancelReason reason) {

                    }

                    public void established() {

                    }

                    public void eventReceived() {

                    }
                };

        OutgoingEventRequestMessage outgoingCall =
                new OutgoingEventRequestMessage(subscription, subscription.getCallbackURLs().get(0));

        upnpService.getConfiguration().getGenaEventProcessor().writeBody(outgoingCall);

        assertEquals(outgoingCall.getBody(), EVENT_MSG);

        StreamRequestMessage incomingStream = new StreamRequestMessage(outgoingCall);

        RemoteDevice remoteDevice = SampleData.createRemoteDevice();
        RemoteService remoteService = SampleData.getFirstService(remoteDevice);

        IncomingEventRequestMessage incomingCall = new IncomingEventRequestMessage(incomingStream, remoteService);

        upnpService.getConfiguration().getGenaEventProcessor().readBody(incomingCall);

        assertEquals(incomingCall.getStateVariableValues().size(), 2);

        boolean gotValueOne = false;
        boolean gotValueTwo = false;
        for (StateVariableValue stateVariableValue : incomingCall.getStateVariableValues()) {
            if (stateVariableValue.getStateVariable().getName().equals("Status")) {
                gotValueOne = (!(Boolean) stateVariableValue.getValue());
            }
            if (stateVariableValue.getStateVariable().getName().equals("SomeVar")) {
                // TODO: So... can it be null at all? It has a default value...
                gotValueTwo = stateVariableValue.getValue() == null;
            }
        }
        assertTrue(gotValueOne && gotValueTwo);
    }


}
