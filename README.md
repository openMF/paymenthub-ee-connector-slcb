# paymenthub-ee-connector-slcb

A Payment Hub EE connector for the **Sierra Leone Commercial Bank (SLCB)**: it sends batches of
payments to the SLCB API, asks how they went, and reports the result back to the workflow.

[![License](https://img.shields.io/badge/License-MPL--2.0-blue.svg)](LICENSE)

## What it does

- Gets a fresh access token from SLCB before each call. There is no token cache yet.
- Sends a batch of payments to SLCB, signing the request with the configured signature key.
- Asks SLCB for the result of a batch and updates the status of each payment in it.
- Reads the account balance.
- Runs a reconciliation over a batch of transactions.
- Writes the results to a CSV file and uploads it to S3.
- Runs Zeebe (Camunda) workers for the `slcb-initiateTransfer` and `slcb-reconciliation` job
  types.

## How it fits into Payment Hub EE

Payment Hub EE runs each payment as a Zeebe (Camunda) workflow. When a workflow reaches the step
that has to move the money through SLCB, this connector's Zeebe workers pick up that job, call the
SLCB API over HTTPS, and report back. Payments are sent in batches: the connector signs the batch,
sends it, then asks for the outcome and writes the per-payment status into a CSV file that the rest
of Payment Hub can read. So it sits between the Payment Hub orchestration layer and the bank,
translating between the two.

## Tech stack

- Java 21
- Spring Boot 3.4
- Apache Camel 4 (routes for token, transfer, transaction status, reconciliation, balance and file
  upload)
- Zeebe / Camunda workers (via the Zeebe Java client)
- Gradle build
- Depends on `paymenthub-ee-bom` (for versions) and `paymenthub-ee-core`

## Build and run

    ./gradlew clean build          # compiles and runs the tests
    ./gradlew bootRun              # runs the connector locally
    docker build -t paymenthub-ee-connector-slcb .

The connector listens on port 5000 for the Camel REST routes and 8080 for Spring Boot. It expects a
Zeebe broker at `zeebe.broker.contactpoint` (`127.0.0.1:26500` by default).

Everything that points at a real system is read from the environment, with a default for local runs:
`SLCB_AUTH_HOST`, `SLCB_API_HOST`, `SLCB_USERNAME`, `SLCB_PASSWORD`, `SLCB_SIGNATURE_KEY`, and
`AWS_ACCESS_KEY` / `AWS_SECRET_KEY` / `AWS_BUCKET_NAME` for the CSV upload.

## How to run the cucumber test case

1. Method 1: Run the test case from the IDE
    1. Setup the project in the IDE
    2. Open the file `build.gradle` and run the task `cucumberCli`
2. Method 2: Run the test case from the command line
    1. Go to the project root directory
    2. Run the command `./gradlew cucumberCli`

## Branches

- `dev` is the active development branch — all PRs should target `dev`.
- `main` holds released versions.

## Contributing

See [contributing.md](contributing.md), our [Code of Conduct](CODE_OF_CONDUCT.md) and the [security policy](security.md).
