import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '15s', target: 10 },   // ramp up to 10 concurrent users
    { duration: '30s', target: 50 },   // ramp up to 50
    { duration: '30s', target: 50 },   // hold at 50
    { duration: '15s', target: 0 },    // ramp down
  ],
  thresholds: {
    http_req_duration: ['p(95)<800'],   // 95% of requests should be under 800ms
    http_req_failed: ['rate<0.01'],     // less than 1% failure rate
  },
};

const CHANNELS = ['UPI', 'BANK_TRANSFER', 'CARD'];

export default function () {
  const channel = CHANNELS[Math.floor(Math.random() * CHANNELS.length)];

  const payload = JSON.stringify({
    senderId: `USER-${Math.floor(Math.random() * 1000)}`,
    receiverId: 'MERCHANT-123',
    amount: (Math.random() * 1000).toFixed(2),
    currency: 'INR',
    channel: channel,
  });

  const res = http.post('http://localhost:8080/api/payments/process', payload, {
    headers: { 'Content-Type': 'application/json' },
  });

  check(res, {
    'status is 200': (r) => r.status === 200,
  });

  sleep(0.5);
}