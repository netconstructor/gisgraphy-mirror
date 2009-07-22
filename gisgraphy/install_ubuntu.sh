
#POSTGRES

sudo apt-get install postgresql-8.3 postgresql-8.3-postgis nmap sysvconfig
sudo apt-get install  sysvconfig
sudo passwd postgres

su - postgres

sudo vi /etc/postgresql/8.3/main/pg_hba.conf
host 	all	 all 	YOURIP/24	password
local  all     all                                        ident sameuser
host   all     all    127.0.0.1         255.255.255.255   ident sameuser

vi /etc/postgresql/8.3/main/postgresql.conf

listen_addresses='YOURIP'

psql -d template1 -c "alter user postgres with password 'your_passwd'"





service postgresql-8.3 restart

sudo vi etc/hosts=>88.191.92.137 gisgraphy.com 

sudo iptables -A INPUT -p tcp -i eth0 --dport 5432 -d gisgraphy.com -j ACCEPT

#install jvm
sudo apt-get install sun-java6-bin sun-java6-fonts sun-java6-javadb sun-java6-jdk sun-java6-jre sun-java6-source ia32-sun-java6-bin 
valid screen (tab +ok)
accept license (TAB to select yes +OK)

java -version=>
java version "1.6.0_06"
Java(TM) SE Runtime Environment (build 1.6.0_06-b02)
Java HotSpot(TM) 64-Bit Server VM (build 10.0-b22, mixed mode)


sudo update-java-alternatives --set ia32-java-6-sun 
 update-alternatives: Impossible de trouver l'alternative « /usr/lib/jvm/ia32-java-6-sun/jre/plugin/i386/ns7/libjavaplugin_oji.so =>ok because no applet in 64 bits
 
 ls -l  /etc/alternatives/java
 
 /etc/alternatives/java -> /usr/lib/jvm/java-6-sun/jre/bin/java
 
add this line to .bashrc

export JAVA_HOME=/usr/lib/jvm/java-6-sun-1.6.0.06/

export CATALINA_OPTS="-Xmx1024m -Xms256m -Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8 "

 
 execute
 source ~/.bashrc or logout and login
 
 type "echo $JAVA_HOME" to check  
/usr/lib/jvm/java-6-sun-1.6.0.06/

#tomcat
download the : <a href=http://mirror.mkhelif.fr/apache/tomcat/tomcat-5/v5.5.27/bin/apache-tomcat-5.5.27.tar.gz">tomcat distrib</a>
 
sudo  mv apache-tomcat-5.5.27.tar.gz /usr/local/; cd /usr/local
tar zxvf apache-tomcat-5.5.27.tar.gz; mv apache-tomcat-5.5.27 tomcat

sudo bash
adduser tomcat && addgroup tomcat; cd /usr/local/tomcat

 Enter new UNIX password: 
Retype new UNIX password: 

Enter the new value, or press ENTER for the default
	Full Name [tomcat]: Tomcat
	Room Number [tomcat]: 00
	Work Phone [00]: 00
	Home Phone [00]: 00
	Other []: 00
Is the information correct? [y/N] y


 chown -R tomcat:tomcat webapps; chmod -R 775 webapps
 
  usermod -aG tomcat www-data
  
  
  OPTIONAL : cd conf; chmod g+w server.xml
  cd /usr/share/tomcat-5.5.25/bin/; chmod 755 shutdown.sh startup.sh
  
  add host in server.xml :
   
  
  
  cd /usr/local/tomcat/bin/; chmod 755 shutdown.sh startup.sh
  
  
  vim /etc/apache2/workers.properties
  
   sudo apt-get install php5-gd
  
  # This file provides minimal jk configuration properties needed to
# connect to Tomcat.
#
# We define a worker named ‘default’
#workers.tomcat_home=/usr/share/tomcat5.5/
workers.java_home=/usr/lib/jvm/java-6-sun-1.6.0.06/
ps=/
worker.list=default
worker.default.port=8009
worker.default.host=localhost
worker.default.type=ajp13
worker.default.lbfactor=1



  <Host name="services.gisgraphy.com"
       appBase="/dir/to/gisgraphy"
     unpackWARs="true" autoDeploy="false"
     deployOnStartup="true" deployXML="false" />

  
sudo vim /etc/apache2/apache2.conf
in apache2.conf :

NameVirtualHost *:80

