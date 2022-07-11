# komoot-challenge

## Requirements

To run the app locally:

- docker
- docker-compose
- java (jdk 17)

To deploy the app on AWS:

- ansible
- python3
- boto3
- aws-cli
- docker

## App configuration

The application is already configured, but to change something (like notification sender, scheduling
interval for the SQS reader or log levels), check `config/application.properties`.

Properties prefixed with `%prod` will be available only on production (so after building docker
image), while properties prefixed with `%dev` will be available only on local development.
Properties with no prefix will be the same on local and on production.

## Run locally

1) run DynamoDB, SNS and SQS using docker. A docker compose file is provided in the root of
   the repository
2) create the DynamoDB table. After executing docker compose, open http://localhost:8000/shell and
   run the following script in the DynamoDB shell

```js
var params = {
  TableName: 'users',
  KeySchema: [
    {AttributeName: 'pkey', KeyType: 'HASH'},
    {AttributeName: 'id', KeyType: 'RANGE'}
  ],
  AttributeDefinitions: [
    {AttributeName: 'pkey', AttributeType: 'S'},
    {AttributeName: 'id', AttributeType: 'N'}
  ],
  ProvisionedThroughput: {ReadCapacityUnits: 1, WriteCapacityUnits: 1}
};

dynamodb.createTable(params, function (err, data) {
  if (err) ppJson(err);
  else ppJson(data);
});
```

4) run the application

```shell
$ ./mvnw quarkus:dev
```

This will also listen for a debugger on port `5005`.
If you want to wait for the debugger to attach before running you can pass -`Dsuspend` on the
command line.
If you don’t want the debugger at all you can use -`Ddebug=false`.

### Send messages to local SNS

```
aws --endpoint-url=http://localhost:9911 sns publish --topic-arn arn:aws:sns:eu-west-1:963797398573:challenge-backend-signups --message '{ "name": "Marcus", "id": 1589278470, "created_at": "2020-05-12T16:11:54.000" }'
```

## Deploy on AWS

To deploy the application on AWS, just run the Ansible playbook in `deployment` directory.
In order to run the playbook, first you need to export some environment variable to be sure Ansible
can access AWS.

```shell
export AWS_ACCOUNT=<ACCOUNT NUMBER>
export AWS_ACCESS_KEY_ID=<KEY ID>
export AWS_SECRET_ACCESS_KEY=<SECRET KEY ID>
export AWS_DEFAULT_REGION=eu-west-1
export AWS_REGION=$AWS_DEFAULT_REGION
```

make sure `AWS_ACCESS_KEY_ID` and `AWS_SECRET_ACCESS_KEY` belongs to an IAM user with correct right
to create needed AWS resources.

```shell
$ cd deployment
$ ansible-playbook playbook.yml
```

The playbook will create everything needed to run the service, so:

- ECR repository to store service's docker image
- SQS queue for SNS topic subscription
- networking resource to run the service on ECS
- cloudwatch log group to collect service logs
- DynamoDB table
- ECS cluster on FARGATE (with a single service instance)

## Rationae

### Architecture design

When new user signup with Komoot, a notification is sent to an SNS topic. Subscribed to that topic
there is an SQS queue that will receive the message to be processed later.

Messages waiting in the SQS queue are then received by a "Welcome message" microservice that polls
the queue at regular intervals.

After receiving the message, the service generates a welcome message notification that is posted to
the push notification endpoint. To generate the message body, the service just tries to pick the
last three registered users and compose the welcome message. If less than three users are picked, a
slightly different message is sent.

Note that even if in the original notification there is a `created_at` field, the service assumes
that a user with a higher ID was registered after ones with a smaller ID, and uses ids to pick
users. This assumption was made taking into account that even if is not correct, for the sole
purpose of choosing some users for the welcome message maybe is not so important that users are
really newer, but in contrast, it allows better data querying for certain use cases, considering the
characteristics of the chosen db (DynamoDB - retrieve user by id to avoid sending duplicated
messages)

### Motivation behind choices

#### SQS subscribing over simple SNS webhook

Unless for this particular use case a webhook was probably enough (we don't expect an extremely high
user registration rate, so we should not need to accumulate messages to elaborate them later),
having an SQS queue that collects notifications helps to avoid problems like missing messages due to
temporary down of the service (think, for example, on what can happen during a re-deploy of the
service). Also, having a queue allows us to easily absorb spikes and avoid wasting resource trying
to process messages (think, for example, about a Komoot event where the speaker encourages the
audience to register to the app)

#### DynamoDB

DynamoDB was chosen because of the ease of setup and the simplicity of requirements (we just need to
retrieve users by id or last N users)

#### JVM language (kotlin)

I chose kotlin as the language to implement the service just because I suppose that jvm languages
are the most used for backend systems in Komoot, and considering that the task doesn't have
particular constraints that require a particular language or feature, I pick a jvm language.
Especially, I choose kotlin because I think is "Java as it should be in 2022", and I prefer way more
the syntax and the features of kotlin over what java have today.

#### Quarkus

Considering the task wasn't requiring much from the web framework perspective, I took the
opportunity to try something I haven't used before (and I was interested in), to study something and
enjoy during the work.

#### Code organization

The code was developed doing DDD and with hexagonal architecture, trying to define a domain for the
service and trying to implement the whole app logic in a dependency-free domain.
Technical stuff like reading from SQS, interacting with DynamoDB, sending messages over HTTP POST,
etc... are done in an infrastructure layer.
In this way, the business logic is separate from the architecture, which can be switched without
changing the core of the service.
So the domain exposes a Service, the `WelcomeMessageService`, that represents the entry point for
the domain: the infrastructure talk to the domain executing the `sendTo` method of the service.
The service act like an orchestrator for single actions (like saving a user or generating the
welcome message), which are performed by simple use cases.

#### Deployment stack

Ansible and docker have been chosen because are some of the most famous (and maybe most used, but
I'm not so sure about docker) in their field.

## Missing features

- Error handling is completely absent
- No teardown scripts are provided to destroy resources created on AWS
- No check for duplicated names. If for some reason 4 users with the same name register in a row,
  the last one will receive "Hi Marcus, welcome to Komoot. Marcus, Marcus and Marcus also joined
  recently."