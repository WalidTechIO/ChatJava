#!/usr/bin/env bash

cd $(dirname $0)

if [[ $# != 1 ]]; then
  echo "You need to precise how many clients you want"
  exit 1
fi

xhost +local:docker #Open Host X-server to Docker Network interface

if [ $? -eq 127 ]; then
  echo "X-server command not detected"
  exit 1
fi

if [ -d ../bin ]; then
  mkdir ../bin
fi

make

if [ $? -eq 127 ]; then
  echo "Make is not detected"
  exit 1
fi

docker compose -f docker/docker-compose.yml up -d --scale client=$1

if [ $? -eq 127 ]; then
  echo "Docker is not detected"
  exit 1
fi

container_name=$(docker inspect -f '{{.Name}}' $(docker compose -f docker/docker-compose.yml ps -aq) | cut -c2-)

echo -e "\nServices lancés:\n"

for element in $container_name
do
  adr_ip=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $element)
  run_cmd=$(docker inspect -f '{{join .Config.Entrypoint " "}} {{join .Config.Cmd " "}}' $element)
  if [[ $element =~ ^(.*)server(.*)$ ]]
  then
    tcp_port=$(docker logs $element | egrep '^TCP Server:(.*)0.0.0.0' | awk -F: '{print $3}')
    mcadr=$(docker logs $element | egrep '^Group Listener: ' | cut -d' ' -f5)
    #hostname=$(echo $element | awk -F- '{last = $NF; printf("%s", $2); if(NF>3) { for(i = 3; i < NF; i++) { printf("-%s",$i); } } }')
    hostname=$(docker inspect -f '{{range .NetworkSettings.Networks}}{{index .Aliases 1}}{{end}}' $element)
    echo -e "$hostname:\n\tCommande de lancement: $run_cmd\n\tAdresse MC: $mcadr\n\tAdresse TCP: $hostname:$tcp_port\n\tLogs: docker logs $element -f\n\tAdresse IP: $adr_ip"
  else
    echo -e "$element:\n\tCommande de lancement: $run_cmd\n\tAdresse IP: $adr_ip"
  fi
done
echo -e "\nLogs de tout les services: docker compose -f docker/docker-compose.yml logs -f\n"
echo "Appuyer sur entrée pour couper les services"
read
docker compose -f docker/docker-compose.yml down
docker image prune -f
docker rmi walkloly/chat:server -f
docker rmi walkloly/chat:client -f
make clean
exit 0
