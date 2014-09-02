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

package org.jupnp.test.model;

import org.jupnp.model.Constants;
import org.jupnp.model.Location;
import org.jupnp.model.NetworkAddress;
import org.jupnp.model.types.NotificationSubtype;
import org.jupnp.model.message.header.HostHeader;
import org.jupnp.model.message.header.MaxAgeHeader;
import org.jupnp.model.message.header.USNRootDeviceHeader;
import org.jupnp.model.message.header.UpnpHeader;
import org.jupnp.model.message.header.ServerHeader;
import org.jupnp.model.message.header.EXTHeader;
import org.jupnp.model.message.header.InterfaceMacHeader;
import org.jupnp.model.message.UpnpMessage;
import org.jupnp.model.message.UpnpRequest;
import org.jupnp.model.message.OutgoingDatagramMessage;
import org.jupnp.model.message.discovery.OutgoingNotificationRequestRootDevice;
import org.jupnp.transport.spi.DatagramProcessor;
import org.jupnp.transport.impl.NetworkAddressFactoryImpl;
import org.jupnp.DefaultUpnpServiceConfiguration;
import org.jupnp.test.data.SampleData;
import org.jupnp.test.data.SampleDeviceRoot;
import org.jupnp.util.io.HexBin;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;

import static org.testng.Assert.*;


public class DatagramParsingTest {

    @Test
    public void readSource() throws Exception {

        String source = "NOTIFY * HTTP/1.1\r\n" +
                        "HOST: 239.255.255.250:1900\r\n" +
                        "CACHE-CONTROL: max-age=2000\r\n" +
                        "LOCATION: http://localhost:0/some/path/123/desc.xml\r\n" +
                        "X-CLING-IFACE-MAC: 00:17:ab:e9:65:a0\r\n" +
                        "NT: upnp:rootdevice\r\n" +
                        "NTS: ssdp:alive\r\n" +
                        "EXT:\r\n" +
                        "SERVER: foo/1 UPnP/1.0" + // FOLDED HEADER LINE!
                        " bar/2\r\n" +
                        "USN: " + SampleDeviceRoot.getRootUDN().toString()+"::upnp:rootdevice\r\n\r\n";

        DatagramPacket packet = new DatagramPacket(source.getBytes(), source.getBytes().length, new InetSocketAddress("123.123.123.123", 1234));

        DatagramProcessor processor = new DefaultUpnpServiceConfiguration().getDatagramProcessor();

        UpnpMessage<UpnpRequest> msg = processor.read(InetAddress.getByName("127.0.0.1"), packet);

        assertEquals(msg.getOperation().getMethod(), UpnpRequest.Method.NOTIFY);

        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.HOST, HostHeader.class).getValue().getHost(), Constants.IPV4_UPNP_MULTICAST_GROUP);
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.HOST, HostHeader.class).getValue().getPort(), Constants.UPNP_MULTICAST_PORT);
        assertEquals(
            msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN, USNRootDeviceHeader.class).getValue().getIdentifierString(),
            SampleDeviceRoot.getRootUDN().getIdentifierString()
        );
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.MAX_AGE, MaxAgeHeader.class).getValue().toString(), "2000");
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getOsName(), "foo");
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getOsVersion(), "1");
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getMajorVersion(), 1);
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getMinorVersion(), 0);
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getProductName(), "bar");
        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER, ServerHeader.class).getValue().getProductVersion(), "2");

        // Doesn't belong in this message but we need to test empty header values
        assert msg.getHeaders().getFirstHeader(UpnpHeader.Type.EXT) != null;

        assertEquals(msg.getHeaders().getFirstHeader(UpnpHeader.Type.EXT_IFACE_MAC, InterfaceMacHeader.class).getString(), "00:17:AB:E9:65:A0");

    }

    @Test
    public void parseRoundtrip() throws Exception {
        Location location = new Location(
                new NetworkAddress(
                        InetAddress.getByName("localhost"),
                        NetworkAddressFactoryImpl.DEFAULT_TCP_HTTP_LISTEN_PORT,
                        HexBin.stringToBytes("00:17:AB:E9:65:A0", ":")
                ),
                "/some/path/123/desc/xml"
        );

        OutgoingDatagramMessage msg =
                new OutgoingNotificationRequestRootDevice(
                        location,
                        SampleData.createLocalDevice(),
                        NotificationSubtype.ALIVE
                );

        msg.getHeaders().add(UpnpHeader.Type.EXT, new EXTHeader()); // Again, the empty header value

        DatagramProcessor processor = new DefaultUpnpServiceConfiguration().getDatagramProcessor();

        DatagramPacket packet = processor.write(msg);

        Assert.assertTrue(new String(packet.getData()).endsWith("\r\n\r\n"));

        UpnpMessage readMsg = processor.read(InetAddress.getByName("127.0.0.1"), packet);

        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.HOST).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.HOST).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.MAX_AGE).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.MAX_AGE).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.LOCATION).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.LOCATION).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.NT).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.NT).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.NTS).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.NTS).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.SERVER).getString());
        assertEquals(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.USN).getString(), msg.getHeaders().getFirstHeader(UpnpHeader.Type.USN).getString());
        assertNotNull(readMsg.getHeaders().getFirstHeader(UpnpHeader.Type.EXT));
    }

}