JkWorkersFile /etc/apache2/workers.properties
 #-------------------------------------------
  vim /etc/apache2/sites-available/000-default
  
  <VirtualHost *:80 >
        ServerAdmin davidmasclet@gisgraphy.com
        ServerName www.gisgraphy.com
        DocumentRoot /var/www/
        
        ErrorDocument 404 /404.php
        ErrorDocument 403 /403.php
        ErrorDocument 500 /500.php
        
        <Directory />
                AllowOverride None
                Options FollowSymLinks
                Order allow,deny
                Allow from all
        </Directory>
        <Directory /var/www/>
                Options Indexes FollowSymLinks MultiViews
                AllowOverride None
                Order allow,deny
                allow from all
        </Directory>

        ErrorLog /var/log/apache2/error.log

        # Possible values include: debug, info, notice, warn, error, crit,
        # alert, emerg.
        LogLevel warn

        CustomLog /var/log/apache2/access.log combined
        ServerSignature On

    Alias /doc/ "/usr/share/doc/"
    <Directory "/usr/share/doc/">
        Options Indexes MultiViews FollowSymLinks
        AllowOverride None
        Order deny,allow
        Deny from all
        Allow from 127.0.0.0/255.0.0.0 ::1/128
    </Directory>

</VirtualHost>
  
  
  
  vim /etc/apache2/sites-available/services
  
<virtualHost *:80 >
ServerName services.gisgraphy.com
ServerAdmin davidmasclet@gisgraphy.com

CustomLog /var/log/apache2/access-services.log combined

ErrorLog /var/log/apache2/error-services.log

JkMount /* default
DirectoryIndex index.jsp index.html
# Globally deny access to the WEB-INF directory
<LocationMatch ‘.*WEB-INF.*’>
Order allow,deny
allow from all
</LocationMatch>
</VirtualHost>

vim /etc/apache2/sites-available/download

<VirtualHost *:80 >
        ServerAdmin davidmasclet@gisgraphy.com
        ServerName download.gisgraphy.com
        DocumentRoot /var/www-download/

      BandWidthModule On
      BandWidth all 8000000
      AddOutputFilterByType MOD_BW application/x-gzip  application/zip application/x-bzip2
      ErrorDocument 509 "Sorry, there is too many users connected, this site has limmited resources, please try again later."
      BandWidthError 509
      MaxConnection all 5
      ReadmeName readme.txt

        <Directory />
                AllowOverride None
                Options FollowSymLinks
                Order allow,deny
                Allow from all
        </Directory>

        <Directory /var/www-download/>
                Options Indexes FollowSymLinks MultiViews
                Order allow,deny
                allow from all
        </Directory>



     

        ErrorLog /var/log/apache2/error-download.log

        # Possible values include: debug, info, notice, warn, error, crit,
        # alert, emerg.
        LogLevel warn

        CustomLog /var/log/apache2/access-download.log combined
        ServerSignature Off


</VirtualHost>


###########################
mod bandwith (need GCC)

wget http://ivn.cl/files/source/mod_bw-0.8.tgz
tar xvzf mod_bw-0.8.tgz
cd mod_bw
apxs2 -i -a -c mod_bw.c


LoadModule bw_module /usr/lib/apache2/modules/mod_bw.so
      BandWidthModule On
      BandWidth all 2000000
      MinBandWidth all -1
#     MinBandWidth all 10000
#     ForceBandWidthModule On
      AddOutputFilterByType MOD_BW application/x-gzip  application/zip

##########################

  
sudo a2ensite services
  
alias psqlg='psql -Upostgres '
alias tomcatlog='tail -f -n 200 /usr/local/tomcat/logs/catalina.out'
alias apachelog='tail -f -n 200 /var/log/apache2/error.log'
alias tomcatstart='cd /home/gisgraphy/services;/usr/local/tomcat/bin/startup.sh'
alias tomcatstop='/usr/local/tomcat/bin/shutdown.sh'
alias solrstart='cd /home/gisgraphy/services/;nohup ./launch.sh & ; solrlog'
alias solrlog='tail -f /home/gisgraphy/services/nohup.out'
alias psj='ps auxf |grep java'

iptables :
sudo iptables -A INPUT -p tcp -s 0/0  -d 88.191.92.137  --dport 8080 -m state --state NEW,ESTABLISHED -j REJECT
sudo iptables -A INPUT -p tcp -s 0/0  -d 88.191.92.137  --dport 8983 -m state --state NEW,ESTABLISHED -j REJECT

to see host names modify in the server.wml of the tomcat:

 <Connector port="8009"
               enableLookups="true" redirectPort="8443" protocol="AJP/1.3" URIEncoding="UTF-8" />





  

