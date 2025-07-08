import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  vus: __ENV.VUS ? parseInt(__ENV.VUS) : 10,
  duration: __ENV.DURATION || '30s',
};

const payload = JSON.stringify({
  keyLabel: 'rsa-2048',
  algorithm: 'RSA',
  data: 'SGVsbG8sIHdvcmxkIQ==' // base64 of "Hello, world!"
});

const params = {
  headers: {
    'Content-Type': 'application/json'
  }
};

export default function () {
  const res = http.post('http://localhost:8085/v1/crypto/sign', payload, params);
  check(res, {
    'status was 200': (r) => r.status === 200,
    'signature present': (r) => JSON.parse(r.body).signature !== undefined,
  });
  sleep(1);
}