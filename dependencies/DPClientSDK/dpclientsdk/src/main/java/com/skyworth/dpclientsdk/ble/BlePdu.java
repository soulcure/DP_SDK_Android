package com.skyworth.dpclientsdk.ble;

import androidx.annotation.NonNull;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BlePdu {

    public static final byte TEMP_CMD = 0x00;   //原通道控制指令
    public static final byte TEMP_PROTO = 0x01; //新增临时连接的协议命名

    /****************************************************
     * basic unit of data type length
     */
    public static final int PDU_BASIC_LENGTH = 1;

    /****************************************************
     * pdu header length
     */
    public static final int PDU_HEADER_LENGTH = 4;
    public static final int PDU_CRC_LENGTH = 1;


    public static final int PDU_BODY_LENGTH_INDEX = 1;

    /****************************************************
     * index 0. pos:[0-1)
     * the begin flag of a pdu.
     */
    public static final byte flag = (byte) 0xAA;

    /****************************************************
     *
     * index 1. pos:[1-3)
     */
    public short length;

    /****************************************************
     * index 2. pos:[3-4)
     *
     */
    public byte cmd;


    /***************************************************
     * index 3. pos:[4-N)
     */
    public byte[] body;

    /**
     * index 4. pos:[4+N-5+N)
     * crc8
     */
    public byte crc;


    /**
     * 将PduBase序列化为网络发送ByteBuffer
     *
     * @return 网络发送ByteBuffer
     */
    public ByteBuffer serializePdu() {

        int length = body.length + BlePdu.PDU_HEADER_LENGTH + BlePdu.PDU_CRC_LENGTH;

        ByteBuffer byteBuffer = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.clear();


        byteBuffer.put((byte) (BlePdu.flag & 0xFF));
        byteBuffer.putShort((short) (length & 0xFFFF));
        byteBuffer.put((byte) (cmd & 0xFF));
        byteBuffer.put(body);

        byte[] list = byteBuffer.array();
        byte[] crcList = Arrays.copyOfRange(list, 1, list.length);
        byte crc = crc8(crcList);

        byteBuffer.put(crc);
        return byteBuffer;


    }


    // 计算 CRC8 校验码
    public static byte crc8(byte[] data) {
        byte crc = 0;
        for (byte b : data) {
            crc ^= b;
            for (int i = 0; i < 8; i++) {
                boolean bitSet = ((crc & 0x80) != 0);
                crc <<= 1;
                if (bitSet) {
                    crc ^= 0x07;  // CRC8 多项式为 x^8 + x^2 + x + 1，用二进制表示为 0000 0111
                }
            }
        }
        return crc;
    }



    @NonNull
    @Override
    public String toString() {
        return "BlePdu{" +
                "length=" + length +
                ", cmd=" + cmd +
                ", body=" + body.length +
                ", crc=0x" + String.format("%02x", crc) +
                '}';
    }
}


