#!/usr/bin/env python

from pcapy import findalldevs, open_live
from impacket import ImpactDecoder, ImpactPacket


def callback(hdr, data):
    packet=ImpactDecoder.EthDecoder().decode(data)
    print packet
    


def get_device():
    devices=findalldevs()
    i=0
    for i in xrange(len(devices)):
         print ("%i - %s" %(i+1, devices[i]))
         i=i+1
    num=raw_input("input device number : ")
    number=int(num)
    dev=devices[number]
    return dev


def sniff(device):
    print "listening on : %s" %device

    reader = open_live(device, 1500, 0, 100)

    ans=raw_input("do you want to set a filter (y/n) : ")
    if ans=="y":
         filt=raw_input("input filter ( ex: udp, tcp, icmp ) : ")
         reader.setfilter(filt)
    reader.loop(-1, callback)
    

def main():
    interface = get_device()
    if interface:
        sniff(interface)

if __name__ == "__main__":
    main()
