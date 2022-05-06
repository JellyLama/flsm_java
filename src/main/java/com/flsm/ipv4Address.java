package com.flsm;

import java.util.StringJoiner;

public class ipv4Address implements Cloneable {
    private String ip;
    private String subnetMask;

    public ipv4Address(String ip, String subnetMask) {
        this.setIp(ip);
        this.setSubnetMask(subnetMask);
    }

    public String getIp() {
        return this.ip;
    }

    public void setIp(String ip) {
        if (validateIpv4(ip)) {
            this.ip = ip;
        } else
            throw new IllegalArgumentException("INVALID IP ADDRESS");
    }

    public String getSubnetMask() {
        return this.subnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        if (validateSubnetMask(subnetMask)) {
            if (subnetMask.contains(".")) {
                this.subnetMask = subnetMask;
            } else {
                this.subnetMask = ipv4Address.getSubnetMaskFromCidr(Integer.parseInt(subnetMask));
            }
        } else
            throw new IllegalArgumentException("INVALID SUBNETMASK");
    }

    public int getCidr() {
        int cidr = 0;
        String[] splittedSubnetMask = this.getSubnetMask().split("\\.");
        for (String s : splittedSubnetMask) {
            cidr += Integer.toBinaryString(Integer.parseInt(s)).lastIndexOf("1") + 1;
        }
        return cidr;
    }

    public String getUsableHostRange() {
        String range = "";
        String[] splittedNetworkId = this.getNetworkId().split("\\.");
        String[] splittedBroadcastIp = this.getBroadcastIp().split("\\.");

        // adds first usable ip
        splittedNetworkId[3] = "" + (Integer.parseInt(splittedNetworkId[3]) + 1);
        range += String.join(".", splittedNetworkId) + " - ";
        // adds last usable ip
        splittedBroadcastIp[3] = "" + (Integer.parseInt(splittedBroadcastIp[3]) - 1);
        range += String.join(".", splittedBroadcastIp);

        return range;
    }

    public String getBroadcastIp() {

        String[] splittedBroadcastIp = this.getNetworkId().split("\\.");
        String[] splittedSubnetMask = this.getSubnetMask().split("\\.");
        int bcIpSector = 0;
        int smSector = 0;
        for (int i = 0; i < 4; i++) {
            if (splittedSubnetMask[i] != "255") {
                bcIpSector = Integer.parseInt(splittedBroadcastIp[i]);
                smSector = Integer.parseInt(splittedSubnetMask[i]);
                splittedBroadcastIp[i] = "" + (bcIpSector | (~smSector) & 0xff);
            }
        }

        return String.join(".", splittedBroadcastIp);
    }

    public String getNetworkId() {
        String[] splittedNetworkId = this.getIp().split("\\.");
        String[] splittedSubnetMask = this.getSubnetMask().split("\\.");
        int ipSector = 0;
        int smMaskSector = 0;
        for (int i = 0; i < 4; i++) {
            if (splittedSubnetMask[i] != "255") {
                ipSector = Integer.parseInt(splittedNetworkId[i]);
                smMaskSector = Integer.parseInt(splittedSubnetMask[i]);
                splittedNetworkId[i] = "" + (ipSector & smMaskSector);
            }
        }

        return String.join(".", splittedNetworkId);
    }

    public static boolean validateIpv4(String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }

    public static boolean validateSubnetMask(String sm) {
        String cidrPattern = "^([1-9])$|^([12][0-9])$|^([3][01])$";

        if (validateIpv4(sm)) {
            String[] splittedSm = sm.split("\\.");

            int min = Integer.parseInt(splittedSm[0]);
            int sector;
            int fIndexOf0;
            int lIndexOf1;
            for (int i = 1; i < splittedSm.length; i++) {
                sector = Integer.parseInt(splittedSm[i]);
                fIndexOf0 = Integer.toBinaryString(sector).indexOf("0");
                lIndexOf1 = Integer.toBinaryString(sector).lastIndexOf("1");
                // checks if the subnetmask is like this 255.128.255.0 OR has binary value
                // without consecutive 1s, like 160 (1010 0000)
                // if sector == 255 then fIndexOf0 == -1 and lIndexOf1 == 7, without the last
                // check it would return false
                if (sector > min || fIndexOf0 < lIndexOf1 && sector < 255)
                    return false;
                else
                    min = sector;
            }
            return true;
        } else if (sm.matches(cidrPattern)) {
            return true;
        } else {
            return false;
        }
    }

    public static String getSubnetMaskFromCidr(int cidr) {
        StringJoiner subnetMask = new StringJoiner(".");
        int offset = 7;
        int sector = 0;
        for (int i = 0; i < 4; i++) {
            while (cidr > 0 && offset >= 0) {
                sector += Math.pow(2, offset);
                offset--;
                cidr--;
            }
            subnetMask.add("" + sector);
            sector = 0;
            offset = 7;
        }
        return subnetMask.toString();
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public String toString() {

        return "Netword ID: " + this.getNetworkId() + " /"
                + this.getCidr() + "\n" +
                "Subnet Mask: " + this.getSubnetMask() + "\n" +
                "Usable Host Range (" + ((int) Math.pow(2, 32 - this.getCidr()) - 2) + "): " + this.getUsableHostRange()
                + "\n" +
                "Broadcast IP: " + this.getBroadcastIp();
    }
}
