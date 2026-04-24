# 5COSC022C.2-CW-W2121217
Client Server Architecture Coursework

Part 1: Service Architecture and Setup 

Question 1 

JAX-RS creates a new instance of each resource class for every incoming request. This means instance variables are wiped after each request, so storing data in a regular HashMap as an instance field would result in data loss between calls.
To fix this, we use a singleton DataStore class that holds all data in ConcurrentHashMap structures accessed via a static getInstance() method. Since the maps live at the class level, every resource instance shares the same data regardless of when it was created. ConcurrentHashMap is used specifically because it handles concurrent reads and writes safely, preventing race conditions when multiple requests arrive simultaneously.

Question 2

HATEOAS means the API embeds navigational links directly in its responses rather than requiring clients to hardcode URLs or rely on external documentation. A client can start at the root endpoint and discover related resources by following the links provided.
The key benefit is that links come from the running server, so they are always accurate. If URL structures change, clients following links continue to work without modification. Static documentation can go out of sync, but hypermedia responses cannot. Our Discovery endpoint at GET /api/v1 demonstrates this by returning a resource map pointing clients to the rooms and sensors collections.

Part 2: Room Management 

Question 1

Returning only IDs keeps responses small but forces clients into multiple follow-up requests to fetch the details they actually need, which is the classic N+1 problem and can hurt performance significantly.
Returning full room objects gives clients everything in one response with no additional calls needed. Since room objects are relatively lightweight, this trade-off almost always favours returning full objects. Our implementation does exactly this at GET /rooms.

Question 2

Yes, DELETE is idempotent in this implementation. The first call removes the room and returns 204. Any subsequent call finds the room already gone and returns 404. In both cases the server state is the same: the room does not exist. The goal of the operation is achieved regardless of how many times it is called.
This matters because network failures can leave a client unsure whether a request succeeded. Idempotency allows safe retries without risk of unintended side effects.

Part 3: Sensor Operations & Linking

Question 1

The @Consumes(MediaType.APPLICATION_JSON) annotation tells JAX-RS to only route requests to that method when the Content-Type header is application/json. If a client sends text/plain or application/xml instead, JAX-RS returns HTTP 415 Unsupported Media Type before any application code runs at all. This prevents malformed data from reaching the deserialization layer and gives the client a clear, actionable error rather than a confusing 50 

Question 2

Path parameters identify a specific resource. Query parameters filter or modify how a collection is retrieved. Using /sensors/type/CO2 as a path implies that "type" is itself a resource, which is semantically wrong and could also collide with actual sensor IDs.
Query parameters are also naturally composable and optional. GET /sensors returns everything, GET /sensors?type=CO2 filters by type, and GET /sensors?type=CO2&status=ACTIVE filters by both. Achieving the same with path segments would require a proliferation of endpoints or an unwieldy URL structure.

Part 4: Deep Nesting with Sub - Resources

Question 1

The Sub-Resource Locator pattern lets a resource method delegate request handling to a separate class. When a request hits /sensors/{sensorId}/readings, SensorResource does not handle it directly. It instantiates SensorReadingResource with the sensor context and returns it, letting JAX-RS continue routing through that dedicated class.
The main benefit is separation of concerns. Each class has one clear responsibility and neither needs to know the internal details of the other. SensorReadingResource can also be tested in complete isolation without standing up the full sensor routing infrastructure. As the API grows, new sub-resource classes are added rather than bloating existing ones, keeping the codebase navigable and maintainable.



Part 5: Advanced Error Handling, Exception Mapping & Logging

Question 1

HTTP 404 means the resource at the requested URL does not exist. HTTP 422 means the request was understood and the JSON was valid, but the content could not be processed due to a semantic problem.
When a client POSTs a new sensor with a valid payload but a roomId that does not exist, the URL is correct and the JSON is well-formed. The problem is the reference inside the payload. Returning 404 would mislead the client into thinking their URL was wrong. A 422 tells them clearly that the issue is in the data they sent, not where they sent it, which makes debugging far faster.

Question 2

A stack trace hands an attacker a detailed map of your application internals. It reveals package and class names showing how the code is organised, exact library versions that can be cross-referenced against known vulnerability databases, server file paths and directory structure, and method names exposing application logic and control flow.
Our GlobalExceptionMapper addresses this by catching all unhandled errors, logging the full detail internally, and returning only a generic 500 response with a safe message to the client. Developers retain full visibility while external parties see nothing exploitable.

Question 3

Adding Logger.info() to every resource method works in small projects but quickly becomes a liability. Coverage becomes inconsistent, formats vary between developers, and new endpoints can easily be forgotten.
A JAX-RS filter solves this by applying logging uniformly at the framework level. Our LoggingFilter intercepts every request and response automatically, meaning every existing and future endpoint gets consistent log coverage with no extra effort. It also keeps resource classes clean by separating infrastructure concerns from business logic entirely.
