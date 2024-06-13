# SAE S2.03

Chose a modifier
- Base64 qui marche pas
- Les logs a savoir si c'est bon comme ils le veulent
- Tester le code dynamique dans une page web


### Chose qui reste a faire :
- L'integration au système linux
    - Les classes dans /usr/local/sbin/myweb
    - Le fichier de config dans /etc/myweb et doit se nommer myweb.conf (c'est bon)
    - quand le serv ce lance créer un fichier dans /var/run nommer myweb.pid qui contient le numéro du processus qui a lancé le serveur (celui de java)

- Service 
    - Utiliser systemD pour créer le service myweb.service
    - a démarré au boot de la machine

- Package 
    - regroupe tout l'ensemble du projet en package Debian avec les classe java, le service (".service") et les script, les fichier de config, et surmant de logs ???
