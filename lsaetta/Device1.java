/*
 * Copyright (c) 2015, 2016, Oracle and/or its affiliates.  All rights reserved.
 *
 * This software is dual-licensed to you under the MIT License (MIT) and 
 * the Universal Permissive License (UPL).  See the LICENSE file in the root
 * directory for license terms.  You may choose either license, or both.
 */

package lsaetta;

import oracle.iot.client.DeviceModel;
import oracle.iot.client.device.Alert;
import oracle.iot.client.device.DirectlyConnectedDevice;
import oracle.iot.client.device.VirtualDevice;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

/*
 * This sample presents a simple sensor as virtual
 * device to the IoT server.
 *
 * The simple sensor is polled every half second and the virtual device is
 * updated. An alert is generated when the value from the sensor exceeds
 * the maximum threshold of the sensor's data model.
 *
 * Note that the code is Java SE 1.5 compatible.
 */
public class Device1 
{

    private static final String DEVICE1_SENSOR_MODEL_URN =
        "urn:lsaetta:device1model1";

    private static final String TEMP_ATTRIBUTE = "temperature";
    
    public static boolean isUnderFramework = false;
    public static boolean exiting = false;

    public static void main(String[] args) 
    {
        DirectlyConnectedDevice dcd = null;

        try 
        {
            
            if (args.length != 2) {
                display("\nIncorrect number of arguments.\n");
                throw new IllegalArgumentException("");
            }
            
            // Initialize the device client
            dcd = new DirectlyConnectedDevice(args[0], args[1]);

            // Activate the device
            if (!dcd.isActivated()) {
                dcd.activate(DEVICE1_SENSOR_MODEL_URN);
            }

            // Create a virtual device implementing the device model
            DeviceModel dcdModel =
                dcd.getDeviceModel(DEVICE1_SENSOR_MODEL_URN);

            VirtualDevice virtualDevice =
                dcd.createVirtualDevice(dcd.getEndpointId(), dcdModel);


            display("\nCreated virtual Device1 sensor " +
                    dcd.getEndpointId() + "\n");

            // Initialize the virtual device from actual device
            int temp = 25;
            
            virtualDevice.update()
                .set(TEMP_ATTRIBUTE, temp).finish();


            display(new Date().toString() + " : " +
                virtualDevice.getEndpointId() +
                " : Set : \"temperature\"=" + temp);

            virtualDevice.setOnError(
                new VirtualDevice.ErrorCallback<VirtualDevice>() {
                    public void onError(VirtualDevice.ErrorEvent<VirtualDevice
                            > event) {
                        VirtualDevice device =  event.getVirtualDevice();
                        display(new Date().toString() + " : onError : " +
                                device.getEndpointId() +
                                " : \"" + event.getMessage() + "\"");
                    }
                });

            display("\n\tPress enter to exit.\n");

            /*
             * A flag to make sure alerts are only sent when
             * crossing the threshold.
             */
            boolean alerted = false;
            
            mainLoop:
            for (;;) 
            {
                // Wait 5 seconds before sending next readings.
                for (int i = 0; i < 50; i++) {
                    Thread.sleep(100);

                    // when running under framework, use framework specific exit
                    if (!isUnderFramework) {
                        // User pressed the enter key while sleeping, exit.
                        if (System.in.available() > 0) {
                            break mainLoop;
                        }
                    }
                }
                if (exiting)
                    break;

                int newValue = 26;

                display(new Date().toString() + " : " +
                        virtualDevice.getEndpointId() +
                        " : Set : \"temperature\"=" + newValue);

                virtualDevice.set(TEMP_ATTRIBUTE, newValue);
            }
        } catch (Throwable e) { 
            // catching Throwable, not Exception:
            // could be java.lang.NoClassDefFoundError
            // which is not Exception

            displayException(e);
            if (isUnderFramework) throw new RuntimeException(e);
        } finally 
        {
            // Dispose of the device client
            try {
                if (dcd != null) dcd.close();
            } catch (IOException ignored) {
            }
        }
    }
    
    private static void showUsage() {
        Class<?> thisClass = new Object() { }.getClass().getEnclosingClass();
        display("Usage: \n"
                + "java " + thisClass.getName()
                + " <trusted assets file> <trusted assets password>\n"
        );
    }

    private static void display(String string) {
        System.out.println(string);
    }

    private static void displayException(Throwable e) 
    {
        StringBuffer sb = new StringBuffer(e.getMessage() == null ? 
                  e.getClass().getName() : e.getMessage());
        if (e.getCause() != null) {
            sb.append(".\n\tCaused by: ");
            sb.append(e.getCause());
        }
        System.out.println('\n' + sb.toString() + '\n');
        showUsage();
    }
}
