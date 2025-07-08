TOKEN_LABEL=token-1
SOFTHSM2_LIB := $(firstword $(wildcard /usr/lib/softhsm/libsofthsm2.so /usr/lib64/softhsm/libsofthsm.so))
SUDO := $(shell if command -v sudo > /dev/null 2>&1; then echo sudo; else echo ""; fi)
setup: install reset pkcs11-test
delete:
	@for serial in `softhsm2-util --show-slots --module $(SOFTHSM2_LIB) | awk -F': ' '/Serial/ {print $$2}'`; do \
  		echo "Deleting token with $$serial"; softhsm2-util --delete-token --force --serial $$serial --module $(SOFTHSM2_LIB); done
reset: delete
	softhsm2-util --init-token --slot 0 --label ${TOKEN_LABEL} --pin 1234 --so-pin 4321
	softhsm2-util --show-slots --label ${TOKEN_LABEL}
	make gen-keys list
gen-keys:
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --keypairgen --id 01 --key-type rsa:2048 --label "rsa-2048"
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --keypairgen --id 02 --key-type EC:prime256v1 --label "ecc-256"
	make gen-certs key_alias=rsa-2048 key_id=01
	make gen-certs key_alias=ecc-256 key_id=02
key_alias=rsa-2048
key_id=01
gen-certs:
	export PKCS11_MODULE_PATH=${SOFTHSM2_LIB};\
    openssl req -engine pkcs11 -keyform engine -new -key "pkcs11:object=${key_alias};type=private;pin-value=1234" \
      	-subj "/CN=${key_alias}" -out ${key_alias}.csr 2>/dev/null
	openssl x509 -engine pkcs11 -keyform engine -req -in "${key_alias}.csr" -signkey "pkcs11:object=${key_alias};type=private;pin-value=1234" \
		-days 365 -out ${key_alias}.pem 2>/dev/null
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --write-object ${key_alias}.pem --type cert --label "${key_alias}" --id ${key_id}
	rm -f ${key_alias}.csr ${key_alias}.pem

install:
	which softhsm2-util || ( ${SUDO} yum install -y softhsm engine_pkcs11 opensc || ( ${SUDO} apt-get update && ${SUDO} apt-get install -y softhsm libengine-pkcs11-openssl opensc))
pkcs11-test:
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 -O
	echo hello > /var/tmp/$@.sign
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 --sign --id 01 --mechanism SHA256-RSA-PKCS \
	  --input-file /var/tmp/$@.sign --output-file /var/tmp/$@.sign.out
PKCS11_CONF=/var/tmp/softhsm.sunpkcs11
list:
	pkcs11-tool --module ${SOFTHSM2_LIB} --login --pin 1234 -O
	printf 'name=SoftHSM\nlibrary=${SOFTHSM2_LIB}\n' | tee ${PKCS11_CONF}
	SUNPKCS11_CFG=/etc/softhsm2.conf keytool -list -keystore NONE -storetype PKCS11 -providerClass sun.security.pkcs11.SunPKCS11 \
		-providerArg ${PKCS11_CONF} -storepass 1234