<h1 align="center">
  blindnet devkit<br />
  Data Access Component
</h1>

<p align=center><img src="https://user-images.githubusercontent.com/7578400/163277439-edd00509-1d1b-4565-a0d3-49057ebeb92a.png#gh-light-mode-only" height="80" /></p>
<p align=center><img src="https://user-images.githubusercontent.com/7578400/163549893-117bbd70-b81a-47fd-8e1f-844911e48d68.png#gh-dark-mode-only" height="80" /></p>

<p align="center">
  <strong>Provides a common interface to the storage connectors</strong>
</p>

<p align="center">
  <a href="https://blindnet.dev"><strong>blindnet.dev</strong></a>
</p>

<p align="center">
  <a href="https://blindnet.dev/docs">Documentation</a>
  &nbsp;•&nbsp;
  <a href="https://github.com/blindnet-io/data-access-component/issues">Submit an Issue</a>
  &nbsp;•&nbsp;
  <a href="https://join.slack.com/t/blindnet/shared_invite/zt-1arqlhqt3-A8dPYXLbrnqz1ZKsz6ItOg">Online Chat</a>
  <br>
  <br>
</p>

## Get Started

:rocket: Check out our [Quick Start Guide](https://blindnet.dev/docs/quickstart) to get started in a snap.

## Usage

First build the DAC by running the `assembly` SBT task. The resulting `data_access.jar` file can be found in the
`target` folder, under the currently used Scala version.

Then run it: `java -jar data_access.jar`.

The DAC expects a Redis server to be running on 127.0.0.1:6379. This is not configurable yet.

Some options can be configured using environment variables:

| Name                       | Description                  | Example                              | Default                  |
|----------------------------|------------------------------|--------------------------------------|:-------------------------|
| BN_ENV                     | Environment                  | development, staging, production     | development              |
| BN_PORT                    | HTTP port                    | 80                                   | 8028                     |
| BN_HOST                    | HTTP host                    | 0.0.0.0                              | 127.0.0.1                |
| BN_BASE_URL                | HTTP base URL                | https://dac.example.org              | http://$BN_HOST:$BN_PORT |
| BN_AZURE_STORAGE_ACC_NAME  | Azure storage account name   |                                      |                          |
| BN_AZURE_STORAGE_ACC_KEY   | Azure storage account key    |                                      |                          |
| BN_AZURE_STORAGE_CONT_NAME | Azure storage container name |                                      |                          |
| BN_IDENTITY_URL            | identity api url             | https://identity.devkit.blindnet.io  |                          |
| BN_IDENTITY_KEY            | identity api public url      | key                                  |                          |

`development` environment has default fake values for BN_AZURE_* keys.

## Contributing

Contributions of all kinds are always welcome!

If you see a bug or room for improvement in this project in particular, please [open an issue][new-issue] or directly [fork this repository][fork] to submit a Pull Request.

If you have any broader questions or suggestions, just open a simple informal [DevRel Request][request], and we'll make sure to quickly find the best solution for you.

## Community

> All community participation is subject to blindnet’s [Code of Conduct][coc].

Stay up to date with new releases and projects, learn more about how to protect your privacy and that of our users, and share projects and feedback with our team.

- [Join our Slack Workspace][chat] to chat with the blindnet community and team
- Follow us on [Twitter][twitter] to stay up to date with the latest news
- Check out our [Openness Framework][openness] and [Product Management][product] on Github to see how we operate and give us feedback.

## License

The blindnet devkit data-access-component is available under [MIT][license] (and [here](https://github.com/blindnet-io/openness-framework/blob/main/docs/decision-records/DR-0001-oss-license.md) is why).

<!-- project's URLs -->
[new-issue]: https://github.com/blindnet-io/data-access-component/issues/new/choose
[fork]: https://github.com/blindnet-io/data-access-component/fork

<!-- common URLs -->
[devkit]: https://github.com/blindnet-io/blindnet.dev
[openness]: https://github.com/blindnet-io/openness-framework
[product]: https://github.com/blindnet-io/product-management
[request]: https://github.com/blindnet-io/devrel-management/issues/new?assignees=noelmace&labels=request%2Ctriage&template=request.yml&title=%5BRequest%5D%3A+
[chat]: https://join.slack.com/t/blindnet/shared_invite/zt-1arqlhqt3-A8dPYXLbrnqz1ZKsz6ItOg
[twitter]: https://twitter.com/blindnet_io
[docs]: https://blindnet.dev/docs
[changelog]: CHANGELOG.md
[license]: LICENSE
[coc]: https://github.com/blindnet-io/openness-framework/blob/main/CODE_OF_CONDUCT.md
