#!/usb/bin/python
#-*-coding: utf-8 -*-

import pygtk
pygtk.require20()
import gtk
import os, time, threading, json
from socket import *
import sys, base64
import mosquitto
from Crypto.Cipher import AES

BLOCK_SIZE = 16 # Block-size for cipher (16, 24 or 32 for AES)

iv = "fedcba9876543210"
key = "1234567812345678"
paddingChar=' '

pad = lambda s: s + (BLOCK_SIZE - len(s) % BLOCK_SIZE) * paddingChar


class Uygulama(object):
    def __init__(self):
        self.pencere = gtk.Window(gtk.WINDOW_TOPLEVEL)
        self.pencere.connect("delete_event", gtk.main_quit)        
        self.pencere.set_title("MOSQUITTO API")
        self.pencere.resize(400, 400)
        self.pencere.move(700, 600)
        self.pencere.show()
            
        self.entry = gtk.Entry()
        self.entry.modify_text(gtk.STATE_NORMAL, gtk.gdk.color_parse("red"))

        self.entry1 = gtk.Entry()
           
        self.lab1 = gtk.Label("TOPIC :")
        self.lab2 = gtk.Label("MESSAGE :")     
        
        self.dugme = gtk.Button("SEND MESSAGE")
        self.dugme.set_size_request(120, 35)
        self.dugme.modify_bg(gtk.STATE_NORMAL, gtk.gdk.Color("green"))
        self.dugme.connect("clicked", self.sendMessage)
        
        self.hazne = gtk.Fixed()
        self.pencere.add(self.hazne)

        self.hazne.put(self.entry, 110, 95)
        self.hazne.put(self.entry1, 110,  130)
        self.hazne.put(self.dugme, 130, 165)

        self.hazne.put(self.lab1, 20, 103)
        self.hazne.put(self.lab2, 20, 135)
        self.pencere.show_all()


    def encryptData(self, plaintext):
        self.JSON = plaintext
        self.ciphertext = pad(self.JSON)

        self.ciphertext = self.cipher.encrypt(self.ciphertext)

        self.ciphertext = self.ciphertext.encode('hex')
              
        return  self.ciphertext
       
    def sendMessage(self,a):
        self.client = mosquitto.Mosquitto("test-client")
        try:
           
            self.client.connect("127.0.0.1")
            self.client.loop()
        except ValueError:
            pass

        self.message= str(self.entry1.get_text())
        self.topic= str(self.entry.get_text())
        
        self.cipher= AES.new(key, AES.MODE_CBC, iv)
        self.ciphertext=self.encryptData(self.message)

        self.client.publish(self.topic,self.ciphertext,1)

                               
    def main(self):
        gtk.main()
                  
uyg = Uygulama()
uyg.main()
