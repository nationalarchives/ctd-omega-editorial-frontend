#!/bin/bash
echo Configuring application..
sudo adduser --system --shell /sbin/nologin ctd-web
sudo chown -R ctd-web:ctd-web /opt/ctd-omega-editorial-frontend
sudo cp /opt/ctd-omega-editorial-frontend/scripts/ctd-omega-editorial-frontend.service /etc/systemd/system
sudo chmod 755 /etc/systemd/system/ctd-omega-editorial-frontend.service
sudo systemctl daemon-reload
sudo systemctl enable ctd-omega-editorial-frontend
