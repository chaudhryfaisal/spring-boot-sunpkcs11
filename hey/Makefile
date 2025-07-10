
ENDPOINT_BASE=http://localhost:8085/v1
ENDPOINT_SIGN=${ENDPOINT_BASE}/crypto/sign
SIGN_NAME=cpu_${cpu}_users_${c_padded}
SIGN_PAYLOAD={"keyLabel":"rsa-2048","algorithm":"RSA","data":"SGVsbG8sIHdvcmxkIQ==","name":"name=${SIGN_NAME}"}
sign:
	curl -X POST '${ENDPOINT_SIGN}' -H 'Content-Type: application/json' \
		-d '${SIGN_PAYLOAD}'
cpu?=2
c=100
c_padded := $(shell printf "%03d" $(c))
q=10
z=5s
hey:
	@hey -z ${z} -c ${c} -q ${q} -m POST -T 'application/json' \
		-d '${SIGN_PAYLOAD}' ${ENDPOINT_SIGN} | \
	  	cat -n #grep -vE '(Summary|Total|Requests|Status|responses)'
hey-warmup:
	make hey c=1 SIGN_NAME=warmup
hey-all: hey-warmup
	@for c in `seq 0 5 100 | sed s/^0/1/`; do echo "C=$$c"; sleep 10; make -s hey c=$$c; done
hey-setup:
	@echo "Installing hey..."
	@which hey || ( curl -sSL https://hey-release.s3.us-east-2.amazonaws.com/hey_linux_amd64 -o ${HOME}/.local/bin/hey && \
		chmod +x ${HOME}/.local/bin/hey )
run=run
ttyplot:
	make ${run} -C .. | awk -F'duration: ' '/duration:/ { split($$2, a, " "); print a[1]; fflush(); }' | \
	 ttyplot -t "Signature Duration (µs)" -u "µs"
ttyplot-setup:
	which ttyplot || (curl -sSL https://github.com/tenox7/ttyplot/archive/refs/tags/1.7.1.tar.gz | tar -xzv -C /var/tmp && \
	cd /var/tmp/ttyplot-1.7.1/; sudo yum install -y ncurses-devel; sudo make install )