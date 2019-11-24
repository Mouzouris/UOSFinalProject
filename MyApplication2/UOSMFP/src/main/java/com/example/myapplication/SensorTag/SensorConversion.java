package com.example.myapplication.SensorTag;

import android.bluetooth.BluetoothGattCharacteristic;

import com.example.myapplication.Model.Point3D;

import java.util.UUID;

import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_ACC_CONF;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_ACC_DATA;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_ACC_SERV;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_GYR_CONF;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_GYR_DATA;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_GYR_SERV;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MAG_CONF;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MAG_DATA;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MAG_SERV;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MOV_CONF;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MOV_DATA;
import static com.example.myapplication.SensorTag.SensorTagGatt.UUID_MOV_SERV;

public enum SensorConversion {



    MOVEMENT_ACC(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte) 3) {
        @Override
        public Point3D convert(final byte[] value) {
            // Range 8G
            final float SCALE = (float) 4096.0;

            int x = (value[7] << 8) + value[6];
            int y = (value[9] << 8) + value[8];
            int z = (value[11] << 8) + value[10];

            return new Point3D(((x / SCALE) * -1), y / SCALE, ((z / SCALE) * -1));
        }
    },
    MOVEMENT_GYRO(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte) 3) {
        @Override
        public Point3D convert(final byte[] value) {


            final float SCALE = (float) 128.0;

            int x = (value[1] << 8) + value[0];
            int y = (value[3] << 8) + value[2];
            int z = (value[5] << 8) + value[4];
            return new Point3D(x / SCALE, y / SCALE, z / SCALE);

        }
    },
    MOVEMENT_MAG(UUID_MOV_SERV, UUID_MOV_DATA, UUID_MOV_CONF, (byte) 3) {
        @Override
        public Point3D convert(final byte[] value) {

            final float SCALE = (float) (32768 / 4912);
            if (value.length >= 18) {
                int x = (value[13] << 8) + value[12];
                int y = (value[15] << 8) + value[14];
                int z = (value[17] << 8) + value[16];
                return new Point3D(x / SCALE, y / SCALE, z / SCALE);
            } else return new Point3D(0, 0, 0);
        }
        },


    ACCELEROMETER(UUID_ACC_SERV, UUID_ACC_DATA, UUID_ACC_CONF,(byte)3) {
        @Override
        public Point3D convert(final byte[] value) {
//            DeviceActivity da = DeviceActivity.getInstance();
//
//            if (da.isSensorTag2()) {
                // Range 8G
                final float SCALE = (float) 4096.0;

                int x = (value[0]<<8) + value[1];
                int y = (value[2]<<8) + value[3];
                int z = (value[4]<<8) + value[5];
                return new Point3D(x / SCALE, y / SCALE, z / SCALE);
//            } else {
//                Point3D v;
//                Integer x = (int) value[0];
//                Integer y = (int) value[1];
//                Integer z = (int) value[2] * -1;
//
//                if (da.firmwareRevision().contains("1.5"))
//                {
//                    // Range 8G
//                    final float SCALE = (float) 64.0;
//                    v = new Point3D(x / SCALE, y / SCALE, z / SCALE);
//                } else {
//                    // Range 2G
//                    final float SCALE = (float) 16.0;
//                    v = new Point3D(x / SCALE, y / SCALE, z / SCALE);
//                }
//                return v;
//            }
//
        }
    },


    MAGNETOMETER(UUID_MAG_SERV,UUID_MAG_DATA, UUID_MAG_CONF) {
        @Override
        public Point3D convert(final byte [] value) {
            Point3D mcal = MagnetometerCalibrationCoefficients.INSTANCE.val;
            // Multiply x and y with -1 so that the values correspond with the image in the app
            float x = shortSignedAtOffset(value, 0) * (2000f / 65536f) * -1;
            float y = shortSignedAtOffset(value, 2) * (2000f / 65536f) * -1;
            float z = shortSignedAtOffset(value, 4) * (2000f / 65536f);

            return new Point3D(x - mcal.x, y - mcal.y, z - mcal.z);
        }
    },


    GYROSCOPE(UUID_GYR_SERV, UUID_GYR_DATA, UUID_GYR_CONF, (byte)7) {
        @Override
        public Point3D convert(final byte [] value) {

            float y = shortSignedAtOffset(value, 0) * (500f / 65536f) * -1;
            float x = shortSignedAtOffset(value, 2) * (500f / 65536f);
            float z = shortSignedAtOffset(value, 4) * (500f / 65536f);

            return new Point3D(x,y,z);
        }
    };




    /**
     * Gyroscope, Magnetometer, Barometer, IR temperature all store 16 bit two's complement values as LSB MSB, which cannot be directly parsed
     * as getIntValue(FORMAT_SINT16, offset) because the bytes are stored as little-endian.
     *
     * This function extracts these 16 bit two's complement values.
     * */
    private static Integer shortSignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1]; // // Interpret MSB as signed
        return (upperByte << 8) + lowerByte;
    }
    private static Integer shortUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer upperByte = (int) c[offset+1] & 0xFF;
        return (upperByte << 8) + lowerByte;
    }
    private static Integer twentyFourBitUnsignedAtOffset(byte[] c, int offset) {
        Integer lowerByte = (int) c[offset] & 0xFF;
        Integer mediumByte = (int) c[offset+1] & 0xFF;
        Integer upperByte = (int) c[offset + 2] & 0xFF;
        return (upperByte << 16) + (mediumByte << 8) + lowerByte;
    }




    public void onCharacteristicChanged(BluetoothGattCharacteristic c) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }


    public Point3D convert(byte[] value) {
        throw new UnsupportedOperationException("Error: the individual enum classes are supposed to override this method.");
    }

    private final UUID service, data, config;
    private byte enableCode; // See getEnableSensorCode for explanation.
    public static final byte DISABLE_SENSOR_CODE = 0;
    public static final byte ENABLE_SENSOR_CODE = 1;
    public static final byte CALIBRATE_SENSOR_CODE = 2;

    /**
     * Constructor called by the Motion sensors because it more than a boolean enable code.
     */
     SensorConversion(UUID service, UUID data, UUID config, byte enableCode) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = enableCode;
    }

    /**
     * Constructor called by all the sensors except Gyroscope
     * */
     SensorConversion(UUID service, UUID data, UUID config) {
        this.service = service;
        this.data = data;
        this.config = config;
        this.enableCode = ENABLE_SENSOR_CODE; // This is the sensor enable code for all sensors except the gyroscope
    }

    /**
     * @return the code which, when written to the configuration characteristic, turns on the sensor.
     * */
    public byte getEnableSensorCode() {
        return enableCode;
    }

    public UUID getService() {
        return service;
    }

    public UUID getData() {
        return data;
    }

    public UUID getConfig() {
        return config;
    }

    public static SensorConversion getFromDataUuid(UUID uuid) {
        for (SensorConversion s : SensorConversion.values()) {
            if (s.getData().equals(uuid)) {
                return s;
            }
        }
        throw new RuntimeException("unable to find UUID.");
    }


}
