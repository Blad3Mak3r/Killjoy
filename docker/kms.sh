#!/bin/bash

################################################################################
# Copyright (c) 2021. Blademaker
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#        http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and limitations under the License.
################################################################################

PROJECT_NAME="Killjoy"

OPTION=$1
SERVICE=$2

VALID_OPTIONS="\032[0m(start|down|logs|pull|upgrade|stop)\033[0m"

printf "\033[0;33m*******************************************************\033[0m\n"
printf "\033[0;33m**                                                   **\033[0m\n"
printf "\033[0;33m**    \033[0mWelcome to Killjoy Manage Script (KMS) v0.3    \033[0;33m**\033[0m\n"
printf "\033[0;33m**                                                   **\033[0m\n"
printf "\033[0;33m*******************************************************\033[0m\n"
printf "\n"

if [ -z "$OPTION" ]; then
  printf "\033[31mPass an option as first argument %s" "$VALID_OPTIONS"
  exit
fi

[[ -n "$SERVICE" ]] && printf "Selected service is \033[32m%s\033[0m\n\n" "$SERVICE"

printf "Selected option is \033[32m%s\033[0m\n\n" "$OPTION"

case $OPTION in
  start)
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME pull $SERVICE
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME up -d $SERVICE
    ;;
  down)
    echo "Execution down operation..."
    docker-compose -p $PROJECT_NAME down
    ;;
  logs)
    # shellcheck disable=SC2086
    exec docker-compose -p $PROJECT_NAME logs $SERVICE -f --tail="2000"
    ;;
  pull)
    docker-compose -p $PROJECT_NAME pull
    ;;
  upgrade)
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME pull $SERVICE
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME down $SERVICE
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME up -d $SERVICE
    ;;
  stop)
    # shellcheck disable=SC2086
    docker-compose -p $PROJECT_NAME stop $SERVICE
    ;;
  *)
    printf "Option \033[31m%s\033[0m is not a valid option. %s" "$OPTION" "$VALID_OPTIONS"
    exit
esac