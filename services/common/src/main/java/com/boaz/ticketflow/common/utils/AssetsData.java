package com.boaz.ticketflow.common.utils;

import java.time.ZoneId;

public class AssetsData {
    public static final String bucketName = "dealx-334a3.firebasestorage.app";
    public static final String firebaseResourcePath = "/firebase_service.json";
    public static ZoneId johannesburgZone = ZoneId.of("Africa/Johannesburg");
    public static ZoneId systemDefault = ZoneId.systemDefault();
    public static final String privateKeyPath = "/etc/4dealx/keys/private_key.pem";
    public static final String publicKeyPath = "/etc/4dealx/keys/public_key.pem";
}
