[![GitHub](https://img.shields.io/github/license/kuflow/kuflow-cli?label=License)](https://github.com/kuflow/kuflow-cli/blob/master/LICENSE)
[![GitHub Release](https://img.shields.io/github/downloads/kuflo/kuflow-cli/latest/kuflow-cli-0.0.1.tar.gz)](https://github.com/kuflow/kuflow-cli/releases)

# KuFlow CLI

> ***"KuFlow is a platform on which you can design and execute your business processes"***

In KuFlow we are engineers and therefore we like concrete things; we like to know 'the how' because it is the only way to understand how a platform can solve our problems.

This project is a command-line client for KuFlow Rest API. 

## Installation

**Important**. For its correct operation, it is necessary to have JAVA installed and available in the system path. The minimum version required is 11.

Download the archive from Github [releases](https://github.com/kuflow/kuflow-cli/releases), and unpack it to a location of your choice.

## Usage

### Available Commands

Display the help to learn about using kuflowctl:

```shell script
kuflowctl --help
Usage: kuflowctl [-hsVv] [--endpoint=<endpoint>]
                 [--environment-file=<environmentFile> |
                 (--client-id=<clientId> --client-secret=<clientSecret>)]
                 [COMMAND]
      --client-id=<clientId>
                      The 'Application' identifier
      --client-secret=<clientSecret>
                      The 'Application' token
      --endpoint=<endpoint>
                      KuFlow Api endpoint. By default is https://api.kuflow.com
      --environment-file=<environmentFile>
                      Environment file in YAML format. More info: https:
                        //github.com/kuflow/kuflow-cli
  -h, --help          Show this help message and exit.
  -s, --[no-]silent   Silent output. False by default.
  -v, --verbose       Specify multiple -v options to increase verbosity.
                      For example, `-v -v -v` or `-vvv`
  -V, --version       Print version information and exit.
Commands:
  append-log
  save-element-document
  save-element-document-by-reference
  save-element-field
  save-element-principal
```

### Authentication

To connect to the KuFlow API it is necessary to have an identifier and password that must be configured in the *"Applications"* section of the administrative part of the KuFlow application. In addition, although it is not required by default, it is possible to explicitly specify the API access endpoint.

With this data, there are different ways to provision the CLI commands with this information. Through, command line options, configuration file or environment variables.

The order of precedence for locating this configuration is as follows:

1. Command line options, through: 
   1. Individual options
   2. Specified configuration file   
2. Environment Variables   
3. Default configuration file location
   1. Try to locate a `.kuflow.yaml` file in the user's home directory

#### Command line options

We have two options, indicate *Client Identifier* (`--client-id`) and *Client Secret* (-`-client-secret`) or indicate the path to a configuration file in YAML format that includes these settings. The format of the configuration file is as follows:

```yaml
kuflow:
    # ID of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    client-id: YOUR_IDENTIFIER

    # TOKEN of the APPLICATION configured in KUFLOW.
    # Get it in "Application details" in the Kuflow APP.
    client-secret: YOUR_SECRET
    
    # OPTIONAL KUFLOW REST API. Default is: https://api.kuflow.com
    #endpoint: https://api.kuflow.com
```

For more information, see the application help with `kuflowctl --help`.

#### Environment Variables

It is possible to specify settings in the following variables:

```shell
KUFLOW_CLIENT_ID=YOUR_IDENTIFIER
KUFLOW_CLIENT_SECRET=YOUR_SECRET

# OPTIONAL KUFLOW REST API. Default is: https://api.kuflow.com
#KUFLOW_ENDPOINT=https://api.kuflow.com
```
## Documentation

See reference, examples and more in our [documentation](https://docs.kuflow.com/developers/kuflowctl/)

## Contributing

We are happy to receive your help and comments, together we will dance a wonderful KuFlow. Please review our [contribution guide](CONTRIBUTING.md).

## License

[MIT License](https://github.com/kuflow/kuflow-engine-client-java/blob/master/LICENSE)
