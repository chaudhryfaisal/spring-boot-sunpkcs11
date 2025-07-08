ENDPOINT_SIGN=http://localhost:8085/v1/crypto/sign
c=100
q=100
z=5s
hey: ## Install hey benchmarking tool
	@hey -z ${z} -c ${c} -m POST -T 'application/json' \
		-d '{"keyLabel":"rsa-2048","algorithm":"RSA","data":"SGVsbG8sIHdvcmxkIQ=="}' ${ENDPOINT_SIGN} | \
	  	cat -n #grep -vE '(Summary|Total|Requests|Status|responses)'
hey-all:
	@for c in `seq 1 10`; do echo "C=$$c"; make -s hey c=$$c; done
hey-setup:
	@echo "Installing hey..."
	@which hey || ( curl -sSL https://hey-release.s3.us-east-2.amazonaws.com/hey_linux_amd64 -o ${HOME}/.local/bin/hey && \
		chmod +x ${HOME}/.local/bin/hey )

sign: ## Test singn request with curl
	curl -X POST '${ENDPOINT_SIGN}' -H 'Content-Type: application/json' \
		-d '{"keyLabel":"rsa-2048","algorithm":"RSA","data":"SGVsbG8sIHdvcmxkIQ=="}'
run=run
ttyplot:
	make ${run} -C .. | awk -F'duration: ' '/duration:/ { split($$2, a, " "); print a[1]; fflush(); }' | \
	 ttyplot -t "Signature Duration (µs)" -u "µs"
ttyplot-setup:
	which ttyplot || (curl -sSL https://github.com/tenox7/ttyplot/archive/refs/tags/1.7.1.tar.gz | tar -xzv -C /var/tmp && \
	cd /var/tmp/ttyplot-1.7.1/; sudo yum install -y ncurses-devel; sudo make install )