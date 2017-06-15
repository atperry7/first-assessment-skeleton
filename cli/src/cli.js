import vorpal from 'vorpal'
import { words } from 'lodash'
import { connect } from 'net'
import { Message } from './Message'

export const cli = vorpal()

let username
let server

cli
  .delimiter(cli.chalk['yellow']('ftd~$'))

cli
  .mode('connect <username>', 'This will connect you to the FTD Chat Server. Requires a username to be signed in.')
  .option('-h, --host <h>', 'IP address to the chat server, defaults localhost')
  .option('-p, --port <p>', 'Port for the chat server, defaults 8080')
  .delimiter(cli.chalk['green'](`<FTD Chat>`))
  .init(function (args, callback) {
    let host = args.options.host === undefined ? 'localhost' : args.options.host
    let port = args.options.port === undefined ? 8080 : args.options.port
    username = args.username

    server = connect({ host: host, port: port }, () => {
      server.write(new Message({ username, command: 'connect' }).toJSON() + '\n')
      callback()
    })

    server.on('data', (buffer) => {
      this.log(Message.fromJSON(buffer).toString())
    })

    server.on('end', () => {
      cli.exec('exit')
    })
  })
  .action(function (input, callback) {
    const [ command, ...rest ] = words(input, /\S+/g) // Used /S+/g to sepearate at the spaces and checks the entire string (global)
    const contents = rest.join(' ')

    if (command === 'disconnect') {
      server.end(new Message({ username, command }).toJSON() + '\n')
    } else {
      server.write(new Message({ username, command, contents }).toJSON() + '\n')
    }

    callback()
  })
