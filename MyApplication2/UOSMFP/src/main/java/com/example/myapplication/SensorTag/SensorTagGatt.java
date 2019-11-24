package com.example.myapplication.SensorTag;

import java.util.UUID;

import static java.util.UUID.fromString;

/**
 * A list of all the UUIDs from the different services and characteristics in the sensor tag
 *
 */
public class SensorTagGatt {

    public final static UUID

    UUID_DEVINFO_SERV = fromString("0000180a-0000-1000-8000-00805f9b34fb"),

    UUID_DEVINFO_FWREV = fromString("00002A26-0000-1000-8000-00805f9b34fb"),

    UUID_NOTIFICATIONS = fromString("00002902-0000-1000-8000-00805f9b34fb"),




    UUID_ACC_SERV = fromString("f000aa10-0451-4000-b000-000000000000"),
    UUID_ACC_DATA = fromString("f000aa11-0451-4000-b000-000000000000"),
    UUID_ACC_CONF = fromString("f000aa12-0451-4000-b000-000000000000"), // 0: disable, 1: enable
    UUID_ACC_PERI = fromString("f000aa13-0451-4000-b000-000000000000"), // Period in tens of milliseconds



    UUID_MAG_SERV = fromString("f000aa30-0451-4000-b000-000000000000"),
    UUID_MAG_DATA = fromString("f000aa31-0451-4000-b000-000000000000"),
    UUID_MAG_CONF = fromString("f000aa32-0451-4000-b000-000000000000"), // 0: disable, 1: enable
    UUID_MAG_PERI = fromString("f000aa33-0451-4000-b000-000000000000"), // Period in tens of milliseconds



    UUID_GYR_SERV = fromString("f000aa50-0451-4000-b000-000000000000"),
    UUID_GYR_DATA = fromString("f000aa51-0451-4000-b000-000000000000"),
    UUID_GYR_CONF = fromString("f000aa52-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
    UUID_GYR_PERI = fromString("f000aa53-0451-4000-b000-000000000000"), // Period in tens of milliseconds


    UUID_MOV_SERV = fromString("f000aa80-0451-4000-b000-000000000000"),
    UUID_MOV_DATA = fromString("f000aa81-0451-4000-b000-000000000000"),
    UUID_MOV_CONF = fromString("f000aa82-0451-4000-b000-000000000000"), // 0: disable, bit 0: enable x, bit 1: enable y, bit 2: enable z
    UUID_MOV_PERI = fromString("f000aa83-0451-4000-b000-000000000000"), // Period in tens of milliseconds


    //LEDs
    UUID_TST_SERV = fromString("f000aa64-0451-4000-b000-000000000000"),
    UUID_TST_DATA = fromString("f000aa65-0451-4000-b000-000000000000"), // Test result
    UUID_TST_CONF = fromString("f000aa66-0451-4000-b000-000000000000"), // 0: local mode, 1: remote mode, 2: test mode

    //Simple Keys Service
    UUID_KEY_SERV = fromString("0000ffe0-0000-1000-8000-00805f9b34fb"),
    UUID_KEY_DATA = fromString("0000ffe1-0000-1000-8000-00805f9b34fb");


}
