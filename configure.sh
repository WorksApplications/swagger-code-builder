#!/bin/bash

FONT_RESET='\033[0m'
BG_RED='\033[0;41m'
BG_GREEN='\033[0;42m'
BG_YELLOW='\033[0;43m'

_print_error() {
	MESSAGE=$1
	echo -e "${BG_RED} Error ${FONT_RESET} $MESSAGE"
}

_print_warning() {
	MESSAGE=$1
	echo -e "${BG_YELLOW} Warning ${FONT_RESET} $MESSAGE"
}

_print_ok() {
	MESSAGE=$1
	echo -e "${BG_GREEN} OK ${FONT_RESET} $MESSAGE"
}

type javac > /dev/null
if [[ $? = 0 ]]; then
	_print_ok "JDK is installed"
else
	_print_error "JDK does not exist"
	echo "Please install JDK"
fi

type gradle > /dev/null
if [[ $? = 0 ]]; then
	_print_ok "gradle command is installed"
else
	_print_error "gradle command does not exist"
	echo "Please install gradle (https://gradle.org/)"
fi

type aws 2>&1 > /dev/null
if [[ $? = 0 ]]; then
	_print_ok "aws command is installed"
else
	_print_warning "aws command does not exist"
	echo "Please install aws-cli (https://aws.amazon.com/cli/)"
fi

type uuidgen 2>&1 > /dev/null
if [[ $? = 0 ]]; then
	_print_ok "uuidgen command is installed"
else
	_print_warning "uuidgen command does not exist"
	echo "Please install uuidgen"
fi