
SHELL := /bin/bash

export

.PHONY: init
init:
	[ ! -e ./venv ] && python3 -m venv venv || echo "venv exists"
	[ ! -e ./certs ] && mkdir certs || echo "certs exists"

	openssl req -x509 -newkey rsa:4096 -nodes -days 9999 \
		-keyout certs/cert.key \
		-out certs/cert.pem \
		-subj "/C=US/ST=Massachusetts/L=Boston/O=Daedalus/OU=Org/CN=localhost"

.PHONY: serve
serve:
	source venv/bin/activate && \
		PYTHONPATH='./daedalus' \
		python -m daedalus serve \
			--cert ./certs/cert.pem \
			--keyfile ./certs/cert.key \
			--platform android \
			./app/src/js/app.js

#subparser.add_argument('--minify', action='store_true')
#subparser.add_argument('--paths', default=None)
#subparser.add_argument('--env', type=str, action='append', default=[])
#subparser.add_argument('--platform', type=str, default=None)
#subparser.add_argument('--static', type=str, default=None)
#subparser.add_argument('index_js')
#subparser.add_argument('out')


.PHONY: build
build:
	source venv/bin/activate && \
		PYTHONPATH='./daedalus' \
		python -m daedalus build \
			--platform android \
			--env baseUrl='https://yueapp.duckdns.org' \
			--static ../yue-react-flask/frontend/resources \
			../yue-react-flask/frontend/src/app.js \
			./app/src/main/assets/site

.PHONY: build-profile
build-profile:
	source venv/bin/activate && \
		PYTHONPATH='./daedalus' \
		python -m daedalus build-profile \
			--platform android \
			--env baseUrl='https://yueapp.duckdns.org' \
			--static ../yue-react-flask/frontend/resources \
			/mnt/d/git/yue-react-flask/frontend/src/app.js \
			./app/src/main/assets/site

.PHONY: serve2
serve2:
	source venv/bin/activate && \
		PYTHONPATH='./daedalus' \
		python -m daedalus serve \
			--platform android \
			--env baseUrl='http://10.0.2.2:4200' \
			--static ../yue-react-flask/frontend/resources \
			../yue-react-flask/frontend/src/app.js

.PHONY: self
self:
	openssl req -x509 -newkey rsa:4096 -nodes -days 9999 \
		-keyout certs/selfsigned.key \
		-out certs/selfsigned.pem \
		-subj "/C=US/ST=Massachusetts/L=Boston/O=Daedalus/OU=Org/CN=localhost"
