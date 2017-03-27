#!/usr/bin/env bash

# Reset iptables rules
iptables -F

# Allow all traffic from localhost
iptables -A INPUT -s 127.0.0.1 -p tcp -j ACCEPT

# Redis
iptables -A INPUT -s 173.255.241.74 -p tcp -m tcp --dport 6379 -j ACCEPT

# Mongodb
iptables -A INPUT -s 173.255.241.74 -p tcp --destination-port 27017 -m state --state NEW,ESTABLISHED -j ACCEPT
iptables -A OUTPUT -d 173.255.241.74 -p tcp --source-port 27017 -m state --state ESTABLISHED -j ACCEPT

# traffic to and from mongodb config server
iptables -A INPUT -s 173.255.241.74 -p tcp --destination-port 27019 -m state --state NEW,ESTABLISHED -j ACCEPT
iptables -A OUTPUT -d 173.255.241.74 -p tcp --source-port 27019 -m state --state ESTABLISHED -j ACCEPT

# mongodb shard server
#iptables -A INPUT -s <ip-address> -p tcp --destination-port 27018 -m state --state NEW,ESTABLISHED -j ACCEPT
#iptables -A OUTPUT -d <ip-address> -p tcp --source-port 27018 -m state --state ESTABLISHED -j ACCEPT

# SSH (replace 22 with the port you use)
iptables -A INPUT -p tcp --dport 22 -j ACCEPT

# Drop all others
iptables -P INPUT DROP
iptables -P FORWARD DROP
iptables -P OUTPUT ACCEPT

# update iptables
bash -c 'iptables-save > /etc/sysconfig/iptables'
