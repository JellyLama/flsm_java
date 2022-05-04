package com.flsm;

import java.util.ArrayList;
import java.util.Scanner;

public class App {
    public static void main(String[] args) {

        // user input for ip and sm
        System.out.println("===================================================");
        Scanner scanner = new Scanner(System.in);
        String starterIp = "";
        boolean valid = false;
        while(!valid){
            System.out.println("-Enter ip address: ");
            starterIp = scanner.next();
            valid = ipv4Address.validateIpv4(starterIp);

            if(!valid)
                System.out.println("INVALID IP ADDRESS!");
        }
        System.out.println("-Enter subnet mask: ");
        String starterSm = scanner.next();
        
        // user input for number of LANs and number of HOSTs for each LAN
        System.out.println("-Enter the number of LANs: ");
        int nLan = scanner.nextInt();
        System.out.println("-Hosts (network and broadcast IPs excluded) for each LAN: ");
        int hosts = scanner.nextInt() + 2;

        // user input to exclude or not the first network IP
        char excludefirstIp = '0';
        while (excludefirstIp != 'y' && excludefirstIp != 'n') {
            System.out.println("-Do you want to exclude the first and last network IPs [y/n]? ");
            excludefirstIp = scanner.next().charAt(0);
        }
        scanner.close();

        // prints the starter address
        ipv4Address starterAddress = new ipv4Address(starterIp, starterSm);
        System.out.println("=================-Starter address-=================\n" + starterAddress.toString());

        // calculates the subnets
        System.out.println("=====================-Subnets-=====================");
        ArrayList<ipv4Address> subnets = new ArrayList<ipv4Address>();
        ipv4Address unassignedSubnet = new ipv4Address(starterAddress.getNetworkId(), starterAddress.getSubnetMask());

        // finds the host id
        boolean found = false;
        int hostId = 0;
        while (!found) {
            if (Math.pow(2, hostId) >= hosts)
                found = true;
            else
                hostId++;
        }
        int netId = 32 - hostId;

        unassignedSubnet.setSubnetMask(netId);

        for (int i = 0; i < nLan; i++) {

            int[] subnetMask = unassignedSubnet.getSubnetMask();
            int[] newIp = unassignedSubnet.getNetworkId();

            if (excludefirstIp == 'n')
                // adds subnet to array
                subnets.add(subnets.size(), new ipv4Address(newIp, subnetMask));

            // updates unassigned ip (network id for the next subnet)
            // calculates ip's sector to update, -1 because index starts from 0
            int sector = (netId / 8) - 1;
            if (netId % 8 != 0)
                sector++;

            int newSectorValue = ((int) Math.pow(2, hostId % 8)) + newIp[sector];

            // if the value of the updated sector exceeds the max value (defined by the
            // subnet mask) then sector--
            while (newSectorValue > subnetMask[sector]) {
                newIp[sector] = 0;
                sector--;
                newSectorValue = newIp[sector] + 1;
            }

            newIp[sector] = newSectorValue;
            unassignedSubnet.setIp(newIp);

            if (excludefirstIp == 'y')
                // adds subnet to array
                subnets.add(subnets.size(), new ipv4Address(newIp, subnetMask));
        }

        // prints the subnets
        for (int i = 0; i < nLan; i++) {
            System.out.println("[LAN " + (i + 1) + "]\n" + subnets.get(i).toString() + "\n");
        }
    }
}
