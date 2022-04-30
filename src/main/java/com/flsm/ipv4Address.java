package com.vlsm;

public class ipv4Address {
    private int[] ip;
    private int[] subnetMask;

    public ipv4Address(int[] ip, int[] subnetMask) {
        this.ip = ip.clone();
        this.subnetMask = subnetMask.clone();
    }

    public ipv4Address(String ip, String subnetMask) {
        // ip
        String[] splittedIp = ip.split("\\.");
        int[] intSplittedIp = { Integer.parseInt(splittedIp[0]), Integer.parseInt(splittedIp[1]),
                Integer.parseInt(splittedIp[2]), Integer.parseInt(splittedIp[3]) };
        this.ip = intSplittedIp;

        // subnet mask
        if (subnetMask.contains(".")) {
            String[] splittedSm = subnetMask.split("\\.");
            int[] intSplittedSm = { Integer.parseInt(splittedSm[0]), Integer.parseInt(splittedSm[1]),
                    Integer.parseInt(splittedSm[2]), Integer.parseInt(splittedSm[3]) };
            this.subnetMask = intSplittedSm;
        } else {
            int cidr = Integer.parseInt(subnetMask);
            this.setSubnetMask(cidr);
        }
    }

    public int[] getIp() {
        return ip.clone();
    }

    public void setIp(int[] ip) {
        this.ip = ip.clone();
    }

    public int[] getSubnetMask() {
        return subnetMask.clone();
    }

    public void setSubnetMask(int[] subnetMask) {
        this.subnetMask = subnetMask.clone();
    }

    public void setSubnetMask(int subnetMask) {
        int[] newSubnetMask = new int[4];
        int offset = 7;
        for (int i = 0; i < newSubnetMask.length; i++) {
            while (subnetMask > 0 && offset >= 0) {
                newSubnetMask[i] += Math.pow(2, offset);
                offset--;
                subnetMask--;
            }
            offset = 7;
        }

        this.subnetMask = newSubnetMask;
    }

    public void setSubnetMask(String subnetMask) {
        String[] splittedSm = subnetMask.split("\\.");
        int[] intSplittedSm = { Integer.parseInt(splittedSm[0]), Integer.parseInt(splittedSm[1]),
                Integer.parseInt(splittedSm[2]), Integer.parseInt(splittedSm[3]) };
        this.subnetMask = intSplittedSm;
    }

    public int getCidr() {
        int cidr = 0;
        for (int i : subnetMask) {
            cidr += Integer.toBinaryString(i).lastIndexOf("1") + 1;
        }
        return cidr;
    }

    public String getUsableHostRange() {
        String range = "";
        int[] networkId = this.getNetworkId();
        int[] broadcastIp = this.getBroadcastIp();

        // adds first usable ip
        range += networkId[0] + "." + networkId[1] + "." + networkId[2] + "." + (networkId[3] + 1) + " - ";
        // adds last usable ip
        range += broadcastIp[0] + "." + broadcastIp[1] + "." + broadcastIp[2] + "." + (broadcastIp[3] - 1);

        return range;
    }

    public int[] getBroadcastIp() {

        int[] broadcastIp = this.getNetworkId();
        for (int i = 0; i < this.subnetMask.length; i++) {
            if (this.subnetMask[i] != 255) {
                broadcastIp[i] |= ~this.subnetMask[i] & 0xff;
            }
        }

        return broadcastIp;
    }

    public int[] getNetworkId() {
        int[] networkId = this.getIp();
        for (int i = 0; i < this.subnetMask.length; i++) {
            if (this.subnetMask[i] != 255) {
                networkId[i] &= this.subnetMask[i];
            }
        }

        return networkId;
    }

    public String toString() {
        int[] broadcastIp = this.getBroadcastIp();
        int[] networkId = this.getNetworkId();

        return "Netword ID: " + networkId[0] + "." + networkId[1] + "." + networkId[2] + "." + networkId[3] + " /"
                + this.getCidr() + "\n" +
                "Subnet Mask: " + this.subnetMask[0] + "." + this.subnetMask[1] + "." + this.subnetMask[2] + "."
                + this.subnetMask[3] + "\n" +
                "Usable Host Range (" + ((int) Math.pow(2, 32 - this.getCidr()) - 2) + "): " + this.getUsableHostRange()
                + "\n" +
                "Broadcast IP: " + broadcastIp[0] + "." + broadcastIp[1] + "." + broadcastIp[2] + "." + broadcastIp[3];
    }
}
