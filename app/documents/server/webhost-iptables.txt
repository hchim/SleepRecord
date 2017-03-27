#!/usr/bin/env bash

# Reset iptables rules
iptables -F

# Allow all traffic from localhost
iptables -A INPUT -s 127.0.0.1 -p tcp -j ACCEPT

# web server
iptables -A INPUT -p tcp --dport 80 -j ACCEPT
iptables -A INPUT -p tcp --dport 443 -j ACCEPT

# Drop incoming connections if IP make more than 100 connection attempts to port 80 within 10 second
iptables -A INPUT -p tcp --dport 80 -i eth0 -m state --state NEW -m recent --set
iptables -A INPUT -p tcp --dport 80 -i eth0 -m state --state NEW -m recent --update --seconds 10  --hitcount 100 -j DROP

# Drop incoming connections if IP make more than 100 connection attempts to port 443 within 10 seconds
iptables -A INPUT -p tcp --dport 443 -i eth0 -m state --state NEW -m recent --set
iptables -A INPUT -p tcp --dport 443 -i eth0 -m state --state NEW -m recent --update --seconds 10  --hitcount 100 -j DROP

# SSH (replace 22 with the port you use)
iptables -A INPUT -p tcp --dport 22 -j ACCEPT

# Drop all others
sudo iptables -P INPUT DROP
sudo iptables -P FORWARD DROP
sudo iptables -P OUTPUT ACCEPT

# update iptables
sudo bash -c 'iptables-save > /etc/sysconfig/iptables'
