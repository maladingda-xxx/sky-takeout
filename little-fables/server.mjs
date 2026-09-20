import { createServer } from "node:http";
import { readFile, stat } from "node:fs/promises";
import { extname, join, normalize } from "node:path";
import { fileURLToPath } from "node:url";

const root = fileURLToPath(new URL(".", import.meta.url));
const port = Number(process.env.PORT || 4173);
const backendUrl = process.env.BACKEND_URL || "http://127.0.0.1:8080";

const contentTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".ico": "image/x-icon",
  ".jpeg": "image/jpeg",
  ".jpg": "image/jpeg",
  ".js": "text/javascript; charset=utf-8",
  ".json": "application/json; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
  ".webp": "image/webp"
};

async function proxyApi(request, response, requestUrl) {
  const target = new URL(
    requestUrl.pathname.slice("/api".length) + requestUrl.search,
    backendUrl
  );
  const headers = new Headers(request.headers);
  headers.delete("host");
  headers.delete("content-length");

  let body;
  if (request.method !== "GET" && request.method !== "HEAD") {
    const chunks = [];
    for await (const chunk of request) {
      chunks.push(chunk);
    }
    body = Buffer.concat(chunks);
  }

  const upstream = await fetch(target, {
    method: request.method,
    headers,
    body
  });
  const responseBody = Buffer.from(await upstream.arrayBuffer());

  response.writeHead(upstream.status, {
    "Cache-Control": "no-store",
    "Content-Type": upstream.headers.get("content-type")
      || "application/json; charset=utf-8"
  });
  response.end(responseBody);
}

const server = createServer(async (request, response) => {
  try {
    const requestUrl = new URL(request.url || "/", `http://${request.headers.host}`);
    if (requestUrl.pathname === "/api" || requestUrl.pathname.startsWith("/api/")) {
      await proxyApi(request, response, requestUrl);
      return;
    }

    const requestedPath = requestUrl.pathname === "/" ? "/index.html" : requestUrl.pathname;
    const safePath = normalize(requestedPath).replace(/^(\.\.(\/|\\|$))+/, "");
    let filePath = join(root, safePath);

    try {
      const fileStats = await stat(filePath);
      if (fileStats.isDirectory()) {
        filePath = join(filePath, "index.html");
      }
    } catch {
      filePath = join(root, "index.html");
    }

    const body = await readFile(filePath);
    response.writeHead(200, {
      "Cache-Control": "no-cache",
      "Content-Type": contentTypes[extname(filePath)] || "application/octet-stream"
    });
    response.end(body);
  } catch (error) {
    response.writeHead(500, { "Content-Type": "text/plain; charset=utf-8" });
    response.end(`Server error: ${error.message}`);
  }
});

server.listen(port, "127.0.0.1", () => {
  console.log(`Little Fables is running at http://127.0.0.1:${port}`);
});
