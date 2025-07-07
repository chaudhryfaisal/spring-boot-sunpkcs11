TOKEN_LABEL=token-1
SOFTHSM2_LIB := $(firstword $(wildcard /usr/lib/softhsm/libsofthsm2.so /usr/lib64/softhsm/libsofthsm.so))
SUDO := $(shell if command -v sudo > /dev/null 2>&1; then echo sudo; else echo ""; fi)
setup: install reset pkcs11-test
delete:
	@for serial in `softhsm2-util --show-slots --module $(SOFTHSM2_LIB) | awk -F': ' '/Serial/ {print $$2}'`; do \
  		echo "Deleting token with $$serial"; softhsm2-util --delete-token --force --serial $$serial --module $(SOFTHSM2_LIB); done
reset: delete
	softhsm2-util --init-token --free --label ${TOKEN_LABEL} --pin 1234 --so-pin 4321
	softhsm2-util --show-slots --label ${TOKEN_LABEL}
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --keypairgen --id 01 --key-type rsa:2048 --label "rsa-2048"
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --keypairgen --id 02 --key-type EC:prime256v1 --label "ecc-256"

install: pkcs11-install
	which softhsm2-util || ( ${SUDO} yum install -y softhsm || (${SUDO} apt-get update && ${SUDO} apt-get install -y softhsm))
pkcs11-install:
	which pkcs11-tool || ( ${SUDO} yum install -y opensc || (${SUDO} apt-get update && ${SUDO} apt-get install -y opensc))
pkcs11-test: pkcs11-install
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 -O
	echo hello > /var/tmp/$@.sign
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --sign --id 01 --mechanism SHA256-RSA-PKCS \
	  --input-file /var/tmp/$@.sign --output-file /var/tmp/$@.sign.out

