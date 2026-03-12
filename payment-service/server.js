import { createServer } from "node:http";

const PORT = 8080;

function parseBody(req) {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      try {
        resolve(JSON.parse(body));
      } catch (err) {
        reject(err);
      }
    });
  });
}

function validatePaymentRequest(orderId, amount) {
  if (!orderId || typeof orderId !== "string") {
    return "Missing or invalid orderId";
  }
  if (amount === undefined || typeof amount !== "number" || amount <= 0) {
    return "Missing or invalid amount";
  }
  return null;
}

const server = createServer(async (req, res) => {
  // TASK.md: POST /payments
  if (req.method === "POST" && req.url === "/payments") {
    try {
      const { orderId, amount } = await parseBody(req);
      console.log(`Payment authorized for order ${orderId}, amount: ${amount}`);

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(
        JSON.stringify({
          paymentId: `pay-${Date.now()}`,
          orderId,
          amount,
          status: "AUTHORIZED",
        })
      );
    } catch (err) {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid request body" }));
    }
    return;
  }

  // TASK2.md: POST /authorize-payment
  if (req.method === "POST" && req.url === "/authorize-payment") {
    try {
      const { orderId, amount } = await parseBody(req);

      const validationError = validatePaymentRequest(orderId, amount);
      if (validationError) {
        res.writeHead(400, { "Content-Type": "application/json" });
        res.end(JSON.stringify({ error: validationError }));
        return;
      }

      const status = amount > 10000 ? "DECLINED" : "AUTHORIZED";
      console.log(`Payment ${status.toLowerCase()} for order ${orderId}, amount: ${amount}`);

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ status }));
    } catch (err) {
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid request body" }));
    }
    return;
  }

  res.writeHead(404, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ error: "Not found" }));
});

server.listen(PORT, () => {
  console.log(`Payment Service listening on port ${PORT}`);
});
