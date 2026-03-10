import { createServer } from "node:http";

const PORT = 8080;

const server = createServer((req, res) => {
  if (req.method === "POST" && req.url === "/payments") {
    let body = "";
    req.on("data", (chunk) => (body += chunk));
    req.on("end", () => {
      try {
        const { orderId, amount } = JSON.parse(body);
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
    });
    return;
  }

  res.writeHead(404, { "Content-Type": "application/json" });
  res.end(JSON.stringify({ error: "Not found" }));
});

server.listen(PORT, () => {
  console.log(`Payment Service listening on port ${PORT}`);
});
