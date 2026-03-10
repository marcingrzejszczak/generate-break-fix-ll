import http from "k6/http";
import { check, sleep } from "k6";

export const options = {
  scenarios: {
    load: {
      executor: "constant-arrival-rate",
      rate: 100,
      timeUnit: "1s",
      duration: "30s",
      preAllocatedVUs: 1000,
    },
  },
};

export default function () {
  const res = http.post(
    "http://localhost:3456/orders",
    JSON.stringify({ productId: "book-123", amount: 49.99 }),
    { headers: { "Content-Type": "application/json" }, timeout: "15s" }
  );

  check(res, {
    "status is 201": (r) => r.status === 201,
  });
}
