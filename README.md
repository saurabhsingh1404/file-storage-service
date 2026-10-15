# User Document Storage Service

Beginner-friendly Java 21 / Spring Boot REST API based on the supplied implementation blueprint.

## What it does
- Searches files by **filename only**, case-insensitively; nested files are included.
- Keeps each user's objects under a prefix such as `sandy/`.
- Sorts by full key before pagination.
- Returns a five-minute download URL. Local mode returns a clearly simulated URL; S3 profiles return a real S3 pre-signed URL.
- Uses consistent JSON errors, Swagger UI, and automated tests.

## Requirements
- JDK 21
- Maven 3.9 or newer
- Optional for live S3: AWS account, private bucket, and least-privilege AWS credentials/role

Check installations:
```bash
java -version
mvn -version
```

## Run locally without AWS (recommended first)
Open a terminal in the folder containing `pom.xml` and run:
```bash
mvn test
mvn package
mvn spring-boot:run
```
The default profile is `local`, which uses demo data and does not need AWS credentials. Keep the terminal running, then open:
- Swagger UI: http://localhost:8080/swagger-ui/index.html
- Search example: http://localhost:8080/api/v1/users/sandy/files?searchTerm=logistics&page=0&size=50

Example with curl:
```bash
curl -i "http://localhost:8080/api/v1/users/sandy/files?searchTerm=logistics&page=0&size=50"
```
Expected: `200 OK`, two matching seeded files, and pagination totals. Search terms are matched against the filename only, not parent folder names.

Try no matches (expected `404`):
```bash
curl -i "http://localhost:8080/api/v1/users/sandy/files?searchTerm=does-not-exist"
```

Try download URL creation (use the full key from search output):
```bash
curl -i "http://localhost:8080/api/v1/users/sandy/files/download?key=sandy%2Farchive%2Flogistics-report.pdf"
```
The local URL is simulated and will not serve real file bytes. It is only for testing the API contract.

To stop the server, press `Ctrl+C` in the terminal.

## Use real Amazon S3
1. Create a **private** bucket in AWS. Do not enable public access.
2. Set permissions for listing the intended bucket and reading intended objects. Prefer IAM roles for deployed environments; locally, use the AWS CLI credential provider chain.
3. Set the bucket and region for the selected profile. Never put AWS access keys in source files or commit them.
4. In PowerShell, for a development bucket:
```powershell
$env:LOCAL_S3_BUCKET="your-private-bucket-name"
$env:AWS_REGION="ap-south-1"
$env:SPRING_PROFILES_ACTIVE="local-s3"
mvn spring-boot:run
```
Change `AWS_REGION` to the actual bucket region. For QA use `QA_S3_BUCKET` and profile `qa`; for production use `PROD_S3_BUCKET` and profile `prod`.

S3 keys must follow the layout `username/path/to/file.ext`, for example `sandy/archive/logistics-report.pdf`. The S3 adapter follows continuation tokens so it reads every page of a prefix listing.

## API contract
### Search
`GET /api/v1/users/{userName}/files?searchTerm=logistics&page=0&size=50`
- `userName`: letters, numbers, dot, underscore, hyphen only
- `searchTerm`: required, not blank
- `page`: zero-based, defaults to 0
- `size`: 1 to 100, defaults to 50
- `200`: matching files; an out-of-range page is an empty list with exact totals
- `404`: no matching files anywhere under that user's prefix
- `400`: invalid input

### Download URL
`GET /api/v1/users/{userName}/files/download?key=sandy%2Farchive%2Flogistics-report.pdf`
Pass the **full key** returned by search, URL-encoded. The service checks that the key starts with `userName + "/"`. A real S3 URL expires after 300 seconds.

## Project structure
- `controller`: HTTP routes and response mapping
- `service`: validation, filename matching, sorting, pagination, download rules
- `storage`: storage interface, local demo adapter, S3 adapter
- `dto`: API response records
- `config`: storage settings and S3 clients
- `exception`: central JSON error handling

## Security and limitations
- Authentication is out of scope per the assignment. Prefix checking alone is not production authorization: anyone who can call the API could request another username's files. Add authentication and authorization before production use.
- The local demo download URL is simulated, not a secure download endpoint.
- Generating a pre-signed URL does not verify that the object still exists. A later download can fail if it was deleted.

## Verification Status

The live AWS S3 integration has been verified using a private S3 bucket in the `eu-north-1` region.

Verified operations:
- Uploaded a test object to `sandy/logistics-report.txt`.
- Searched for the file by filename through the REST API.
- Generated a pre-signed S3 download URL with a 300-second expiry.
- Accessed the file content through the generated URL.

Automated test results:
- Tests run: 6
- Failures: 0
- Errors: 0
- Skipped: 0
- Build status: BUILD SUCCESS
